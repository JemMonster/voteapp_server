package com.example.voteapp.server.votings.models

import kotlinx.serialization.Serializable
import kotlinx.validation.constraints.Max
import kotlinx.validation.constraints.Min

@Serializable
data class NewVoting(
    val title: String,
    val description: String?,
    val imageUrl: String?,
    val type: VotingType,

    @Min(1)
    @Max(360)
    val durationDays: Int,

    val options: List<String>?
)

