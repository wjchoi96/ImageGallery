package com.gallery.kakaogallery.data.repository

import com.gallery.kakaogallery.data.datasource.ImageSearchDataSource
import com.gallery.kakaogallery.data.datasource.SaveImageDataSource
import com.gallery.kakaogallery.data.datasource.VideoSearchDataSource
import com.gallery.kakaogallery.domain.model.GalleryImageModel
import com.gallery.kakaogallery.domain.model.MaxPageException
import com.gallery.kakaogallery.domain.model.SearchImageModel
import com.gallery.kakaogallery.domain.model.UnKnownException
import com.gallery.kakaogallery.domain.repository.ImageRepository
import com.gallery.kakaogallery.domain.util.GalleryDateConvertUtil
import com.gallery.kakaogallery.presentation.di.DefaultDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject

/**
 * 이미지 검색, 비디오 검색 2가지를 repository 에서 메소드 화 하고
 * useCase 에서 merge?
 */
class ImageRepositoryImpl @Inject constructor(
    private val imageSearchDataSource: ImageSearchDataSource,
    private val videoSearchDataSource: VideoSearchDataSource,
    private val saveImageDataSource: SaveImageDataSource,
    @DefaultDispatcher private val dispatcher: CoroutineDispatcher
) : ImageRepository {

    private fun fetchImageQuery(query: String, page: Int): Flow<List<SearchImageModel>> {
        return imageSearchDataSource.fetchImageQueryRes(query, page)
            .map {
                it.map { data ->
                    data.toModel(
                        dateTimeToShow = GalleryDateConvertUtil.convertToPrint(data.datetime) ?: "",
                        dateTimeMill = GalleryDateConvertUtil.convertToMill(data.datetime) ?: 0L
                    )
                }
            }
            .flowOn(dispatcher)
    }

    private fun fetchVideoQuery(query: String, page: Int): Flow<List<SearchImageModel>> {
        return videoSearchDataSource.fetchVideoQueryRes(query, page)
            .map {
                it.map { data ->
                    data.toModel(
                        dateTimeToShow = GalleryDateConvertUtil.convertToPrint(data.datetime) ?: "",
                        dateTimeMill = GalleryDateConvertUtil.convertToMill(data.datetime) ?: 0L
                    )
                }
            }
            .flowOn(dispatcher)
    }

    // zip 은 가장 최근에 zip 되지 않은 데이터들끼리 zip 을 한다
    // 두개의 api 중 하나만 성공하고, 하나만 실패하는경우 다음 검색의 결과값에 이전 결과값의 데이터가 남아서 영향을 주게 되므로 api 에러가 뜨는 경우 빈 데이터를 넣어서 onNext 해준다
    // BiFunction 의 작업 환경은 첫번째 Stream 스케쥴러를 따라간다
    override fun fetchQueryData(query: String, page: Int): Flow<List<SearchImageModel>> {
        return fetchImageQuery(query, page)
            .wrapResult()
            .zip(fetchVideoQuery(query, page)
                .wrapResult()
            ) { t1, t2 ->
                Timber.d("Flow.zip run at " + Thread.currentThread().name)
                when {
                    t1.isFailure && t2.isFailure -> {
                        throw when {
                            t1.exceptionOrNull() is MaxPageException -> t2.exceptionOrNull() ?: UnKnownException()
                            else -> t1.exceptionOrNull() ?: UnKnownException()
                        }
                    }
                    else -> {
                        (t1.getOrNull() ?: emptyList()) + (t2.getOrNull() ?: emptyList()).run {
                            sortedByDescending { it.dateTimeMill }
                        }
                    }
                }
            }.flowOn(dispatcher)
            .catch {
                Timber.d("error debug => after zip => $it")
                throw it
            }
    }

    private fun Flow<List<SearchImageModel>>.wrapResult(): Flow<Result<List<SearchImageModel>>> =
        this.map {
            Result.success(it)
        }.catch {
            emit(Result.failure(it))
        }

    override fun fetchSaveImages(): Flow<List<GalleryImageModel>> {
        return saveImageDataSource.fetchSaveImages()
            .map {
                it.map {  data ->
                    data.toModel(
                        dateTimeToShow = GalleryDateConvertUtil.convertToPrint(data.saveDateTimeMill),
                    )
                }
            }.flowOn(dispatcher)
    }

    override fun removeImages(idxList: List<Int>): Flow<Boolean> {
        return saveImageDataSource.removeImages(idxList)
    }

    override fun saveImages(image: List<SearchImageModel>, saveDateTimeMill: Long): Flow<Boolean> {
        return saveImageDataSource.saveImages(image, saveDateTimeMill)
    }

}
