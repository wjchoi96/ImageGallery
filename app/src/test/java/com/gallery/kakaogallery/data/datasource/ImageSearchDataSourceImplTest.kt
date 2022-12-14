package com.gallery.kakaogallery.data.datasource

import com.gallery.kakaogallery.data.FakeNetworkConnectionInterceptor
import com.gallery.kakaogallery.data.UnitTestUtil
import com.gallery.kakaogallery.data.constant.SearchConstant
import com.gallery.kakaogallery.data.entity.remote.request.ImageSearchRequest
import com.gallery.kakaogallery.data.entity.remote.response.ImageSearchResponse
import com.gallery.kakaogallery.data.service.ImageSearchService
import com.gallery.kakaogallery.domain.model.MaxPageException
import com.google.gson.Gson
import io.mockk.mockk
import io.mockk.verify
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.HttpURLConnection

@Suppress("NonAsciiCharacters")
internal class ImageSearchDataSourceImplTest {

    private lateinit var imageSearchDataSource: ImageSearchDataSource

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
    fun `fetchImageQueryRes는 Network상태로 인한 오류발생을 처리할 수 있다`() {
        networkConnectionInterceptor.setNetworkState(false)
        imageSearchDataSource = ImageSearchDataSourceImpl(
            mockRetrofit.create(ImageSearchService::class.java)
        )
        val (query, page) = "test" to 1
        val actualResponseJson = UnitTestUtil.readResource("image_search_success.json")
        val actualResponse = MockResponse().apply {
            setResponseCode(HttpURLConnection.HTTP_FORBIDDEN)
            setBody(actualResponseJson)
        }
        mockWebServer.enqueue(actualResponse)
        val actual = catchThrowable { imageSearchDataSource.fetchImageQueryRes(query, page).blockingGet() }
        val expect = FakeNetworkConnectionInterceptor.ioExceptionMessage
        assertThat(actual)
            .isInstanceOf(Throwable::class.java)
            .hasMessageContaining(expect)

    }

    //state test
    @Test
    fun `fetchImageQueryRes는 1번 페이지를 검색한다면 페이징 여부를 초기화한다`() {
        imageSearchDataSource = ImageSearchDataSourceImpl(
            mockRetrofit.create(ImageSearchService::class.java)
        )
        val (query, page) = "test" to 1
        val actualResponseJson = UnitTestUtil.readResource("image_search_success_is_end.json")
        val actualResponse = MockResponse().apply {
            setResponseCode(HttpURLConnection.HTTP_OK)
            setBody(actualResponseJson)
        }
        mockWebServer.enqueue(actualResponse)
        mockWebServer.enqueue(actualResponse)

        imageSearchDataSource.fetchImageQueryRes(query, page).blockingGet()
        val actual = imageSearchDataSource.fetchImageQueryRes(query, page).blockingGet()
        val expect = Gson().fromJson(actualResponseJson, ImageSearchResponse::class.java).documents
        assertThat(actual)
            .isEqualTo(expect)
    }

    //state test
    @Test
    fun `fetchImageQueryRes는 다음 페이지가 없다면 MaxPageException을 발생시킨다`() {
        imageSearchDataSource = ImageSearchDataSourceImpl(
            mockRetrofit.create(ImageSearchService::class.java)
        )
        val (query, page) = "test" to 1
        val actualResponseJson = UnitTestUtil.readResource("image_search_success_is_end.json")
        val actualResponse = MockResponse().apply {
            setResponseCode(HttpURLConnection.HTTP_OK)
            setBody(actualResponseJson)
        }
        mockWebServer.enqueue(actualResponse)

        catchThrowable { imageSearchDataSource.fetchImageQueryRes(query, page).blockingGet() }
        val actual = catchThrowable { imageSearchDataSource.fetchImageQueryRes(query, page + 1).blockingGet() }
        val expect = MaxPageException().message
        assertThat(actual)
            .isInstanceOf(Throwable::class.java)
            .hasMessageContaining(expect)
    }

    //state test
    @Test
    fun `fetchImageQueryRes는 잘못된 HTTP응답 CODE를 처리할 수 있다`() {
        imageSearchDataSource = ImageSearchDataSourceImpl(
            mockRetrofit.create(ImageSearchService::class.java)
        )
        val (query, page) = "test" to 1
        val actualResponseJson = UnitTestUtil.readResource("image_search_success.json")
        val actualResponse = MockResponse().apply {
            setResponseCode(HttpURLConnection.HTTP_FORBIDDEN)
            setBody(actualResponseJson)
        }
        mockWebServer.enqueue(actualResponse)

        val actual = catchThrowable { imageSearchDataSource.fetchImageQueryRes(query, page).blockingGet() }
//        val actual = catchThrowable { throw MaxPageException("test") }
        assertThat(actual)
            .isInstanceOf(HttpException::class.java)
    }

    //state test
    @Test
    fun `fetchImageQueryRes는 잘못된 서버 응답을 처리할 수 있다`() {
        imageSearchDataSource = ImageSearchDataSourceImpl(
            mockRetrofit.create(ImageSearchService::class.java)
        )
        val (query, page) = "test" to 1
        val actualResponseJson = UnitTestUtil.readResource("image_search_fail.json")
        val actualResponse = MockResponse().apply {
            setResponseCode(HttpURLConnection.HTTP_OK)
            setBody(actualResponseJson)
        }
        mockWebServer.enqueue(actualResponse)

        assertThatThrownBy { imageSearchDataSource.fetchImageQueryRes(query, page).blockingGet() }
            .isInstanceOf(Throwable::class.java)
    }

    //state test
    @Test
    fun `fetchImageQueryRes는 올바른 서버 응답을 처리할 수 있다`() {
        imageSearchDataSource = ImageSearchDataSourceImpl(
            mockRetrofit.create(ImageSearchService::class.java)
        )
        val (query, page) = "test" to 1
        val actualResponseJson = UnitTestUtil.readResource("image_search_success.json")
        val actualResponse = MockResponse().apply {
            setResponseCode(HttpURLConnection.HTTP_OK)
            setBody(actualResponseJson)
        }
        mockWebServer.enqueue(actualResponse)

        val actual = imageSearchDataSource.fetchImageQueryRes(query, page).blockingGet()

        val expect = Gson().fromJson(actualResponseJson, ImageSearchResponse::class.java).documents
        assertThat(actual).isEqualTo(expect)
    }

    //behavior test
    @Test
    fun `fetchImageQueryRes는 ImageSearchService의 fetch메소드를 호출한다`() {
        val (query, page) = "test" to 1
        val mockService: ImageSearchService = mockk(relaxed = true)
        imageSearchDataSource = ImageSearchDataSourceImpl(mockService)

        imageSearchDataSource.fetchImageQueryRes(query, page)

        verify {
            mockService.requestSearchImage(
                query,
                ImageSearchRequest.SortType.Recency.key,
                page, // 1~50
                SearchConstant.ImagePageSizeMaxValue
            )
        }
    }
}