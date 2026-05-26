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

    embeddedServer(
        Netty,
        port = AppConfig.port,
        host = AppConfig.host,
        module = Application::module
    ).start(wait = true)
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
    }

    configureFlyway()
    configureStatusPages()
    configureAuth()
    configureRouting()
    configureMonitoring()

    Runtime.getRuntime().addShutdownHook(Thread {
        runCatching {
            com.example.voteapp.server.db.DatabaseFactory.init()
        }
    })
}









