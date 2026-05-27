package com.example.voteapp.server.votings.domain.usecase

import com.example.voteapp.server.votings.domain.model.Voting
import com.example.voteapp.server.votings.domain.port.VotingRepository
import java.util.UUID

class GetVotingDetailsUseCase(
    private val repository: VotingRepository,
) {
    suspend operator fun invoke(id: UUID): Voting {
        return repository.getVotingById(id) ?: throw VotingNotFoundException(id)
    }
}

