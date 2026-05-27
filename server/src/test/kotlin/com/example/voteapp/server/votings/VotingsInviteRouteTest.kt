package com.example.voteapp.server.votings

import com.example.voteapp.server.plugins.configureStatusPages
import com.example.voteapp.server.plugins.UserIdPrincipal
import com.example.voteapp.server.votings.domain.port.VotingRepository
import com.example.voteapp.server.votings.domain.usecase.InviteUseCase
import com.example.voteapp.server.votings.domain.usecase.ValidationException
import com.example.voteapp.server.votings.models.InvitePayload
import com.example.voteapp.server.votings.models.InviteResponse
import com.example.voteapp.server.votings.models.VotingStatus
import com.example.voteapp.server.votings.models.VotingType
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.bearer
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.UUID

class VotingsInviteRouteTest {

    @Test
    fun `POST /api/v1/votings/{id}/invite returns 200`() = testApplication {
        val repo = FakeVotingRepository(emailExists = true)
        val dummyCreate = com.example.voteapp.server.votings.domain.usecase.CreateVotingUseCase(repo)
        val dummyVote = com.example.voteapp.server.votings.domain.usecase.VoteUseCase(repo)
        val dummyResults = com.example.voteapp.server.votings.domain.usecase.GetResultsUseCase(repo)

        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; isLenient = true })
            }

            install(Authentication) {
                bearer("firebase-jwt") {
                    realm = "test"
                    authenticate {
                        UserIdPrincipal("3fa85f64-5717-4562-b3fc-2c963f66afa6")
                    }
                }
            }

            configureStatusPages()
            configureVotingsRouting(
                getVotingsUseCase = { emptyList() },
                createVotingUseCase = dummyCreate,
                voteUseCase = dummyVote,
                getResultsUseCase = dummyResults,
            )
        }

        val votingId = UUID.randomUUID()
        val response = client.post("/api/v1/votings/$votingId/invite") {
            header(HttpHeaders.Authorization, "Bearer test")
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(InvitePayload.serializer(), InvitePayload("a@test.com")))
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `POST /api/v1/votings/{id}/invite without token returns 401`() = testApplication {
        val repo = FakeVotingRepository(emailExists = true)

        val dummyCreate = com.example.voteapp.server.votings.domain.usecase.CreateVotingUseCase(repo)
        val dummyVote = com.example.voteapp.server.votings.domain.usecase.VoteUseCase(repo)
        val dummyResults = com.example.voteapp.server.votings.domain.usecase.GetResultsUseCase(repo)

        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; isLenient = true })
            }

            install(Authentication) {
                bearer("firebase-jwt") {
                    realm = "test"
                    // no authenticate => 401
                }
            }

            configureStatusPages()
            configureVotingsRouting(getVotingsUseCase = { emptyList() }, createVotingUseCase = dummyCreate, voteUseCase = dummyVote, getResultsUseCase = dummyResults)
        }

        val response = client.post("/api/v1/votings/${UUID.randomUUID()}/invite") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(InvitePayload.serializer(), InvitePayload("a@test.com")))
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    private class FakeVotingRepository(
        private val emailExists: Boolean,
    ) : VotingRepository {
        override suspend fun getVotings() = emptyList<com.example.voteapp.server.votings.domain.model.Voting>()
        override suspend fun getVotingsFiltered(status: String?, type: String?) = emptyList<com.example.voteapp.server.votings.domain.model.Voting>()

        override suspend fun create(dto: com.example.voteapp.server.votings.models.NewVoting, creatorId: UUID) =
            throw UnsupportedOperationException()

        override suspend fun vote(
            id: UUID,
            userId: UUID,
            payload: com.example.voteapp.server.votings.models.VotePayload
        ) = throw UnsupportedOperationException()

        override suspend fun getResults(id: UUID) = throw UnsupportedOperationException()
        override suspend fun getById(id: UUID) = null

        override suspend fun getVotingById(id: UUID) = null
        override suspend fun getVotingHistory(userId: UUID) = emptyList<com.example.voteapp.server.votings.domain.model.Voting>()

        override suspend fun invite(votingId: UUID, email: String): InviteResponse {
            if (!emailExists) throw com.example.voteapp.server.votings.domain.usecase.EmailNotFoundException(email)
            return InviteResponse(votingId.leastSignificantBits, email, status = "SUCCESS")
        }
    }
}


