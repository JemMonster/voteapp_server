package com.example.voteapp.server.votings.models

import kotlinx.serialization.Serializable

@Serializable
data class VotePayload(
    val optionId: Long?,
    val optionIds: List<Long>?
)


