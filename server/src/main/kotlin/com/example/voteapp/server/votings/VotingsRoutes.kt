package com.example.voteapp.server.votings

import com.example.voteapp.server.plugins.UserIdPrincipal
import com.example.voteapp.server.votings.domain.usecase.CreateVotingUseCase
import com.example.voteapp.server.votings.domain.usecase.GetResultsUseCase
import com.example.voteapp.server.votings.domain.usecase.GetVotingsUseCase
import com.example.voteapp.server.votings.domain.usecase.VoteUseCase
import com.example.voteapp.server.votings.models.NewVoting
import com.example.voteapp.server.votings.models.VotePayload
import com.example.voteapp.server.votings.models.VotingResult
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
) {
    get("/votings") {
        val status = call.request.queryParameters["status"] ?: "active"
        val type = call.request.queryParameters["type"]

        // Пока репозиторий возвращает все голосования; фильтрация будет реализована на слое use-case/repository.
        // Для совместимости контрактов возвращаем текущий набор.
        val votings = getVotingsUseCase()
        call.respond(votings)
    }

    authenticate("firebase-jwt") {
        // TODO (Phase 3): реализовать:
        // GET /api/v1/votings/{id}
        // GET /api/v1/votings/history
        // POST /api/v1/votings/{id}/invite
        // После создания use-cases/repository methods и миграции invites.

        post("/votings") {
            val userId = call.principal<UserIdPrincipal>()?.name
                ?: throw com.example.voteapp.server.votings.domain.usecase.ValidationException("Unauthorized")

            val dto = call.receive<NewVoting>()

            if (dto.title.isBlank()) {
                throw com.example.voteapp.server.votings.domain.usecase.ValidationException("Title is required")
            }

            val created = createVotingUseCase(dto, UUID.fromString(userId))

            call.response.header("Location", "/api/v1/votings/${created.id}")
            call.respond(created)
        }

        post("/votings/{id}/vote") {
            val votingId = UUID.fromString(call.parameters["id"] ?: error("Missing id"))

            val userId = call.principal<UserIdPrincipal>()?.name
                ?: throw com.example.voteapp.server.votings.domain.usecase.ValidationException("Unauthorized")

            val payload = call.receive<VotePayload>()
            val result = voteUseCase(votingId, UUID.fromString(userId), payload)
            call.respond(result)
        }

        get("/votings/{id}/results") {
            val votingId = UUID.fromString(call.parameters["id"] ?: error("Missing id"))
            val result = getResultsUseCase(votingId)
            call.respond(result)
        }
    }
}

fun io.ktor.server.application.Application.configureVotingsRouting(
    getVotingsUseCase: GetVotingsUseCase,
    createVotingUseCase: CreateVotingUseCase,
    voteUseCase: VoteUseCase,
    getResultsUseCase: GetResultsUseCase,
) {
    routing {
        route("/api/v1") {
            v1Votings(getVotingsUseCase, createVotingUseCase, voteUseCase, getResultsUseCase)
        }
    }
}



