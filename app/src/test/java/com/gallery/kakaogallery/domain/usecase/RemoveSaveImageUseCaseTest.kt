package com.gallery.kakaogallery.domain.usecase

import com.gallery.kakaogallery.domain.repository.ImageRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
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
    fun `useCase는 Flow타입을 리턴한다`() {
        val map = mutableMapOf(
            "test1" to 1,
            "test2" to 2
        )
        every { repository.removeImages(map.values.toList()) } returns flow { emit(true) }
        val actual = useCase(map)
        assertThat(actual)
            .isInstanceOf(Flow::class.java)
    }

    //state test
    @Test
    fun `useCase는 결과를 Result로 래핑하여 리턴한다`() = runTest {
        val map = mutableMapOf(
            "test1" to 1,
            "test2" to 2
        )
        every { repository.removeImages(map.values.toList()) } returns flow { emit(true) }
        val actual = useCase(map).firstOrNull()
        assertThat(actual)
            .isNotNull
            .isInstanceOf(Result::class.java)
    }

    //state test
    @Test
    fun `useCase는 repoistory가 에러를 전달하면 Result로 래핑하여 전달한다`() = runTest {
        val map = mutableMapOf(
            "test1" to 1,
            "test2" to 2
        )
        val unitTestException = Exception("unit test exception")
        every { repository.removeImages(map.values.toList()) } returns flow { throw unitTestException }

        val actual = useCase(map).firstOrNull()?.exceptionOrNull()
        assertThat(actual)
            .isInstanceOf(java.lang.Exception::class.java)
            .hasMessageContaining(unitTestException.message)
    }

    //state test
    @Test
    fun `useCase는 repository가 정상 응답시 Result로 래핑된 true를 전달한다`() = runTest {
        val map = mutableMapOf(
            "test1" to 1,
            "test2" to 2
        )
        every { repository.removeImages(map.values.toList()) } returns flow { emit(true) }
        val actual = useCase(map).firstOrNull()?.getOrNull()
        assertThat(actual)
            .isNotNull
            .isTrue
    }

    //behavior test
    @Test
    fun `useCase는 repository의 removeImages를 호출한다`() {
        val map = mutableMapOf(
            "test1" to 1,
            "test2" to 2
        )
        useCase(map)
        verify { repository.removeImages(map.values.toList()) }
    }

}