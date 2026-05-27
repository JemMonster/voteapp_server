package com.example.voteapp.server.models

import kotlinx.serialization.Serializable
import kotlinx.datetime.LocalDateTime

@Serializable
data class Voting(
    val id: String,
    val title: String,
    val description: String,
    val type: VotingType,
    val status: VotingStatus,
    val imageUrl: String? = null,
    val endsAt: LocalDateTime,
    val totalVotes: Int = 0,
    val hasVoted: Boolean = false
)

@Serializable
enum class VotingType {
    SINGLE, MULTIPLE, PETITION, RAFFLE
}

@Serializable
enum class VotingStatus {
    ACTIVE, CLOSED
}

val votingList = listOf(
    Voting(
        id = "1",
        title = "Лучший язык программирования",
        description = "Голосуйте за любимый язык",
        type = VotingType.SINGLE,
        status = VotingStatus.ACTIVE,
        endsAt = LocalDateTime(2025, 1, 1, 0, 0),
        totalVotes = 120
    )
)

