package com.example.voteapp.server.votings

import com.example.voteapp.server.plugins.configureStatusPages
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
    fun `GET voting details without token returns 401`() = testApplication {
        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; isLenient = true })
            }

            install(Authentication) {
                bearer("firebase-jwt") {
                    realm = "test"
                    // unauthorized
                }
            }

            configureStatusPages()
            // route registration is part of Phase 3 endpoint work
        }

        val response = client.get("/api/v1/votings/${UUID.randomUUID()}")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}

