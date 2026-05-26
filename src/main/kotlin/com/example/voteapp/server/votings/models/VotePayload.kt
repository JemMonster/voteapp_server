package com.example.voteapp.server.votings.models

import kotlinx.serialization.Serializable

@Serializable
data class VotePayload(
    val selectedOptionIds: List<Long>?,
    val isParticipating: Boolean
)

