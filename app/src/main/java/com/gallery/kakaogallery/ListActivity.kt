package com.gallery.kakaogallery

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ProgressBar
import androidx.activity.viewModels
import com.gallery.kakaogallery.databinding.ActivityListBinding
import com.gallery.kakaogallery.view.base.BaseActivity
import com.gallery.kakaogallery.view.base.BaseViewModel
import com.gallery.kakaogallery.view.comp.HeaderComp

class ListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)
    }
}