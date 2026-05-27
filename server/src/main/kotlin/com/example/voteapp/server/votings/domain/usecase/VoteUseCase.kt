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
            VotingType.SINGLE -> {
                val selected = payload.selectedOptionIds
                if (selected?.size != 1) {
                    throw ValidationException("Invalid payload for voting type")
                }
            }

            VotingType.MULTIPLE -> {
                val selected = payload.selectedOptionIds
                if (selected?.isNotEmpty() != true) {
                    throw ValidationException("Invalid payload for voting type")
                }
            }

            VotingType.PETITION, VotingType.GIVEAWAY -> {
                if (payload.isParticipating != true) {
                    throw ValidationException("Invalid payload for voting type")
                }
            }
        }

        return repository.vote(votingId, userId, payload)
    }
}


