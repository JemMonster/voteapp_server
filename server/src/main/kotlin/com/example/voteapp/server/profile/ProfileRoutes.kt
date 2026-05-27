package com.example.voteapp.server.profile

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

// Каркас под будущие профили пользователей.
fun Route.v1Profile() {
    route("/profile") {
        get("/health") {
            call.respond(mapOf("status" to "ok", "module" to "profile"))
        }
    }
}

fun Application.configureProfileRouting() {
    routing {
        route("/api/v1") {
            v1Profile()
        }
    }
}

