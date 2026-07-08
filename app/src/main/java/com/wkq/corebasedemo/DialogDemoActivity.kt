package com.wkq.corebasedemo

import android.graphics.Color
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.wkq.base.activity.BaseTitleActivity
import com.wkq.base.dialog.DialogAction
import com.wkq.base.dialog.DialogActionRole
import com.wkq.base.dialog.DialogKit
import com.wkq.corebasedemo.databinding.ActivityDialogDemoBinding

class DialogDemoActivity : BaseTitleActivity<ActivityDialogDemoBinding>() {

    override fun initView() {
        // Keep this page close to real business usage: only call DialogKit from click handlers.
        showTitleFullScreen()
        setLeftVisible(true)
        setLeftIcon(com.wkq.base.R.mipmap.ic_toolbar_back_black) { finish() }
        setPageTitle(getString(R.string.demo_dialog_page_name))
        setPageTitleColor(Color.parseColor("#182033"))

        contentBinding.btnCommon.setOnClickListener {
            // Daily business dialog: title, description, button text and color are the intended surface.
            DialogKit.common(
                context = this,
                title = getString(R.string.demo_dialog_common_title),
                description = getString(R.string.demo_dialog_common_desc),
                onConfirm = {
                    showToast(getString(R.string.demo_dialog_confirm_toast))
                    true
                }
            )
        }

        contentBinding.btnColor.setOnClickListener {
            // Visual regression sample for external text and button color overrides.
            DialogKit.common(
                context = this,
                title = getString(R.string.demo_dialog_color_title),
                description = getString(R.string.demo_dialog_color_desc),
                cancelText = getString(R.string.demo_action_close),
                confirmText = getString(R.string.demo_action_done),
                titleColor = Color.parseColor("#1F2937"),
                descriptionColor = Color.parseColor("#4B5563"),
                cancelTextColor = Color.parseColor("#526070"),
                confirmTextColor = Color.WHITE,
                cancelBackgroundColor = Color.parseColor("#EEF2F7"),
                confirmBackgroundColor = Color.parseColor("#2457D6")
            )
        }

        contentBinding.btnLong.setOnClickListener {
            // Long copy should scroll inside the content area without pushing actions off screen.
            DialogKit.common(
                context = this,
                title = getString(R.string.demo_dialog_long_title),
                description = getString(R.string.demo_dialog_long_desc),
                confirmText = getString(R.string.demo_action_done),
                cancelText = null
            )
        }

        contentBinding.btnLoading.setOnClickListener {
            // Loading returns a PopupHandle so callers can dismiss it from async callbacks.
            val loading = DialogKit.loading(this, getString(R.string.demo_loading))
            contentBinding.root.postDelayed({ loading.dismiss() }, 1000)
        }

        contentBinding.btnPermission.setOnClickListener {
            // Permission dialogs are non-cancelable by default and guide the user to settings.
            DialogKit.permission(
                context = this,
                title = getString(R.string.demo_dialog_permission_title),
                message = getString(R.string.demo_dialog_permission_desc),
                onConfirm = {
                    showToast(getString(R.string.demo_dialog_permission_toast))
                    true
                }
            )
        }

        contentBinding.btnCustom.setOnClickListener {
            // Custom keeps the common title/action shell and lets business code own the content view.
            DialogKit.custom(
                context = this,
                title = getString(R.string.demo_dialog_custom_title),
                contentView = createCustomContent(),
                actions = listOf(
                    DialogAction(getString(R.string.cancel), DialogActionRole.SECONDARY),
                    DialogAction(getString(R.string.demo_action_done), DialogActionRole.PRIMARY) {
                        showToast(getString(R.string.demo_dialog_confirm_toast))
                        true
                    }
                )
            )
        }

        contentBinding.btnRaw.setOnClickListener {
            // Raw view is for fully-owned layouts; DialogKit only provides the popup container.
            DialogKit.rawView(
                context = this,
                contentView = createRawContent()
            )
        }
    }

    override fun initData() = Unit

    private fun createCustomContent(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(label(getString(R.string.demo_dialog_custom_line_1), 16f, "#182033", true))
            addView(label(getString(R.string.demo_dialog_custom_line_2), 14f, "#5E6678", false).apply {
                setPadding(0, dp(10), 0, 0)
            })
        }
    }

    private fun createRawContent(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(24), dp(22), dp(24), dp(20))
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dp(18).toFloat()
            }
            addView(label(getString(R.string.demo_dialog_raw_title), 18f, "#182033", true))
            addView(label(getString(R.string.demo_dialog_raw_desc), 14f, "#5E6678", false).apply {
                gravity = Gravity.CENTER
                setPadding(0, dp(10), 0, 0)
            })
        }
    }

    private fun label(textValue: String, sizeSp: Float, color: String, bold: Boolean): TextView {
        return TextView(this).apply {
            text = textValue
            textSize = sizeSp
            setTextColor(Color.parseColor(color))
            includeFontPadding = false
            if (bold) {
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
