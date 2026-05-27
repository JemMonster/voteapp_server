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
    fun `POST /api/v1/votings id invite without token returns 401`() = testApplication {
        val repository = FakeVotingRepository(emailExists = true)
        val inviteUseCase = InviteUseCase(repository)

        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; isLenient = true })
            }

            install(Authentication) {
                bearer("firebase-jwt") {
                    realm = "test"
                    // no authenticate => unauthorized
                }
            }

            configureStatusPages()
            // configure routes via VotingsRoutes extension in next step
            // placeholder: route registration is part of Phase 3 endpoint work
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
        override suspend fun create(dto: com.example.voteapp.server.votings.models.NewVoting, creatorId: UUID) =
            com.example.voteapp.server.votings.domain.model.Voting(
                id = creatorId,
                title = dto.title,
                description = dto.description.orEmpty(),
                type = dto.type,
                status = VotingStatus.ACTIVE,
                imageUrl = dto.imageUrl,
                endsAt = kotlinx.datetime.Clock.System.now().toKotlinLocalDateTime(),
                totalVotes = 0,
                hasVoted = false
            )

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

