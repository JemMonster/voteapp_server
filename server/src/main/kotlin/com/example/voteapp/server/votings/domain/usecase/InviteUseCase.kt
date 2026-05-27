package com.example.voteapp.server.votings.domain.usecase

import com.example.voteapp.server.votings.domain.port.VotingRepository

import com.example.voteapp.server.votings.models.InviteResponse
import java.util.UUID

class InviteUseCase(
    private val repository: VotingRepository,
) {
    suspend operator fun invoke(votingId: UUID, email: String): InviteResponse {
        return repository.invite(votingId, email)
    }
}

