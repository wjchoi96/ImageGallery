package com.gallery.kakaogallery.domain.usecase

import com.gallery.kakaogallery.domain.model.ImageModel
import com.gallery.kakaogallery.domain.repository.ImageRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Observable
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

@Suppress("NonAsciiCharacters")
internal class FetchSaveImageUseCaseTest {

    private lateinit var useCase: FetchSaveImageUseCase
    private lateinit var repository: ImageRepository

    @Before
    fun setup() {
        repository = mockk(relaxed = true)
        useCase = FetchSaveImageUseCase(repository)
    }

    // state test
    @Test
    fun `에러 전달시 결과를 Result로 래핑`() {
        val unitTestException = Exception("unit test exception")
        every { repository.fetchSaveImages() } returns Observable.error(unitTestException)

        val actual = useCase().blockingFirst()
        val expect = Result.failure<List<ImageModel>>(unitTestException)
        assertThat(actual)
            .isEqualTo(expect)

        assertThat(actual.exceptionOrNull())
            .isNotNull
            .isInstanceOf(Exception::class.java)
            .hasMessageContaining(unitTestException.message)
    }

    //state test
    @Test
    fun `정상 요청시 결과를 Result로 래핑`() {
        val images = listOf(
            ImageModel.Empty,
            ImageModel.Empty.copy(imageUrl = "test123")
        )
        every { repository.fetchSaveImages() } returns Observable.just(images)

        val actual = useCase().blockingFirst()
        val expect = Result.success(images)

        assertThat(actual)
            .isEqualTo(expect)
    }

    //behavior test
    @Test
    fun `repository의 fetchSaveImages를 호출한다`() {
        useCase()
        verify { repository.fetchSaveImages() }
    }
}