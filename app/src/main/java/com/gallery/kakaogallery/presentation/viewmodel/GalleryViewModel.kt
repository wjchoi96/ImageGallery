package com.gallery.kakaogallery.presentation.viewmodel

import com.gallery.kakaogallery.domain.model.ImageModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject

class GalleryViewModel : BaseViewModel() {

    private val savedImageSubject : PublishSubject<ArrayList<ImageModel>> = PublishSubject.create()
    var savedImageListObservable : Observable<ArrayList<ImageModel>> = savedImageSubject.observeOn(AndroidSchedulers.mainThread())

    private val removeImageIdxSubject : PublishSubject<ArrayList<Int>> = PublishSubject.create()
    var removeImageIdxListObservable : Observable<ArrayList<Int>> = removeImageIdxSubject.observeOn(AndroidSchedulers.mainThread())

    private val insertedImageIdxSubject : PublishSubject<ArrayList<Int>> = PublishSubject.create()
    var insertedImageIdxListObservable : Observable<ArrayList<Int>> = insertedImageIdxSubject.observeOn(AndroidSchedulers.mainThread())


    val imageList : ArrayList<ImageModel> = ArrayList()
    var selectMode : Boolean = false
    val selectImageIdxList : ArrayList<Int> = ArrayList()

    init {
        bind()
    }

    private fun bind(){
//        saveImageStorage.imageInsertedSubject.subscribe {
//            getSavedImageList()
//            if(selectMode){
//                for(idx in 0 until selectImageIdxList.size){
//                    selectImageIdxList[idx] += it.size // 해당 개수만큼 item 이 앞쪽에 추가된것
//                }
//            }
//            insertedImageIdxSubject.onNext(it)
//        }.apply { addDisposable(this) }
    }

    private fun getSavedImageList(){
//        imageList.clear()
//        imageList.addAll(saveImageStorage.imageList)
    }

    fun requestSavedImageList() {
        getSavedImageList()
        savedImageSubject.onNext(imageList)
    }

    fun requestRemoveImageList(imgIdxList : ArrayList<Int>){
//        Thread{
//            Log.d(TAG, "requestRemoveImageList : ${imgIdxList.size} - thread : ${Thread.currentThread().name}")
//            val res = saveImageStorage.removeImageList(imgIdxList)
//            getSavedImageList()
//            removeImageIdxSubject.onNext(res)
//        }.start()
    }
}