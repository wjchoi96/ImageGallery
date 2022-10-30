package com.gallery.kakaogallery.presentation.ui.dialog

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.gallery.kakaogallery.R
import com.gallery.kakaogallery.databinding.DialogImageManageBinding
import com.gallery.kakaogallery.presentation.viewmodel.ImageManageBottomSheetViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import timber.log.Timber

class ImageManageBottomSheetDialog : BottomSheetDialogFragment() {
    companion object {
        private const val TAG = "image_manage_bottom_sheet_dialog"
        private const val EXTRA_CONTENT = "extra_content"
        private const val EXTRA_POSITIVE_TEXT = "extra_positive_text"
        private const val EXTRA_NEGATIVE_TEXT = "extra_negative_text"

        fun get(
            content: String,
            positiveBtnText: String,
            negativeBtnText: String,
            positiveListener: (() -> Unit)? = null,
            negativeListener: (() -> Unit)? = null
        ) = ImageManageBottomSheetDialog().apply {
            arguments = bundleOf(
                EXTRA_CONTENT to content,
                EXTRA_POSITIVE_TEXT to positiveBtnText,
                EXTRA_NEGATIVE_TEXT to negativeBtnText
            )
            setPositiveBtnListener(positiveListener)
            setNegativeBtnListener(negativeListener)
        }
    }

    private var _binding: DialogImageManageBinding? = null
    private val binding get() = _binding ?: error("Binding not Initialized")
    private val viewModel: ImageManageBottomSheetViewModel by viewModels()


    private var cachePositiveListener: (() -> Unit)? = null
    private fun setPositiveBtnListener(positiveListener: (() -> Unit)? = null) {
        cachePositiveListener = positiveListener
    }

    private var cacheNegativeListener: (() -> Unit)? = null
    private fun setNegativeBtnListener(negativeListener: (() -> Unit)? = null) {
        cacheNegativeListener = negativeListener
    }

    private val defaultBtnText: String by lazy { requireContext().getString(R.string.confirm) }

    fun show(fragmentManager: FragmentManager){
        if(fragmentManager.findFragmentByTag(TAG) == null)
            this.show(fragmentManager, TAG)
    }

    override fun getTheme(): Int {
        return R.style.CustomBottomSheetDialog
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Timber.d("onCreateView at bottom sheet")
        _binding = DialogImageManageBinding.inflate(LayoutInflater.from(context), container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        arguments?.let {
            viewModel.initData(
                it.getString(EXTRA_CONTENT),
                it.getString(EXTRA_POSITIVE_TEXT) ?: defaultBtnText,
                it.getString(EXTRA_NEGATIVE_TEXT),
                cachePositiveListener,
                cacheNegativeListener
            )
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (binding.root.parent as View).setBackgroundColor(Color.parseColor("#00000000"))
        setDefaultListener()
        observeData()
    }

    private fun setDefaultListener() {
        binding.btnPositive.setOnClickListener { this.dismiss() }
        binding.btnNegative.setOnClickListener { this.dismiss() }
    }

    private fun observeData(){
        viewModel.uiEvent.observe(viewLifecycleOwner){ event ->
            event.getContentIfNotHandled()?.let {
                when (it) {
                    is ImageManageBottomSheetViewModel.UiEvent.Dismiss -> {
                        this.dismiss()
                    }
                }
            }
        }
    }

}