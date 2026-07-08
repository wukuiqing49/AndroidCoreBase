package com.wkq.base.dialog

import android.content.Context
import android.view.View
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
        return DialogKit.confirm(
            context = context,
            title = title,
            message = message,
            confirmText = confirmText,
            cancelText = cancelText,
            tone = if (confirmDanger) DialogTone.ERROR else DialogTone.NORMAL,
            cancelable = cancelable,
            onCancel = onCancel,
            onConfirm = {
                onConfirm()
                true
            }
        )
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
        val tone = if (confirmDanger) DialogTone.ERROR else DialogTone.NORMAL
        return DialogKit.custom(
            context = context,
            title = title,
            contentView = contentView,
            scrollable = scrollable,
            tone = tone,
            cancelable = cancelable,
            onDismiss = onDismiss,
            actions = buildList {
                if (!neutralText.isNullOrEmpty()) {
                    add(DialogAction(neutralText, DialogActionRole.SECONDARY) {
                        onNeutral?.invoke()
                        true
                    })
                }
                if (cancelText.isNotEmpty()) {
                    add(DialogAction(cancelText, DialogActionRole.SECONDARY) {
                        onCancel?.invoke()
                        true
                    })
                }
                if (confirmText.isNotEmpty()) {
                    add(DialogAction(confirmText, if (confirmDanger) DialogActionRole.DANGER else DialogActionRole.PRIMARY) {
                        onConfirm?.invoke() ?: true
                    })
                }
            }
        )
    }

    fun showRawCenter(
        context: Context,
        contentView: View,
        cancelable: Boolean = true,
        onDismiss: (() -> Unit)? = null
    ): PopupHandle {
        return DialogKit.rawView(
            context = context,
            contentView = contentView,
            options = DialogOptions(
                cancelable = cancelable,
                widthRatio = 0.90f,
                onDismiss = onDismiss
            )
        )
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
        val content = when {
            customContentView != null -> DialogContent.Custom(customContentView, scrollable)
            !contentText.isNullOrEmpty() -> DialogContent.Text(contentText)
            else -> null
        }
        val tone = if (confirmDanger) DialogTone.ERROR else DialogTone.NORMAL
        return DialogKit.show(
            context = context,
            state = DialogState(
                title = titleText,
                content = content,
                titleColor = titleColor,
                contentColor = contentColor,
                tone = tone,
                options = DialogOptions(cancelable = cancelable),
                actions = buildList {
                    if (!neutralText.isNullOrEmpty()) {
                        add(
                            DialogAction(
                                text = neutralText,
                                role = DialogActionRole.SECONDARY,
                                textColor = neutralColor,
                                backgroundColor = neutralBgColor
                            ) {
                                onNeutralClick?.invoke()
                                true
                            }
                        )
                    }
                    if (!cancelText.isNullOrEmpty()) {
                        add(
                            DialogAction(
                                text = cancelText,
                                role = DialogActionRole.SECONDARY,
                                textColor = cancelColor,
                                backgroundColor = cancelBgColor
                            ) {
                                onCancelClick?.invoke()
                                true
                            }
                        )
                    }
                    if (!confirmText.isNullOrEmpty()) {
                        add(
                            DialogAction(
                                text = confirmText,
                                role = if (confirmDanger) DialogActionRole.DANGER else DialogActionRole.PRIMARY,
                                textColor = confirmColor,
                                backgroundColor = confirmBgColor
                            ) {
                                onConfirmClick?.invoke() ?: true
                            }
                        )
                    }
                }
            )
        )
    }
}

internal fun CenterPopupView.asHandle(): PopupHandle {
    return object : PopupHandle {
        override fun dismiss() {
            this@asHandle.dismiss()
        }

        override fun isShowing(): Boolean = this@asHandle.isShow
    }
}
