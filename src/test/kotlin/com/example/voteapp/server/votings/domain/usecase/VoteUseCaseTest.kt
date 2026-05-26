package com.example.voteapp.server.votings.domain.usecase

import com.example.voteapp.server.votings.domain.model.Voting
import com.example.voteapp.server.votings.domain.port.VotingRepository
import com.example.voteapp.server.votings.models.VotePayload
import com.example.voteapp.server.votings.models.VotingResult
import com.example.voteapp.server.votings.models.VotingStatus
import com.example.voteapp.server.votings.models.VotingType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.datetime.Clock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.util.UUID

class VoteUseCaseTest {

    private fun activeVoting(type: VotingType, endsAtPast: Boolean): Voting {
        val endsAt = if (endsAtPast) {
            Clock.System.now().minus(java.time.Duration.ofMinutes(1))
                .toKotlinLocalDateTime()
        } else {
            Clock.System.now().plus(java.time.Duration.ofMinutes(5))
                .toKotlinLocalDateTime()
        }

        return Voting(
            id = UUID.randomUUID(),
            title = "t",
            description = "d",
            type = type,
            status = VotingStatus.ACTIVE,
            imageUrl = null,
            endsAt = endsAt,
            totalVotes = 0,
            hasVoted = false
        )
    }

    @Test
    fun `votes successfully for SINGLE type`() {
        val repository = mockk<VotingRepository>()
        val useCase = VoteUseCase(repository)

        val votingId = UUID.randomUUID()
        val userId = UUID.randomUUID()

        val voting = Voting(
            id = votingId,
            title = "t",
            description = "d",
            type = VotingType.SINGLE,
            status = VotingStatus.ACTIVE,
            imageUrl = null,
            endsAt = Clock.System.now().plus(java.time.Duration.ofMinutes(5)).toKotlinLocalDateTime(),
            totalVotes = 0,
            hasVoted = false
        )

        val payload = VotePayload(selectedOptionIds = listOf(1L), isParticipating = true)

        val result = VotingResult(
            votingId = votingId,
            status = VotingStatus.ACTIVE,
            type = VotingType.SINGLE,
            totalParticipants = 1,
            optionsResults = null,
            signaturesCount = null,
            winnerInfo = null
        )

        coEvery { repository.getById(votingId) } returns voting
        coEvery { repository.vote(votingId, userId, payload) } returns result

        val actual = kotlinx.coroutines.runBlocking { useCase.invoke(votingId, userId, payload) }
        assertEquals(result, actual)
        coVerify(exactly = 1) { repository.vote(votingId, userId, payload) }
    }

    @Test
    fun `throws VotingNotFoundException for non-existent voting`() {
        val repository = mockk<VotingRepository>()
        val useCase = VoteUseCase(repository)

        val votingId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val payload = VotePayload(selectedOptionIds = listOf(1L), isParticipating = true)

        coEvery { repository.getById(votingId) } returns null

        assertThrows(VotingNotFoundException::class.java) {
            kotlinx.coroutines.runBlocking { useCase.invoke(votingId, userId, payload) }
        }
    }

    @Test
    fun `throws VotingAlreadyClosedException if ended`() {
        val repository = mockk<VotingRepository>()
        val useCase = VoteUseCase(repository)

        val votingId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val payload = VotePayload(selectedOptionIds = listOf(1L), isParticipating = true)

        val voting = Voting(
            id = votingId,
            title = "t",
            description = "d",
            type = VotingType.SINGLE,
            status = VotingStatus.ACTIVE,
            imageUrl = null,
            endsAt = Clock.System.now().minus(java.time.Duration.ofMinutes(1)).toKotlinLocalDateTime(),
            totalVotes = 0,
            hasVoted = false
        )

        coEvery { repository.getById(votingId) } returns voting

        assertThrows(VotingAlreadyClosedException::class.java) {
            kotlinx.coroutines.runBlocking { useCase.invoke(votingId, userId, payload) }
        }
    }

    @Test
    fun `throws AlreadyVotedException for duplicate`() {
        val repository = mockk<VotingRepository>()
        val useCase = VoteUseCase(repository)

        val votingId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val payload = VotePayload(selectedOptionIds = listOf(1L), isParticipating = true)

        val voting = Voting(
            id = votingId,
            title = "t",
            description = "d",
            type = VotingType.SINGLE,
            status = VotingStatus.ACTIVE,
            imageUrl = null,
            endsAt = Clock.System.now().plus(java.time.Duration.ofMinutes(5)).toKotlinLocalDateTime(),
            totalVotes = 0,
            hasVoted = false
        )

        coEvery { repository.getById(votingId) } returns voting
        coEvery { repository.vote(votingId, userId, payload) } throws AlreadyVotedException()

        assertThrows(AlreadyVotedException::class.java) {
            kotlinx.coroutines.runBlocking { useCase.invoke(votingId, userId, payload) }
        }
    }
}

