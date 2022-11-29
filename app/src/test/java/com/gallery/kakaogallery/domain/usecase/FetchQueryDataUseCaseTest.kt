package com.gallery.kakaogallery.domain.usecase

import com.gallery.kakaogallery.domain.model.*
import com.gallery.kakaogallery.domain.repository.ImageRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.Before
import org.junit.Test

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
    fun `useCase는 Observable타입을 리턴한다`() {
        every { repository.fetchQueryData(any(), any()) } returns Single.just(emptyList())

        val actual = useCase("query", 1)
        assertThat(actual)
            .isInstanceOf(Observable::class.java)
    }

    //state test
    @Test
    fun `useCase는 repository가 에러를 전달하면 처리할 수 있다`() {
        val (query, page) = "query" to 1
        val unitTestException = Exception("unit test exception")

        every { repository.fetchQueryData(query, page) } returns Single.error(unitTestException)
        val actual = catchThrowable { useCase(query, page).blockingLast().getOrThrow() }
        assertThat(actual)
            .isInstanceOf(Exception::class.java)
            .isEqualTo(unitTestException)
    }

    //state test
    @Test
    fun `useCase는 page가 1이면 결과와 상관없이 skeletonData를 먼저 emit한다`() {
        val (query, page) = "query" to 1
        every { repository.fetchQueryData(query, page) } returns Single.just(emptyList())

        val skeletonSize = 1
        val expectSkeletonData = Result.success(
            listOf(SearchImageListTypeModel.Query(query)) +
            MutableList(skeletonSize) { SearchImageListTypeModel.Skeleton }
        )

        assertThat(useCase(query, page, skeletonSize).blockingFirst())
            .isEqualTo(expectSkeletonData)

        every { repository.fetchQueryData(query, page) } returns Single.error(Throwable("test"))

        assertThat(useCase(query, page, skeletonSize).blockingFirst())
            .isEqualTo(expectSkeletonData)
    }

    //state test
    @Test
    fun `useCase는 page가 1이 아니면 결과와 상관없이 skeletonData를 emit하지 않는다`() {
        val (query, page) = "query" to 2
        every { repository.fetchQueryData(query, page) } returns Single.just(emptyList())

        val skeletonSize = 1
        val expectSkeletonData = Result.success(
            listOf(SearchImageListTypeModel.Query(query)) +
                    MutableList(skeletonSize) { SearchImageListTypeModel.Skeleton }
        )
        val expectException = Throwable("test")

        assertThat(useCase(query, page, skeletonSize).blockingFirst())
            .isNotEqualTo(expectSkeletonData)
            .isEqualTo(Result.success(emptyList<SearchImageListTypeModel>()))

        every { repository.fetchQueryData(query, page) } returns Single.error(expectException)

        assertThat(useCase(query, page, skeletonSize).blockingFirst().exceptionOrNull())
            .isNotNull
            .isNotEqualTo(expectSkeletonData)
            .isEqualTo(expectException)
    }

    //state test
    @Test
    fun `useCase는 page가 1일때 repository가 에러 전달 시 skeletonData, skeletonData를 제거할 List, 에러 순서대로 emit한다`() {
        val (query, page) = "query" to 1
        val skeletonSize = 1
        val unitTestException = Exception("unit test exception")

        every { repository.fetchQueryData(any(), any()) } returns Single.error(unitTestException)

        val actualIterator = useCase(query, page, skeletonSize).blockingIterable().iterator()
        val expectIterator = listOf<Result<List<SearchImageListTypeModel>>>(
            Result.success(
                listOf(SearchImageListTypeModel.Query(query)) + MutableList(skeletonSize){SearchImageListTypeModel.Skeleton}
            ),
            Result.success(listOf(SearchImageListTypeModel.Query(query))),
            Result.failure(unitTestException)
        ).iterator()

        assertThat(actualIterator.next())
            .isEqualTo(expectIterator.next())

        assertThat(actualIterator.next())
            .isEqualTo(expectIterator.next())

        assertThat(actualIterator.next().exceptionOrNull())
            .isEqualTo(expectIterator.next().exceptionOrNull())
            .isNotNull
            .isInstanceOf(Exception::class.java)
    }

    //state test
    @Test
    fun `useCase는 page가 1이 아닐때 repository가 에러를 전달하면 Result로 래핑된 에러만을 전달한다`() {
        val unitTestException = Exception("unit test exception")
        every { repository.fetchQueryData(any(), any()) } returns Single.error(unitTestException)

        val actual = useCase("query", 2).blockingFirst()

        assertThat(actual.exceptionOrNull())
            .isNotNull
            .isInstanceOf(Exception::class.java)
            .hasMessageContaining(unitTestException.message)
    }

    //state test
    @Test
    fun `useCase는 repository의 fetchQueryData 결과를 SearchImageListTypeModel로 변환한다`() {
        every { repository.fetchQueryData(any(), any()) } returns Single.just(listOf(SearchImageModel.Empty))
        val actual = useCase("query", 1).blockingLast().getOrNull()

        assertThat(actual?.first())
            .isNotNull
            .isInstanceOf(SearchImageListTypeModel::class.java)
            .isInstanceOf(SearchImageListTypeModel.Query::class.java)

        assertThat(actual?.last())
            .isNotNull
            .isInstanceOf(SearchImageListTypeModel::class.java)
            .isInstanceOf(SearchImageListTypeModel.Image::class.java)
    }

    //state test
    @Test
    fun `useCase는 repository가 정상 응답시 결과를 Result로 래핑한다`() {
        every { repository.fetchQueryData(any(), any()) } returns Single.just(emptyList())

        val actual = useCase("query", 1).blockingLast()

        assertThat(actual)
            .isInstanceOf(Result::class.java)
    }

    //state test
    @Test
    fun `useCase는 reposiory가 에러 전달시 Result로 래핑한다`() {
        val unitTestException = Exception("unit test exception")
        every { repository.fetchQueryData(any(), any()) } returns Single.error(unitTestException)

        val actual = useCase("query", 1).blockingLast()
        val expect = Result.failure<List<ImageModel>>(unitTestException)
        assertThat(actual)
            .isEqualTo(expect)
            .isInstanceOf(Result::class.java)
    }

    //state test
    @Test
    fun `useCase는 page가 1일때 skeletonData를 먼저 emit하고 결과를 emit한다`() {
        val (query, page) = "query" to 1
        every { repository.fetchQueryData(query, page) } returns Single.just(emptyList())

        val skeletonSize = 1
        val actualObservable = useCase(query, page, skeletonSize)
        val expectSkeletonData = Result.success(
            listOf(SearchImageListTypeModel.Query(query)) + MutableList(skeletonSize) { SearchImageListTypeModel.Skeleton }
        )
        val expectSearchResultData = Result.success(
            listOf(SearchImageListTypeModel.Query(query)) + emptyList<SearchImageListTypeModel>()
        )

        assertThat(actualObservable.blockingFirst())
            .isEqualTo(expectSkeletonData)

        assertThat(actualObservable.blockingLast())
            .isEqualTo(expectSearchResultData)
    }

    //state test
    @Test
    fun `useCase는 query가 비어있다면 Query만를 포함한 리스트를 리턴한다`() {
        val (query, page) = "" to 1

        val actual = useCase(query, page).blockingLast()
        val except = listOf<SearchImageListTypeModel>(SearchImageListTypeModel.Query(query))
        assertThat(actual.getOrNull())
            .isNotNull
            .isEqualTo(except)
    }

    //state test
    @Test
    fun `useCase는 page가 1이면 Query를 첫번째 아이템으로 가지는 SearchImageListType리스트를 리턴한다`() {
        val (query, page) = "query" to 1
        val images = listOf(
            SearchImageModel.Empty,
            SearchImageModel.Empty.copy(imageUrl = "123")
        )

        every { repository.fetchQueryData(query, page) } returns Single.just(images)
        val actual = useCase(query, page).blockingLast().getOrNull()
        val except = listOf(
            SearchImageListTypeModel.Query(query),
            SearchImageListTypeModel.Image(images[0]),
            SearchImageListTypeModel.Image(images[1])
        )
        assertThat(actual?.first())
            .isNotNull
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
        val actual = useCase(query, page).blockingFirst()
        val except = images.map { SearchImageListTypeModel.Image(it) }

        assertThat(actual.getOrNull())
            .isNotNull
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