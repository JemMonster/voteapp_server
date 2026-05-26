package com.example.voteapp.server.votings.domain.usecase

import com.example.voteapp.server.votings.domain.model.Voting
import com.example.voteapp.server.votings.domain.port.VotingRepository
import com.example.voteapp.server.votings.models.NewVoting
import com.example.voteapp.server.votings.models.VotingStatus
import com.example.voteapp.server.votings.models.VotingType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.Assert.assertThrows
import org.junit.Test
import java.util.UUID
import kotlinx.datetime.Clock

@RunWithMockK
class CreateVotingUseCaseTest {

    @Test
    fun `creates voting with valid data`() {
        val repository = mockk<VotingRepository>()
        val useCase = CreateVotingUseCase(repository)

        val creatorId = UUID.randomUUID()
        val dto = NewVoting(
            title = "Title",
            description = "desc",
            imageUrl = null,
            type = VotingType.SINGLE,
            durationDays = 15,
            options = listOf("o1", "o2")
        )

        val result = Voting(
            id = UUID.randomUUID(),
            title = dto.title,
            description = dto.description ?: "",
            type = dto.type,
            status = VotingStatus.ACTIVE,
            imageUrl = dto.imageUrl,
            endsAt = Clock.System.now().toLocalDateTime(),
            totalVotes = 0,
            hasVoted = false
        )

        coEvery { repository.create(dto, creatorId) } returns result

        val actual = kotlinx.coroutines.runBlocking {
            useCase.invoke(dto, creatorId)
        }

        assert(actual.id == result.id)
        coVerify(exactly = 1) { repository.create(dto, creatorId) }
    }

    @Test
    fun `throws ValidationException for empty title`() {
        val repository = mockk<VotingRepository>()
        val useCase = CreateVotingUseCase(repository)

        val creatorId = UUID.randomUUID()
        val dto = NewVoting(
            title = "",
            description = null,
            imageUrl = null,
            type = VotingType.SINGLE,
            durationDays = 15,
            options = listOf("o1", "o2")
        )

        assertThrows(ValidationException::class.java) {
            kotlinx.coroutines.runBlocking {
                useCase.invoke(dto, creatorId)
            }
        }
    }

    @Test
    fun `throws ValidationException for durationDays < 1`() {
        val repository = mockk<VotingRepository>()
        val useCase = CreateVotingUseCase(repository)

        val creatorId = UUID.randomUUID()
        val dto = NewVoting(
            title = "Title",
            description = null,
            imageUrl = null,
            type = VotingType.PETITION,
            durationDays = 0,
            options = null
        )

        assertThrows(ValidationException::class.java) {
            kotlinx.coroutines.runBlocking {
                useCase.invoke(dto, creatorId)
            }
        }
    }

    @Test
    fun `throws ValidationException for durationDays > 360`() {
        val repository = mockk<VotingRepository>()
        val useCase = CreateVotingUseCase(repository)

        val creatorId = UUID.randomUUID()
        val dto = NewVoting(
            title = "Title",
            description = null,
            imageUrl = null,
            type = VotingType.PETITION,
            durationDays = 361,
            options = null
        )

        assertThrows(ValidationException::class.java) {
            kotlinx.coroutines.runBlocking {
                useCase.invoke(dto, creatorId)
            }
        }
    }

    @Test
    fun `throws ValidationException for SINGLE with options size = 1`() {
        val repository = mockk<VotingRepository>()
        val useCase = CreateVotingUseCase(repository)

        val creatorId = UUID.randomUUID()
        val dto = NewVoting(
            title = "Title",
            description = null,
            imageUrl = null,
            type = VotingType.SINGLE,
            durationDays = 15,
            options = listOf("only")
        )

        assertThrows(ValidationException::class.java) {
            kotlinx.coroutines.runBlocking {
                useCase.invoke(dto, creatorId)
            }
        }
    }
}

