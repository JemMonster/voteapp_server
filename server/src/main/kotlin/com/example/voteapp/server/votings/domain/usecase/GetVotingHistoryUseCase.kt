package com.example.voteapp.server.votings.domain.usecase

import com.example.voteapp.server.votings.domain.model.Voting
import com.example.voteapp.server.votings.domain.port.VotingRepository
import io.ktor.util.date.GMTDate
import java.util.UUID

class GetVotingHistoryUseCase(
    private val repository: VotingRepository,
) {
    suspend operator fun invoke(userId: UUID): List<Voting> {
        return repository.getVotingHistory(userId)
    }
}

