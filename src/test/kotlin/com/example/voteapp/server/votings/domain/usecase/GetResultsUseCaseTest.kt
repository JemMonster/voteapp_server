package com.example.voteapp.server.votings.domain.usecase

import com.example.voteapp.server.votings.domain.port.VotingRepository
import com.example.voteapp.server.votings.domain.model.Voting
import com.example.voteapp.server.votings.models.VotingResult
import com.example.voteapp.server.votings.models.VotingStatus
import com.example.voteapp.server.votings.models.VotingType
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.datetime.Clock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.util.UUID

class GetResultsUseCaseTest {

    @Test
    fun `returns results for active voting`() {
        val repository = mockk<VotingRepository>()
        val useCase = GetResultsUseCase(repository)

        val votingId = UUID.randomUUID()

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
        coEvery { repository.getResults(votingId) } returns result

        val actual = kotlinx.coroutines.runBlocking { useCase.invoke(votingId) }
        assertEquals(result, actual)
    }

    @Test
    fun `returns CLOSED status if now > endsAt`() {
        val repository = mockk<VotingRepository>()
        val useCase = GetResultsUseCase(repository)

        val votingId = UUID.randomUUID()

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

        val result = VotingResult(
            votingId = votingId,
            status = VotingStatus.CLOSED,
            type = VotingType.SINGLE,
            totalParticipants = 1,
            optionsResults = null,
            signaturesCount = null,
            winnerInfo = null
        )

        coEvery { repository.getById(votingId) } returns voting
        coEvery { repository.getResults(votingId) } returns result

        val actual = kotlinx.coroutines.runBlocking { useCase.invoke(votingId) }
        assertEquals(VotingStatus.CLOSED, actual.status)
    }

    @Test
    fun `throws VotingNotFoundException for non-existent voting`() {
        val repository = mockk<VotingRepository>()
        val useCase = GetResultsUseCase(repository)

        val votingId = UUID.randomUUID()

        coEvery { repository.getById(votingId) } returns null

        assertThrows(VotingNotFoundException::class.java) {
            kotlinx.coroutines.runBlocking { useCase.invoke(votingId) }
        }
    }
}

