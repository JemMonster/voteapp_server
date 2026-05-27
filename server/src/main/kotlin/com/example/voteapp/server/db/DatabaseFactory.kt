package com.example.voteapp.server.db

import com.example.voteapp.server.config.AppConfig
import org.jetbrains.exposed.sql.Database

object DatabaseFactory {
    fun init() {
        val dbUrl = AppConfig.databaseUrl
        Database.connect(dbUrl, driver = "org.postgresql.Driver")
        // Schema is managed by Flyway migrations.
        createDatabaseSchema()

    }
}



