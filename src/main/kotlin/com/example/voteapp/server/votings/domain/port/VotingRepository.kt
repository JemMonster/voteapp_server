package com.example.voteapp.server.votings.domain.port

import com.example.voteapp.server.votings.domain.model.Voting

interface VotingRepository {
    suspend fun getVotings(): List<Voting>
}

