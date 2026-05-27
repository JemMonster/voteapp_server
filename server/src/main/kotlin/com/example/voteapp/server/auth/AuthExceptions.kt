package com.example.voteapp.server.auth

import java.util.UUID

sealed class AuthException(message: String) : Exception(message)

class UserNotFoundException : AuthException("User not found")

class EmailAlreadyExistsException(email: String) : AuthException("Email already exists: $email")

class InvalidTokenException : AuthException("Invalid or expired token")

class FirebaseVerificationException(message: String) : AuthException("Firebase verification failed: $message")
