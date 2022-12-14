package com.gallery.kakaogallery.data.datasource

import com.gallery.kakaogallery.data.FakeNetworkConnectionInterceptor
import com.gallery.kakaogallery.data.UnitTestUtil
import com.gallery.kakaogallery.data.constant.SearchConstant
import com.gallery.kakaogallery.data.entity.remote.request.VideoSearchRequest
import com.gallery.kakaogallery.data.entity.remote.response.VideoSearchResponse
import com.gallery.kakaogallery.data.service.VideoSearchService
import com.gallery.kakaogallery.domain.model.MaxPageException
import com.google.gson.Gson
import io.mockk.mockk
import io.mockk.verify
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.HttpURLConnection

@Suppress("NonAsciiCharacters")
internal class VideoSearchDataSourceImplTest {

    private lateinit var videoSearchDataSource: VideoSearchDataSource

    private lateinit var mockRetrofit: Retrofit
    private lateinit var mockWebServer: MockWebServer

    private lateinit var networkConnectionInterceptor: FakeNetworkConnectionInterceptor

    @Before
    fun setup() {
        networkConnectionInterceptor = FakeNetworkConnectionInterceptor(true)
        mockWebServer = MockWebServer()
        mockWebServer.start()
        mockRetrofit = Retrofit.Builder().apply {
            baseUrl(mockWebServer.url(""))
            client(
                OkHttpClient.Builder()
                    .addInterceptor(networkConnectionInterceptor)
                    .build()
            )
            addConverterFactory(GsonConverterFactory.create())
        }.build()
    }

    @After
    fun downMockServer(){
        mockWebServer.shutdown()
    }

    //state test
    @Test
    fun `fetchVideoQueryRes는 Network상태로 인한 오류발생을 처리할 수 있다`() {
        networkConnectionInterceptor.setNetworkState(false)
        videoSearchDataSource = VideoSearchDataSourceImpl(
            mockRetrofit.create(VideoSearchService::class.java)
        )
        val (query, page) = "test" to 1
        val actualResponseJson = UnitTestUtil.readResource("video_search_success.json")
        val actualResponse = MockResponse().apply {
            setResponseCode(HttpURLConnection.HTTP_FORBIDDEN)
            setBody(actualResponseJson)
        }
        mockWebServer.enqueue(actualResponse)
        val actual = Assertions.catchThrowable { videoSearchDataSource.fetchVideoQueryRes(query, page).blockingGet() }
        val expect = FakeNetworkConnectionInterceptor.ioExceptionMessage
        Assertions.assertThat(actual)
            .isInstanceOf(Throwable::class.java)
            .hasMessageContaining(expect)

    }

    //state test
    @Test
    fun `fetchVideoQueryRes는 1번 페이지를 검색한다면 페이징 여부를 초기화한다`() {
        videoSearchDataSource = VideoSearchDataSourceImpl(
            mockRetrofit.create(VideoSearchService::class.java)
        )
        val (query, page) = "test" to 1
        val actualResponseJson = UnitTestUtil.readResource("video_search_success_is_end.json")
        val actualResponse = MockResponse().apply {
            setResponseCode(HttpURLConnection.HTTP_OK)
            setBody(actualResponseJson)
        }
        mockWebServer.enqueue(actualResponse)
        mockWebServer.enqueue(actualResponse)

        videoSearchDataSource.fetchVideoQueryRes(query, page).blockingGet()
        val actual = videoSearchDataSource.fetchVideoQueryRes(query, page).blockingGet()
        val expect = Gson().fromJson(actualResponseJson, VideoSearchResponse::class.java).documents
        Assertions.assertThat(actual)
            .isEqualTo(expect)
    }

    //state test
    @Test
    fun `fetchVideoQueryRes는 다음 페이지가 없다면 MaxPageException을 발생시킨다`() {
        videoSearchDataSource = VideoSearchDataSourceImpl(
            mockRetrofit.create(VideoSearchService::class.java)
        )
        val (query, page) = "test" to 1
        val actualResponseJson = UnitTestUtil.readResource("video_search_success_is_end.json")
        val actualResponse = MockResponse().apply {
            setResponseCode(HttpURLConnection.HTTP_OK)
            setBody(actualResponseJson)
        }
        mockWebServer.enqueue(actualResponse)

        Assertions.catchThrowable { videoSearchDataSource.fetchVideoQueryRes(query, page).blockingGet() }
        val actual = Assertions.catchThrowable { videoSearchDataSource.fetchVideoQueryRes(query, page + 1).blockingGet() }
        val expect = MaxPageException().message
        Assertions.assertThat(actual)
            .isInstanceOf(Throwable::class.java)
            .hasMessageContaining(expect)
    }

    //state test
    @Test
    fun `fetchVideoQueryRes는 잘못된 HTTP응답 CODE를 처리할 수 있다`() {
        videoSearchDataSource = VideoSearchDataSourceImpl(
            mockRetrofit.create(VideoSearchService::class.java)
        )
        val (query, page) = "test" to 1
        val actualResponseJson = UnitTestUtil.readResource("video_search_success.json")
        val actualResponse = MockResponse().apply {
            setResponseCode(HttpURLConnection.HTTP_FORBIDDEN)
            setBody(actualResponseJson)
        }
        mockWebServer.enqueue(actualResponse)

        val actual = Assertions.catchThrowable { videoSearchDataSource.fetchVideoQueryRes(query, page).blockingGet() }
        Assertions.assertThat(actual)
            .isInstanceOf(HttpException::class.java)
    }

    //state test
    @Test
    fun `fetchVideoQueryRes는 잘못된 서버 응답을 처리할 수 있다`() {
        videoSearchDataSource = VideoSearchDataSourceImpl(
            mockRetrofit.create(VideoSearchService::class.java)
        )
        val (query, page) = "test" to 1
        val actualResponseJson = UnitTestUtil.readResource("video_search_fail.json")
        val actualResponse = MockResponse().apply {
            setResponseCode(HttpURLConnection.HTTP_OK)
            setBody(actualResponseJson)
        }
        mockWebServer.enqueue(actualResponse)

        Assertions.assertThatThrownBy { videoSearchDataSource.fetchVideoQueryRes(query, page).blockingGet() }
            .isInstanceOf(Throwable::class.java)
    }

    //state test
    @Test
    fun `fetchVideoQueryRes는 올바른 서버 응답을 처리할 수 있다`() {
        videoSearchDataSource = VideoSearchDataSourceImpl(
            mockRetrofit.create(VideoSearchService::class.java)
        )
        val (query, page) = "test" to 1
        val actualResponseJson = UnitTestUtil.readResource("video_search_success.json")
        val actualResponse = MockResponse().apply {
            setResponseCode(HttpURLConnection.HTTP_OK)
            setBody(actualResponseJson)
        }
        mockWebServer.enqueue(actualResponse)

        val actual = videoSearchDataSource.fetchVideoQueryRes(query, page).blockingGet()

        val expect = Gson().fromJson(actualResponseJson, VideoSearchResponse::class.java).documents
        Assertions.assertThat(actual).isEqualTo(expect)
    }

    //behavior test
    @Test
    fun `fetchVideoQueryRes는 VideoSearchService의 fetch메소드를 호출한다`() {
        val (query, page) = "test" to 1
        val mockService: VideoSearchService = mockk(relaxed = true)
        videoSearchDataSource = VideoSearchDataSourceImpl(mockService)

        videoSearchDataSource.fetchVideoQueryRes(query, page)

        verify {
            mockService.requestSearchVideo(
                query,
                VideoSearchRequest.SortType.Recency.key,
                page, // 1~50
                SearchConstant.VideoPageSizeMaxValue
            )
        }
    }

}