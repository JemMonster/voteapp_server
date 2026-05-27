package com.example.voteapp.server.votings.domain.usecase

import java.util.UUID

class InviteNotFoundException : VotingException("Invite not found")

class EmailNotFoundException(email: String) : VotingException("Email $email not found")

class InviteAlreadyExistsException : VotingException("Invite already exists")

class VotingNotOwnedException(votingId: UUID) : VotingException("Voting $votingId is not found for this user")

