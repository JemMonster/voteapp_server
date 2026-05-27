package com.example.voteapp.server.votings

import com.example.voteapp.server.plugins.configureStatusPages
import com.example.voteapp.server.plugins.UserIdPrincipal

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

class VotingsDetailsRouteTest {

    @Test
    fun `GET /api/v1/votings/{id} without token returns 401`() = testApplication {
        val repo = FakeVotingRepository()
        val getDetailsUseCase = com.example.voteapp.server.votings.domain.usecase.GetVotingDetailsUseCase(repo)
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
                    // no authenticate => 401 without Authorization header
                }
            }

            configureStatusPages()
            configureVotingsRouting(getVotingsUseCase = { emptyList() }, createVotingUseCase = dummyCreate, voteUseCase = dummyVote, getResultsUseCase = dummyResults)
        }

        val response = client.get("/api/v1/votings/${UUID.randomUUID()}")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `GET /api/v1/votings/{id} returns 200`() = testApplication {
        val repo = FakeVotingRepository()
        val getDetailsUseCase = com.example.voteapp.server.votings.domain.usecase.GetVotingDetailsUseCase(repo)
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
        val response = client.get("/api/v1/votings/$votingId")
        assertEquals(HttpStatusCode.OK, response.status)
    }

    private class FakeVotingRepository : VotingRepository {
        override suspend fun getVotings() = emptyList<com.example.voteapp.server.votings.domain.model.Voting>()
        override suspend fun getVotingsFiltered(status: String?, type: String?) = emptyList<com.example.voteapp.server.votings.domain.model.Voting>()

        override suspend fun create(dto: com.example.voteapp.server.votings.models.NewVoting, creatorId: UUID) =
            throw UnsupportedOperationException()
        override suspend fun vote(id: UUID, userId: UUID, payload: com.example.voteapp.server.votings.models.VotePayload) =
            throw UnsupportedOperationException()
        override suspend fun getResults(id: UUID) = throw UnsupportedOperationException()
        override suspend fun getById(id: UUID) =
            com.example.voteapp.server.votings.domain.model.Voting(
                id = id,
                title = "t",
                description = "d",
                type = com.example.voteapp.server.votings.models.VotingType.SINGLE_CHOICE,
                status = com.example.voteapp.server.votings.models.VotingStatus.ACTIVE,
                imageUrl = null,
                endsAt = kotlinx.datetime.Clock.System.now().toKotlinLocalDateTime(),
                totalVotes = 0,
                hasVoted = false,
            )

        override suspend fun getVotingById(id: UUID) = getById(id)
        override suspend fun getVotingHistory(userId: UUID) = emptyList<com.example.voteapp.server.votings.domain.model.Voting>()
        override suspend fun invite(votingId: UUID, email: String) =
            throw UnsupportedOperationException()
    }
}





