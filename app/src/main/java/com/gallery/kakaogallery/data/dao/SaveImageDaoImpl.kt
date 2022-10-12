package com.gallery.kakaogallery.data.dao

import android.util.Log
import com.gallery.kakaogallery.KakaoGallerySharedPreferences
import com.gallery.kakaogallery.domain.model.ImageModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject

class SaveImageDaoImpl(
    private val sp: KakaoGallerySharedPreferences
): SaveImageDao {
    companion object {
        private const val TAG = "SaveImageDao"
    }

    private lateinit var saveImagesSubject: BehaviorSubject<List<ImageModel>>

    init {
        initImageStream()
    }

    private fun initImageStream(){
        val listJson = sp.savedImageList
        val list = if(listJson.isBlank())
            emptyList<ImageModel>()
        else{
            val typeToken = object : TypeToken<ArrayList<ImageModel>>() {}.type
            Gson().fromJson(listJson, typeToken)
        }
        saveImagesSubject = BehaviorSubject.createDefault(list)
    }

    override fun fetchSaveImages(): Observable<List<ImageModel>> {
        return saveImagesSubject // 공유된 하나의 hot stream 에서 데이터를 전달받게끔 설정
    }

    override fun removeImages(idxList: List<Int>): Boolean {
        // idx 가 큰수부터 remove를 실행해줘야 중간에 idx가 꼬이지 않는다
        val list = saveImagesSubject.value?.toMutableList() ?: mutableListOf()
        for(idx in idxList.sorted().reversed()){
            Log.d(TAG, "remove idx : $idx")
            list.removeAt(idx)
        }
        syncData(list)
        return true
    }

    override fun saveImage(image: ImageModel): Boolean {
        val list = saveImagesSubject.value?.toMutableList() ?: mutableListOf<ImageModel>().apply {
            add(image)
        }
        syncData(list)
        return true
    }

    private fun syncData(list: List<ImageModel>){
        val jsonStr = Gson().toJson(saveImagesSubject.value ?: emptyList<ImageModel>())
        Log.d(TAG, "syncData save image list data(${saveImagesSubject.value?.size}) => \n$jsonStr\n")
        sp.savedImageList = jsonStr
        Log.d(TAG, "syncData save finish : \n${sp.savedImageList}")
        saveImagesSubject.onNext(list)
    }
}