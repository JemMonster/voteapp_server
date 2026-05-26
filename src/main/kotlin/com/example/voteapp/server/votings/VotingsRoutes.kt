package com.example.voteapp.server.votings

import com.example.voteapp.server.votings.domain.usecase.GetVotingsUseCase
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.v1Votings(getVotingsUseCase: GetVotingsUseCase) {
    authenticate("firebase-jwt") {
        route("/votings") {
            get {
                // Example of userId extraction from token
                val userId = call.principal<com.example.voteapp.server.plugins.UserIdPrincipal>()?.name
                // Not used yet (repository is still shared), but kept for future per-user filtering.
                val votings = getVotingsUseCase()
                call.respond(votings)
            }

            // POST /api/v1/votings, /api/v1/votings/{id} later
        }
    }
}


fun Application.configureVotingsRouting(getVotingsUseCase: GetVotingsUseCase) {
    routing {
        route("/api/v1") {
            v1Votings(getVotingsUseCase)
        }
    }
}


