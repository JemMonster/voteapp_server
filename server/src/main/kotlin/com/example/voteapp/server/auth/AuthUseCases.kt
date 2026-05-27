package com.example.voteapp.server.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.FirebaseApp
import java.util.UUID

/**
 * Use case for user registration
 * Verifies Firebase token and creates user in local database
 */
class RegisterUserUseCase(
    private val authRepository: AuthRepository
) {
    suspend fun execute(request: RegisterRequest): RegisterResponse {
        val email = request.email.trim()
        
        if (email.isBlank()) {
            throw ValidationException("Email is required")
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            throw ValidationException("Invalid email format")
        }
        
        // Verify Firebase token
        val firebaseUserId = verifyFirebaseToken(request.firebaseToken)
        
        // Check if user already exists
        val existingUser = authRepository.findByEmail(email)
        if (existingUser != null) {
            throw EmailAlreadyExistsException(email)
        }
        
        // Create user
        val user = authRepository.createUser(email, request.name, firebaseUserId)
        
        return RegisterResponse(
            userId = user.userId.toString(),
            email = user.email,
            name = user.name
        )
    }
    
    private fun verifyFirebaseToken(token: String): UUID {
        try {
            val app = FirebaseApp.getApps().firstOrNull { it.name == FirebaseApp.DEFAULT_APP_NAME }
                ?: throw FirebaseVerificationException("Firebase not initialized")
            
            val decoded = FirebaseAuth.getInstance(app).verifyIdToken(token, true)
            return UUID.fromString(decoded.uid)
        } catch (e: Exception) {
            throw FirebaseVerificationException("Token verification failed: ${e.message}")
        }
    }
}

/**
 * Use case for user login
 * Verifies Firebase token and returns user info
 */
class LoginUseCase(
    private val authRepository: AuthRepository
) {
    suspend fun execute(request: LoginRequest): LoginResponse {
        val firebaseUserId = verifyFirebaseToken(request.firebaseToken)
        
        val user = authRepository.findByUserId(firebaseUserId)
            ?: throw UserNotFoundException()
        
        return LoginResponse(
            userId = user.userId.toString(),
            email = user.email,
            name = user.name,
            message = "Login successful"
        )
    }
    
    private fun verifyFirebaseToken(token: String): UUID {
        try {
            val app = FirebaseApp.getApps().firstOrNull { it.name == FirebaseApp.DEFAULT_APP_NAME }
                ?: throw FirebaseVerificationException("Firebase not initialized")
            
            val decoded = FirebaseAuth.getInstance(app).verifyIdToken(token, true)
            return UUID.fromString(decoded.uid)
        } catch (e: Exception) {
            throw FirebaseVerificationException("Token verification failed: ${e.message}")
        }
    }
}

class ValidationException(message: String) : AuthException(message)
