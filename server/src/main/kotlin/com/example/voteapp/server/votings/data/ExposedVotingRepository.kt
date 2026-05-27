package com.example.voteapp.server.votings.data

import com.example.voteapp.server.db.VotingOptions
import com.example.voteapp.server.db.Votings
import com.example.voteapp.server.db.Votes
import com.example.voteapp.server.votings.domain.model.Voting
import com.example.voteapp.server.votings.domain.port.VotingRepository
import com.example.voteapp.server.votings.models.NewVoting
import com.example.voteapp.server.votings.models.OptionResult
import com.example.voteapp.server.votings.models.VotePayload
import com.example.voteapp.server.votings.models.VotingResult
import com.example.voteapp.server.votings.models.VotingStatus
import com.example.voteapp.server.votings.models.VotingType
import com.example.voteapp.server.votings.models.WinnerInfo
import com.example.voteapp.server.votings.models.UpdateVotingRequest
import com.example.voteapp.server.votings.models.VotingOption
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.Clock
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.datetime.toJavaInstant
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.andWhere
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.delete
import io.ktor.server.routing.route
import io.ktor.server.request.receive
import io.ktor.server.routing.path
import io.ktor.server.routing.parameter
import io.ktor.server.application.call
import io.ktor.server.routing.respond
import org.jetbrains.exposed.sql.JoinType
import java.security.SecureRandom
import java.sql.SQLException
import java.time.ZoneOffset
import java.time.Instant
import java.util.UUID
import com.example.voteapp.server.plugins.UserIdPrincipal


class ExposedVotingRepository : VotingRepository {
    private val log = LoggerFactory.getLogger(ExposedVotingRepository::class.java)
    private val random = SecureRandom()

    override suspend fun getVotings(): List<Voting> =
        newSuspendedTransaction(Dispatchers.IO) {
            Votings.selectAll().map { mapVotingRow(it) }
        }

    override suspend fun getVotingsFiltered(status: String?, type: String?): List<Voting> =
        newSuspendedTransaction(Dispatchers.IO) {
            val statusEnum = status?.let { VotingStatus.valueOf(it.uppercase()) }
            val typeEnum = type?.let { VotingType.valueOf(it.uppercase()) }

            Votings
                .selectAll()
                .apply {
                    if (statusEnum != null) {
                        andWhere { Votings.status eq statusEnum }
                    }
                    if (typeEnum != null) {
                        andWhere { Votings.type eq typeEnum }
                    }
                }
                .map { mapVotingRow(it) }
        }


    override suspend fun getById(id: UUID): Voting? =
        newSuspendedTransaction(Dispatchers.IO) {
            Votings
                .select { Votings.id eq id }
                .limit(1)
                .firstOrNull()
                ?.let { mapVotingRow(it) }
        }

    override suspend fun getVotingById(id: UUID): Voting? {
        return getById(id)
    }

    override suspend fun getVotingHistory(userId: UUID): List<Voting> =
        newSuspendedTransaction(Dispatchers.IO) {
            Votings
                .join(Votes, JoinType.INNER, additionalConstraint = { Votings.id eq Votes.votingId })
                .slice(Votings.columns)
                .select { Votes.userId eq userId }
                .withDistinct()
                .map { mapVotingRow(it) }
        }

    override suspend fun invite(votingId: UUID, email: String): com.example.voteapp.server.votings.models.InviteResponse {
        return newSuspendedTransaction(Dispatchers.IO) {
            val emailTrimmed = email.trim()
            require(emailTrimmed.isNotBlank()) { "Email is required" }

            val votingExists = Votings.select { Votings.id eq votingId }.limit(1).any()
            if (!votingExists) {
                throw com.example.voteapp.server.votings.domain.usecase.VotingNotFoundException(votingId)
            }

            val userRow = com.example.voteapp.server.db.Users.select { com.example.voteapp.server.db.Users.email eq emailTrimmed }
                .limit(1)
                .firstOrNull()

            if (userRow == null) {
                throw com.example.voteapp.server.votings.domain.usecase.EmailNotFoundException(emailTrimmed)
            }

            com.example.voteapp.server.votings.models.InviteResponse(
                votingId = votingId.leastSignificantBits,
                email = emailTrimmed,
                status = "SUCCESS"
            )
        }
    }

    override suspend fun update(votingId: UUID, creatorId: UUID, request: UpdateVotingRequest): Voting =
        newSuspendedTransaction(Dispatchers.IO) {
            val voting = getById(votingId) ?: throw com.example.voteapp.server.votings.domain.usecase.VotingNotFoundException(votingId)
            
            if (voting.creatorId != creatorId) {
                throw com.example.voteapp.server.votings.domain.usecase.UnauthorizedException("Only the creator can update this voting")
            }
            
            Votings.update({ Votings.id eq votingId }) {
                request.title?.let { it[Votings.title] = it }
                request.description?.let { it[Votings.description] = it }
                request.imageUrl?.let { it[Votings.imageUrl] = it }
                request.endsAt?.let { 
                    it[Votings.endsAt] = Instant.parse(it).toJavaInstant() 
                }
                request.close?.let { 
                    if (it) it[Votings.status] = VotingStatus.CLOSED 
                }
            }
            
            getById(votingId) ?: throw com.example.voteapp.server.votings.domain.usecase.VotingNotFoundException(votingId)
        }

    override suspend fun delete(votingId: UUID) =
        newSuspendedTransaction(Dispatchers.IO) {
            val voting = getById(votingId) ?: throw com.example.voteapp.server.votings.domain.usecase.VotingNotFoundException(votingId)
            
            // Delete associated votes
            Votes.deleteWhere { Votes.votingId eq votingId }
            // Delete voting options
            VotingOptions.deleteWhere { VotingOptions.votingId eq votingId }
            // Delete the voting
            Votings.deleteWhere { Votings.id eq votingId }
        }

    override suspend fun hasUserVoted(votingId: UUID, userId: UUID): Boolean =
        newSuspendedTransaction(Dispatchers.IO) {
            Votes.select { (Votes.userId eq userId) and (Votes.votingId eq votingId) }.limit(1).any()
        }

    override suspend fun updateVote(votingId: UUID, userId: UUID, optionIds: List<String>) =
        newSuspendedTransaction(Dispatchers.IO) {
            val voting = getById(votingId) ?: throw com.example.voteapp.server.votings.domain.usecase.VotingNotFoundException(votingId)
            
            if (voting.status != VotingStatus.ACTIVE) {
                throw com.example.voteapp.server.votings.domain.usecase.VotingAlreadyClosedException()
            }
            
            val optionsText = optionIds.joinToString(",")
            
            Votes.update({ (Votes.userId eq userId) and (Votes.votingId eq votingId) }) {
                it[optionIds] = optionsText
                it[updatedAt] = java.time.Instant.now()
            }
        }

    override suspend fun removeVote(votingId: UUID, userId: UUID) =
        newSuspendedTransaction(Dispatchers.IO) {
            val voting = getById(votingId) ?: throw com.example.voteapp.server.votings.domain.usecase.VotingNotFoundException(votingId)
            
            if (voting.status != VotingStatus.ACTIVE) {
                throw com.example.voteapp.server.votings.domain.usecase.VotingAlreadyClosedException()
            }
            
            Votes.deleteWhere { (Votes.userId eq userId) and (Votes.votingId eq votingId) }
        }

    override suspend fun getVotingOptions(votingId: UUID): List<VotingOption> =
        newSuspendedTransaction(Dispatchers.IO) {
            VotingOptions
                .select { VotingOptions.votingId eq votingId }
                .orderBy(VotingOptions.id to SortOrder.ASC)
                .map { row ->
                    VotingOption(
                        id = row[VotingOptions.id].value.toString(),
                        text = row[VotingOptions.text],
                        votesCount = 0 // Would need to count votes
                    )
                }
        }


    override suspend fun create(dto: NewVoting, creatorId: UUID): Voting =
        newSuspendedTransaction(Dispatchers.IO) {
            try {
                val endsAt = dto.endTime
                val startAt = dto.startTime

                val votingId = Votings.insertAndGetId { stmt ->
                    stmt[title] = dto.title
                    stmt[description] = dto.description
                    stmt[type] = dto.votingType
                    stmt[status] = VotingStatus.ACTIVE
                    stmt[imageUrl] = dto.imageUrl
                    stmt[Votings.creatorId] = creatorId
                    stmt[endsAt] = endsAt.toJavaInstant()
                }

                dto.options.forEach { optionText ->
                    VotingOptions.insert { stmt ->
                        stmt[VotingOptions.votingId] = votingId
                        stmt[VotingOptions.text] = optionText
                        stmt[VotingOptions.votes] = 0
                    }
                }

                Voting(
                    id = votingId.value,
                    title = dto.title,
                    description = dto.description.orEmpty(),
                    type = dto.votingType,
                    status = VotingStatus.ACTIVE,
                    imageUrl = dto.imageUrl,
                    endsAt = endsAt.toKotlinLocalDateTime(),
                    totalVotes = 0,
                    hasVoted = false
                )
            } catch (e: SQLException) {
                log.error("SQL error while creating voting", e)
                throw e
            }
        }

    override suspend fun vote(id: UUID, userId: UUID, payload: VotePayload): VotingResult =
        newSuspendedTransaction(Dispatchers.IO) {
            val voting = getByIdOrThrow(id)
            val now = Clock.System.now().toKotlinLocalDateTime()

            if (voting.status != VotingStatus.ACTIVE) {
                throw IllegalStateException("Voting is CLOSED")
            }
            if (now > voting.endsAt) {
                throw IllegalStateException("Voting time is ended")
            }

            val alreadyVoted = Votes
                .select { (Votes.userId eq userId) and (Votes.votingId eq id) }
                .limit(1)
                .any()

            if (alreadyVoted) {
                throw IllegalStateException("User already voted")
            }

            val optionIdsText = when (voting.type) {
                VotingType.SINGLE_CHOICE -> payload.optionId?.toString()
                VotingType.MULTIPLE_CHOICE -> payload.optionIds?.joinToString(",")
            }

            Votes.insert { stmt ->
                stmt[Votes.userId] = userId
                stmt[Votes.votingId] = id
                stmt[Votes.optionIds] = optionIdsText
            }

            getResults(id)
        }

    override suspend fun getResults(id: UUID): VotingResult =
        newSuspendedTransaction(Dispatchers.IO) {
            val voting = getByIdOrThrow(id)
            val now = Clock.System.now().toKotlinLocalDateTime()

            val status = if (now > voting.endsAt) VotingStatus.CLOSED else voting.status
            val votes = Votes.select { Votes.votingId eq id }
            val totalParticipants = votes.count().toInt()

            when (voting.type) {
                VotingType.SINGLE_CHOICE,
                VotingType.MULTIPLE_CHOICE -> {
                    val options = VotingOptions
                        .select { VotingOptions.votingId eq id }
                        .orderBy(VotingOptions.id to SortOrder.ASC)

                    val optionResults = options.map { optRow ->
                        val optionText = optRow[VotingOptions.text]
                        val optionId = optRow[VotingOptions.id].value

                        val votesCount = votes.count { voteRow ->
                            val raw = voteRow[Votes.optionIds]
                            raw?.split(",")?.any { it.trim() == optionId.toString() } == true
                        }

                        val percent = if (totalParticipants == 0) 0.0
                        else (votesCount.toDouble() * 100.0 / totalParticipants.toDouble())

                        OptionResult(
                            optionId = optionId.toLongParticipantsIdOption(),
                            text = optionText,
                            percent = percent,
                            votesCount = votesCount
                        )
                    }

                    VotingResult(
                        votingId = id.toLongParticipantsId(),
                        status = status,
                        type = voting.type,
                        totalParticipants = totalParticipants,
                        optionsResults = optionResults,
                        signaturesCount = null,
                        winnerInfo = null,
                        winnerOptionText = null
                    )
                }
            }
        }

    private fun getByIdOrThrow(id: UUID): Voting =
        getById(id) ?: throw NoSuchElementException("Voting not found")

    private fun mapVotingRow(row: org.jetbrains.exposed.sql.ResultRow): Voting {
        val endsAt = row[Votings.endsAt].value.toKotlinLocalDateTime()
        return Voting(
            id = row[Votings.id].value,
            title = row[Votings.title],
            description = row[Votings.description],
            type = row[Votings.type],
            status = row[Votings.status],
            imageUrl = row[Votings.imageUrl],
            endsAt = endsAt,
            totalVotes = 0,
            hasVoted = false,
            creatorId = row[Votings.creatorId].value,
            createdAt = null // Would need to add createdAt column to votings table
        )
    }
}

private fun java.time.Instant.toKotlinLocalDateTime(): kotlinx.datetime.LocalDateTime {
    val epochSecond = this.epochSecond
    return kotlinx.datetime.LocalDateTime.fromEpochSeconds(epochSecond, 0, ZoneOffset.UTC)
}

private fun UUID.toLongParticipantsId(): Long =
    this.leastSignificantBits

private fun UUID.toLongParticipantsIdOption(): Long =
    this.leastSignificantBits

