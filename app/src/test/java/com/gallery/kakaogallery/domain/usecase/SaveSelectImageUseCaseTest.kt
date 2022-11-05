package com.gallery.kakaogallery.domain.usecase

import com.gallery.kakaogallery.domain.model.ImageModel
import com.gallery.kakaogallery.domain.repository.ImageRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Completable
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.Before
import org.junit.Test
import java.lang.Exception


/**
 * 내부에서 DateMill을 생성중이라서 mocck의 every를 설정해놓기 까다로운 상태
 * 수정 예정
 */
@Suppress("NonAsciiCharacters")
internal class SaveSelectImageUseCaseTest {
    private lateinit var useCase: SaveSelectImageUseCase
    private lateinit var repository: ImageRepository

    @Before
    fun setup(){
        repository = mockk(relaxed = true)
        useCase = SaveSelectImageUseCase(repository)
    }

    @Test
    //state test
    fun `useCase는 잘못된 selectMap과 imageList의 상태가 일치하지 않는다면 에러를 발생시켜 전달한다`() {
        val map = mutableMapOf(
            "test" to 3
        )
        val actual = catchThrowable { useCase(map, emptyList()).blockingGet() }
        assertThat(actual)
            .isInstanceOf(Exception::class.java)

    }

    //behavior test
    @Test
    fun `useCase는 repository의 saveImages를 호출한다`() {
        every { repository.saveImages(any()) } returns Completable.complete()
        useCase(mutableMapOf(), emptyList()).blockingGet()
        verify { repository.saveImages(any()) }
    }
}