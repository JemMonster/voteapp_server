package com.example.voteapp.server.db

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

object DatabaseFactory {
    fun init() {
        // TODO: Read from context/neon.txt or env var
        val dbUrl = System.getenv("DATABASE_URL") ?: "jdbc:postgresql://localhost:5432/voteapp?user=postgres&password=pass" // Placeholder
Database.connect(dbUrl, driver = "org.postgresql.Driver")
        createDatabaseSchema()

    }
}

