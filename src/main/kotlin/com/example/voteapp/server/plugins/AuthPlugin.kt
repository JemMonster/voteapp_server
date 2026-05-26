package com.example.voteapp.server.plugins

import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseToken
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.Principal
import io.ktor.server.auth.UserIdPrincipal as KtorUserIdPrincipal
import io.ktor.server.auth.bearer
import io.ktor.server.auth.authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.request.ApplicationRequest
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.auth.challenge
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import java.io.FileInputStream

/**
 * Firebase ID-token auth для Ktor.
 *
 * Требования к окружению:
 * - GOOGLE_APPLICATION_CREDENTIALS: путь к service-account JSON для Firebase Admin SDK
 */
private fun ensureFirebaseApp(): FirebaseApp {
    val existing = FirebaseApp.getApps().firstOrNull { it.name == FirebaseApp.DEFAULT_APP_NAME }
    if (existing != null) return existing

    val credentialsPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS")
        ?: error("GOOGLE_APPLICATION_CREDENTIALS env var is required")

    return FirebaseApp.initializeApp(
        FirebaseOptions.builder()
            .setCredentials(
                com.google.auth.oauth2.GoogleCredentials.fromStream(
                    FileInputStream(credentialsPath)
                )
            )
            .build()
    )
}

/**
 * Principal, содержащий userId (Firebase uid).
 */
class UserIdPrincipal(val userIdValue: String) : Principal {
    override val name: String get() = userIdValue
}

/**
 * Удобный помощник для получения userId.
 */
fun <T : Any> io.ktor.server.application.ApplicationCall.principalUserId(): String? {
    return principal<UserIdPrincipal>()?.name
}

fun Application.configureAuth() {
    install(Authentication) {
        bearer("firebase-jwt") {
            realm = "firebase"

            verify { tokenCredential ->
                val token = tokenCredential.token

                runCatching {
                    val app = ensureFirebaseApp()
                    val decoded: FirebaseToken = FirebaseAuth.getInstance(app).verifyIdToken(token, true)
                    val uid = decoded.uid

                    // Attach principal
                    UserIdPrincipal(uid)
                }.getOrElse {
                    null
                }
            }

            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "unauthorized"))
            }
        }
    }
}

