package com.wkq.corebasedemo

import android.graphics.Color
import android.widget.Toast
import com.lxj.xpopup.XPopup
import com.wkq.base.activity.BaseTitleActivity
import com.wkq.base.dialog.CommonCenterPopupView
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
        // Call the newly refactored CommonDialog.show(...) utility method directly
        CommonDialog.show(
            context = this,
            titleText = "自定义配色与事件弹框",
            contentText = "该弹框由新重构的 CommonDialog.show() 统一方法调用。支持修改文字、设定独立字体颜色、自定义按钮背景色，以及定制的回调交互。",
            cancelText = "取消操作",
            confirmText = "确认执行",
            titleColor = Color.parseColor("#1F2937"),      // 深灰字
            contentColor = Color.parseColor("#4B5563"),    // 中灰字
            cancelColor = Color.parseColor("#6B7280"),     // 取消按钮灰色字
            cancelBgColor = Color.parseColor("#F3F4F6"),   // 取消按钮浅灰背景
            confirmColor = Color.WHITE,                   // 确认按钮白字
            confirmBgColor = Color.parseColor("#DC2626"),  // 确认按钮红色背景
            onCancelClick = {
                showShortToast("点击了取消")
            },
            onConfirmClick = {
                showShortToast("点击了确认，开始执行任务")
                true // 返回 true 表示自动关闭弹框
            }
        )
    }

    private fun showShortToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
