package com.example.voteapp.server.votings.models

import kotlinx.serialization.Serializable

@Serializable
data class VotingResult(
    val votingId: Long,
    val status: VotingStatus,
    val type: VotingType,
    val totalParticipants: Int,
    val optionsResults: List<OptionResult>?,
    val signaturesCount: Int?,
    val winnerInfo: WinnerInfo?,
    val signaturesParticipatingCount: Int? = null,
    val isParticipating: Boolean? = null,
    val winnerOptionText: String? = null,
    val signaturesCountOpt: Int? = null,
    val signaturesCountFinal: Int? = signaturesCount
)

