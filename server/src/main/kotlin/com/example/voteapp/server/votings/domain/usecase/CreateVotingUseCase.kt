package com.example.voteapp.server.votings.domain.usecase

import com.example.voteapp.server.votings.domain.model.Voting
import com.example.voteapp.server.votings.domain.port.VotingRepository
import com.example.voteapp.server.votings.models.NewVoting
import kotlinx.datetime.Clock
import org.slf4j.LoggerFactory
import java.util.UUID

class CreateVotingUseCase(
    private val repository: VotingRepository,
) {
    private val log = LoggerFactory.getLogger(CreateVotingUseCase::class.java)

    suspend operator fun invoke(dto: NewVoting, creatorId: UUID): Voting {
        if (dto.title.isBlank()) {
            throw ValidationException("Title is required")
        }

        val durationDays = dto.durationDays
        if (durationDays !in 1..360) {
            throw ValidationException("Duration must be 1..360 days")
        }

        if (dto.type == com.example.voteapp.server.votings.models.VotingType.SINGLE ||
            dto.type == com.example.voteapp.server.votings.models.VotingType.MULTIPLE
        ) {
            val optionsCount = dto.options?.size ?: 0
            if (optionsCount < 2) {
                throw ValidationException("At least 2 options required")
            }
        }

        repository.create(dto, creatorId)
    }
}


