package com.example.voteapp.server.votings.models

import kotlinx.serialization.Serializable

@Serializable
data class WinnerInfo(
    val winnerUserId: String
)

