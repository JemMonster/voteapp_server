package com.example.voteapp.server.auth

import com.example.voteapp.server.plugins.UserIdPrincipal
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

/**
 * Public auth routes (no authentication required)
 */
fun Route.authRoutes(
    registerUserUseCase: RegisterUserUseCase,
    loginUseCase: LoginUseCase
) {
    // User registration
    post("/register") {
        try {
            val request = call.receive<RegisterRequest>()
            val response = registerUserUseCase.execute(request)
            call.respond(HttpStatusCode.Created, response)
        } catch (e: EmailAlreadyExistsException) {
            call.respond(HttpStatusCode.Conflict, mapOf(
                "error" to "email_already_exists",
                "message" to e.message
            ))
        } catch (e: FirebaseVerificationException) {
            call.respond(HttpStatusCode.Unauthorized, mapOf(
                "error" to "invalid_token",
                "message" to e.message
            ))
        } catch (e: ValidationException) {
            call.respond(HttpStatusCode.BadRequest, mapOf(
                "error" to "validation_error",
                "message" to e.message
            ))
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, mapOf(
                "error" to "internal_error",
                "message" to "An unexpected error occurred"
            ))
        }
    }
    
    // User login
    post("/login") {
        try {
            val request = call.receive<LoginRequest>()
            val response = loginUseCase.execute(request)
            call.respond(HttpStatusCode.OK, response)
        } catch (e: UserNotFoundException) {
            call.respond(HttpStatusCode.NotFound, mapOf(
                "error" to "user_not_found",
                "message" to "User not found. Please register first."
            ))
        } catch (e: FirebaseVerificationException) {
            call.respond(HttpStatusCode.Unauthorized, mapOf(
                "error" to "invalid_token",
                "message" to e.message
            ))
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, mapOf(
                "error" to "internal_error",
                "message" to "An unexpected error occurred"
            ))
        }
    }
    
    // Health check
    get("/health") {
        call.respond(mapOf("status" to "ok", "module" to "auth"))
    }
}

/**
 * Protected auth routes (require authentication)
 */
fun Route.authProtectedRoutes() {
    authenticate("firebase-jwt") {
        // Get current user info
        get("/me") {
            val userId = call.principal<UserIdPrincipal>()?.name
                ?: throw io.ktor.server.auth.AuthenticationException("unauthorized")
            
            call.respond(mapOf(
                "userId" to userId,
                "authenticated" to true
            ))
        }
    }
}

fun Application.configureAuthRoutes(
    registerUserUseCase: RegisterUserUseCase,
    loginUseCase: LoginUseCase
) {
    routing {
        route("/api/v1/auth") {
            authRoutes(registerUserUseCase, loginUseCase)
        }
        
        routing {
            route("/api/v1/auth") {
                authProtectedRoutes()
            }
        }
    }
}

