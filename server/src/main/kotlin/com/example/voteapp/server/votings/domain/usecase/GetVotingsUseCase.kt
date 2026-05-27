package com.example.voteapp.server.votings.domain.usecase

import com.example.voteapp.server.votings.domain.port.VotingRepository

class GetVotingsUseCase(
    private val repository: VotingRepository,
) {
    suspend operator fun invoke(
        status: String?,
        type: String?,
    ): List<com.example.voteapp.server.votings.domain.model.Voting> =
        repository.getVotingsFiltered(status = status, type = type)

    suspend operator fun invoke(): List<com.example.voteapp.server.votings.domain.model.Voting> =
        invoke(status = "active", type = null)

}



