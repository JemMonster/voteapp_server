package com.example.voteapp.server.votings

import com.example.voteapp.server.plugins.UserIdPrincipal
import com.example.voteapp.server.votings.domain.model.Voting
import com.example.voteapp.server.votings.domain.port.VotingRepository
import com.example.voteapp.server.votings.domain.usecase.GetVotingsUseCase
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class VotingsRouteTest {

    @Test
    fun `GET votings without token returns 401`() = testApplication {
        val repository = object : VotingRepository {
            override suspend fun getVotings(): List<Voting> = listOf(Voting(1, "t1", "d1"))
        }
        val useCase = GetVotingsUseCase(repository)

        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; isLenient = true })
            }

            install(Authentication) {
                // No provider registered -> authenticate("firebase-jwt") fails
            }

            com.example.voteapp.server.votings.configureVotingsRouting(useCase)
        }

        val response = client.get("/api/v1/votings")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `GET votings with mocked principal returns 200`() = testApplication {
        val repository = object : VotingRepository {
            override suspend fun getVotings(): List<Voting> = listOf(Voting(1, "t1", "d1"))
        }
        val useCase = GetVotingsUseCase(repository)

        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; isLenient = true })
            }

            // Provide a test authentication provider with the same name as production.
            install(Authentication) {
                basic("firebase-jwt") {
                    realm = "test"
                    validate { _, _ ->
                        UserIdPrincipal("user-1")
                    }
                }
            }

            com.example.voteapp.server.votings.configureVotingsRouting(useCase)
        }

        val response = client.get("/api/v1/votings") {
            header(HttpHeaders.Authorization, "Basic dGVzdDp0ZXN0")
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }
}


