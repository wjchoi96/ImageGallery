package com.gallery.kakaogallery.presentation.application

import android.content.Context

class StringResourceProviderImpl(
    private val context: Context
) : StringResourceProvider {
    override fun getString(id: StringResourceProvider.StringResourceId): String {
        return context.resources.getString(id.id) // 잘못된 id 전달시에 throw
    }

    override fun getString(id: StringResourceProvider.StringResourceId, vararg formatArg: Any): String {
        return context.resources.getString(id.id, *formatArg) // 잘못된 id 전달시에 throw
    }
}