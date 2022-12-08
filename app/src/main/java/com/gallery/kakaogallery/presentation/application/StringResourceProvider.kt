package com.gallery.kakaogallery.presentation.application

import com.gallery.kakaogallery.R

interface StringResourceProvider {
    fun getString(id: StringResourceId): String

    fun getString(id: StringResourceId, vararg formatArg: Any): String

    enum class StringResourceId(val id: Int){
        MenuSearchImage(R.string.menu_search_image),
        MenuGallery(R.string.menu_gallery),

        Loading(R.string.message_loading),
        Confirm(R.string.confirm),
        Retry(R.string.retry),

        NoneQuery(R.string.message_none_query),
        SearchFail(R.string.message_search_query_fail),
        LastPage(R.string.message_last_page),

        NoneSelectImage(R.string.message_none_select_image),
        SelectFail(R.string.message_select_fail),
        SelectState(R.string.message_select_state),

        SaveSuccess(R.string.message_save_success),
        SaveFail(R.string.message_save_fail),
        EmptySaveImage(R.string.message_empty_save_image),
        FetchFailSaveImage(R.string.message_fetch_save_image_fail),

        RemoveSuccess(R.string.message_remove_success),
        RemoveFail(R.string.message_remove_fail),
    }
}