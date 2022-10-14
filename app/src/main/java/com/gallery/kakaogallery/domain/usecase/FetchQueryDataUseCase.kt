package com.gallery.kakaogallery.domain.usecase

import android.util.Log
import com.gallery.kakaogallery.domain.model.ImageModel
import com.gallery.kakaogallery.domain.model.Result
import com.gallery.kakaogallery.domain.model.ResultError
import com.gallery.kakaogallery.domain.repository.ImageRepository
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable

class FetchQueryDataUseCase(
    private val imageRepository: ImageRepository
) {

    operator fun invoke(query: String, page: Int): Observable<Result<List<ImageModel>>>{
        return imageRepository
            .fetchQueryData(query, page)
            .onErrorReturn {
                it.printStackTrace()
                Log.e("TAG", "onErrorReturn images search res")
                Result.Fail(ResultError.Crash)
            }
            .observeOn(AndroidSchedulers.mainThread())
    }
}