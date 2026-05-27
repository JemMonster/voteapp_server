package com.example.voteapp.server.votings

import com.example.voteapp.server.plugins.configureStatusPages
import com.example.voteapp.server.plugins.UserIdPrincipal
import com.example.voteapp.server.votings.domain.port.VotingRepository
import com.example.voteapp.server.votings.domain.usecase.GetVotingHistoryUseCase
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

class VotingsHistoryRouteTest {

    @Test
    fun `GET /api/v1/votings/history without token returns 401`() = testApplication {
        val repo = FakeVotingRepository()
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

        val response = client.get("/api/v1/votings/history")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    private class FakeVotingRepository : VotingRepository {
        override suspend fun getVotings() = emptyList<com.example.voteapp.server.votings.domain.model.Voting>()
        override suspend fun create(dto: com.example.voteapp.server.votings.models.NewVoting, creatorId: UUID) =
            throw UnsupportedOperationException()
        override suspend fun vote(id: UUID, userId: UUID, payload: com.example.voteapp.server.votings.models.VotePayload) =
            throw UnsupportedOperationException()
        override suspend fun getResults(id: UUID) = throw UnsupportedOperationException()
        override suspend fun getById(id: UUID) = null
        override suspend fun getVotingById(id: UUID) = null
        override suspend fun getVotingHistory(userId: UUID) = emptyList<com.example.voteapp.server.votings.domain.model.Voting>()
        override suspend fun invite(votingId: UUID, email: String) =
            throw UnsupportedOperationException()
    }
}


