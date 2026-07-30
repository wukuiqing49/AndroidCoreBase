package com.wkq.base.dialog

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.lxj.xpopup.core.CenterPopupView
import com.wkq.base.R
import com.wkq.base.databinding.DialogCommonPopBinding

/**
 * Universal center dialog view based on dialog_common_pop layout.
 * Supports complete customizations of text values, font colors, button backgrounds,
 * click listeners, and custom content layouts.
 */
internal class CommonCenterPopupView(
    context: Context,
    var titleText: CharSequence? = null,
    var contentText: CharSequence? = null,
    var cancelText: CharSequence? = null,
    var confirmText: CharSequence? = null,
    var neutralText: CharSequence? = null,
    var titleColor: Int? = null,
    var contentColor: Int? = null,
    var cancelColor: Int? = null,
    var confirmColor: Int? = null,
    var neutralColor: Int? = null,
    var cancelBgColor: Int? = null,
    var confirmBgColor: Int? = null,
    var neutralBgColor: Int? = null,
    var confirmDanger: Boolean = false,
    var scrollable: Boolean = true,
    var onCancelClick: (() -> Unit)? = null,
    var onConfirmClick: (() -> Boolean)? = null,
    var onNeutralClick: (() -> Unit)? = null,
    var customContentView: View? = null,
    var actions: List<DialogAction>? = null,
    var tone: DialogTone = DialogTone.NORMAL,
    var popupWidthRatio: Float = 0.88f,
    var maxWidthDp: Int = 460,
    var maxContentHeightRatio: Float = 0.55f,
    var spacing: DialogSpacing = DialogSpacing(),
    var onDismissCallback: (() -> Unit)? = null
) : CenterPopupView(context) {

    companion object {
        private val PRIMARY = Color.rgb(31, 111, 235)
        private val DANGER = Color.rgb(220, 38, 38)
        private val DIVIDER = Color.rgb(228, 231, 236)
    }

    private lateinit var binding: DialogCommonPopBinding

    override val implLayoutId: Int
        get() = R.layout.dialog_common_pop

    override val maxWidth: Int
        get() = (resources.displayMetrics.widthPixels * popupWidthRatio).toInt()
            .coerceAtMost(dp(maxWidthDp))

    override fun onCreate() {
        super.onCreate()
        centerPopupContainer.setBackgroundColor(Color.TRANSPARENT)
        binding = DialogCommonPopBinding.bind(requireNotNull(contentView))
        initView()
    }

    override fun onDismiss() {
        super.onDismiss()
        onDismissCallback?.invoke()
    }

    private fun initView() {
        binding.root.setPadding(
            dp(spacing.horizontalPaddingDp),
            dp(spacing.topPaddingDp),
            dp(spacing.horizontalPaddingDp),
            dp(spacing.bottomPaddingDp)
        )

        // 1. Setup Title
        if (titleText.isNullOrEmpty()) {
            binding.tvTitle.visibility = View.GONE
        } else {
            binding.tvTitle.apply {
                text = titleText
                titleColor?.let { setTextColor(it) }
                visibility = View.VISIBLE
            }
        }

        // 2. Setup Content
        val topMargin = if (titleText.isNullOrEmpty()) {
            dp(spacing.contentTopWithoutTitleDp)
        } else {
            dp(spacing.contentTopWithTitleDp)
        }
        val bottomMargin = if (hasVisibleActions()) {
            dp(spacing.contentBottomWithActionsDp)
        } else {
            dp(spacing.contentBottomWithoutActionsDp)
        }
        if (!contentText.isNullOrEmpty()) {
            binding.tvContent.visibility = View.GONE
            binding.flCustomContainer.apply {
                visibility = View.VISIBLE
                (layoutParams as LinearLayout.LayoutParams).apply {
                    this.topMargin = topMargin
                    this.bottomMargin = bottomMargin
                }
            }
            setupTextContentView(contentText!!)
        } else if (customContentView != null) {
            binding.tvContent.visibility = View.GONE
            binding.flCustomContainer.apply {
                visibility = View.VISIBLE
                (layoutParams as LinearLayout.LayoutParams).apply {
                    this.topMargin = topMargin
                    this.bottomMargin = bottomMargin
                }
            }
            setupCustomContentView()
        } else {
            binding.tvContent.visibility = View.GONE
            binding.flCustomContainer.visibility = View.GONE
        }

        // 3. Setup Action Buttons
        setupActions()
    }

    private fun hasVisibleActions(): Boolean {
        val configuredActions = actions
        if (configuredActions != null) {
            return configuredActions.any { it.text.isNotBlank() }
        }
        return !neutralText.isNullOrEmpty() || !cancelText.isNullOrEmpty() || !confirmText.isNullOrEmpty()
    }

    private fun setupTextContentView(textValue: CharSequence) {
        binding.flCustomContainer.removeAllViews()
        val textView = TextView(context).apply {
            text = textValue
            gravity = if (textValue.length > 80) Gravity.START else Gravity.CENTER
            includeFontPadding = false
            textSize = 14f
            setTextColor(contentColor ?: Color.parseColor("#ff5c5c66"))
            setLineSpacing(0f, 1.2f)
        }
        binding.flCustomContainer.addView(
            MaxHeightScrollView(context, maxContentHeight()).apply {
                isFillViewport = false
                addView(
                    textView,
                    FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                    )
                )
            },
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        )
    }

    private fun setupCustomContentView() {
        val contentView = customContentView ?: return
        (contentView.parent as? ViewGroup)?.removeView(contentView)

        binding.flCustomContainer.removeAllViews()
        if (scrollable) {
            val scrollView = MaxHeightScrollView(context, maxContentHeight()).apply {
                isFillViewport = false
                addView(
                    contentView,
                    FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                    )
                )
            }
            binding.flCustomContainer.addView(
                scrollView,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                )
            )
        } else {
            binding.flCustomContainer.addView(
                contentView,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                )
            )
        }
    }

    private fun setupActions() {
        val configuredActions = actions
        if (configuredActions != null) {
            setupConfiguredActions(configuredActions)
            return
        }

        val buttons = mutableListOf<TextView>()

        // 1. Neutral Button
        if (!neutralText.isNullOrEmpty()) {
            val tvNeutral = TextView(context).apply {
                text = neutralText
                gravity = Gravity.CENTER
                textSize = 14f
                neutralColor?.let { setTextColor(it) } ?: setTextColor(Color.parseColor("#ff242433"))
                background = neutralBgColor?.let { roundRect(it, dp(8).toFloat()) }
                    ?: roundRect(Color.TRANSPARENT, dp(8).toFloat(), strokeColor = DIVIDER)
                minHeight = dp(44)
                maxLines = 2
                setPadding(dp(12), dp(6), dp(12), dp(6))
                setOnClickListener {
                    onNeutralClick?.invoke()
                    dismiss()
                }
            }
            buttons.add(tvNeutral)
        }

        // 2. Cancel Button (tv_left)
        if (!cancelText.isNullOrEmpty()) {
            binding.tvLeft.apply {
                text = cancelText
                cancelColor?.let { setTextColor(it) }
                background = cancelBgColor?.let { roundRect(it, dp(8).toFloat()) }
                    ?: roundRect(Color.TRANSPARENT, dp(8).toFloat(), strokeColor = DIVIDER)
                setOnClickListener {
                    onCancelClick?.invoke()
                    dismiss()
                }
                visibility = View.VISIBLE
            }
            buttons.add(binding.tvLeft)
        } else {
            binding.tvLeft.visibility = View.GONE
        }

        // 3. Confirm Button (tv_right)
        if (!confirmText.isNullOrEmpty()) {
            val defaultBtnColor = if (confirmDanger) DANGER else PRIMARY
            binding.tvRight.apply {
                text = confirmText
                confirmColor?.let { setTextColor(it) }
                background = confirmBgColor?.let { roundRect(it, dp(8).toFloat()) }
                    ?: roundRect(defaultBtnColor, dp(8).toFloat())
                setOnClickListener {
                    val shouldDismiss = onConfirmClick?.invoke() ?: true
                    if (shouldDismiss) dismiss()
                }
                visibility = View.VISIBLE
            }
            buttons.add(binding.tvRight)
        } else {
            binding.tvRight.visibility = View.GONE
        }

        // Clear and rebuild ll_actions
        binding.llActions.removeAllViews()
        buttons.forEachIndexed { index, button ->
            (button.parent as? ViewGroup)?.removeView(button)
            val lp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                if (index < buttons.size - 1) {
                    rightMargin = dp(12) // Space between buttons
                }
            }
            button.minHeight = dp(44)
            button.maxLines = 2
            button.setPadding(dp(12), dp(6), dp(12), dp(6))
            binding.llActions.addView(button, lp)
        }

        binding.llActions.visibility = if (buttons.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun setupConfiguredActions(configuredActions: List<DialogAction>) {
        val visibleActions = configuredActions.filter { it.text.isNotBlank() }
        binding.llActions.removeAllViews()
        binding.llActions.orientation = if (visibleActions.size <= 2) {
            LinearLayout.HORIZONTAL
        } else {
            LinearLayout.VERTICAL
        }

        visibleActions.forEachIndexed { index, action ->
            val button = TextView(context).apply {
                text = action.text
                gravity = Gravity.CENTER
                includeFontPadding = false
                textSize = 14f
                minHeight = dp(44)
                maxLines = 2
                setPadding(dp(12), dp(6), dp(12), dp(6))
                setTextColor(action.textColor ?: textColorForRole(action.role))
                background = action.backgroundColor?.let { roundRect(it, dp(8).toFloat()) }
                    ?: backgroundForRole(action.role)
                setOnClickListener {
                    val shouldDismiss = action.onClick?.invoke() ?: true
                    if (shouldDismiss) dismiss()
                }
            }
            val lp = if (visibleActions.size <= 2) {
                LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    if (index < visibleActions.lastIndex) rightMargin = dp(12)
                }
            } else {
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    if (index > 0) topMargin = dp(10)
                }
            }
            binding.llActions.addView(button, lp)
        }

        binding.llActions.visibility = if (visibleActions.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun backgroundForRole(role: DialogActionRole): GradientDrawable {
        return when (role) {
            DialogActionRole.PRIMARY -> roundRect(colorForTone(tone), dp(8).toFloat())
            DialogActionRole.DANGER -> roundRect(DANGER, dp(8).toFloat())
            DialogActionRole.SECONDARY -> roundRect(Color.TRANSPARENT, dp(8).toFloat(), strokeColor = DIVIDER)
        }
    }

    private fun textColorForRole(role: DialogActionRole): Int {
        return when (role) {
            DialogActionRole.PRIMARY, DialogActionRole.DANGER -> Color.WHITE
            DialogActionRole.SECONDARY -> Color.parseColor("#ff242433")
        }
    }

    private fun colorForTone(tone: DialogTone): Int {
        return when (tone) {
            DialogTone.NORMAL -> PRIMARY
            DialogTone.SUCCESS -> Color.rgb(22, 163, 74)
            DialogTone.WARNING -> Color.rgb(217, 119, 6)
            DialogTone.ERROR -> DANGER
            DialogTone.PERMISSION -> PRIMARY
        }
    }

    private fun roundRect(color: Int, radius: Float, strokeColor: Int = Color.TRANSPARENT): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius
            if (strokeColor != Color.TRANSPARENT) {
                setStroke(dp(1), strokeColor)
            }
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun maxContentHeight(): Int {
        return (resources.displayMetrics.heightPixels * maxContentHeightRatio).toInt()
    }
}

private class MaxHeightScrollView(
    context: Context,
    private val maxHeight: Int
) : ScrollView(context) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val limitedHeightSpec = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST)
        super.onMeasure(widthMeasureSpec, limitedHeightSpec)
    }
}
