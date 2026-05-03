package com.example.voteapp.server.db

import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun createDatabaseSchema() {
    transaction {
        SchemaUtils.create(Users, Votings, VotingOptions, Votes)
    }
}

