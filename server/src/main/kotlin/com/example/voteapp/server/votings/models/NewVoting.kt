package com.example.voteapp.server.votings.models

import kotlinx.serialization.Serializable
import kotlinx.validation.constraints.Max
import kotlinx.validation.constraints.Min

@Serializable
data class NewVoting(
    val title: String,
    val description: String?,
    val imageUrl: String?,
    val votingType: VotingType,
    val startTime: kotlinx.datetime.Instant,
    val endTime: kotlinx.datetime.Instant,

    val options: List<String>
)


