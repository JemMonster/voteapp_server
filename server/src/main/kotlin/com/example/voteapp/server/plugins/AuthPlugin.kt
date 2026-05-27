package com.example.voteapp.server.plugins

import com.example.voteapp.server.config.AppConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseToken
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.Principal
import io.ktor.server.auth.bearer
import io.ktor.server.auth.authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.UserIdPrincipal as KtorUserIdPrincipal
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.auth.challenge
import io.ktor.server.auth.*
import io.ktor.server.auth.bearer.BearerChallenge
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.header
import io.ktor.server.application.call
import java.io.FileInputStream
import org.slf4j.LoggerFactory
import com.google.firebase.FirebaseApp as FirebaseAppApi
import io.ktor.server.auth.Principal
import com.example.voteapp.server.plugins.UserIdPrincipal

class UserIdPrincipal(val userIdValue: String) : Principal {
    override val name: String
        get() = userIdValue
}

fun <T : Any> io.ktor.server.application.ApplicationCall.principalUserId(): String? {
    return principal<UserIdPrincipal>()?.name
}

class InvalidTokenException(message: String) : RuntimeException(message)

class ExpiredTokenException(message: String) : RuntimeException(message)

private fun ensureFirebaseApp(): FirebaseApp {
    val existing = FirebaseApp.getApps().firstOrNull { it.name == FirebaseApp.DEFAULT_APP_NAME }
    if (existing != null) return existing

    val serviceAccountPath = System.getenv("FIREBASE_SERVICE_ACCOUNT_JSON_PATH")
        ?: AppConfig.firebaseJsonPath

    return FirebaseApp.initializeApp(
        FirebaseOptions.builder()
            .setCredentials(com.google.auth.oauth2.GoogleCredentials.fromStream(FileInputStream(serviceAccountPath)))
            .build()
    )
}

fun Application.configureAuth(
    registerUserUseCase: com.example.voteapp.server.auth.RegisterUserUseCase,
    loginUseCase: com.example.voteapp.server.auth.LoginUseCase
) {
    val log = LoggerFactory.getLogger("AuthPlugin")

    install(Authentication) {
        bearer("firebase-jwt") {
            realm = "firebase"

            verify { tokenCredential ->
                val token = tokenCredential.token

                runCatching {
                    val app = ensureFirebaseApp()
                    val decoded: FirebaseToken = FirebaseAuth.getInstance(app).verifyIdToken(token, true)
                    val uid = decoded.uid
                    UserIdPrincipal(uid)
                }.getOrElse { t ->
                    log.warn("Firebase token verification failed", t)
                    null
                }
            }

            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, mapOf(
                    "error" to "unauthorized",
                    "code" to HttpStatusCode.Unauthorized.value
                ))
            }
        }
    }
    
    // Configure auth routes
    configureAuthRoutes(registerUserUseCase, loginUseCase)
}

