package com.example.voteapp.server.votings.domain.usecase

import com.example.voteapp.server.votings.domain.port.VotingRepository
import com.example.voteapp.server.votings.models.VotingResult
import kotlinx.datetime.Clock
import org.slf4j.LoggerFactory
import java.util.UUID

class GetResultsUseCase(
    private val repository: VotingRepository,
) {
    private val log = LoggerFactory.getLogger(GetResultsUseCase::class.java)

    suspend operator fun invoke(votingId: UUID): VotingResult {
        repository.getById(votingId) ?: throw VotingNotFoundException(votingId)
        return repository.getResults(votingId)
    }
}


