package com.example.voteapp.server.votings.models

import kotlinx.serialization.Serializable

@Serializable
data class InvitePayload(
    val email: String,
)

