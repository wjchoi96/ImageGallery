package com.gallery.kakaogallery.domain.usecase

import android.util.Log
import com.gallery.kakaogallery.domain.model.ImageModel
import com.gallery.kakaogallery.domain.model.MaxPageException
import com.gallery.kakaogallery.domain.model.Result
import com.gallery.kakaogallery.domain.model.ResultError
import com.gallery.kakaogallery.domain.repository.ImageRepository
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers

class FetchQueryDataUseCase(
    private val imageRepository: ImageRepository
) {

    operator fun invoke(query: String, page: Int): Observable<Result<List<ImageModel>>>{
        return imageRepository
            .fetchQueryData(query, page)
            .observeOn(Schedulers.computation())
            .map {
                if(it != null)
                    Result.Success(it)
                else
                    Result.Fail(ResultError.Fail)
            }
            .onErrorReturn {
                it.printStackTrace()
                Log.d("TAG", "error debug at useCase => $it")
                when(it){
                    is MaxPageException -> Result.Fail(ResultError.MaxPage)
                    else -> Result.Fail(ResultError.Crash)
                }
            }
    }
}