package com.example.voteapp.server.plugins

import com.example.voteapp.server.votings.domain.usecase.AlreadyVotedException
import com.example.voteapp.server.votings.domain.usecase.ValidationException
import com.example.voteapp.server.votings.domain.usecase.VotingAlreadyClosedException
import com.example.voteapp.server.votings.domain.usecase.VotingException
import com.example.voteapp.server.votings.domain.usecase.VotingNotFoundException
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.plugins.StatusPages
import io.ktor.server.response.respond
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.Database
import org.slf4j.LoggerFactory

fun Application.configureStatusPages() {
    val log = LoggerFactory.getLogger("StatusPages")

    install(StatusPages) {
        exception<ValidationException> { call, _ ->
            call.respond(HttpStatusCode.BadRequest, mapOf(
                "error" to (it.message ?: "Validation error"),
                "code" to HttpStatusCode.BadRequest.value
            ))
        }

        exception<VotingNotFoundException> { call, _ ->
            call.respond(HttpStatusCode.NotFound, mapOf(
                "error" to (it.message ?: "Voting not found"),
                "code" to HttpStatusCode.NotFound.value
            ))
        }

        exception<VotingAlreadyClosedException> { call, _ ->
            call.respond(HttpStatusCode.BadRequest, mapOf(
                "error" to (it.message ?: "Voting already closed"),
                "code" to HttpStatusCode.BadRequest.value
            ))
        }

        exception<AlreadyVotedException> { call, _ ->
            call.respond(HttpStatusCode.BadRequest, mapOf(
                "error" to (it.message ?: "User already voted"),
                "code" to HttpStatusCode.BadRequest.value
            ))
        }

        exception<Throwable> { call, cause ->
            log.error("Unhandled error", cause)
            call.respond(HttpStatusCode.InternalServerError, mapOf(
                "error" to "Internal server error",
                "code" to HttpStatusCode.InternalServerError.value
            ))
        }
    }
}

