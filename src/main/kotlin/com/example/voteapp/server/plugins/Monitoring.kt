package com.example.voteapp.server.plugins

import io.ktor.server.plugins.callloging.*
import io.ktor.server.request.*
import org.slf4j.event.*
import io.ktor.server.application.*

fun Application.configureMonitoring() {
    install(CallLogging) {
        level = LogLevel.INFO
        filter { call -> call.request.path().startsWith("/api/") }
    }
}

