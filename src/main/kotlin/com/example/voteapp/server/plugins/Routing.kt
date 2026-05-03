package com.example.voteapp.server.plugins

import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.application.*
import com.example.voteapp.server.models.votingList

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Voting App Server is running! APIs at /api/v1/votings")
        }
        
        route("/api/v1") {
            get("/votings") {
                call.respond(votingList)
            }
            
            // POST /votings, etc. later
        }
    }
}

