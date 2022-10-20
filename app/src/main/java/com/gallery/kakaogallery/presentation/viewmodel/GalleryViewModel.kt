package com.gallery.kakaogallery.presentation.viewmodel

import com.gallery.kakaogallery.domain.model.ImageModel
import com.gallery.kakaogallery.domain.repository.ImageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val imageRepository: ImageRepository
) : DisposableManageViewModel() {
    private val errorMessageSubject: PublishSubject<String> = PublishSubject.create()
    var errorMessageObservable: Observable<String> =
        errorMessageSubject.observeOn(AndroidSchedulers.mainThread())

    private val savedImageSubject: PublishSubject<List<ImageModel>> = PublishSubject.create()
    var savedImageListObservable: Observable<List<ImageModel>> =
        savedImageSubject.observeOn(AndroidSchedulers.mainThread())

    private val removeImageIdxSubject: PublishSubject<List<Int>> = PublishSubject.create()
    var removeImageIdxListObservable: Observable<List<Int>> =
        removeImageIdxSubject.observeOn(AndroidSchedulers.mainThread())

    private val insertedImageIdxSubject: PublishSubject<List<Int>> = PublishSubject.create()
    var insertedImageIdxListObservable: Observable<List<Int>> =
        insertedImageIdxSubject.observeOn(AndroidSchedulers.mainThread())


    val imageList = mutableListOf<ImageModel>()
    var selectMode: Boolean = false
    val selectImageIdxList = mutableListOf<Int>()

    init {
        bind()
    }

    private fun bind() {
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

    private fun getSavedImageList() {
//        imageList.clear()
//        imageList.addAll(saveImageStorage.imageList)
    }

    fun requestSavedImageList() {
        getSavedImageList()
        savedImageSubject.onNext(imageList)
    }

    fun requestRemoveImageList(imgIdxList: List<Int>) {
//        Thread{
//            Timber.d("requestRemoveImageList : ${imgIdxList.size} - thread : ${Thread.currentThread().name}")
//            val res = saveImageStorage.removeImageList(imgIdxList)
//            getSavedImageList()
//            removeImageIdxSubject.onNext(res)
//        }.start()
    }
}