package com.gallery.kakaogallery.domain.usecase

import com.gallery.kakaogallery.domain.model.GalleryImageListTypeModel
import com.gallery.kakaogallery.domain.model.GalleryImageModel
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

    //state test
    @Test
    fun `useCase는 결과와 상관없이 skeletonData를 먼저 emit한다`() {
        every { repository.fetchSaveImages() } returns Observable.just(emptyList())

        val skeletonSize = 1
        val expectSkeletonData = Result.success(MutableList(skeletonSize) { GalleryImageListTypeModel.Skeleton })

        assertThat(useCase(skeletonSize).blockingFirst())
            .isEqualTo(expectSkeletonData)

        every { repository.fetchSaveImages() } returns Observable.error(Throwable("test"))

        assertThat(useCase(skeletonSize).blockingFirst())
            .isEqualTo(expectSkeletonData)
    }

    //state test
    @Test
    fun `useCase는 정상 응답시 skeletonData를 먼저 emit하고 saveImageData를 emit한다`() {
        every { repository.fetchSaveImages() } returns Observable.just(emptyList())

        val skeletonSize = 1
        val actualObservable = useCase(skeletonSize)
        val expectSkeletonData = Result.success(MutableList(skeletonSize) { GalleryImageListTypeModel.Skeleton })

        assertThat(actualObservable.blockingFirst())
            .isEqualTo(expectSkeletonData)

        assertThat(actualObservable.blockingLast())
            .isEqualTo(Result.success(emptyList<GalleryImageListTypeModel>()))
    }

    //state test
    @Test
    fun `useCase는 repository의 fetchSaveImage의 결과를 GalleryImageListTypeModel로 변환한다`() {
        every { repository.fetchSaveImages() } returns Observable.just(listOf(GalleryImageModel.Empty))
        val actual = useCase().blockingLast().getOrNull()?.first()

        assertThat(actual)
            .isNotNull
            .isInstanceOf(GalleryImageListTypeModel::class.java)
            .isInstanceOf(GalleryImageListTypeModel.Image::class.java)
    }

    // state test
    @Test
    fun `useCase는 reposiory가 에러 전달시 결과를 Result로 래핑한다`() {
        val unitTestException = Exception("unit test exception")
        every { repository.fetchSaveImages() } returns Observable.error(unitTestException)

        val actual = useCase().blockingLast()
        val expect = Result.failure<List<ImageModel>>(unitTestException)
        assertThat(actual)
            .isEqualTo(expect)
            .isInstanceOf(Result::class.java)
    }

    //state test
    @Test
    fun `useCase는 repository가 에러 전달 시 skeletonData, skeletonData를 제거할 List, 에러 순서대로 emit한다`() {
        val unitTestException = Exception("unit test exception")
        val skeletonSize = 1
        every { repository.fetchSaveImages() } returns Observable.error(unitTestException)

        val actualIterator = useCase(skeletonSize).blockingIterable().iterator()
        val expectIterator = listOf<Result<List<GalleryImageListTypeModel>>>(
            Result.success(listOf(GalleryImageListTypeModel.Skeleton)),
            Result.success(emptyList()),
            Result.failure(unitTestException),
        ).iterator()

        assertThat(actualIterator.next())
            .isEqualTo(expectIterator.next())
        assertThat(actualIterator.next())
            .isEqualTo(expectIterator.next())
        assertThat(actualIterator.next())
            .isEqualTo(expectIterator.next())
    }

    //state test
    @Test
    fun `useCase는 repository가 정상 응답시 결과를 Result로 래핑한다`() {
        val images = listOf(
            GalleryImageModel.Empty,
            GalleryImageModel.Empty.copy(imageUrl = "test123")
        )
        every { repository.fetchSaveImages() } returns Observable.just(images)

        val actual = useCase().blockingLast()
        val expect = Result.success(images.map { GalleryImageListTypeModel.Image(it) })

        assertThat(actual)
            .isEqualTo(expect)
            .isInstanceOf(Result::class.java)
    }

    //state test
    @Test
    fun `useCase는 Observable타입을 리턴한다`() {
        every { repository.fetchSaveImages() } returns Observable.just(emptyList())
        val actual = useCase()

        assertThat(actual)
            .isInstanceOf(Observable::class.java)
    }

    //behavior test
    @Test
    fun `useCase는 repository의 fetchSaveImages를 호출한다`() {
        useCase()
        verify { repository.fetchSaveImages() }
    }
}