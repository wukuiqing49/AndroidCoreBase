package com.wkq.base.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.wkq.base.R

class EmptyView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val ivEmpty: ImageView
    private val tvEmpty: TextView
    private val defaultImageTargetSizePx: Int

    init {
        LayoutInflater.from(context).inflate(R.layout.view_empty, this, true)
        ivEmpty = findViewById(R.id.iv_empty)
        tvEmpty = findViewById(R.id.tv_empty)
        defaultImageTargetSizePx = dpToPx(DEFAULT_EMPTY_IMAGE_TARGET_DP)

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.EmptyView)
        try {
            val imageRes = typedArray.getResourceId(R.styleable.EmptyView_emptyImage, -1)
            if (imageRes != -1) {
                setEmptyImage(imageRes)
            }

            val text = typedArray.getString(R.styleable.EmptyView_emptyText)
            setEmptyText(text)

            val textColor = typedArray.getColor(
                R.styleable.EmptyView_emptyTextColor,
                Color.parseColor("#999999")
            )
            tvEmpty.setTextColor(textColor)

            val textSize = typedArray.getDimension(R.styleable.EmptyView_emptyTextSize, -1f)
            if (textSize != -1f) {
                tvEmpty.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
            }
        } finally {
            typedArray.recycle()
        }
    }

    fun setEmptyImage(@DrawableRes resId: Int) {
        val bitmap = decodeSampledBitmapResource(
            resId = resId,
            reqWidth = resolveTargetSize(ivEmpty.layoutParams?.width),
            reqHeight = resolveTargetSize(ivEmpty.layoutParams?.height)
        )
        if (bitmap != null) {
            ivEmpty.setImageBitmap(bitmap)
        } else {
            ivEmpty.setImageResource(resId)
        }
    }

    fun setEmptyText(text: CharSequence?) {
        if (text.isNullOrEmpty()) {
            tvEmpty.visibility = View.GONE
        } else {
            tvEmpty.text = text
            tvEmpty.visibility = View.VISIBLE
        }
    }

    fun setEmptyTextColor(@ColorInt color: Int) {
        tvEmpty.setTextColor(color)
    }

    fun setEmptyTextSize(sizeSp: Float) {
        tvEmpty.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp)
    }

    fun setOnEmptyClickListener(listener: OnClickListener?) {
        this.setOnClickListener(listener)
    }

    private fun decodeSampledBitmapResource(
        @DrawableRes resId: Int,
        reqWidth: Int,
        reqHeight: Int
    ): Bitmap? = runCatching {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeResource(resources, resId, options)
        if (options.outWidth <= 0 || options.outHeight <= 0) {
            return@runCatching null
        }

        options.inSampleSize = Companion.calculateInSampleSize(options.outHeight, options.outWidth, reqWidth, reqHeight)
        options.inJustDecodeBounds = false
        BitmapFactory.decodeResource(resources, resId, options)
    }.getOrNull()

    private fun resolveTargetSize(layoutSize: Int?): Int {
        return Companion.resolveTargetSize(layoutSize, defaultImageTargetSizePx)
    }

    private fun dpToPx(value: Int): Int {
        return (value * resources.displayMetrics.density + 0.5f).toInt()
    }

    internal companion object {
        private const val DEFAULT_EMPTY_IMAGE_TARGET_DP = 160

        internal fun calculateInSampleSize(
            outHeight: Int,
            outWidth: Int,
            reqWidth: Int,
            reqHeight: Int
        ): Int {
            var inSampleSize = 1
            if (outHeight > reqHeight || outWidth > reqWidth) {
                val halfHeight = outHeight / 2
                val halfWidth = outWidth / 2

                while (halfHeight / inSampleSize >= reqHeight &&
                    halfWidth / inSampleSize >= reqWidth
                ) {
                    inSampleSize *= 2
                }
            }
            return inSampleSize
        }

        internal fun resolveTargetSize(layoutSize: Int?, defaultSizePx: Int): Int {
            return if (layoutSize != null && layoutSize > 0) {
                layoutSize
            } else {
                defaultSizePx
            }
        }
    }
}
