package com.example.voteapp.server.votings.domain.usecase

import java.util.UUID

sealed class VotingException(message: String) : Exception(message)

class VotingNotFoundException(id: UUID) : VotingException("Voting $id not found")

class VotingAlreadyClosedException : VotingException("Voting is already closed")

class AlreadyVotedException : VotingException("User already voted")

class ValidationException(message: String) : VotingException(message)

