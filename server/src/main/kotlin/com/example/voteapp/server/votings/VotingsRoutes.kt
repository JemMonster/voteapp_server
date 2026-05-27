package com.example.voteapp.server.votings

import com.example.voteapp.server.plugins.UserIdPrincipal
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
import com.example.voteapp.server.votings.domain.usecase.UnauthorizedException
import com.example.voteapp.server.votings.domain.usecase.VotingAlreadyClosedException
import com.example.voteapp.server.votings.domain.usecase.AlreadyVotedException
import com.example.voteapp.server.votings.domain.usecase.VotingNotFoundException
import com.example.voteapp.server.votings.domain.usecase.ValidationException

import com.example.voteapp.server.votings.models.NewVoting
import com.example.voteapp.server.votings.models.VotePayload
import com.example.voteapp.server.votings.models.VotingResult
import com.example.voteapp.server.votings.models.InvitePayload
import com.example.voteapp.server.votings.models.UpdateVotingRequest
import com.example.voteapp.server.votings.models.UpdateVoteRequest
import com.example.voteapp.server.votings.models.VoteResponse

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.response.header
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.delete
import io.ktor.server.routing.route
import io.ktor.server.request.receive
import io.ktor.server.routing.path
import io.ktor.server.routing.parameter
import io.ktor.server.application.call
import io.ktor.server.routing.respond
import java.util.UUID

fun Route.v1Votings(
    getVotingsUseCase: GetVotingsUseCase,
    createVotingUseCase: CreateVotingUseCase,
    voteUseCase: VoteUseCase,
    getResultsUseCase: GetResultsUseCase,
    getVotingDetailsUseCase: GetVotingDetailsUseCase,
    getVotingHistoryUseCase: GetVotingHistoryUseCase,
    inviteUseCase: InviteUseCase,
    updateVotingUseCase: UpdateVotingUseCase,
    deleteVotingUseCase: DeleteVotingUseCase,
    updateVoteUseCase: UpdateVoteUseCase,
) {
    // Get all votings (public)
    get("/votings") {
        val status = call.request.queryParameters["status"]
        val type = call.request.queryParameters["type"]

        val votings = getVotingsUseCase(status, type)
        call.respond(votings)
    }

    // Protected routes
    authenticate("firebase-jwt") {
        // Get voting details
        get("/votings/{id}") {
            try {
                val votingId = UUID.fromString(call.parameters["id"] ?: error("Missing id"))
                val voting = getVotingDetailsUseCase(votingId)
                call.respond(voting)
            } catch (e: VotingNotFoundException) {
                call.respond(HttpStatusCode.NotFound, mapOf(
                    "error" to "not_found",
                    "message" to e.message
                ))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf(
                    "error" to "invalid_request",
                    "message" to e.message
                ))
            }
        }

        // Get voting history
        get("/votings/history") {
            val userId = call.principal<UserIdPrincipal>()?.name
                ?: throw io.ktor.server.auth.AuthenticationException("unauthorized")

            val history = getVotingHistoryUseCase(UUID.fromString(userId))
            call.respond(history)
        }

        // Invite user to voting
        post("/votings/{id}/invite") {
            try {
                val votingId = UUID.fromString(call.parameters["id"] ?: error("Missing id"))
                val userId = call.principal<UserIdPrincipal>()?.name
                    ?: throw io.ktor.server.auth.AuthenticationException("unauthorized")

                val payload = call.receive<InvitePayload>()

                val response = inviteUseCase(votingId, payload.email)

                call.respond(response)
            } catch (e: Exception) {
                handleException(call, e)
            }
        }

        // Create voting
        post("/votings") {
            try {
                val userId = call.principal<UserIdPrincipal>()?.name
                    ?: throw io.ktor.server.auth.AuthenticationException("unauthorized")

                val dto = call.receive<NewVoting>()

                if (dto.title.isBlank()) {
                    throw ValidationException("Title is required")
                }

                val created = createVotingUseCase(dto, UUID.fromString(userId))

                call.response.header("Location", "/api/v1/votings/${created.id}")
                call.respond(HttpStatusCode.Created, created)
            } catch (e: ValidationException) {
                call.respond(HttpStatusCode.BadRequest, mapOf(
                    "error" to "validation_error",
                    "message" to e.message
                ))
            } catch (e: Exception) {
                handleException(call, e)
            }
        }

        // Update voting (PUT)
        put("/votings/{id}") {
            try {
                val votingId = UUID.fromString(call.parameters["id"] ?: error("Missing id"))
                val userId = call.principal<UserIdPrincipal>()?.name
                    ?: throw io.ktor.server.auth.AuthenticationException("unauthorized")

                val request = call.receive<UpdateVotingRequest>()

                val updated = updateVotingUseCase.execute(votingId, UUID.fromString(userId), request)

                call.respond(HttpStatusCode.OK, updated)
            } catch (e: UnauthorizedException) {
                call.respond(HttpStatusCode.Forbidden, mapOf(
                    "error" to "forbidden",
                    "message" to e.message
                ))
            } catch (e: VotingAlreadyClosedException) {
                call.respond(HttpStatusCode.Conflict, mapOf(
                    "error" to "voting_closed",
                    "message" to e.message
                ))
            } catch (e: ValidationException) {
                call.respond(HttpStatusCode.BadRequest, mapOf(
                    "error" to "validation_error",
                    "message" to e.message
                ))
            } catch (e: VotingNotFoundException) {
                call.respond(HttpStatusCode.NotFound, mapOf(
                    "error" to "not_found",
                    "message" to e.message
                ))
            } catch (e: Exception) {
                handleException(call, e)
            }
        }

        // Delete voting (DELETE)
        delete("/votings/{id}") {
            try {
                val votingId = UUID.fromString(call.parameters["id"] ?: error("Missing id"))
                val userId = call.principal<UserIdPrincipal>()?.name
                    ?: throw io.ktor.server.auth.AuthenticationException("unauthorized")

                deleteVotingUseCase.execute(votingId, UUID.fromString(userId))

                call.respond(HttpStatusCode.NoContent)
            } catch (e: UnauthorizedException) {
                call.respond(HttpStatusCode.Forbidden, mapOf(
                    "error" to "forbidden",
                    "message" to e.message
                ))
            } catch (e: VotingNotFoundException) {
                call.respond(HttpStatusCode.NotFound, mapOf(
                    "error" to "not_found",
                    "message" to e.message
                ))
            } catch (e: Exception) {
                handleException(call, e)
            }
        }

        // Cast vote (POST)
        post("/votings/{id}/vote") {
            try {
                val votingId = UUID.fromString(call.parameters["id"] ?: error("Missing id"))

                val userId = call.principal<UserIdPrincipal>()?.name
                    ?: throw io.ktor.server.auth.AuthenticationException("unauthorized")

                val payload = call.receive<VotePayload>()
                val result = voteUseCase(votingId, UUID.fromString(userId), payload)
                call.respond(HttpStatusCode.OK, result)
            } catch (e: VotingAlreadyClosedException) {
                call.respond(HttpStatusCode.Conflict, mapOf(
                    "error" to "voting_closed",
                    "message" to e.message
                ))
            } catch (e: AlreadyVotedException) {
                call.respond(HttpStatusCode.Conflict, mapOf(
                    "error" to "already_voted",
                    "message" to e.message
                ))
            } catch (e: Exception) {
                handleException(call, e)
            }
        }

        // Update vote (PUT)
        put("/votings/{id}/vote") {
            try {
                val votingId = UUID.fromString(call.parameters["id"] ?: error("Missing id"))
                val userId = call.principal<UserIdPrincipal>()?.name
                    ?: throw io.ktor.server.auth.AuthenticationException("unauthorized")

                val request = call.receive<UpdateVoteRequest>()
                updateVoteUseCase.execute(votingId, UUID.fromString(userId), request.optionIds ?: emptyList())

                call.respond(HttpStatusCode.OK, VoteResponse(
                    success = true,
                    message = "Vote updated successfully",
                    votingId = votingId.toString()
                ))
            } catch (e: VotingAlreadyClosedException) {
                call.respond(HttpStatusCode.Conflict, mapOf(
                    "error" to "voting_closed",
                    "message" to e.message
                ))
            } catch (e: AlreadyVotedException) {
                call.respond(HttpStatusCode.Conflict, mapOf(
                    "error" to "not_voted",
                    "message" to "You haven't voted yet"
                ))
            } catch (e: Exception) {
                handleException(call, e)
            }
        }

        // Remove vote (DELETE)
        delete("/votings/{id}/vote") {
            try {
                val votingId = UUID.fromString(call.parameters["id"] ?: error("Missing id"))
                val userId = call.principal<UserIdPrincipal>()?.name
                    ?: throw io.ktor.server.auth.AuthenticationException("unauthorized")

                updateVoteUseCase.removeVote(votingId, UUID.fromString(userId))

                call.respond(HttpStatusCode.NoContent)
            } catch (e: VotingAlreadyClosedException) {
                call.respond(HttpStatusCode.Conflict, mapOf(
                    "error" to "voting_closed",
                    "message" to e.message
                ))
            } catch (e: Exception) {
                handleException(call, e)
            }
        }

        // Get results
        get("/votings/{id}/results") {
            try {
                val votingId = UUID.fromString(call.parameters["id"] ?: error("Missing id"))
                val result = getResultsUseCase(votingId)
                call.respond(result)
            } catch (e: VotingNotFoundException) {
                call.respond(HttpStatusCode.NotFound, mapOf(
                    "error" to "not_found",
                    "message" to e.message
                ))
            } catch (e: Exception) {
                handleException(call, e)
            }
        }
    }
}

fun io.ktor.server.application.Application.configureVotingsRouting(
    getVotingsUseCase: GetVotingsUseCase,
    createVotingUseCase: CreateVotingUseCase,
    voteUseCase: VoteUseCase,
    getResultsUseCase: GetResultsUseCase,
    getVotingDetailsUseCase: GetVotingDetailsUseCase,
    getVotingHistoryUseCase: GetVotingHistoryUseCase,
    inviteUseCase: InviteUseCase,
    updateVotingUseCase: UpdateVotingUseCase,
    deleteVotingUseCase: DeleteVotingUseCase,
    updateVoteUseCase: UpdateVoteUseCase,
) {
    routing {
        route("/api/v1") {
            v1Votings(
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
    }
}

/**
 * Centralized exception handler for routes
 */
fun handleException(call: ApplicationCall, exception: Exception) {
    when (exception) {
        is IllegalArgumentException -> {
            call.respond(HttpStatusCode.BadRequest, mapOf(
                "error" to "invalid_request",
                "message" to exception.message
            ))
        }
        is IllegalStateException -> {
            call.respond(HttpStatusCode.Conflict, mapOf(
                "error" to "conflict",
                "message" to exception.message
            ))
        }
        is NoSuchElementException -> {
            call.respond(HttpStatusCode.NotFound, mapOf(
                "error" to "not_found",
                "message" to exception.message
            ))
        }
        else -> {
            call.respond(HttpStatusCode.InternalServerError, mapOf(
                "error" to "internal_error",
                "message" to "An unexpected error occurred"
            ))
        }
    }
}



