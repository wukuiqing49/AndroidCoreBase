package com.wkq.base.dialog

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.lxj.xpopup.core.CenterPopupView
import com.wkq.base.databinding.DialogCommonPopBinding

/**
 * Universal center dialog view based on dialog_common_pop layout.
 * Supports complete customizations of text values, font colors, button backgrounds,
 * click listeners, and custom content layouts.
 */
class CommonCenterPopupView(
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
    var customContentView: View? = null
) : CenterPopupView(context) {

    companion object {
        private val PRIMARY = Color.rgb(31, 111, 235)
        private val DANGER = Color.rgb(220, 38, 38)
        private val DIVIDER = Color.rgb(228, 231, 236)
    }

    private val binding = DialogCommonPopBinding.inflate(LayoutInflater.from(context))

    override fun addInnerContent() {
        centerPopupContainer.setBackgroundColor(Color.TRANSPARENT)
        val popupWidth = (resources.displayMetrics.widthPixels * 0.88f).toInt()
            .coerceAtMost(dp(460))
        centerPopupContainer.addView(
            binding.root,
            FrameLayout.LayoutParams(popupWidth, FrameLayout.LayoutParams.WRAP_CONTENT).apply {
                gravity = Gravity.CENTER
            }
        )
    }

    override fun onCreate() {
        super.onCreate()
        initView()
    }

    private fun initView() {
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
        val topMargin = if (titleText.isNullOrEmpty()) dp(36) else dp(32)
        if (!contentText.isNullOrEmpty()) {
            binding.tvContent.apply {
                text = contentText
                contentColor?.let { setTextColor(it) }
                visibility = View.VISIBLE
                (layoutParams as LinearLayout.LayoutParams).topMargin = topMargin
            }
            binding.flCustomContainer.visibility = View.GONE
        } else if (customContentView != null) {
            binding.tvContent.visibility = View.GONE
            binding.flCustomContainer.apply {
                visibility = View.VISIBLE
                (layoutParams as LinearLayout.LayoutParams).topMargin = topMargin
            }
            setupCustomContentView()
        } else {
            binding.tvContent.visibility = View.GONE
            binding.flCustomContainer.visibility = View.GONE
        }

        // 3. Setup Action Buttons
        setupActions()
    }

    private fun setupCustomContentView() {
        val contentView = customContentView ?: return
        (contentView.parent as? ViewGroup)?.removeView(contentView)

        binding.flCustomContainer.removeAllViews()
        if (scrollable) {
            val scrollView = ScrollView(context).apply {
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
        val buttons = mutableListOf<TextView>()

        // 1. Neutral Button
        if (!neutralText.isNullOrEmpty()) {
            val tvNeutral = TextView(context).apply {
                text = neutralText
                gravity = Gravity.CENTER
                textSize = 13f
                neutralColor?.let { setTextColor(it) } ?: setTextColor(Color.parseColor("#ff242433"))
                background = neutralBgColor?.let { roundRect(it, dp(8).toFloat()) }
                    ?: roundRect(Color.TRANSPARENT, dp(8).toFloat(), strokeColor = DIVIDER)
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
            val lp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f).apply {
                if (index < buttons.size - 1) {
                    rightMargin = dp(12) // Space between buttons
                }
            }
            binding.llActions.addView(button, lp)
        }

        binding.llActions.visibility = if (buttons.isEmpty()) View.GONE else View.VISIBLE
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
}
