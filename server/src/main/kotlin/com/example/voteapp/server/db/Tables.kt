package com.example.voteapp.server.db

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.timestamp
import kotlinx.datetime.Instant
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDTable
import java.util.UUID

object Users : UUIDTable() {
    val email = varchar("email", 255).uniqueIndex()
    val name = varchar("name", 100)
    override val primaryKey = super.primaryKey
}

object Votings : UUIDTable() {
    val title = varchar("title", 255)
    val description = text("description")
    val type = enumerationByName("type", 20, VotingType::class)
    val status = enumerationByName("status", 10, VotingStatus::class)
    val imageUrl = varchar("image_url", 500).nullable()
    val creatorId = uuid("creator_id") references Users.id
    val endsAt = timestamp("ends_at")
    override val primaryKey = super.primaryKey
}

object VotingOptions : UUIDTable("voting_options") {
    val votingId = uuid("voting_id") references Votings.id
    val text = text("text")
    val votes = integer("votes").default(0)
    override val primaryKey = super.primaryKey
}

object Votes : Table("votes") {
    val userId = uuid("user_id") references Users.id
    val votingId = uuid("voting_id") references Votings.id
    val optionIds = text("option_ids") // JSON array of option UUIDs
}

enum class VotingType {
    SINGLE, MULTIPLE, PETITION, RAFFLE
}

enum class VotingStatus {
    ACTIVE, CLOSED
}

