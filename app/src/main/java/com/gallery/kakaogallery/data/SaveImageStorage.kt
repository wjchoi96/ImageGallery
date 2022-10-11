package com.gallery.kakaogallery.data

import android.util.Log
import com.gallery.kakaogallery.presentation.application.KakaoGalleryApplication
import com.gallery.kakaogallery.model.ImageModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.rxjava3.subjects.PublishSubject

class SaveImageStorage private constructor() {
    companion object {
        private val TAG = KakaoGalleryApplication.getTag(this::class.java)
        val instance = SaveImageStorage()
    }

    val imageList : ArrayList<ImageModel> = ArrayList()
    val imageInsertedSubject : PublishSubject<ArrayList<Int>> = PublishSubject.create()
    val imageRemovedSubject : PublishSubject<ArrayList<ImageModel>> = PublishSubject.create()

    init {
        initSavedList()
    }

    // data storage 에 저장된 list 를 가져온다
    private fun initSavedList() {
        imageList.clear()
        val listJson = KakaoGalleryApplication.dataStorage.savedImageList
        imageList.addAll(if(listJson.isBlank() || listJson == DataStorage.emptyValue){
            ArrayList()
        }else{
            val typeToken = object : TypeToken<ArrayList<ImageModel>>() {}.type
            Gson().fromJson(listJson, typeToken)
        })
        Log.d(TAG, "initSavedList \n${Gson().toJson(imageList)}")
    }

    // 검색 결과에서 보관했던 이미지들이 보관한 순서대로 보입니다.
    // 순서가 오름차순인지 내림차순인지 헷갈린다
    fun saveImage(image : ImageModel) : Int {
        image.setSaveDateTime() // 저장 시간 설정
        imageList.add(0, image.copy()) // 지금 저장한게 무조건 가장 최근에 저장된 image 이다, image 깊은복사
        syncData()
        imageInsertedSubject.onNext(ArrayList<Int>().apply { add(0) })
        return 0
    }
    fun saveImageList(newImageList : ArrayList<ImageModel>) : ArrayList<Int> {
        val idxList = ArrayList<Int>()
        for((idx, image) in newImageList.withIndex()){
            image.setSaveDateTime() // 저장 시간 설정
            imageList.add(0, image.copy()) // 지금 저장한게 무조건 가장 최근에 저장된 image 이다, image 깊은복사
            idxList.add(idx) // 매번 0번째에 add 하니, add 할수록 먼저 한 item 은 뒤로 밀린다
        }
        syncData()
        imageInsertedSubject.onNext(idxList)
        return idxList
    }
    fun removeImage(image : ImageModel) : Int {
        var removeIdx = -1
        for((idx, item) in imageList.withIndex()){
            if(image.saveTimeMill == item.saveTimeMill){
                removeIdx = idx
                break
            }
        }
        imageList.remove(image)
        syncData()
        imageRemovedSubject.onNext(ArrayList<ImageModel>().apply { add(image) })
        return removeIdx
    }
    fun removeImageList(idxList : ArrayList<Int>) : ArrayList<Int> {
        // idx 가 큰 순으로 정렬
        // idx 가 큰수부터 remove를 실행해줘야 중간에 idx가 꼬이지 않는다
        val reverseIdxList = idxList.sorted().reversed()
        val removedList = ArrayList<ImageModel>()
        for(idx in reverseIdxList){
            Log.d(TAG, "remove idx : $idx")
            removedList.add(imageList.removeAt(idx))
        }
        syncData()
        imageRemovedSubject.onNext(removedList)
        return  ArrayList(reverseIdxList)
    }

    private fun syncData(){
        val jsonStr = Gson().toJson(imageList)
        Log.d(TAG, "syncData save image list data(${imageList.size}) => \n$jsonStr\n")
        KakaoGalleryApplication.dataStorage.savedImageList = jsonStr
        Log.d(TAG, "syncData save finish : \n${KakaoGalleryApplication.dataStorage.savedImageList}")
    }
}