package com.example.voteapp.server.votings.data

import com.example.voteapp.server.db.Votings
import com.example.voteapp.server.votings.domain.model.Voting
import com.example.voteapp.server.votings.domain.port.VotingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toInstant
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.slf4j.LoggerFactory
import java.sql.SQLException
import java.time.ZoneOffset
import java.util.UUID

class ExposedVotingRepository : VotingRepository {
    private val log = LoggerFactory.getLogger(ExposedVotingRepository::class.java)

    override suspend fun getVotings(): List<Voting> =
        newSuspendedTransaction(Dispatchers.IO) {
            try {
                // NOTE: Для production можно расширить до выборки totalVotes/hasVoted.
                Votings
                    .selectAll()
                    .map { row ->
                        Voting(
                            id = row[Votings.id].value.toString(),
                            title = row[Votings.title],
                            description = row[Votings.description],
                            type = row[Votings.type],
                            status = row[Votings.status],
                            imageUrl = row[Votings.imageUrl],
                            endsAt = row[Votings.endsAt].value.toKotlinLocalDateTime(),
                            totalVotes = 0,
                            hasVoted = false,
                        )
                    }
            } catch (e: SQLException) {
                log.error("SQL error while reading votings", e)
                throw e
            }
        }
}

private fun java.time.Instant.toKotlinLocalDateTime(): LocalDateTime {
    // Exposed timestamp maps to java.time.Instant via JDBC.
    val epochSecond = this.epochSecond
    return LocalDateTime.fromEpochSeconds(epochSecond, 0, ZoneOffset.UTC)
}


