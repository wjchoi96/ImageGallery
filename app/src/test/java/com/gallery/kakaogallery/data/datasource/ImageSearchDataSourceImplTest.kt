package com.gallery.kakaogallery.data.datasource

import com.gallery.kakaogallery.data.FakeNetworkConnectionInterceptor
import com.gallery.kakaogallery.data.UnitTestUtil
import com.gallery.kakaogallery.data.constant.SearchConstant
import com.gallery.kakaogallery.data.entity.remote.request.ImageSearchRequest
import com.gallery.kakaogallery.data.entity.remote.response.ImageSearchResponse
import com.gallery.kakaogallery.data.service.ImageSearchService
import com.gallery.kakaogallery.domain.model.MaxPageException
import com.google.gson.Gson
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.runTest
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

@ExperimentalCoroutinesApi
@Suppress("NonAsciiCharacters")
internal class ImageSearchDataSourceImplTest {

    private lateinit var imageSearchDataSource: ImageSearchDataSource

    private lateinit var mockRetrofit: Retrofit
    private lateinit var mockWebServer: MockWebServer

    private lateinit var networkConnectionInterceptor: FakeNetworkConnectionInterceptor

    private val testDispatcher = StandardTestDispatcher(TestCoroutineScheduler())

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
    fun downMockServer() {
        mockWebServer.shutdown()
    }

    //state test
    @Test
    fun `fetchImageQueryRes는 Network상태로 인한 오류발생을 처리할 수 있다`() {
        networkConnectionInterceptor.setNetworkState(false)
        imageSearchDataSource = ImageSearchDataSourceImpl(
            mockRetrofit.create(ImageSearchService::class.java),
            testDispatcher
        )
        val (query, page) = "test" to 1
        val actualResponseJson = UnitTestUtil.readResource("image_search_success.json")
        val actualResponse = MockResponse().apply {
            setResponseCode(HttpURLConnection.HTTP_FORBIDDEN)
            setBody(actualResponseJson)
        }
        mockWebServer.enqueue(actualResponse)
        val actual = catchThrowable {
            runTest(testDispatcher) {
                imageSearchDataSource.fetchImageQueryRes(query, page).first()
            }
        }
        val expect = FakeNetworkConnectionInterceptor.ioExceptionMessage
        assertThat(actual)
            .isInstanceOf(Throwable::class.java)
            .hasMessageContaining(expect)

    }

    //state test
    @Test
    fun `fetchImageQueryRes는 1번 페이지를 검색한다면 페이징 여부를 초기화한다`() = runTest(testDispatcher) {
        imageSearchDataSource = ImageSearchDataSourceImpl(
            mockRetrofit.create(ImageSearchService::class.java),
            testDispatcher
        )
        val (query, page) = "test" to 1
        val actualResponseJson = UnitTestUtil.readResource("image_search_success_is_end.json")
        val actualResponse = MockResponse().apply {
            setResponseCode(HttpURLConnection.HTTP_OK)
            setBody(actualResponseJson)
        }
        mockWebServer.enqueue(actualResponse)
        mockWebServer.enqueue(actualResponse)

        imageSearchDataSource.fetchImageQueryRes(query, page).firstOrNull()
        val actual = imageSearchDataSource.fetchImageQueryRes(query, page).firstOrNull()
        val expect = Gson().fromJson(actualResponseJson, ImageSearchResponse::class.java).documents
        assertThat(actual)
            .isNotNull
            .isEqualTo(expect)
    }

    //state test
    @Test
    fun `fetchImageQueryRes는 다음 페이지가 없다면 MaxPageException을 발생시킨다`() {
        imageSearchDataSource = ImageSearchDataSourceImpl(
            mockRetrofit.create(ImageSearchService::class.java),
            testDispatcher
        )
        val (query, page) = "test" to 1
        val actualResponseJson = UnitTestUtil.readResource("image_search_success_is_end.json")
        val actualResponse = MockResponse().apply {
            setResponseCode(HttpURLConnection.HTTP_OK)
            setBody(actualResponseJson)
        }
        mockWebServer.enqueue(actualResponse)

        runTest(testDispatcher) { imageSearchDataSource.fetchImageQueryRes(query, page).firstOrNull() }
        val actual = catchThrowable {
            runTest(testDispatcher) {
                imageSearchDataSource.fetchImageQueryRes(query, page + 1).firstOrNull()
            }
        }
        val expect = MaxPageException().message
        assertThat(actual)
            .isNotNull
            .isInstanceOf(Throwable::class.java)
            .hasMessageContaining(expect)
    }

    //state test
    @Test
    fun `fetchImageQueryRes는 잘못된 HTTP응답 CODE를 처리할 수 있다`() {
        imageSearchDataSource = ImageSearchDataSourceImpl(
            mockRetrofit.create(ImageSearchService::class.java),
            testDispatcher
        )
        val (query, page) = "test" to 1
        val actualResponseJson = UnitTestUtil.readResource("image_search_success.json")
        val actualResponse = MockResponse().apply {
            setResponseCode(HttpURLConnection.HTTP_FORBIDDEN)
            setBody(actualResponseJson)
        }
        mockWebServer.enqueue(actualResponse)

        val actual = catchThrowable {
            runTest(testDispatcher) {
                imageSearchDataSource.fetchImageQueryRes(query, page).firstOrNull()
            }
        }

        assertThat(actual)
            .isNotNull
            .isInstanceOf(HttpException::class.java)
    }

    //state test
    @Test
    fun `fetchImageQueryRes는 잘못된 서버 응답을 처리할 수 있다`() {
        imageSearchDataSource = ImageSearchDataSourceImpl(
            mockRetrofit.create(ImageSearchService::class.java),
            testDispatcher
        )
        val (query, page) = "test" to 1
        val actualResponseJson = UnitTestUtil.readResource("image_search_fail.json")
        val actualResponse = MockResponse().apply {
            setResponseCode(HttpURLConnection.HTTP_OK)
            setBody(actualResponseJson)
        }
        mockWebServer.enqueue(actualResponse)

        val actual = catchThrowable {
            runTest(testDispatcher) {
                imageSearchDataSource.fetchImageQueryRes(query, page).firstOrNull()
            }
        }

        assertThat(actual)
            .isNotNull
            .isInstanceOf(Throwable::class.java)
    }

    //state test
    @Test
    fun `fetchImageQueryRes는 올바른 서버 응답을 처리할 수 있다`() = runTest(testDispatcher) {
        imageSearchDataSource = ImageSearchDataSourceImpl(
            mockRetrofit.create(ImageSearchService::class.java),
            testDispatcher
        )
        val (query, page) = "test" to 1
        val actualResponseJson = UnitTestUtil.readResource("image_search_success.json")
        val actualResponse = MockResponse().apply {
            setResponseCode(HttpURLConnection.HTTP_OK)
            setBody(actualResponseJson)
        }
        mockWebServer.enqueue(actualResponse)

        val actual = imageSearchDataSource.fetchImageQueryRes(query, page).firstOrNull()

        val expect = Gson().fromJson(actualResponseJson, ImageSearchResponse::class.java).documents
        assertThat(actual)
            .isNotNull
            .isEqualTo(expect)
    }

    //behavior test
    @Test
    fun `fetchImageQueryRes는 ImageSearchService의 fetch메소드를 호출한다`() {
        val (query, page) = "test" to 1
        val mockService: ImageSearchService = mockk(relaxed = true)
        imageSearchDataSource = ImageSearchDataSourceImpl(mockService, testDispatcher)

        runTest(testDispatcher) {
            imageSearchDataSource.fetchImageQueryRes(query, page).firstOrNull()
        }

        coVerify(exactly = 1) {
            runTest(testDispatcher) {
                mockService.requestSearchImage(
                    query,
                    ImageSearchRequest.SortType.Recency.key,
                    page, // 1~50
                    SearchConstant.ImagePageSizeMaxValue
                )
            }
        }
    }
}