package com.example.voteapp.server.votings

import com.example.voteapp.server.plugins.UserIdPrincipal
import com.example.voteapp.server.votings.domain.port.VotingRepository
import com.example.voteapp.server.votings.domain.usecase.CreateVotingUseCase
import com.example.voteapp.server.votings.domain.usecase.GetResultsUseCase
import com.example.voteapp.server.votings.domain.usecase.GetVotingsUseCase
import com.example.voteapp.server.votings.domain.usecase.VoteUseCase
import com.example.voteapp.server.votings.models.NewVoting
import com.example.voteapp.server.votings.models.VotePayload
import com.example.voteapp.server.votings.models.VotingResult
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

class VotingsRouteTest {

    @Test
    fun `GET /api/v1/votings returns 200 with empty list`() = testApplication {
        val getVotingsUseCase = GetVotingsUseCase { emptyList() }
        val repo = FakeVotingRepository()
        val createVotingUseCase = CreateVotingUseCase(repo)
        val voteUseCase = VoteUseCase(repo)
        val getResultsUseCase = GetResultsUseCase(repo)

        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; isLenient = true })
            }

            install(Authentication) {
                bearer("firebase-jwt") {
                    realm = "test"
                    authenticate { _ -> UserIdPrincipal("3fa85f64-5717-4562-b3fc-2c963f66afa6") }
                }
            }

            configureStatusPages()
            configureVotingsRouting(getVotingsUseCase, createVotingUseCase, voteUseCase, getResultsUseCase)
        }

        val response = client.get("/api/v1/votings")
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `POST /api/v1/votings without token returns 401`() = testApplication {
        val getVotingsUseCase = GetVotingsUseCase { emptyList() }
        val repo = FakeVotingRepository()
        val createVotingUseCase = CreateVotingUseCase(repo)
        val voteUseCase = VoteUseCase(repo)
        val getResultsUseCase = GetResultsUseCase(repo)

        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; isLenient = true })
            }

            install(Authentication) {
                bearer("firebase-jwt") {
                    realm = "test"
                    // no validate -> unauthorized without header
                }
            }

            configureStatusPages()
            configureVotingsRouting(getVotingsUseCase, createVotingUseCase, voteUseCase, getResultsUseCase)
        }

        val newVoting = NewVoting(
            title = "Title",
            description = null,
            imageUrl = null,
            type = VotingType.SINGLE,
            durationDays = 15,
            options = listOf("o1", "o2")
        )

        val response = client.post("/api/v1/votings") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(NewVoting.serializer(), newVoting))
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `POST /api/v1/votings with valid data returns 201`() = testApplication {
        val getVotingsUseCase = GetVotingsUseCase { emptyList() }
        val repo = FakeVotingRepository()
        val createVotingUseCase = CreateVotingUseCase(repo)
        val voteUseCase = VoteUseCase(repo)
        val getResultsUseCase = GetResultsUseCase(repo)

        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; isLenient = true })
            }

            install(Authentication) {
                bearer("firebase-jwt") {
                    realm = "test"
                    authenticate { UserIdPrincipal("3fa85f64-5717-4562-b3fc-2c963f66afa6") }
                }
            }

            configureStatusPages()
            configureVotingsRouting(getVotingsUseCase, createVotingUseCase, voteUseCase, getResultsUseCase)
        }

        val newVoting = NewVoting(
            title = "Title",
            description = null,
            imageUrl = null,
            type = VotingType.SINGLE,
            durationDays = 15,
            options = listOf("o1", "o2")
        )

        val response = client.post("/api/v1/votings") {
            header(HttpHeaders.Authorization, "Bearer test")
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(NewVoting.serializer(), newVoting))
        }

        assertEquals(HttpStatusCode.Created, response.status)
    }

    @Test
    fun `POST /api/v1/votings with empty title returns 400`() = testApplication {
        val getVotingsUseCase = GetVotingsUseCase { emptyList() }
        val repo = FakeVotingRepository()
        val createVotingUseCase = CreateVotingUseCase(repo)
        val voteUseCase = VoteUseCase(repo)
        val getResultsUseCase = GetResultsUseCase(repo)

        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; isLenient = true })
            }

            install(Authentication) {
                bearer("firebase-jwt") {
                    realm = "test"
                    authenticate { UserIdPrincipal("3fa85f64-5717-4562-b3fc-2c963f66afa6") }
                }
            }

            configureStatusPages()
            configureVotingsRouting(getVotingsUseCase, createVotingUseCase, voteUseCase, getResultsUseCase)
        }

        val newVoting = NewVoting(
            title = "",
            description = null,
            imageUrl = null,
            type = VotingType.SINGLE,
            durationDays = 15,
            options = listOf("o1", "o2")
        )

        val response = client.post("/api/v1/votings") {
            header(HttpHeaders.Authorization, "Bearer test")
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(NewVoting.serializer(), newVoting))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    private class FakeVotingRepository : VotingRepository {
        override suspend fun getVotings(): List<com.example.voteapp.server.votings.domain.model.Voting> = emptyList()
        override suspend fun create(dto: NewVoting, creatorId: java.util.UUID): com.example.voteapp.server.votings.domain.model.Voting {
            return com.example.voteapp.server.votings.domain.model.Voting(
                id = java.util.UUID.randomUUID(),
                title = dto.title,
                description = dto.description.orEmpty(),
                type = dto.type,
                status = VotingStatus.ACTIVE,
                imageUrl = dto.imageUrl,
                endsAt = kotlinx.datetime.Clock.System.now().toKotlinLocalDateTime(),
                totalVotes = 0,
                hasVoted = false
            )
        }

        override suspend fun vote(
            id: java.util.UUID,
            userId: java.util.UUID,
            payload: VotePayload
        ): VotingResult {
            return VotingResult(
                votingId = id,
                status = VotingStatus.ACTIVE,
                type = VotingType.SINGLE,
                totalParticipants = 1,
                optionsResults = null,
                signaturesCount = null,
                winnerInfo = null
            )
        }

        override suspend fun getResults(id: java.util.UUID): VotingResult {
            return VotingResult(
                votingId = id,
                status = VotingStatus.ACTIVE,
                type = VotingType.SINGLE,
                totalParticipants = 1,
                optionsResults = null,
                signaturesCount = null,
                winnerInfo = null
            )
        }

        override suspend fun getById(id: java.util.UUID): com.example.voteapp.server.votings.domain.model.Voting? {
            return com.example.voteapp.server.votings.domain.model.Voting(
                id = id,
                title = "t",
                description = "d",
                type = VotingType.SINGLE,
                status = VotingStatus.ACTIVE,
                imageUrl = null,
                endsAt = kotlinx.datetime.Clock.System.now().toKotlinLocalDateTime(),
                totalVotes = 0,
                hasVoted = false
            )
        }
    }
}

