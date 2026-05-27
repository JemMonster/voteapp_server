package com.example.voteapp.server.votings.models

import kotlinx.serialization.Serializable

@Serializable
data class InviteResponse(
    val votingId: Long,
    val email: String,
    val status: String,
)

