package com.example.voteapp.server.votings.models

import kotlinx.serialization.Serializable

@Serializable
enum class VotingType {
    SINGLE,
    MULTIPLE,
    PETITION,
    GIVEAWAY
}

