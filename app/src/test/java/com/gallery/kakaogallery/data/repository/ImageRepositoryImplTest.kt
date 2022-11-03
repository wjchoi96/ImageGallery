package com.gallery.kakaogallery.data.repository

import com.gallery.kakaogallery.data.datasource.*
import com.gallery.kakaogallery.data.entity.remote.response.ImageSearchResponse
import com.gallery.kakaogallery.data.entity.remote.response.VideoSearchResponse
import com.gallery.kakaogallery.domain.model.ImageModel
import com.gallery.kakaogallery.domain.repository.ImageRepository
import com.gallery.kakaogallery.domain.util.GalleryDateConvertUtil
import com.google.gson.Gson
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Single
import org.assertj.core.api.Assertions.assertThat
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

    private fun readResponse(fileName: String): String{
        return File("src/test/java/com/gallery/kakaogallery/data/resources/$fileName").readText()
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
    fun `fetchQueryData는 결과를 최신순으로 sort하여 리턴한다`() {
        val (query, page) = "test" to 0
        val actualImageSearchResponse = Gson().fromJson(readResponse("image_search_success.json"), ImageSearchResponse::class.java).documents
        val actualVideoSearchResponse = Gson().fromJson(readResponse("video_search_success.json"), VideoSearchResponse::class.java).documents
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
        val actualImageSearchResponse = Gson().fromJson(readResponse("image_search_success.json"), ImageSearchResponse::class.java).documents
        val actualVideoSearchResponse = Gson().fromJson(readResponse("video_search_success.json"), VideoSearchResponse::class.java).documents
        every { imageSearchDataSource.fetchImageQueryRes(query, page) } returns Single.just(actualImageSearchResponse)
        every { videoSearchDataSource.fetchVideoQueryRes(query, page) } returns Single.just(actualVideoSearchResponse)

        val actual = repository.fetchQueryData(query, page).blockingGet()
        assertThat(actual.first()).isInstanceOf(ImageModel::class.java)
    }

    //state test
    @Test
    fun `fetchQueryData는 올바른 Item개수를 리턴한다`() {
        val (query, page) = "test" to 0
        val actualImageSearchResponse = Gson().fromJson(readResponse("image_search_success.json"), ImageSearchResponse::class.java).documents
        val actualVideoSearchResponse = Gson().fromJson(readResponse("video_search_success.json"), VideoSearchResponse::class.java).documents
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