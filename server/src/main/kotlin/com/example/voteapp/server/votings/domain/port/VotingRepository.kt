package com.example.voteapp.server.votings.domain.port

import com.example.voteapp.server.votings.domain.model.Voting
import com.example.voteapp.server.votings.models.NewVoting
import com.example.voteapp.server.votings.models.VotePayload
import com.example.voteapp.server.votings.models.VotingResult
import com.example.voteapp.server.votings.models.UpdateVotingRequest
import com.example.voteapp.server.votings.models.VotingOption
import java.util.UUID

interface VotingRepository {
    suspend fun getVotings(): List<Voting>
    suspend fun getVotingsFiltered(status: String?, type: String?): List<Voting>

    suspend fun create(dto: NewVoting, creatorId: UUID): Voting
    suspend fun vote(id: UUID, userId: UUID, payload: VotePayload): VotingResult
    suspend fun getResults(id: UUID): VotingResult
    suspend fun getById(id: UUID): Voting?

    suspend fun getVotingById(id: UUID): Voting?
    suspend fun getVotingHistory(userId: UUID): List<Voting>
    suspend fun invite(votingId: UUID, email: String): com.example.voteapp.server.votings.models.InviteResponse
    
    // New methods for voting management
    suspend fun update(votingId: UUID, creatorId: UUID, request: UpdateVotingRequest): Voting
    suspend fun delete(votingId: UUID)
    suspend fun hasUserVoted(votingId: UUID, userId: UUID): Boolean
    suspend fun updateVote(votingId: UUID, userId: UUID, optionIds: List<String>)
    suspend fun removeVote(votingId: UUID, userId: UUID)
    suspend fun getVotingOptions(votingId: UUID): List<VotingOption>
}




