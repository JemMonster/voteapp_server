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

        val startTime = dto.startTime
        val endTime = dto.endTime
        if (endTime <= startTime) {
            throw ValidationException("endTime must be after startTime")
        }

        if (dto.votingType == com.example.voteapp.server.votings.models.VotingType.SINGLE_CHOICE ||
            dto.votingType == com.example.voteapp.server.votings.models.VotingType.MULTIPLE_CHOICE
        ) {
            val optionsCount = dto.options.size
            if (optionsCount < 2) {
                throw ValidationException("At least 2 options required")
            }
        }

        if (dto.options.any { it.isBlank() }) {
            throw ValidationException("Options cannot be blank")
        }

        repository.create(dto, creatorId)
    }
}


