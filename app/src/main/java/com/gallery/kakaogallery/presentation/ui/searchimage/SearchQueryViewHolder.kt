package com.gallery.kakaogallery.presentation.ui.searchimage

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gallery.kakaogallery.databinding.ItemSearchQueryBinding
import com.gallery.kakaogallery.domain.model.SearchImageListTypeModel
import timber.log.Timber


class SearchQueryViewHolder private constructor(
    private val binding: ItemSearchQueryBinding,
    val searchQueryListener: (String) -> Unit,
    val queryEditorActionListener: TextView.OnEditorActionListener
) : RecyclerView.ViewHolder(binding.root) {
    companion object {
        fun from(
            parent: ViewGroup,
            searchQueryListener: (String) -> Unit,
            queryEditorActionListener: TextView.OnEditorActionListener
        ): SearchQueryViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ItemSearchQueryBinding.inflate(layoutInflater, parent, false)
            return SearchQueryViewHolder(binding, searchQueryListener, queryEditorActionListener)
        }
    }

    val recentQuery: String
        get() {
            Timber.d("getQuery : " + binding.etQuery.text)
            return binding.etQuery.text.toString()
        }

    fun bind(query: SearchImageListTypeModel.Query) {
        binding.holder = this
        bindQuery(query)
//        binding.executePendingBindings()
    }

    fun bindQuery(query: SearchImageListTypeModel.Query){
        binding.lastQuery = query.query
    }
}