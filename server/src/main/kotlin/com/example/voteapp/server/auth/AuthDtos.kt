package com.example.voteapp.server.auth

import kotlinx.serialization.Serializable

/**
 * Request body for user registration
 */
@Serializable
data class RegisterRequest(
    val email: String,
    val name: String? = null,
    val firebaseToken: String // Firebase ID token for verification
)

/**
 * Response for registration
 */
@Serializable
data class RegisterResponse(
    val userId: String,
    val email: String,
    val name: String?
)

/**
 * Request body for login (just Firebase token verification)
 */
@Serializable
data class LoginRequest(
    val firebaseToken: String
)

/**
 * Response for login
 */
@Serializable
data class LoginResponse(
    val userId: String,
    val email: String,
    val name: String?,
    val message: String = "Login successful"
)

/**
 * Generic auth response
 */
@Serializable
data class AuthResponse(
    val userId: String,
    val email: String,
    val name: String?
)
