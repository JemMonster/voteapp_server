package com.example.voteapp.server.votings.domain.port

import com.example.voteapp.server.votings.domain.model.Voting
import com.example.voteapp.server.votings.models.NewVoting
import com.example.voteapp.server.votings.models.VotePayload
import com.example.voteapp.server.votings.models.VotingResult
import java.util.UUID

interface VotingRepository {
    suspend fun getVotings(): List<Voting>
    suspend fun create(dto: NewVoting, creatorId: UUID): Voting
    suspend fun vote(id: UUID, userId: UUID, payload: VotePayload): VotingResult
    suspend fun getResults(id: UUID): VotingResult
    suspend fun getById(id: UUID): Voting?
}



