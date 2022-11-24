package com.gallery.kakaogallery.data.datasource

import com.gallery.kakaogallery.data.dao.SaveImageDao
import com.gallery.kakaogallery.data.entity.local.ImageEntity
import com.gallery.kakaogallery.domain.model.ImageModel
import com.gallery.kakaogallery.domain.model.SearchImageModel
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import java.util.*

@Suppress("NonAsciiCharacters")
internal class SaveImageDataSourceImplTest {

    private lateinit var saveImageDataSource: SaveImageDataSource

    //behavior test
    @Test
    fun `fetchSaveImages는 SaveImageDao의 fetchSaveImages를 호출한다`() {
        val saveImageDao: SaveImageDao = mockk(relaxed = true)
        saveImageDataSource = SaveImageDataSourceImpl(saveImageDao)
        saveImageDataSource.fetchSaveImages()

        verify { saveImageDao.fetchSaveImages() }
    }

    //behavior test
    @Test
    fun `removeImages는 SaveImageDao의 removeImages를 호출한다`() {
        val saveImageDao: SaveImageDao = mockk(relaxed = true)
        val list = emptyList<Int>()
        saveImageDataSource = SaveImageDataSourceImpl(saveImageDao)
        saveImageDataSource.removeImages(list).blockingAwait()

        verify { saveImageDao.removeImages(list) }
    }

    //behavior test
    @Test
    fun `saveImages는 SaveImageDao의 saveImages를 호출한다`() {
        val saveImageDao: SaveImageDao = mockk(relaxed = true)
        val saveMill = Date().time
        val searchImages = emptyList<SearchImageModel>()
        saveImageDataSource = SaveImageDataSourceImpl(saveImageDao)
        saveImageDataSource.saveImages(searchImages, saveMill).blockingAwait()

        verify { saveImageDao.saveImages(searchImages, saveMill) }
    }

}