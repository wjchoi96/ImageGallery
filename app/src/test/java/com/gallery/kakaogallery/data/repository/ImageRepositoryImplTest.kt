package com.gallery.kakaogallery.data.repository

import com.gallery.kakaogallery.data.UnitTestUtil
import com.gallery.kakaogallery.data.datasource.*
import com.gallery.kakaogallery.data.entity.remote.response.ImageSearchResponse
import com.gallery.kakaogallery.data.entity.remote.response.VideoSearchResponse
import com.gallery.kakaogallery.domain.model.ImageModel
import com.gallery.kakaogallery.domain.model.MaxPageException
import com.gallery.kakaogallery.domain.repository.ImageRepository
import com.google.gson.Gson
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Single
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Before
import org.junit.Test
import java.io.File


class ImageRepositoryImplTest {

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
        repository.fetchQueryData(query, page)
        verify { imageSearchDataSource.fetchImageQueryRes(query, page) }
        verify { videoSearchDataSource.fetchVideoQueryRes(query, page) }
    }

    //state test
    @Test
    fun `fetchQueryData는 DataSource하나만 Exception을 발생시키면 정상 동작한다`() {
        val (query, page) = "test" to 0
        val actualImageSearchResponse = Gson().fromJson(UnitTestUtil.readResource("image_search_success.json"), ImageSearchResponse::class.java).documents
        val actualVideoSearchResponse = Gson().fromJson(UnitTestUtil.readResource("video_search_success.json"), VideoSearchResponse::class.java).documents

        every { imageSearchDataSource.fetchImageQueryRes(query, page) } returns Single.error(MaxPageException())
        every { videoSearchDataSource.fetchVideoQueryRes(query, page) } returns Single.just(actualVideoSearchResponse)

        assertThat(repository.fetchQueryData(query, page)
            .blockingGet().size)
            .isEqualTo(actualVideoSearchResponse.size)

        every { imageSearchDataSource.fetchImageQueryRes(query, page) } returns Single.just(actualImageSearchResponse)
        every { videoSearchDataSource.fetchVideoQueryRes(query, page) } returns Single.error(MaxPageException())

        assertThat(repository.fetchQueryData(query, page)
            .blockingGet().size)
            .isEqualTo(actualImageSearchResponse.size)
    }

    //state test
    @Test
    fun `fetchQueryData는 DataSource모두 Exception을 발생시키면 해당 Exception을 발생시킨다`() {
        val (query, page) = "test" to 0
        every { imageSearchDataSource.fetchImageQueryRes(query, page) } returns Single.error(MaxPageException())
        every { videoSearchDataSource.fetchVideoQueryRes(query, page) } returns Single.error(MaxPageException())

        val except = MaxPageException().message
        assertThatThrownBy { repository.fetchQueryData(query, page).blockingGet() }
            .isInstanceOf(Throwable::class.java)  // blockingGet 에서 RuntimeException 으로 래핑해서 예외를 전달해줌
            .hasMessageContaining(except)
    }

    //state test
    @Test
    fun `fetchQueryData는 결과를 최신순으로 sort하여 리턴한다`() {
        val (query, page) = "test" to 0
        val actualImageSearchResponse = Gson().fromJson(UnitTestUtil.readResource("image_search_success.json"), ImageSearchResponse::class.java).documents
        val actualVideoSearchResponse = Gson().fromJson(UnitTestUtil.readResource("video_search_success.json"), VideoSearchResponse::class.java).documents
        every { imageSearchDataSource.fetchImageQueryRes(query, page) } returns Single.just(actualImageSearchResponse)
        every { videoSearchDataSource.fetchVideoQueryRes(query, page) } returns Single.just(actualVideoSearchResponse)

        val actual = repository.fetchQueryData(query, page).blockingGet()
        assertThat(actual).isSortedAccordingTo { i1, i2 ->
            ((i1.dateTimeMill ?: 0L) - (i2.dateTimeMill ?: 0L)).toInt()
        }
    }

    //state test
    @Test
    fun `fetchQueryData는 Response객체를 ImageModel로 가공하여 리턴한다`() {
        val (query, page) = "test" to 0
        val actualImageSearchResponse = Gson().fromJson(UnitTestUtil.readResource("image_search_success.json"), ImageSearchResponse::class.java).documents
        val actualVideoSearchResponse = Gson().fromJson(UnitTestUtil.readResource("video_search_success.json"), VideoSearchResponse::class.java).documents
        every { imageSearchDataSource.fetchImageQueryRes(query, page) } returns Single.just(actualImageSearchResponse)
        every { videoSearchDataSource.fetchVideoQueryRes(query, page) } returns Single.just(actualVideoSearchResponse)

        val actual = repository.fetchQueryData(query, page).blockingGet()
        assertThat(actual.first()).isInstanceOf(ImageModel::class.java)
    }

    //state test
    @Test
    fun `fetchQueryData는 올바른 Item개수를 리턴한다`() {
        val (query, page) = "test" to 0
        val actualImageSearchResponse = Gson().fromJson(UnitTestUtil.readResource("image_search_success.json"), ImageSearchResponse::class.java).documents
        val actualVideoSearchResponse = Gson().fromJson(UnitTestUtil.readResource("video_search_success.json"), VideoSearchResponse::class.java).documents
        every { imageSearchDataSource.fetchImageQueryRes(query, page) } returns Single.just(actualImageSearchResponse)
        every { videoSearchDataSource.fetchVideoQueryRes(query, page) } returns Single.just(actualVideoSearchResponse)

        val expect = actualImageSearchResponse.size + actualVideoSearchResponse.size
        val actual = repository.fetchQueryData(query, page).blockingGet()
        assertThat(actual.size).isEqualTo(expect)
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
        repository.saveImages(emptyList())
        verify { saveImageDataSource.saveImages(emptyList()) }
    }

    //behavior test
    @Test
    fun `removeImages는 SaveImageDataSource의 removeImages를 호출한다`() {
        repository.removeImages(emptyList())
        verify { saveImageDataSource.removeImages(emptyList()) }
    }

}