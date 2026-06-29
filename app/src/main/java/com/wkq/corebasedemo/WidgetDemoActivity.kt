package com.wkq.corebasedemo

import android.graphics.Color
import android.widget.Toast
import com.wkq.base.activity.BaseTitleActivity
import com.wkq.base.dialog.CommonDialog
import com.wkq.base.dialog.LoadingDialog
import com.wkq.base.widget.MultiSpanTextView
import com.wkq.corebasedemo.databinding.ActivityWidgetDemoBinding

class WidgetDemoActivity : BaseTitleActivity<ActivityWidgetDemoBinding>() {

    override fun initView() {
        setPageTitle(getString(R.string.demo_widget_page_name))
        setRightText(getString(R.string.demo_right_action)) {
            showCoreDialog()
        }

        contentBinding.tvDependency.text = getString(R.string.demo_dependency)
        contentBinding.tvSummary.text = getString(R.string.demo_summary)
        contentBinding.tvStatus.text = getString(R.string.demo_verify_waiting)

        contentBinding.tvProtocol.setTextWithSpans(
            getString(R.string.demo_protocol),
            MultiSpanTextView.SpanItem(
                keyword = getString(R.string.demo_protocol_user),
                color = Color.parseColor("#2457D6"),
                clickAction = { showShortToast(getString(R.string.demo_protocol_user)) }
            ),
            MultiSpanTextView.SpanItem(
                keyword = getString(R.string.demo_protocol_privacy),
                color = Color.parseColor("#2457D6"),
                clickAction = { showShortToast(getString(R.string.demo_protocol_privacy)) }
            )
        )

        contentBinding.verifyCode.onCodeChangedListener = { code, complete ->
            contentBinding.tvStatus.text = if (complete) {
                getString(R.string.demo_verify_done, code)
            } else {
                getString(R.string.demo_verify_inputting, code.length)
            }
        }

        contentBinding.btnFillCode.setOnClickListener {
            contentBinding.verifyCode.setCode("2026")
        }
        contentBinding.btnClearCode.setOnClickListener {
            contentBinding.verifyCode.clearCode()
        }
        contentBinding.btnDialog.setOnClickListener {
            showCoreDialog()
        }
        contentBinding.btnLoading.setOnClickListener {
            val loading = LoadingDialog.show(this, getString(R.string.demo_loading))
            contentBinding.root.postDelayed({ loading.dismiss() }, 900)
        }
    }

    override fun initData() = Unit

    private fun showCoreDialog() {
        CommonDialog.showConfirm(
            context = this,
            title = getString(R.string.demo_dialog_title),
            message = getString(R.string.demo_dialog_message),
            onConfirm = {
                showShortToast(getString(R.string.demo_dialog_confirmed))
            }
        )
    }

    private fun showShortToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
