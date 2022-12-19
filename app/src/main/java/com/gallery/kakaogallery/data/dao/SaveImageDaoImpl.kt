package com.gallery.kakaogallery.data.dao

import com.gallery.kakaogallery.KakaoGallerySharedPreferences
import com.gallery.kakaogallery.data.entity.local.ImageEntity
import com.gallery.kakaogallery.domain.model.SearchImageModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SaveImageDaoImpl @Inject constructor(
    private val sp: KakaoGallerySharedPreferences
) : SaveImageDao {
    private lateinit var saveImagesFlow: MutableStateFlow<List<ImageEntity>>

    init {
        initImageStream()
    }

    private fun initImageStream() {
        val listJson = sp.savedImageList
        val list = if (listJson.isBlank())
            emptyList<ImageEntity>()
        else {
            val typeToken = object : TypeToken<List<ImageEntity>>() {}.type
            Gson().fromJson(listJson, typeToken)
        }
        saveImagesFlow = MutableStateFlow(list)
    }

    override suspend fun fetchSaveImages(): Flow<List<ImageEntity>> {
        Timber.d("fetchSaveImages at Dao run in ${Thread.currentThread().name}")
        return saveImagesFlow // 공유된 하나의 hot stream 에서 데이터를 전달받게끔 설정
    }

    override suspend fun removeImages(idxList: List<Int>) {
        Timber.d("removeImages at Dao run in ${Thread.currentThread().name}")
        // idx 가 큰수부터 remove 를 실행해줘야 중간에 idx가 꼬이지 않는다
        val list = saveImagesFlow.value.toMutableList()
        for (removeIdx in idxList.sorted().reversed()) {
            Timber.d("remove idx : $removeIdx")
            list.removeAt(removeIdx)
        }
        syncData(list)
    }

    override suspend fun saveImages(image: List<SearchImageModel>, saveDateTimeMill: Long) {
        Timber.d("saveImages at Dao run in ${Thread.currentThread().name}")
        val list = saveImagesFlow.value.toMutableList().apply {
            addAll(
                image.toList().map {
                    ImageEntity.from(
                        it,
                        saveDateTimeMill
                    )
                }
            )
        }
        syncData(list)
    }

    private fun syncData(list: List<ImageEntity>) {
        Timber.d("syncData at Dao run in ${Thread.currentThread().name}")
        val jsonStr = Gson().toJson(list)
        Timber.d("syncData save image list data(${list.size}) => \n$jsonStr\n")
        sp.savedImageList = jsonStr
        Timber.d("syncData save finish : \n${sp.savedImageList}")
        Timber.d("syncData run at \n${Thread.currentThread().name}")
        saveImagesFlow.value = list
    }
}