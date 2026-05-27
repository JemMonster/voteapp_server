package com.example.voteapp.server.votings

import com.example.voteapp.server.votings.data.ExposedVotingRepository
import com.example.voteapp.server.votings.domain.usecase.CreateVotingUseCase
import com.example.voteapp.server.votings.domain.usecase.GetResultsUseCase
import com.example.voteapp.server.votings.domain.usecase.GetVotingsUseCase
import com.example.voteapp.server.votings.domain.usecase.GetVotingDetailsUseCase
import com.example.voteapp.server.votings.domain.usecase.GetVotingHistoryUseCase
import com.example.voteapp.server.votings.domain.usecase.InviteUseCase
import com.example.voteapp.server.votings.domain.usecase.VoteUseCase
import com.example.voteapp.server.votings.domain.usecase.UpdateVotingUseCase
import com.example.voteapp.server.votings.domain.usecase.DeleteVotingUseCase
import com.example.voteapp.server.votings.domain.usecase.UpdateVoteUseCase
import io.ktor.server.application.Application

fun Application.installVotings() {
    val repository = ExposedVotingRepository()
    
    val getVotingsUseCase = GetVotingsUseCase(repository)
    val createVotingUseCase = CreateVotingUseCase(repository)
    val voteUseCase = VoteUseCase(repository)
    val getResultsUseCase = GetResultsUseCase(repository)
    val getVotingDetailsUseCase = GetVotingDetailsUseCase(repository)
    val getVotingHistoryUseCase = GetVotingHistoryUseCase(repository)
    val inviteUseCase = InviteUseCase(repository)
    val updateVotingUseCase = UpdateVotingUseCase(repository)
    val deleteVotingUseCase = DeleteVotingUseCase(repository)
    val updateVoteUseCase = UpdateVoteUseCase(repository)
    
    configureVotingsRouting(
        getVotingsUseCase = getVotingsUseCase,
        createVotingUseCase = createVotingUseCase,
        voteUseCase = voteUseCase,
        getResultsUseCase = getResultsUseCase,
        getVotingDetailsUseCase = getVotingDetailsUseCase,
        getVotingHistoryUseCase = getVotingHistoryUseCase,
        inviteUseCase = inviteUseCase,
        updateVotingUseCase = updateVotingUseCase,
        deleteVotingUseCase = deleteVotingUseCase,
        updateVoteUseCase = updateVoteUseCase
    )
}





