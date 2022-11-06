package com.gallery.kakaogallery.data.dao

import android.content.Context
import android.content.SharedPreferences
import com.gallery.kakaogallery.KakaoGallerySharedPreferences
import com.gallery.kakaogallery.domain.model.ImageModel
import com.google.gson.Gson
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

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
        dao.saveImages(emptyList())
        verify { sp.savedImageList = any() }
    }

    //state test
    @Test
    fun saveImages_called_KakaoGallerySharedPreferences_savedImageList_use_combined_saved_list() {
        val images = listOf(
            ImageModel.Empty.copy(imageUrl = "test1"),
            ImageModel.Empty.copy(imageUrl = "test2")
        )
        val daoImage = dao.fetchSaveImages().blockingFirst()
        dao.saveImages(images)

        val expectJson = Gson().toJson(daoImage + images)
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
        dao.saveImages(listOf(
            ImageModel.Empty.copy(imageUrl = "test1"),
            ImageModel.Empty.copy(imageUrl = "test2"),
            ImageModel.Empty.copy(imageUrl = "test3")
        ))
        val removeIdxList = listOf(0, 1)

        dao.removeImages(removeIdxList)
        val expect = Gson().toJson(
            listOf(
                ImageModel.Empty.copy(imageUrl = "test3")
            )
        )
        verify { sp.savedImageList = expect }
    }

    //state test
    @Test
    fun fetchSaveImages_returns_continuous_stream() {
        val list = mutableListOf(
            ImageModel.Empty.copy(imageUrl = "test1"),
            ImageModel.Empty.copy(imageUrl = "test2"),
            ImageModel.Empty.copy(imageUrl = "test3")
        )
        dao.saveImages(list)
        val actualObservable = dao.fetchSaveImages()
        actualObservable.test().assertValue(list)

        dao.removeImages(listOf(0, 1, 2))
        list.clear()
        actualObservable.test().assertValue(list)

        list.add(ImageModel.Empty.copy(imageUrl = "test1"))
        dao.saveImages(list)
        actualObservable.test().assertValue(list)
    }

}