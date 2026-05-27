package com.example.voteapp.server.votings.domain.usecase

import com.example.voteapp.server.votings.domain.model.Voting
import com.example.voteapp.server.votings.domain.port.VotingRepository
import com.example.voteapp.server.votings.models.UpdateVotingRequest
import com.example.voteapp.server.votings.models.VotingDetailsResponse
import com.example.voteapp.server.votings.models.VotingOption
import kotlinx.datetime.Clock
import kotlinx.datetime.toKotlinLocalDateTime
import java.time.Instant
import java.util.UUID

/**
 * Use case to update a voting
 * Only the creator can update the voting
 */
class UpdateVotingUseCase(
    private val repository: VotingRepository
) {
    suspend fun execute(votingId: UUID, creatorId: UUID, request: UpdateVotingRequest): Voting {
        val voting = repository.getById(votingId) 
            ?: throw VotingNotFoundException(votingId)
        
        // Check ownership
        if (voting.creatorId != creatorId) {
            throw UnauthorizedException("Only the creator can update this voting")
        }
        
        // If voting is already closed, only some fields can be updated
        if (voting.status == VotingStatus.CLOSED && !request.close.equals(false)) {
            throw VotingAlreadyClosedException()
        }
        
        // Validate new end time if provided
        request.endsAt?.let { newEndTime ->
            try {
                val parsedTime = Instant.parse(newEndTime).toKotlinLocalDateTime()
                val now = Clock.System.now().toKotlinLocalDateTime()
                
                if (parsedTime <= now) {
                    throw ValidationException("End time must be in the future")
                }
            } catch (e: Exception) {
                throw ValidationException("Invalid end time format. Use ISO 8601 format")
            }
        }
        
        // Update voting (repository should handle partial updates)
        return repository.update(votingId, creatorId, request)
    }
}

/**
 * Use case to delete a voting
 * Only the creator can delete the voting
 */
class DeleteVotingUseCase(
    private val repository: VotingRepository
) {
    suspend fun execute(votingId: UUID, creatorId: UUID) {
        val voting = repository.getById(votingId)
            ?: throw VotingNotFoundException(votingId)
        
        // Check ownership
        if (voting.creatorId != creatorId) {
            throw UnauthorizedException("Only the creator can delete this voting")
        }
        
        repository.delete(votingId)
    }
}

/**
 * Use case to get voting details with creator info
 */
class GetVotingDetailsExtendedUseCase(
    private val repository: VotingRepository
) {
    suspend fun execute(votingId: UUID): VotingDetailsResponse {
        val voting = repository.getById(votingId)
            ?: throw VotingNotFoundException(votingId)
        
        val options = repository.getVotingOptions(votingId)
        
        return VotingDetailsResponse(
            id = voting.id.toString(),
            title = voting.title,
            description = voting.description,
            type = voting.type,
            status = voting.status,
            imageUrl = voting.imageUrl,
            creatorId = voting.creatorId.toString(),
            creatorEmail = null, // Would need to fetch from Users table
            endsAt = voting.endsAt.toString(),
            createdAt = voting.createdAt?.toString() ?: Clock.System.now().toKotlinLocalDateTime().toString(),
            options = options.map { opt ->
                VotingOption(
                    id = opt.id.toString(),
                    text = opt.text,
                    votesCount = opt.votesCount
                )
            }
        )
    }
}

/**
 * Use case to update/remove vote
 */
class UpdateVoteUseCase(
    private val repository: VotingRepository
) {
    suspend fun execute(votingId: UUID, userId: UUID, optionIds: List<String>) {
        val voting = repository.getById(votingId)
            ?: throw VotingNotFoundException(votingId)
        
        if (voting.status != VotingStatus.ACTIVE) {
            throw VotingAlreadyClosedException()
        }
        
        // Check if user already voted
        val hasVoted = repository.hasUserVoted(votingId, userId)
        if (!hasVoted) {
            throw AlreadyVotedException()
        }
        
        // Update vote
        repository.updateVote(votingId, userId, optionIds)
    }
    
    suspend fun removeVote(votingId: UUID, userId: UUID) {
        val voting = repository.getById(votingId)
            ?: throw VotingNotFoundException(votingId)
        
        if (voting.status != VotingStatus.ACTIVE) {
            throw VotingAlreadyClosedException()
        }
        
        repository.removeVote(votingId, userId)
    }
}

class UnauthorizedException(message: String) : Exception(message)
