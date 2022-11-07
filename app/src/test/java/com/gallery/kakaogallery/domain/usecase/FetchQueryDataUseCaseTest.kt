package com.gallery.kakaogallery.domain.usecase

import com.gallery.kakaogallery.domain.model.ImageListTypeModel
import com.gallery.kakaogallery.domain.model.ImageModel
import com.gallery.kakaogallery.domain.model.SearchImageModel
import com.gallery.kakaogallery.domain.repository.ImageRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Single
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.Before
import org.junit.Test
import java.lang.RuntimeException

@Suppress("NonAsciiCharacters")
internal class FetchQueryDataUseCaseTest {

    private lateinit var useCase: FetchQueryDataUseCase
    private lateinit var repository: ImageRepository

    @Before
    fun setup(){
        repository = mockk(relaxed = true)
        useCase = FetchQueryDataUseCase(repository)
    }

    //state test
    @Test
    fun `useCase는 repository가 에러를 전달하면 처리할 수 있다`() {
        val (query, page) = "query" to 1
        val unitTestException = Exception("unit test exception")

        every { repository.fetchQueryData(query, page) } returns Single.error(unitTestException)
        val actual = catchThrowable { useCase(query, page).blockingGet() }
        assertThat(actual)
            .isInstanceOf(RuntimeException::class.java)
            .hasMessageContaining(unitTestException.message)

    }

    //state test
    @Test
    fun `useCase는 query가 비어있다면 Query만를 포함한 리스트를 리턴한다`() {
        val (query, page) = "" to 1

        val actual = useCase(query, page).blockingGet()
        val except = listOf<ImageListTypeModel>(ImageListTypeModel.Query(query))
        assertThat(actual)
            .isEqualTo(except)
    }

    //state test
    @Test
    fun `useCase는 page가 1이면 Query를 첫번째 아이템으로 가지는 ImageListType리스트를 리턴한다`() {
        val (query, page) = "query" to 1
        val images = listOf(
            SearchImageModel.Empty,
            SearchImageModel.Empty.copy(imageUrl = "123")
        )

        every { repository.fetchQueryData(query, page) } returns Single.just(images)
        val actual = useCase(query, page).blockingGet()
        val except = listOf(
            ImageListTypeModel.Query(query),
            ImageListTypeModel.Image(images[0]),
            ImageListTypeModel.Image(images[1])
        )
        assertThat(actual.first())
            .isEqualTo(except.first())

        assertThat((actual))
            .isEqualTo(except)
    }

    //state test
    @Test
    fun `useCase는 page가 1이 아니라면 Image만을 포함하는 리스트를 리턴한다`() {
        val (query, page) = "query" to 2
        val images = listOf(
            SearchImageModel.Empty,
            SearchImageModel.Empty.copy(imageUrl = "123")
        )

        every { repository.fetchQueryData(query, page) } returns Single.just(images)
        val actual = useCase(query, page).blockingGet()
        val except = images.map { ImageListTypeModel.Image(it) }

        assertThat(actual)
            .isEqualTo(except)
    }

    //behavior test
    @Test
    fun `useCase는 repository의 fetch메소드를 호출한다`() {
        val (query, page) = "query" to 1
        useCase(query, page)
        verify { repository.fetchQueryData(query, page) }
    }
}