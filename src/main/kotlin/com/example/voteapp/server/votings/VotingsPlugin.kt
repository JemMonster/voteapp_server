package com.example.voteapp.server.votings

import io.ktor.server.application.*

// Плейсхолдер: при желании можно будет вынести в отдельный plugin.
fun Application.installVotings() {
    configureVotingsRouting()
}

