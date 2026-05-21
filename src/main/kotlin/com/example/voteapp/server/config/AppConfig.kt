package com.example.voteapp.server.config

import java.util.Properties

object AppConfig {
    // DB
    val databaseUrl: String by lazy {
        // TODO: read from neon.txt if/when supported
        System.getenv("DATABASE_URL")
            ?: "jdbc:postgresql://localhost:5432/voteapp?user=postgres&password=pass"
    }

    // Networking
    val host: String = "0.0.0.0"
    val port: Int = (System.getenv("PORT") ?: "8080").toInt()

    // Convenience if we later load from application.conf
    fun toProperties(): Properties = Properties().apply {
        setProperty("databaseUrl", databaseUrl)
        setProperty("host", host)
        setProperty("port", port.toString())
    }
}

