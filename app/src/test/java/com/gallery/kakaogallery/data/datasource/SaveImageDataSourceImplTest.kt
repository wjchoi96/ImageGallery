package com.gallery.kakaogallery.data.datasource

import com.gallery.kakaogallery.data.dao.SaveImageDao
import com.gallery.kakaogallery.domain.model.SearchImageModel
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.util.*

@ExperimentalCoroutinesApi
@Suppress("NonAsciiCharacters")
internal class SaveImageDataSourceImplTest {

    private lateinit var saveImageDataSource: SaveImageDataSource
    private val testDispatcher = StandardTestDispatcher(TestCoroutineScheduler())

    //behavior test
    @Test
    fun `fetchSaveImages는 SaveImageDao의 fetchSaveImages를 호출한다`() = runTest(testDispatcher) {
        val saveImageDao: SaveImageDao = mockk(relaxed = true)
        saveImageDataSource = SaveImageDataSourceImpl(saveImageDao, testDispatcher)
        saveImageDataSource.fetchSaveImages()

        verify { saveImageDao.fetchSaveImages() }
    }

    //behavior test
    @Test
    fun `removeImages는 SaveImageDao의 removeImages를 호출한다`() = runTest(testDispatcher) {
        val saveImageDao: SaveImageDao = mockk(relaxed = true)
        val list = emptyList<Int>()
        saveImageDataSource = SaveImageDataSourceImpl(saveImageDao, testDispatcher)
        saveImageDataSource.removeImages(list).firstOrNull()

        coVerify { saveImageDao.removeImages(list) }
    }

    //behavior test
    @Test
    fun `saveImages는 SaveImageDao의 saveImages를 호출한다`() = runTest(testDispatcher) {
        val saveImageDao: SaveImageDao = mockk(relaxed = true)
        val saveMill = Date().time
        val searchImages = emptyList<SearchImageModel>()
        saveImageDataSource = SaveImageDataSourceImpl(saveImageDao, testDispatcher)
        saveImageDataSource.saveImages(searchImages, saveMill).firstOrNull()

        coVerify { saveImageDao.saveImages(searchImages, saveMill) }
    }

}