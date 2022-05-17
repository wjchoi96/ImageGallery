package com.gallery.kakaogallery.view.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.gallery.kakaogallery.util.AppHelper
import io.reactivex.rxjava3.disposables.CompositeDisposable

abstract class BaseFragment<T : ViewDataBinding, R : BaseViewModel> : Fragment() {
    protected val TAG = AppHelper.getTag(this::class.java)

    lateinit var vd : T
    abstract val layoutResId : Int
    abstract val viewModel : R

    protected var mContext : Context? = null
    protected val compositeDisposable = CompositeDisposable()
    protected var loading = false

    private var progress : ProgressBar? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        vd = DataBindingUtil.inflate(inflater, layoutResId, container, false)
        val view = vd.root as ViewGroup
        initAtBase(view)
        return view
    }

    protected open fun startMainFunc(){

    }

    override fun onDetach() {
        super.onDetach()
        if ( !compositeDisposable.isDisposed ) {
            compositeDisposable.dispose() // clear? dispose
        }
        if ( mContext != null )
            mContext = null
    }

    private fun initAtBase(view : ViewGroup){
        progress = getProgress()
        initData()
        initView(view)
        bind()
        startMainFunc()
    }
    abstract fun initData()
    abstract fun initView(root : ViewGroup)
    abstract fun bind()
    abstract fun getProgress() : ProgressBar?

    protected open fun setProgress(visible : Boolean){
        loading = visible
        if(visible)
            progress?.visibility = View.VISIBLE
        else
            progress?.visibility = View.GONE
    }

    protected fun showToast(message : String){
        Toast.makeText(mContext?.applicationContext ?: return, message, Toast.LENGTH_SHORT).show()
    }

    protected fun hideKeyBoard(view : View){
        val imm = mContext?.applicationContext?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}