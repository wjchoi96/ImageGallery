package com.gallery.kakaogallery.data.dao

import com.gallery.kakaogallery.KakaoGallerySharedPreferences
import com.gallery.kakaogallery.data.entity.local.ImageEntity
import com.gallery.kakaogallery.domain.model.SearchImageModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SaveImageDaoImpl @Inject constructor(
    private val sp: KakaoGallerySharedPreferences
) : SaveImageDao {
    private lateinit var saveImagesSubject: BehaviorSubject<List<ImageEntity>>

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
        saveImagesSubject = BehaviorSubject.createDefault(list)
    }

    override fun fetchSaveImages(): Observable<List<ImageEntity>> {
        Timber.d("fetchSaveImages at Dao run in ${Thread.currentThread().name}")
        return saveImagesSubject // 공유된 하나의 hot stream 에서 데이터를 전달받게끔 설정
    }

    override fun removeImages(idxList: List<Int>) {
        Timber.d("removeImages at Dao run in ${Thread.currentThread().name}")
        // idx 가 큰수부터 remove 를 실행해줘야 중간에 idx가 꼬이지 않는다
        val list = saveImagesSubject.value?.toMutableList() ?: mutableListOf()
        for (removeIdx in idxList.sorted().reversed()) {
            Timber.d("remove idx : $removeIdx")
            list.removeAt(removeIdx)
        }
        syncData(list)
    }

    override fun saveImages(image: List<SearchImageModel>, saveDateTimeMill: Long) {
        Timber.d("saveImages at Dao run in ${Thread.currentThread().name}")
        val list = (saveImagesSubject.value?.toMutableList() ?: mutableListOf()).apply {
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
        saveImagesSubject.onNext(list)
    }
}