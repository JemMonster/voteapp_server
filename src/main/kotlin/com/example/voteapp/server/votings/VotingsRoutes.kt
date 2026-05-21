package com.example.voteapp.server.votings

import com.example.voteapp.server.votings.model.votingList
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.v1Votings() {
    route("/votings") {
        get {
            call.respond(votingList)
        }

        // POST /api/v1/votings, /api/v1/votings/{id} later
    }
}

fun Application.configureVotingsRouting() {
    routing {
        route("/api/v1") {
            v1Votings()
        }
    }
}

