package com.gallery.kakaogallery.data.repository

import com.gallery.kakaogallery.data.UnitTestUtil
import com.gallery.kakaogallery.data.datasource.*
import com.gallery.kakaogallery.data.entity.local.ImageEntity
import com.gallery.kakaogallery.data.entity.remote.response.ImageSearchResponse
import com.gallery.kakaogallery.data.entity.remote.response.VideoSearchResponse
import com.gallery.kakaogallery.domain.model.GalleryImageModel
import com.gallery.kakaogallery.domain.model.ImageModel
import com.gallery.kakaogallery.domain.model.MaxPageException
import com.gallery.kakaogallery.domain.model.UnKnownException
import com.gallery.kakaogallery.domain.repository.ImageRepository
import com.google.gson.Gson
import io.mockk.*
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.*
import org.junit.Before
import org.junit.Test
import java.util.*

@ExperimentalCoroutinesApi
@Suppress("NonAsciiCharacters")
internal class ImageRepositoryImplTest {

    private lateinit var repository: ImageRepository
    private lateinit var imageSearchDataSource: ImageSearchDataSource
    private lateinit var videoSearchDataSource: VideoSearchDataSource
    private lateinit var saveImageDataSource: SaveImageDataSource

    @Before
    fun setUp() {
        imageSearchDataSource = mockk<ImageSearchDataSourceImpl>(relaxed = true)
        videoSearchDataSource = mockk<VideoSearchDataSourceImpl>(relaxed = true)
        saveImageDataSource = mockk<SaveImageDataSourceImpl>(relaxed = true)
        repository = ImageRepositoryImpl(
            imageSearchDataSource,
            videoSearchDataSource,
            saveImageDataSource
        )
    }

    //behavior test
    @Test
    fun `fetchQueryData는 ImageDataSource와 VideoDataSource의 fetchQuery메소드를 호출한다`() {
        val (query, page) = "test" to 1
        runTest { repository.fetchQueryData(query, page) }
        coVerify { imageSearchDataSource.fetchImageQueryRes(query, page) }
        coVerify { videoSearchDataSource.fetchVideoQueryRes(query, page) }
    }

    //state test
    @Test
    fun `fetchQueryData는 DataSource하나만 Exception을 발생시키면 정상 동작한다`() = runTest {
        val (query, page) = "test" to 0
        val actualImageSearchResponse = Gson().fromJson(UnitTestUtil.readResource("image_search_success.json"), ImageSearchResponse::class.java).documents
        val actualVideoSearchResponse = Gson().fromJson(UnitTestUtil.readResource("video_search_success.json"), VideoSearchResponse::class.java).documents

        coEvery { imageSearchDataSource.fetchImageQueryRes(query, page) } returns flow { throw MaxPageException() }
        coEvery { videoSearchDataSource.fetchVideoQueryRes(query, page) } returns flow { emit(actualVideoSearchResponse) }

        assertThat(repository.fetchQueryData(query, page)
            .first().size)
            .isEqualTo(actualVideoSearchResponse.size)

        coEvery { imageSearchDataSource.fetchImageQueryRes(query, page) } returns flow { emit(actualImageSearchResponse) }
        coEvery { videoSearchDataSource.fetchVideoQueryRes(query, page) } returns flow { throw MaxPageException() }

        assertThat(repository.fetchQueryData(query, page)
            .first().size)
            .isEqualTo(actualImageSearchResponse.size)
    }

    //state test
    @Test
    fun `fetchQueryData는 DataSource가 서로 다른 Exception을 발생시키면 MaxPageException이 아닌것을 우선적으로 전달한다`() {
        val (query, page) = "test" to 0
        val unitTestException = UnKnownException("unit test exception")
        coEvery { imageSearchDataSource.fetchImageQueryRes(query, page) } returns flow { throw MaxPageException() }
        coEvery { videoSearchDataSource.fetchVideoQueryRes(query, page) } returns flow { throw unitTestException }

        val actualException1 = catchThrowable {
            runTest {
                repository.fetchQueryData(query, page).firstOrNull()
            }
        }

        assertThat(actualException1)
            .isNotNull
            .isInstanceOf(Throwable::class.java)  // blockingGet 에서 RuntimeException 으로 래핑해서 예외를 전달해줌
            .hasMessageContaining(unitTestException.message)

        coEvery { imageSearchDataSource.fetchImageQueryRes(query, page) } returns flow { throw unitTestException }
        coEvery { videoSearchDataSource.fetchVideoQueryRes(query, page) } returns flow { throw MaxPageException() }

        val actualException2 = catchThrowable {
            runTest {
                repository.fetchQueryData(query, page).firstOrNull()
            }
        }

        assertThat(actualException2)
            .isNotNull
            .isInstanceOf(Throwable::class.java)  // blockingGet 에서 RuntimeException 으로 래핑해서 예외를 전달해줌
            .hasMessageContaining(unitTestException.message)
    }

    //state test
    @Test
    fun `fetchQueryData는 DataSource가 둘다 MaxPageException이 아닌 Exception을 발생시키면 ImageStream의 Exception를 우선적으로 전달한다`() {
        val (query, page) = "test" to 0
        val unitTestException1 = UnKnownException("unit test exception image")
        val unitTestException2 = UnKnownException("unit test exception video")
        coEvery { imageSearchDataSource.fetchImageQueryRes(query, page) } returns flow { throw unitTestException1 }
        coEvery { videoSearchDataSource.fetchVideoQueryRes(query, page) } returns flow { throw unitTestException2 }

        val actualException = catchThrowable {
            runTest {
                repository.fetchQueryData(query, page).firstOrNull()
            }
        }

        assertThat(actualException)
            .isNotNull
            .isInstanceOf(Throwable::class.java)  // blockingGet 에서 RuntimeException 으로 래핑해서 예외를 전달해줌
            .hasMessageContaining(unitTestException1.message)
    }

    //state test
    @Test
    fun `fetchQueryData는 DataSource모두 Exception을 발생시키면 해당 Exception을 발생시킨다`() {
        val (query, page) = "test" to 0
        coEvery { imageSearchDataSource.fetchImageQueryRes(query, page) } returns flow { throw MaxPageException() }
        coEvery { videoSearchDataSource.fetchVideoQueryRes(query, page) } returns flow { throw MaxPageException() }

        val except = MaxPageException().message
        val actualException = catchThrowable {
            runTest {
                repository.fetchQueryData(query, page).firstOrNull()
            }
        }
        assertThat(actualException)
            .isNotNull
            .isInstanceOf(Throwable::class.java)  // blockingGet 에서 RuntimeException 으로 래핑해서 예외를 전달해줌
            .hasMessageContaining(except)
    }

    //state test
    @Test
    fun `fetchQueryData는 결과를 최신순으로 sort하여 리턴한다`() = runTest {
        val (query, page) = "test" to 0
        val actualImageSearchResponse = Gson().fromJson(UnitTestUtil.readResource("image_search_success.json"), ImageSearchResponse::class.java).documents
        val actualVideoSearchResponse = Gson().fromJson(UnitTestUtil.readResource("video_search_success.json"), VideoSearchResponse::class.java).documents
        coEvery { imageSearchDataSource.fetchImageQueryRes(query, page) } returns flow { emit(actualImageSearchResponse) }
        coEvery { videoSearchDataSource.fetchVideoQueryRes(query, page) } returns flow { emit(actualVideoSearchResponse) }

        val actual = repository.fetchQueryData(query, page).firstOrNull()
        assertThat(actual)
            .isNotNull
            .isSortedAccordingTo { i1, i2 ->
                ((i1.dateTimeMill) - (i2.dateTimeMill)).toInt()
            }
    }

    //state test
    @Test
    fun `fetchQueryData는 Response객체를 ImageModel로 가공하여 리턴한다`() = runTest {
        val (query, page) = "test" to 0
        val actualImageSearchResponse = Gson().fromJson(UnitTestUtil.readResource("image_search_success.json"), ImageSearchResponse::class.java).documents
        val actualVideoSearchResponse = Gson().fromJson(UnitTestUtil.readResource("video_search_success.json"), VideoSearchResponse::class.java).documents
        coEvery { imageSearchDataSource.fetchImageQueryRes(query, page) } returns flow { emit(actualImageSearchResponse) }
        coEvery { videoSearchDataSource.fetchVideoQueryRes(query, page) } returns flow { emit(actualVideoSearchResponse) }

        val actual = repository.fetchQueryData(query, page).firstOrNull()
        assertThat(actual?.first())
            .isNotNull
            .isInstanceOf(ImageModel::class.java)
    }

    //state test
    @Test
    fun `fetchQueryData는 올바른 Item개수를 리턴한다`() = runTest {
        val (query, page) = "test" to 0
        val actualImageSearchResponse = Gson().fromJson(UnitTestUtil.readResource("image_search_success.json"), ImageSearchResponse::class.java).documents
        val actualVideoSearchResponse = Gson().fromJson(UnitTestUtil.readResource("video_search_success.json"), VideoSearchResponse::class.java).documents
        coEvery { imageSearchDataSource.fetchImageQueryRes(query, page) } returns flow { emit(actualImageSearchResponse) }
        coEvery { videoSearchDataSource.fetchVideoQueryRes(query, page) } returns flow { emit(actualVideoSearchResponse) }

        val expect = actualImageSearchResponse.size + actualVideoSearchResponse.size
        val actual = repository.fetchQueryData(query, page).firstOrNull()
        assertThat(actual?.size)
            .isNotNull
            .isEqualTo(expect)
    }

    //state test
    @Test
    fun `fetchSaveImages는 ImageEntity를 GalleryImageModel로 가공한다`() {
        val saveImages = listOf(
            ImageEntity.Empty.copy(imageUrl = "1"),
            ImageEntity.Empty.copy(imageUrl = "2")
        )
        every { saveImageDataSource.fetchSaveImages() } returns Observable.just(saveImages)
        val actual = repository.fetchSaveImages().blockingFirst().first()
        assertThat(actual)
            .isInstanceOf(GalleryImageModel::class.java)
    }

    //behavior test
    @Test
    fun `fetchSaveImages는 SaveImageDataSource의 fetch메소드를 호출한다`() {
        repository.fetchSaveImages()
        verify { saveImageDataSource.fetchSaveImages() }
    }

    //behavior test
    @Test
    fun `saveImages는 SaveImageDataSource의 saveImages를 호출한다`() {
        val saveMill = Date().time
        repository.saveImages(emptyList(), saveMill)
        verify { saveImageDataSource.saveImages(emptyList(), saveMill) }
    }

    //behavior test
    @Test
    fun `removeImages는 SaveImageDataSource의 removeImages를 호출한다`() {
        repository.removeImages(emptyList())
        verify { saveImageDataSource.removeImages(emptyList()) }
    }

}