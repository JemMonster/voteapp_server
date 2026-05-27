package com.example.voteapp.server.auth

import com.example.voteapp.server.db.Users
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import kotlinx.coroutines.Dispatchers
import java.util.UUID

interface AuthRepository {
    suspend fun findByEmail(email: String): AuthUser?
    suspend fun findByUserId(userId: UUID): AuthUser?
    suspend fun createUser(email: String, name: String?, userId: UUID): AuthUser
}

data class AuthUser(
    val userId: UUID,
    val email: String,
    val name: String?
)

class ExposedAuthRepository : AuthRepository {
    
    override suspend fun findByEmail(email: String): AuthUser? =
        newSuspendedTransaction(Dispatchers.IO) {
            Users.select { Users.email eq email.trim() }
                .limit(1)
                .firstOrNull()
                ?.let { row ->
                    AuthUser(
                        userId = row[Users.id].value,
                        email = row[Users.email],
                        name = row[Users.name]
                    )
                }
        }

    override suspend fun findByUserId(userId: UUID): AuthUser? =
        newSuspendedTransaction(Dispatchers.IO) {
            Users.select { Users.id eq userId }
                .limit(1)
                .firstOrNull()
                ?.let { row ->
                    AuthUser(
                        userId = row[Users.id].value,
                        email = row[Users.email],
                        name = row[Users.name]
                    )
                }
        }

    override suspend fun createUser(email: String, name: String?, userId: UUID): AuthUser =
        newSuspendedTransaction(Dispatchers.IO) {
            val existing = Users.select { Users.email eq email.trim() }.limit(1).firstOrNull()
            if (existing != null) {
                throw EmailAlreadyExistsException(email)
            }
            
            Users.insert {
                it[id] = userId
                it[email] = email.trim()
                it[name] = name?.trim()?.takeIf { it.isNotBlank() }
            }
            
            AuthUser(userId = userId, email = email.trim(), name = name?.trim()?.takeIf { it.isNotBlank() })
        }
}
