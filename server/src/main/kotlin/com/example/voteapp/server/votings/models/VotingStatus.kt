package com.example.voteapp.server.votings.models

import kotlinx.serialization.Serializable

@Serializable
enum class VotingStatus {
    ACTIVE,
    CLOSED
}

