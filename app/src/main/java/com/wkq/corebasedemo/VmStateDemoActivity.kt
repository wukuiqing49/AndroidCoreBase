package com.wkq.corebasedemo

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import com.wkq.base.BaseUiState
import com.wkq.base.BaseViewModel
import com.wkq.base.activity.BaseVMTitleActivity
import com.wkq.corebasedemo.databinding.ActivityBaseVmDemoBinding

class VmStateDemoActivity :
    BaseVMTitleActivity<ActivityBaseVmDemoBinding, CoreBaseDemoViewModel>() {

    override fun initView() {
        setPageTitle(getString(R.string.demo_vm_page_name))
        contentBinding.tvIntro.text = getString(R.string.demo_vm_intro)

        viewModel.stateText.observe(this) {
            contentBinding.tvState.text = it
        }

        contentBinding.btnLoading.setOnClickListener {
            viewModel.showLoadingSample()
        }
        contentBinding.btnContent.setOnClickListener {
            viewModel.showContentSample()
        }
        contentBinding.btnEmpty.setOnClickListener {
            viewModel.showEmptySample()
        }
        contentBinding.btnError.setOnClickListener {
            viewModel.showErrorSample()
        }
        contentBinding.btnToast.setOnClickListener {
            viewModel.sendToastSample()
        }
        contentBinding.btnConfirm.setOnClickListener {
            viewModel.sendConfirmSample()
        }
    }

    override fun initData() {
        viewModel.showContentSample()
    }

    override fun onBaseUiStateChanged(state: BaseUiState) {
        contentBinding.tvLastState.text = when (state) {
            BaseUiState.Content -> getString(R.string.demo_state_content)
            BaseUiState.Idle -> getString(R.string.demo_state_idle)
            is BaseUiState.Empty -> getString(R.string.demo_state_empty, state.message.orEmpty())
            is BaseUiState.Error -> getString(R.string.demo_state_error, state.message)
            is BaseUiState.Loading -> getString(R.string.demo_state_loading, state.message.orEmpty())
        }
    }
}

class CoreBaseDemoViewModel : BaseViewModel() {

    val stateText = MutableLiveData("Content")
    private val mainHandler = Handler(Looper.getMainLooper())

    fun showLoadingSample() {
        stateText.value = "Loading"
        showLoading("LoadingDialog from BaseViewModel")
        mainHandler.postDelayed({
            showContentSample()
        }, 900)
    }

    fun showContentSample() {
        stateText.value = "Content"
        showContent()
    }

    fun showEmptySample() {
        stateText.value = "Empty"
        showEmpty("No demo data")
    }

    fun showErrorSample() {
        stateText.value = "Error"
        showError("Demo error")
    }

    fun sendToastSample() {
        sendToast("Toast event from BaseViewModel")
    }

    fun sendConfirmSample() {
        sendConfirmDialog(
            requestKey = "vm_confirm",
            title = "ConfirmDialog",
            message = "This dialog is dispatched by BaseViewModel."
        )
    }

    override fun onConfirmDialogResult(requestKey: String, confirmed: Boolean) {
        stateText.value = if (confirmed) {
            "Confirm dialog result: confirmed"
        } else {
            "Confirm dialog result: cancelled"
        }
    }

    override fun onCleared() {
        mainHandler.removeCallbacksAndMessages(null)
        super.onCleared()
    }
}
