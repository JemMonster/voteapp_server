package com.example.voteapp.server.plugins

import com.example.voteapp.server.config.AppConfig
import io.ktor.server.application.*
import org.flywaydb.core.Flyway

fun Application.configureFlyway() {
    val migrateEnabled = (System.getenv("FLYWAY_MIGRATE") ?: "false").toBooleanStrictOrNull()
        ?: (System.getenv("flyway.migrate") ?: "false").toBooleanStrictOrNull()
        ?: false

    if (!migrateEnabled) return

    val (jdbcUrl, user, password) = AppConfig.jdbcCredentials

    val flyway = Flyway.configure()
        .dataSource(
            jdbcUrl,
            user,
            password
        )

        .locations("classpath:db/migration")
        .baselineOnMigrate(true)
        .load()

    flyway.migrate()
}

