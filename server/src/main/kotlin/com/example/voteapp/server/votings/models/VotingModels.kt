package com.example.voteapp.server.votings.models

import kotlinx.serialization.Serializable

/**
 * Request to update voting details
 */
@Serializable
data class UpdateVotingRequest(
    val title: String? = null,
    val description: String? = null,
    val imageUrl: String? = null,
    val endsAt: String? = null, // ISO 8601 format
    val close: Boolean? = null // If true, closes the voting
)

/**
 * Response for voting details with creator info
 */
@Serializable
data class VotingDetailsResponse(
    val id: String,
    val title: String,
    val description: String,
    val type: VotingType,
    val status: VotingStatus,
    val imageUrl: String?,
    val creatorId: String,
    val creatorEmail: String?,
    val endsAt: String,
    val createdAt: String,
    val options: List<VotingOption>
)

@Serializable
data class VotingOption(
    val id: String,
    val text: String,
    val votesCount: Int
)

/**
 * Vote modification request
 */
@Serializable
data class UpdateVoteRequest(
    val optionIds: List<String>? = null // Array of option UUIDs
)

/**
 * Vote response
 */
@Serializable
data class VoteResponse(
    val success: Boolean,
    val message: String,
    val votingId: String
)
