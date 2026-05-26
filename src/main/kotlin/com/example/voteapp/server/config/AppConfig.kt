package com.example.voteapp.server.config

import java.util.Properties

object AppConfig {
    // DB
    // Prefer explicit credentials for Flyway.
    val databaseUrl: String by lazy {
        // TODO: read from neon.txt if/when supported
        System.getenv("DATABASE_URL")
            ?: "jdbc:postgresql://localhost:5432/voteapp?user=postgres&password=pass"
    }

    val jdbcCredentials: Triple<String, String?, String?> by lazy {
        val url = databaseUrl
        val user = System.getenv("DATABASE_USER")
        val password = System.getenv("DATABASE_PASSWORD")
        Triple(url, user, password)
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




