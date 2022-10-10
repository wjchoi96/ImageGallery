package com.gallery.kakaogallery.presentation.ui.base

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.gallery.kakaogallery.presentation.application.KakaoGalleryApplication
import com.gallery.kakaogallery.presentation.ui.comp.HeaderComp
import io.reactivex.rxjava3.disposables.CompositeDisposable


abstract  class BaseActivity<T : ViewDataBinding, S : BaseViewModel> : AppCompatActivity() {
    protected val TAG = KakaoGalleryApplication.getTag(this::class.java)

    lateinit var vd : T
    abstract val layoutResId : Int
    abstract val viewModel : S

    protected val compositeDisposable : CompositeDisposable = CompositeDisposable()

    protected var loading = false
    private var progress : ProgressBar? = null
    abstract fun getProgress() : ProgressBar?

    protected var headerComp : HeaderComp? = null
    abstract fun getHeader() : HeaderComp?

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(layoutResId)
        vd = DataBindingUtil.setContentView(this, layoutResId)
        init()
        startMainFunc()

    }
    private fun init(){
        initData()
        initAtBase()
        initView()
        bind()
    }
    private fun initAtBase(){
//        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT // 세로모드 고정
        setBar()
        progress = getProgress()
    }

    abstract fun initData()
    abstract fun initView()
    abstract fun bind()
    protected open fun startMainFunc(){

    }

    private fun setBar(){
        val actionBar  = supportActionBar!!

        actionBar.setDisplayShowCustomEnabled(true)
        actionBar.setDisplayHomeAsUpEnabled(false)
        actionBar.setDisplayShowTitleEnabled(false)
        actionBar.elevation = 0f

        headerComp = getHeader()
        if(headerComp == null){
            actionBar.hide()
            return
        }else
            actionBar.show()

        actionBar.customView = headerComp

        //val tool : Toolbar = actionBarView.parent as Toolbar
        val tool : Toolbar = headerComp?.parent as Toolbar
        tool.setContentInsetsAbsolute(0,0)

        val params : ActionBar.LayoutParams = ActionBar.LayoutParams(
            ActionBar.LayoutParams.MATCH_PARENT,
            ActionBar.LayoutParams.MATCH_PARENT,
            Gravity.CENTER)

        //actionBar.setCustomView(actionBarView,params)
        actionBar.setCustomView(headerComp ,params)
    }

    protected fun hideKeyboard(view : View){
        val imm = applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    protected fun showToast(message : String){
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    @Override
    open fun setProgress(visible : Boolean){
        loading = visible
        if(visible)
            progress?.visibility = View.VISIBLE
        else
            progress?.visibility = View.GONE
    }
}