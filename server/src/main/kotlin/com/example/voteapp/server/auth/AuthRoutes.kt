package com.example.voteapp.server.auth

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

// Пока каркас. Дальше здесь будет проверка JWT/Firebase token и middleware.
fun Route.v1Auth() {
    route("/auth") {
        get("/health") {
            call.respond(mapOf("status" to "ok", "module" to "auth"))
        }
    }
}

fun Application.configureAuthRouting() {
    routing {
        route("/api/v1") {
            v1Auth()
        }
    }
}

