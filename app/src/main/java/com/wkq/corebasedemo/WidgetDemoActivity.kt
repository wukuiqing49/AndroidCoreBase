package com.wkq.corebasedemo

import android.graphics.Color
import android.widget.Toast
import com.wkq.base.activity.BaseTitleActivity
import com.wkq.base.dialog.DialogKit
import com.wkq.base.widget.MultiSpanTextView
import com.wkq.corebasedemo.databinding.ActivityWidgetDemoBinding

class WidgetDemoActivity : BaseTitleActivity<ActivityWidgetDemoBinding>() {

    override fun initView() {
        showTitleFullScreen()
        setLeftVisible(true)
        setLeftIcon(com.wkq.base.R.mipmap.ic_toolbar_back_black) { finish() }
        setPageTitle(getString(R.string.demo_widget_page_name))
        setPageTitleColor(Color.parseColor("#182033"))
        setRightText(getString(R.string.demo_right_action)) {
            showCoreDialog()
        }
        setRightTextColor(Color.parseColor("#2457D6"))

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
            val loading = DialogKit.loading(this, getString(R.string.demo_loading))
            contentBinding.root.postDelayed({ loading.dismiss() }, 900)
        }
    }

    override fun initData() = Unit

    private fun showCoreDialog() {
        DialogKit.common(
            context = this,
            title = "自定义配色弹框",
            description = "该弹框由 DialogKit.common() 统一出口调用。支持标题、简介、按钮文字、字体颜色和按钮背景色配置。",
            cancelText = "取消操作",
            confirmText = "确认执行",
            titleColor = Color.parseColor("#1F2937"),
            descriptionColor = Color.parseColor("#4B5563"),
            cancelTextColor = Color.parseColor("#6B7280"),
            cancelBackgroundColor = Color.parseColor("#F3F4F6"),
            confirmTextColor = Color.WHITE,
            confirmBackgroundColor = Color.parseColor("#DC2626"),
            onCancel = {
                showShortToast("点击了取消")
            },
            onConfirm = {
                showShortToast("点击了确认，开始执行任务")
                true
            }
        )
    }

    private fun showShortToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
