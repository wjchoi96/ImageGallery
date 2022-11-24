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
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class ImageManageBottomSheetDialog : BottomSheetDialogFragment() {
    companion object {
        private const val TAG = "image_manage_bottom_sheet_dialog"

        fun newInstance(
            content: String,
            positiveBtnText: String,
            negativeBtnText: String
        ) = ImageManageBottomSheetDialog().apply {
            arguments = bundleOf(
                ImageManageBottomSheetViewModel.KEY_CONTENT to content,
                ImageManageBottomSheetViewModel.KEY_POSITIVE_TEXT to positiveBtnText,
                ImageManageBottomSheetViewModel.KEY_NEGATIVE_TEXT to negativeBtnText
            )
        }
    }

    private var _binding: DialogImageManageBinding? = null
    private val binding get() = _binding ?: error("Binding not Initialized")
    private val viewModel: ImageManageBottomSheetViewModel by viewModels()

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
        Timber.d("onCreateView at bottom sheet => ${parentFragment is ImageManageBottomSheetEventReceiver}")
        _binding = DialogImageManageBinding.inflate(LayoutInflater.from(context), container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
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
                    is ImageManageBottomSheetViewModel.UiEvent.PositiveEvent -> {
                        (parentFragment as? ImageManageBottomSheetEventReceiver)?.onPositiveEventReceive()
                    }
                    is ImageManageBottomSheetViewModel.UiEvent.NegativeEvent -> {
                        (parentFragment as? ImageManageBottomSheetEventReceiver)?.onNegativeEventReceive()
                    }
                    is ImageManageBottomSheetViewModel.UiEvent.Dismiss -> {
                        this.dismiss()
                    }
                }
            }
        }
    }

}