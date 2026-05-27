package com.example.voteapp.server

import com.example.voteapp.server.config.AppConfig
import com.example.voteapp.server.db.DatabaseFactory
import com.example.voteapp.server.plugins.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.cors.CORS
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

fun main(args: Array<String>) {
    // Firebase Admin SDK init will be triggered lazily by AuthPlugin.
    DatabaseFactory.init()

    val server = embeddedServer(
        Netty,
        port = AppConfig.port,
        host = AppConfig.host,
        module = Application::module
    )

    server.start(wait = true)

    runCatching {
        DatabaseFactory.close()
    }
}

fun Application.module() {
    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            },
        )
    }

    install(CallLogging)

    install(CORS) {
        allowHost("localhost")
        allowNonSimpleContentTypes = true
        anyHost()
        allowHeader(io.ktor.http.HttpHeaders.Authorization)
        allowHeader(io.ktor.http.HttpHeaders.ContentType)
        allowCredentials = true
        allowMethod(io.ktor.http.HttpMethod.Get)
        allowMethod(io.ktor.http.HttpMethod.Post)
        allowMethod(io.ktor.http.HttpMethod.Put)
        allowMethod(io.ktor.http.HttpMethod.Delete)
    }

    configureFlyway()
    configureStatusPages()
    
    // Initialize Auth modules
    val authRepository = com.example.voteapp.server.auth.ExposedAuthRepository()
    val registerUserUseCase = com.example.voteapp.server.auth.RegisterUserUseCase(authRepository)
    val loginUseCase = com.example.voteapp.server.auth.LoginUseCase(authRepository)
    
    configureAuth(registerUserUseCase, loginUseCase)
    configureRouting()
    configureMonitoring()
}









