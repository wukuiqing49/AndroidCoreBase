package com.wkq.base.dialog

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.CenterPopupView
import com.wkq.base.R

/**
 * CommonDialog helper object providing quick static functions to show common center popups.
 */
object CommonDialog {

    fun showConfirm(
        context: Context,
        title: String,
        message: String,
        confirmText: String = context.getString(R.string.base_confirm),
        cancelText: String = context.getString(R.string.base_cancel),
        confirmDanger: Boolean = false,
        cancelable: Boolean = true,
        onCancel: (() -> Unit)? = null,
        onConfirm: () -> Unit
    ): PopupHandle {
        val popupView = CommonCenterPopupView(
            context = context,
            titleText = title,
            contentText = message,
            cancelText = cancelText,
            confirmText = confirmText,
            confirmDanger = confirmDanger,
            onCancelClick = onCancel,
            onConfirmClick = {
                onConfirm()
                true
            }
        )
        XPopup.Builder(context)
            .dismissOnTouchOutside(cancelable)
            .dismissOnBackPressed(cancelable)
            .moveUpToKeyboard(false)
            .hasShadowBg(true)
            .isViewMode(true)
            .enableDrag(false)
            .isDestroyOnDismiss(true)
            .asCustom(popupView)
            .show()
        return popupView.asHandle()
    }

    fun showContent(
        context: Context,
        title: String,
        contentView: View,
        confirmText: String = context.getString(R.string.base_confirm),
        cancelText: String = context.getString(R.string.base_cancel),
        neutralText: String? = null,
        confirmDanger: Boolean = false,
        scrollable: Boolean = true,
        cancelable: Boolean = true,
        onConfirm: (() -> Boolean)? = null,
        onNeutral: (() -> Unit)? = null,
        onCancel: (() -> Unit)? = null,
        onDismiss: (() -> Unit)? = null
    ): PopupHandle {
        val popupView = CommonCenterPopupView(
            context = context,
            titleText = title,
            contentText = null,
            cancelText = cancelText,
            confirmText = confirmText,
            neutralText = neutralText,
            confirmDanger = confirmDanger,
            scrollable = scrollable,
            onCancelClick = onCancel,
            onConfirmClick = onConfirm,
            onNeutralClick = onNeutral,
            customContentView = contentView
        )
        XPopup.Builder(context)
            .dismissOnTouchOutside(cancelable)
            .dismissOnBackPressed(cancelable)
            .moveUpToKeyboard(false)
            .hasShadowBg(true)
            .isViewMode(true)
            .enableDrag(false)
            .isDestroyOnDismiss(true)
            .asCustom(popupView)
            .show()
        return popupView.asHandle()
    }

    fun showRawCenter(
        context: Context,
        contentView: View,
        cancelable: Boolean = true,
        onDismiss: (() -> Unit)? = null
    ): PopupHandle {
        val popupView = CommonRawCenterPopupView(context, contentView, onDismiss)
        XPopup.Builder(context)
            .dismissOnTouchOutside(cancelable)
            .dismissOnBackPressed(cancelable)
            .moveUpToKeyboard(false)
            .hasShadowBg(true)
            .isViewMode(true)
            .enableDrag(false)
            .isDestroyOnDismiss(true)
            .asCustom(popupView)
            .show()
        return popupView.asHandle()
    }

    fun show(
        context: Context,
        titleText: CharSequence? = null,
        contentText: CharSequence? = null,
        cancelText: CharSequence? = null,
        confirmText: CharSequence? = null,
        neutralText: CharSequence? = null,
        titleColor: Int? = null,
        contentColor: Int? = null,
        cancelColor: Int? = null,
        confirmColor: Int? = null,
        neutralColor: Int? = null,
        cancelBgColor: Int? = null,
        confirmBgColor: Int? = null,
        neutralBgColor: Int? = null,
        confirmDanger: Boolean = false,
        scrollable: Boolean = true,
        cancelable: Boolean = true,
        onCancelClick: (() -> Unit)? = null,
        onConfirmClick: (() -> Boolean)? = null,
        onNeutralClick: (() -> Unit)? = null,
        customContentView: View? = null
    ): PopupHandle {
        val popupView = CommonCenterPopupView(
            context = context,
            titleText = titleText,
            contentText = contentText,
            cancelText = cancelText,
            confirmText = confirmText,
            neutralText = neutralText,
            titleColor = titleColor,
            contentColor = contentColor,
            cancelColor = cancelColor,
            confirmColor = confirmColor,
            neutralColor = neutralColor,
            cancelBgColor = cancelBgColor,
            confirmBgColor = confirmBgColor,
            neutralBgColor = neutralBgColor,
            confirmDanger = confirmDanger,
            scrollable = scrollable,
            onCancelClick = onCancelClick,
            onConfirmClick = onConfirmClick,
            onNeutralClick = onNeutralClick,
            customContentView = customContentView
        )
        XPopup.Builder(context)
            .dismissOnTouchOutside(cancelable)
            .dismissOnBackPressed(cancelable)
            .moveUpToKeyboard(false)
            .hasShadowBg(true)
            .isViewMode(true)
            .enableDrag(false)
            .isDestroyOnDismiss(true)
            .asCustom(popupView)
            .show()
        return popupView.asHandle()
    }
}

private class CommonRawCenterPopupView(
    context: Context,
    private val popupContentView: View,
    private val onDismissCallback: (() -> Unit)?
) : CenterPopupView(context) {

    override fun addInnerContent() {
        centerPopupContainer.setBackgroundColor(Color.TRANSPARENT)
        (popupContentView.parent as? ViewGroup)?.removeView(popupContentView)
        val popupWidth = (resources.displayMetrics.widthPixels * 0.90f).toInt()
            .coerceAtMost(dp(460))
        centerPopupContainer.addView(
            popupContentView,
            FrameLayout.LayoutParams(popupWidth, FrameLayout.LayoutParams.WRAP_CONTENT).apply {
                gravity = Gravity.CENTER
            }
        )
    }

    override fun onDismiss() {
        super.onDismiss()
        onDismissCallback?.invoke()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}

internal fun CenterPopupView.asHandle(): PopupHandle {
    return object : PopupHandle {
        override fun dismiss() {
            this@asHandle.dismiss()
        }

        override fun isShowing(): Boolean = this@asHandle.isShow
    }
}
