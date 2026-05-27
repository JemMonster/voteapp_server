package com.example.voteapp.server.votings.domain.usecase

import com.example.voteapp.server.votings.domain.model.Voting
import com.example.voteapp.server.votings.domain.port.VotingRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class GetVotingsUseCaseTest {

    @Test
    fun `invoke calls repository and returns votings`() = runTest {
        val repository = mockk<VotingRepository>()

        val expected = listOf(
            Voting(
                id = 1,
                title = "t1",
                description = "d1",
            )
        )

        coEvery { repository.getVotings() } returns expected

        val useCase = GetVotingsUseCase(repository)

        val result = useCase()

        assertEquals(expected, result)
        coVerify(exactly = 1) { repository.getVotings() }
    }
}

