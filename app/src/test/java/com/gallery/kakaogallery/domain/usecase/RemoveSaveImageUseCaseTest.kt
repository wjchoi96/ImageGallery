package com.gallery.kakaogallery.domain.usecase

import com.gallery.kakaogallery.domain.repository.ImageRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Completable
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.Before
import org.junit.Test

@Suppress("NonAsciiCharacters")
internal class RemoveSaveImageUseCaseTest {
    private lateinit var useCase: RemoveSaveImageUseCase
    private lateinit var repository: ImageRepository

    @Before
    fun setup(){
        repository = mockk(relaxed = true)
        useCase = RemoveSaveImageUseCase(repository)
    }

    //state test
    @Test
    fun `repoistory가 에러를 전달하면 처리할 수 있다`() {
        val map = mutableMapOf(
            "test1" to 1,
            "test2" to 2
        )
        val unitTestException = Exception("unit test exception")
        every { repository.removeImages(map.values.toList()) } returns Completable.error(unitTestException)

        val actual = catchThrowable { useCase(map).blockingGet() }
        assertThat(actual)
            .isInstanceOf(java.lang.Exception::class.java)
            .hasMessageContaining(unitTestException.message)
    }

    //state test
    @Test
    fun `repository가 정상 응답시 true를 전달한다`() {
        val map = mutableMapOf(
            "test1" to 1,
            "test2" to 2
        )
        every { repository.removeImages(map.values.toList()) } returns Completable.complete()
        val actual = useCase(map).blockingGet()
        assertThat(actual).isTrue
    }

    //behavior test
    @Test
    fun `repository의 removeImages를 호출한다`() {
        val map = mutableMapOf(
            "test1" to 1,
            "test2" to 2
        )
        useCase(map)
        verify { repository.removeImages(map.values.toList()) }
    }

}