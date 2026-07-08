package com.wkq.base.dialog

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.CenterPopupView
import com.wkq.base.R

object DialogKit {

    /**
     * Recommended entry for daily business dialogs.
     *
     * The stable business surface is intentionally small: title, description, button text,
     * text colors, and button background colors. Caller-provided copy should already come from
     * localized string resources.
     */
    fun common(
        context: Context,
        title: CharSequence? = null,
        description: CharSequence? = null,
        cancelText: CharSequence? = context.getString(R.string.base_cancel),
        confirmText: CharSequence? = context.getString(R.string.base_confirm),
        titleColor: Int? = null,
        descriptionColor: Int? = null,
        cancelTextColor: Int? = null,
        confirmTextColor: Int? = null,
        cancelBackgroundColor: Int? = null,
        confirmBackgroundColor: Int? = null,
        cancelable: Boolean = true,
        onCancel: (() -> Unit)? = null,
        onConfirm: (() -> Boolean)? = null,
        onDismiss: (() -> Unit)? = null
    ): PopupHandle {
        val options = DialogOptions(cancelable = cancelable)
        val popupView = CommonCenterPopupView(
            context = context,
            titleText = title,
            contentText = description,
            cancelText = cancelText,
            confirmText = confirmText,
            titleColor = titleColor,
            contentColor = descriptionColor,
            cancelColor = cancelTextColor,
            confirmColor = confirmTextColor,
            cancelBgColor = cancelBackgroundColor,
            confirmBgColor = confirmBackgroundColor,
            onCancelClick = onCancel,
            onConfirmClick = onConfirm,
            popupWidthRatio = options.widthRatio,
            maxWidthDp = options.maxWidthDp,
            maxContentHeightRatio = options.maxContentHeightRatio,
            spacing = options.spacing,
            onDismissCallback = onDismiss
        )
        showCenterPopup(context, popupView, options)
        return popupView.asHandle()
    }

    /**
     * Fully configurable dialog entry for custom actions, custom content, and advanced options.
     * Prefer [common] when a page only needs a regular confirm/cancel dialog.
     */
    fun show(context: Context, state: DialogState): PopupHandle {
        val content = state.content
        val popupView = CommonCenterPopupView(
            context = context,
            titleText = state.title,
            contentText = (content as? DialogContent.Text)?.value,
            customContentView = (content as? DialogContent.Custom)?.view,
            titleColor = state.titleColor,
            contentColor = state.contentColor,
            scrollable = (content as? DialogContent.Custom)?.scrollable ?: true,
            actions = state.actions,
            tone = state.tone,
            popupWidthRatio = state.options.widthRatio,
            maxWidthDp = state.options.maxWidthDp,
            maxContentHeightRatio = state.options.maxContentHeightRatio,
            spacing = state.options.spacing,
            onDismissCallback = state.options.onDismiss
        )
        showCenterPopup(context, popupView, state.options)
        return popupView.asHandle()
    }

    /**
     * Confirm/cancel shortcut used by ViewModel events and other two-action flows.
     */
    fun confirm(
        context: Context,
        title: CharSequence,
        message: CharSequence,
        confirmText: CharSequence = context.getString(R.string.base_confirm),
        cancelText: CharSequence = context.getString(R.string.base_cancel),
        tone: DialogTone = DialogTone.NORMAL,
        cancelable: Boolean = true,
        options: DialogOptions = DialogOptions(cancelable = cancelable),
        onCancel: (() -> Unit)? = null,
        onConfirm: () -> Boolean = { true }
    ): PopupHandle {
        return show(
            context,
            DialogState(
                title = title,
                content = DialogContent.Text(message),
                tone = tone,
                options = options,
                actions = listOf(
                    DialogAction(cancelText, DialogActionRole.SECONDARY) {
                        onCancel?.invoke()
                        true
                    },
                    DialogAction(confirmText, roleForTone(tone), onClick = onConfirm)
                )
            )
        )
    }

    /**
     * One-button message dialog for neutral notices and status-style dialogs.
     */
    fun message(
        context: Context,
        title: CharSequence,
        message: CharSequence,
        confirmText: CharSequence = context.getString(R.string.base_confirm),
        tone: DialogTone = DialogTone.NORMAL,
        cancelable: Boolean = true,
        options: DialogOptions = DialogOptions(cancelable = cancelable),
        onConfirm: (() -> Unit)? = null
    ): PopupHandle {
        return show(
            context,
            DialogState(
                title = title,
                content = DialogContent.Text(message),
                tone = tone,
                options = options,
                actions = listOf(
                    DialogAction(confirmText, roleForTone(tone)) {
                        onConfirm?.invoke()
                        true
                    }
                )
            )
        )
    }

    /** Success message shortcut using the common message shell. */
    fun success(
        context: Context,
        title: CharSequence,
        message: CharSequence,
        confirmText: CharSequence = context.getString(R.string.base_confirm),
        options: DialogOptions = DialogOptions(),
        onConfirm: (() -> Unit)? = null
    ): PopupHandle {
        return message(context, title, message, confirmText, DialogTone.SUCCESS, options.cancelable, options, onConfirm)
    }

    /** Warning message shortcut using the common message shell. */
    fun warning(
        context: Context,
        title: CharSequence,
        message: CharSequence,
        confirmText: CharSequence = context.getString(R.string.base_confirm),
        options: DialogOptions = DialogOptions(),
        onConfirm: (() -> Unit)? = null
    ): PopupHandle {
        return message(context, title, message, confirmText, DialogTone.WARNING, options.cancelable, options, onConfirm)
    }

    /** Error/danger message shortcut using the common message shell. */
    fun error(
        context: Context,
        title: CharSequence,
        message: CharSequence,
        confirmText: CharSequence = context.getString(R.string.base_confirm),
        options: DialogOptions = DialogOptions(),
        onConfirm: (() -> Unit)? = null
    ): PopupHandle {
        return message(context, title, message, confirmText, DialogTone.ERROR, options.cancelable, options, onConfirm)
    }

    /**
     * Permission guide dialog. It is non-cancelable by default so the settings action is clear.
     */
    fun permission(
        context: Context,
        title: CharSequence = context.getString(R.string.permission_request_title),
        message: CharSequence,
        confirmText: CharSequence = context.getString(R.string.go_to_settings),
        cancelText: CharSequence = context.getString(R.string.base_cancel),
        cancelable: Boolean = false,
        options: DialogOptions = DialogOptions(cancelable = cancelable),
        onCancel: (() -> Unit)? = null,
        onConfirm: () -> Boolean
    ): PopupHandle {
        return show(
            context,
            DialogState(
                title = title,
                content = DialogContent.Text(message),
                tone = DialogTone.PERMISSION,
                options = options,
                actions = listOf(
                    DialogAction(cancelText, DialogActionRole.SECONDARY) {
                        onCancel?.invoke()
                        true
                    },
                    DialogAction(confirmText, DialogActionRole.PRIMARY, onClick = onConfirm)
                )
            )
        )
    }

    /**
     * Common shell with external content. DialogKit owns title, actions, max height, and scrolling;
     * caller owns the content view.
     */
    fun custom(
        context: Context,
        title: CharSequence? = null,
        contentView: View,
        actions: List<DialogAction> = emptyList(),
        scrollable: Boolean = true,
        tone: DialogTone = DialogTone.NORMAL,
        cancelable: Boolean = true,
        options: DialogOptions = DialogOptions(cancelable = cancelable),
        onDismiss: (() -> Unit)? = null
    ): PopupHandle {
        return show(
            context,
            DialogState(
                title = title,
                content = DialogContent.Custom(contentView, scrollable),
                tone = tone,
                actions = actions,
                options = options.copy(onDismiss = onDismiss ?: options.onDismiss)
            )
        )
    }

    /**
     * Raw external view dialog. Use when the caller owns the full visual layout and DialogKit only
     * provides the popup container plus [PopupHandle].
     */
    fun rawView(
        context: Context,
        contentView: View,
        options: DialogOptions = DialogOptions(widthRatio = 0.90f),
        useConfiguredWidth: Boolean = true
    ): PopupHandle {
        val popupView = DialogKitRawViewPopupView(
            context = context,
            rawContentView = contentView,
            options = options,
            useConfiguredWidth = useConfiguredWidth
        )
        showCenterPopup(context, popupView, options)
        return popupView.asHandle()
    }

    /**
     * Loading dialog. Store the returned [PopupHandle] and dismiss it when async work finishes.
     */
    fun loading(
        context: Context,
        message: CharSequence = context.getString(R.string.base_loading),
        cancelable: Boolean = false,
        options: DialogOptions = DialogOptions(
            cancelable = cancelable,
            widthRatio = 0.56f,
            maxWidthDp = 220
        ),
        onDismiss: (() -> Unit)? = null
    ): PopupHandle {
        val popupView = DialogKitLoadingPopupView(context, message, onDismiss)
        showCenterPopup(context, popupView, options)
        return popupView.asHandle()
    }

    private fun showCenterPopup(
        context: Context,
        popupView: CenterPopupView,
        options: DialogOptions
    ) {
        var builder = XPopup.Builder(context)
            .dismissOnTouchOutside(options.cancelable)
            .dismissOnBackPressed(options.cancelable)
            .moveUpToKeyboard(options.moveUpToKeyboard)
            .hasShadowBg(options.hasShadow)
            .isViewMode(options.viewMode)
            .enableDrag(options.enableDrag)
            .isLightStatusBar(options.lightStatusBar)
            .isDestroyOnDismiss(options.destroyOnDismiss)

        options.atView?.let { builder = builder.atView(it) }
        options.watchView?.let { builder = builder.watchView(it) }
        builder.asCustom(popupView).show()
    }

    private fun roleForTone(tone: DialogTone): DialogActionRole {
        return when (tone) {
            DialogTone.ERROR -> DialogActionRole.DANGER
            else -> DialogActionRole.PRIMARY
        }
    }
}

data class DialogState(
    val title: CharSequence? = null,
    val content: DialogContent? = null,
    val titleColor: Int? = null,
    val contentColor: Int? = null,
    val tone: DialogTone = DialogTone.NORMAL,
    val actions: List<DialogAction> = emptyList(),
    val options: DialogOptions = DialogOptions()
)

sealed class DialogContent {
    data class Text(val value: CharSequence) : DialogContent()
    data class Custom(val view: View, val scrollable: Boolean = true) : DialogContent()
}

data class DialogAction(
    val text: CharSequence,
    val role: DialogActionRole = DialogActionRole.SECONDARY,
    val textColor: Int? = null,
    val backgroundColor: Int? = null,
    val onClick: (() -> Boolean)? = null
)

data class DialogOptions(
    val cancelable: Boolean = true,
    val hasShadow: Boolean = true,
    val lightStatusBar: Boolean = true,
    val destroyOnDismiss: Boolean = true,
    val viewMode: Boolean = true,
    val moveUpToKeyboard: Boolean = false,
    val enableDrag: Boolean = false,
    val atView: View? = null,
    val watchView: View? = null,
    val widthRatio: Float = 0.86f,
    val maxWidthDp: Int = 460,
    val maxContentHeightRatio: Float = 0.55f,
    val spacing: DialogSpacing = DialogSpacing(),
    val onDismiss: (() -> Unit)? = null
)

/**
 * Controls the default spacing used by CommonCenterPopupView.
 *
 * The defaults are tuned for a general mobile center dialog:
 * - compact enough for short confirm dialogs;
 * - wide enough for common long descriptions;
 * - callers can still override this from DialogOptions for special business pages.
 */
data class DialogSpacing(
    /** Left and right inner padding of the dialog card. Smaller values give long text more width. */
    val horizontalPaddingDp: Int = 24,
    /** Top inner padding before the title or first content block. */
    val topPaddingDp: Int = 28,
    /** Bottom inner padding after the action area, or after content when there are no actions. */
    val bottomPaddingDp: Int = 24,
    /** Space between title and content. This is intentionally independent from card padding. */
    val contentTopWithTitleDp: Int = 16,
    /** Space above content when there is no title. */
    val contentTopWithoutTitleDp: Int = 0,
    /** Space between content and action buttons. */
    val contentBottomWithActionsDp: Int = 24,
    /** Extra space below content when no action buttons are shown. */
    val contentBottomWithoutActionsDp: Int = 0
)

enum class DialogTone {
    NORMAL,
    SUCCESS,
    WARNING,
    ERROR,
    PERMISSION
}

enum class DialogActionRole {
    PRIMARY,
    SECONDARY,
    DANGER
}

private class DialogKitLoadingPopupView(
    context: Context,
    private val message: CharSequence,
    private val onDismissCallback: (() -> Unit)?
) : CenterPopupView(context) {

    override fun addInnerContent() {
        centerPopupContainer.setBackgroundColor(Color.TRANSPARENT)
        centerPopupContainer.addView(
            LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(dp(28), dp(24), dp(28), dp(22))
                background = GradientDrawable().apply {
                    setColor(Color.WHITE)
                    cornerRadius = dp(12).toFloat()
                }
                addView(ProgressBar(context), LinearLayout.LayoutParams(dp(40), dp(40)))
                addView(
                    TextView(context).apply {
                        text = message
                        gravity = Gravity.CENTER
                        includeFontPadding = false
                        textSize = 14f
                        typeface = Typeface.DEFAULT_BOLD
                        setTextColor(Color.rgb(36, 36, 51))
                    },
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        topMargin = dp(14)
                    }
                )
            },
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
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

private class DialogKitRawViewPopupView(
    context: Context,
    private val rawContentView: View,
    private val options: DialogOptions,
    private val useConfiguredWidth: Boolean
) : CenterPopupView(context) {

    override fun addInnerContent() {
        centerPopupContainer.setBackgroundColor(Color.TRANSPARENT)
        (rawContentView.parent as? ViewGroup)?.removeView(rawContentView)
        centerPopupContainer.addView(rawContentView, createLayoutParams())
    }

    override fun onDismiss() {
        super.onDismiss()
        options.onDismiss?.invoke()
    }

    private fun createLayoutParams(): FrameLayout.LayoutParams {
        val width = if (useConfiguredWidth) {
            (resources.displayMetrics.widthPixels * options.widthRatio).toInt()
                .coerceAtMost(dp(options.maxWidthDp))
        } else {
            FrameLayout.LayoutParams.WRAP_CONTENT
        }
        return FrameLayout.LayoutParams(width, FrameLayout.LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.CENTER
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
