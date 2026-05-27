package com.example.voteapp.server.votings.domain.usecase

import com.example.voteapp.server.votings.domain.port.VotingRepository
import com.example.voteapp.server.votings.models.VotePayload
import com.example.voteapp.server.votings.models.VotingResult
import com.example.voteapp.server.votings.models.VotingType
import kotlinx.datetime.Clock
import kotlinx.datetime.toKotlinLocalDateTime
import org.slf4j.LoggerFactory
import java.util.UUID

class VoteUseCase(
    private val repository: VotingRepository,
) {
    private val log = LoggerFactory.getLogger(VoteUseCase::class.java)

    suspend operator fun invoke(votingId: UUID, userId: UUID, payload: VotePayload): VotingResult {
        val voting = repository.getById(votingId) ?: throw VotingNotFoundException(votingId)
        val now = Clock.System.now().toKotlinLocalDateTime()

        if (now >= voting.endsAt) {
            throw VotingAlreadyClosedException()
        }

        when (voting.type) {
            VotingType.SINGLE_CHOICE -> {
                val optionId = payload.optionId
                if (optionId == null) {
                    throw ValidationException("optionId is required for SINGLE_CHOICE")
                }
            }

            VotingType.MULTIPLE_CHOICE -> {
                val optionIds = payload.optionIds
                if (optionIds.isNullOrEmpty()) {
                    throw ValidationException("optionIds is required for MULTIPLE_CHOICE")
                }
            }
        }

        return repository.vote(votingId, userId, payload)
    }
}



