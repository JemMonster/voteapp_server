package com.example.voteapp.server.votings.data

import com.example.voteapp.server.votings.domain.model.Voting
import com.example.voteapp.server.votings.domain.port.VotingRepository
import com.example.voteapp.server.votings.model.votingList

class InMemoryVotingRepository : VotingRepository {

    override suspend fun getVotings(): List<Voting> = votingList
}

// TODO: оставить только для локальной разработки/временного режима


