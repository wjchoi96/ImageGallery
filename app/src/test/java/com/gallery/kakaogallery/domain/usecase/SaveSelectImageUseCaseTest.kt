package com.gallery.kakaogallery.domain.usecase

import com.gallery.kakaogallery.domain.repository.ImageRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test


/**
 * 내부에서 DateMill을 생성중이라서 mocck의 every를 설정해놓기 까다로운 상태
 * 수정 예정
 */
@ExperimentalCoroutinesApi
@FlowPreview
@Suppress("NonAsciiCharacters")
internal class SaveSelectImageUseCaseTest {
    private lateinit var useCase: SaveSelectImageUseCase
    private lateinit var repository: ImageRepository

    private val testDispatcher = StandardTestDispatcher(TestCoroutineScheduler())

    @Before
    fun setup(){
        repository = mockk(relaxed = true)
        useCase = SaveSelectImageUseCase(repository, testDispatcher)

    }

    //state test
    @Test
    fun `useCase는 Flow타입을 리턴한다`() {
        every { repository.saveImages(any(), any()) } returns flow { emit(true) }
        val actual = useCase(mutableMapOf(), emptyList())

        assertThat(actual)
            .isNotNull
            .isInstanceOf(Flow::class.java)
    }

    //state test
    @Test
    fun `useCase는 결과를 Result로 래핑하여 리턴한다`() = runTest(testDispatcher) {
        every { repository.saveImages(any(), any()) } returns flow { emit(true) }
        val actual = useCase(mutableMapOf(), emptyList()).firstOrNull()

        assertThat(actual)
            .isNotNull
            .isInstanceOf(Result::class.java)
    }

    @Test
    //state test
    fun `useCase는 repository가 정상 응답시 Result로 래핑된 true를 리턴한다`() = runTest(testDispatcher) {
        every { repository.saveImages(any(), any()) } returns flow { emit(true) }
        val actual = useCase(mutableMapOf(), emptyList()).firstOrNull()?.getOrNull()
        assertThat(actual)
            .isNotNull
            .isTrue
    }

    @Test
    //state test
    fun `useCase는 repository가 에러를 전달하면 Result로 래핑하여 전달한다`() = runTest(testDispatcher) {
        val unitTestException = Exception("unit test exception")
        every { repository.saveImages(any(), any()) } returns flow { throw unitTestException }
        val actual = useCase(mutableMapOf(), emptyList()).firstOrNull()?.exceptionOrNull()
        assertThat(actual)
            .isNotNull
            .isInstanceOf(Exception::class.java)
            .hasMessageContaining(unitTestException.message)
    }

    @Test
    //state test
    fun `useCase는 잘못된 selectMap과 imageList의 상태가 일치하지 않는다면 에러를 발생시켜 전달한다`() = runTest(testDispatcher) {
        val map = mutableMapOf(
            "test" to 3
        )
        val actual = useCase(map, emptyList()).firstOrNull()?.exceptionOrNull()
        assertThat(actual)
            .isNotNull
            .isInstanceOf(Exception::class.java)

    }

    //behavior test
    @Test
    fun `useCase는 repository의 saveImages를 호출한다`() = runTest(testDispatcher) {
        every { repository.saveImages(any(), any()) } returns flow { emit(true) }
        useCase(mutableMapOf(), emptyList()).firstOrNull()
        verify { repository.saveImages(any(), any()) }
    }
}