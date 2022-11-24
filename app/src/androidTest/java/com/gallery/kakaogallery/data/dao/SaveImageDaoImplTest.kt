package com.gallery.kakaogallery.data.dao

import android.content.Context
import android.content.SharedPreferences
import com.gallery.kakaogallery.KakaoGallerySharedPreferences
import com.gallery.kakaogallery.data.entity.local.ImageEntity
import com.gallery.kakaogallery.domain.model.SearchImageModel
import com.google.gson.Gson
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import java.util.*

@Suppress("NonAsciiCharacters")
internal class SaveImageDaoImplTest {

    private lateinit var dao: SaveImageDao
    private lateinit var sp: KakaoGallerySharedPreferences

    @Before
    fun setup(){
        val context: Context = mockk(relaxed = true)
        val sharedPreferences: SharedPreferences = mockk(relaxed = true)
        every { context.getSharedPreferences(any(), any()) } returns sharedPreferences
        sp = KakaoGallerySharedPreferences(context)
        every { sp.savedImageList } returns ""
        dao = SaveImageDaoImpl(sp)
    }

    //behavior test
    @Test
    fun init_called_KakaoGallerySharedPreferences_savedImageList() {
        verify { sp.savedImageList }
    }

    //behavior test
    @Test
    fun saveImages_called_KakaoGallerySharedPreferences_savedImageList() {
        dao.saveImages(emptyList(), Date().time)
        verify { sp.savedImageList = any() }
    }

    //state test
    @Test
    fun saveImages_called_KakaoGallerySharedPreferences_savedImageList_use_combined_saved_list() {
        val saveMill = Date().time
        val images = listOf(
            SearchImageModel.Empty.copy(imageUrl = "test1"),
            SearchImageModel.Empty.copy(imageUrl = "test2")
        )
        val daoImage = dao.fetchSaveImages().blockingFirst()
        dao.saveImages(images, saveMill)

        val expectJson = Gson().toJson(daoImage + images.map { ImageEntity.from(it, saveMill) })
        verify { sp.savedImageList = expectJson }
    }

    //behavior test
    @Test
    fun removeImages_called_KakaoGallerySharedPreferences_savedImageList() {
        dao.removeImages(emptyList())
        verify { sp.savedImageList = any() }
    }

    //state test
    @Test
    fun removeImages_called_KakaoGallerySharedPreferences_savedImageList_use_removed_list(){
        val saveMill = Date().time
        val images = listOf(
            SearchImageModel.Empty.copy(imageUrl = "test1"),
            SearchImageModel.Empty.copy(imageUrl = "test2"),
            SearchImageModel.Empty.copy(imageUrl = "test3")
        )
        dao.saveImages(images, saveMill)
        val removeIdxList = listOf(0, 1)

        dao.removeImages(removeIdxList)
        val expect = Gson().toJson(
            images.filterIndexed { i, _ -> i == 2 }
                .map { ImageEntity.from(it, saveMill) }
        )
        verify { sp.savedImageList = expect }
    }

    //state test
    @Test
    fun fetchSaveImages_returns_continuous_stream() {
        val saveMill = Date().time
        val searchImages = mutableListOf(
            SearchImageModel.Empty.copy(imageUrl = "test1"),
            SearchImageModel.Empty.copy(imageUrl = "test2"),
            SearchImageModel.Empty.copy(imageUrl = "test3")
        )
        val saveImages = searchImages.map {
            ImageEntity.from(it, saveMill)
        }.toMutableList()

        dao.saveImages(searchImages, saveMill)
        val actualObservable = dao.fetchSaveImages()
        actualObservable.test().assertValue(saveImages)

        dao.removeImages(listOf(0, 1, 2))
        searchImages.clear()
        saveImages.clear()

        actualObservable.test().assertValue(saveImages)

        val newImage = SearchImageModel.Empty.copy(imageUrl = "test1")
        searchImages.add(newImage)
        saveImages.add(ImageEntity.from(newImage, saveMill))
        dao.saveImages(searchImages, saveMill)
        actualObservable.test().assertValue(saveImages)
    }

}