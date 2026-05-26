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
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.Clock
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.datetime.toJavaInstant
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.slf4j.LoggerFactory
import java.security.SecureRandom
import java.sql.SQLException
import java.time.ZoneOffset
import java.util.UUID

class ExposedVotingRepository : VotingRepository {
    private val log = LoggerFactory.getLogger(ExposedVotingRepository::class.java)
    private val random = SecureRandom()

    override suspend fun getVotings(): List<Voting> =
        newSuspendedTransaction(Dispatchers.IO) {
            Votings.selectAll().map { mapVotingRow(it) }
        }

    override suspend fun getById(id: UUID): Voting? =
        newSuspendedTransaction(Dispatchers.IO) {
            Votings
                .select { Votings.id eq id }
                .limit(1)
                .firstOrNull()
                ?.let { mapVotingRow(it) }
        }

    override suspend fun create(dto: NewVoting, creatorId: UUID): Voting =
        newSuspendedTransaction(Dispatchers.IO) {
            try {
                val endsAt = Clock.System.now()
                    .plus(dto.durationDays.toLong(), kotlinx.datetime.DateTimeUnit.DAY)

                val votingId = Votings.insertAndGetId { stmt ->
                    stmt[title] = dto.title
                    stmt[description] = dto.description
                    stmt[type] = dto.type
                    stmt[status] = VotingStatus.ACTIVE
                    stmt[imageUrl] = dto.imageUrl
                    stmt[Votings.creatorId] = creatorId
                    stmt[endsAt] = endsAt.toJavaInstant()
                }

                val options = dto.options ?: emptyList()
                options.forEach { optionText ->
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
                    type = dto.type,
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

            val optionIdsText = payload.selectedOptionIds?.joinToString(",")

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
                VotingType.PETITION -> {
                    VotingResult(
                        votingId = id.toLongParticipantsId(),
                        status = status,
                        type = voting.type,
                        totalParticipants = totalParticipants,
                        optionsResults = null,
                        signaturesCount = totalParticipants,
                        winnerInfo = null,
                        signaturesParticipatingCount = totalParticipants,
                        winnerOptionText = null
                    )
                }

                VotingType.GIVEAWAY -> {
                    val votesList = votes.toList()
                    val winnerUserId = if (votesList.isNotEmpty()) votesList[random.nextInt(votesList.size)][Votes.userId] else null

                    VotingResult(
                        votingId = id.toLongParticipantsId(),
                        status = status,
                        type = voting.type,
                        totalParticipants = totalParticipants,
                        optionsResults = null,
                        signaturesCount = null,
                        winnerInfo = winnerUserId?.let { WinnerInfo(it.toString()) },
                        winnerOptionText = null
                    )
                }

                VotingType.SINGLE,
                VotingType.MULTIPLE -> {
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
            hasVoted = false
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

