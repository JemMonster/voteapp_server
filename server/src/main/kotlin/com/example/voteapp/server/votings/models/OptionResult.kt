package com.example.voteapp.server.votings.models

import kotlinx.serialization.Serializable

@Serializable
data class OptionResult(
    val optionId: Long,
    val text: String,
    val percent: Double,
    val votesCount: Int
)

