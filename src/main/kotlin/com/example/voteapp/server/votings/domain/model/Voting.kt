package com.example.voteapp.server.votings.domain.model

import com.example.voteapp.server.votings.models.VotingStatus
import com.example.voteapp.server.votings.models.VotingType
import kotlinx.datetime.LocalDateTime
import java.util.UUID

data class Voting(
    val id: UUID,
    val title: String,
    val description: String,
    val type: VotingType,
    val status: VotingStatus,
    val imageUrl: String?,
    val endsAt: LocalDateTime,
    val totalVotes: Int,
    val hasVoted: Boolean
)

