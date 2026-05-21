package com.example.voteapp.server.votings

import com.example.voteapp.server.votings.domain.usecase.GetVotingsUseCase
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.v1Votings(getVotingsUseCase: GetVotingsUseCase) {

    route("/votings") {
        get {
            val votings = getVotingsUseCase()
            call.respond(votings)
        }

        // POST /api/v1/votings, /api/v1/votings/{id} later
    }
}

fun Application.configureVotingsRouting(getVotingsUseCase: GetVotingsUseCase) {
    routing {
        route("/api/v1") {
            v1Votings(getVotingsUseCase)
        }
    }
}


