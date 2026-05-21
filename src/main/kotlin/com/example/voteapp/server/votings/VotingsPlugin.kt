package com.example.voteapp.server.votings

import com.example.voteapp.server.votings.domain.usecase.GetVotingsUseCase
import com.example.voteapp.server.votings.data.InMemoryVotingRepository
import io.ktor.server.application.*

// Плейсхолдер: при желании можно будет вынести в отдельный plugin.
fun Application.installVotings() {
    val repository = InMemoryVotingRepository()
    val useCase = GetVotingsUseCase(repository)
    configureVotingsRouting(useCase)
}




