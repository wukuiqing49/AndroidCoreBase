package com.wkq.base.insets

import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.WeakHashMap

object SystemBarInsets {

    private val initialStates = WeakHashMap<View, InitialViewState>()
    private val pendingAttachListeners =
        WeakHashMap<View, View.OnAttachStateChangeListener>()

    /**
     * Removes the listener installed by this helper. When requested, the View is restored to the
     * padding, height and bottom margin captured by its first Insets registration.
     */
    fun clearInsets(
        view: View,
        restoreInitialState: Boolean = true
    ) {
        val initialState = initialStates.remove(view) ?: return
        ViewCompat.setOnApplyWindowInsetsListener(view, null)
        pendingAttachListeners.remove(view)?.let(view::removeOnAttachStateChangeListener)
        if (!restoreInitialState) return

        view.applyPadding(initialState.padding)
        initialState.height?.let { view.updateHeight(it) }
        val params = view.layoutParams as? ViewGroup.MarginLayoutParams
        if (params != null && initialState.bottomMargin != null) {
            params.bottomMargin = initialState.bottomMargin
            view.layoutParams = params
        }
    }

    /**
     * Updates the business padding baseline without including any WindowInsets.
     * Use this instead of setPadding after an Insets helper has already been installed.
     */
    fun updateInitialPadding(
        view: View,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ) {
        val current = view.initialState()
        initialStates[view] = current.copy(
            padding = PaddingSnapshot(
                left = left,
                top = top,
                right = right,
                bottom = bottom,
                start = if (view.layoutDirection == View.LAYOUT_DIRECTION_RTL) right else left,
                end = if (view.layoutDirection == View.LAYOUT_DIRECTION_RTL) left else right,
                isRelative = false
            )
        )
        view.setPadding(left, top, right, bottom)
        view.requestApplyInsetsWhenAttached()
    }

    /**
     * Relative-padding counterpart of [updateInitialPadding].
     */
    fun updateInitialPaddingRelative(
        view: View,
        start: Int,
        top: Int,
        end: Int,
        bottom: Int
    ) {
        val isRtl = view.layoutDirection == View.LAYOUT_DIRECTION_RTL
        val current = view.initialState()
        initialStates[view] = current.copy(
            padding = PaddingSnapshot(
                left = if (isRtl) end else start,
                top = top,
                right = if (isRtl) start else end,
                bottom = bottom,
                start = start,
                end = end,
                isRelative = true
            )
        )
        view.setPaddingRelative(start, top, end, bottom)
        view.requestApplyInsetsWhenAttached()
    }

    fun applySystemBarsInset(
        view: View,
        includeTop: Boolean = true,
        includeBottom: Boolean = true,
        includeHorizontal: Boolean = false,
        includeIme: Boolean = false,
        includeGestureInset: Boolean = true
    ) {
        val initial = view.initialState().padding
        ViewCompat.setOnApplyWindowInsetsListener(view) { target, insets ->
            val topInset = if (includeTop) {
                insets.getInsets(
                    WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.displayCutout()
                ).top
            } else {
                0
            }
            val bottomInset = insets.resolveBottomInset(
                includeNavigationBar = includeBottom,
                includeIme = includeIme,
                includeGestureInset = includeBottom && includeGestureInset
            )
            val horizontalInset = if (includeHorizontal) {
                // Regular content only needs safe-drawing insets. Edge gestures are handled by
                // applyHorizontalGestureInset() for controls that actually sit on a screen edge.
                insets.resolveHorizontalInset(includeGestureInset = false)
            } else {
                null
            }
            target.applyPadding(
                initial = initial,
                addedLeft = horizontalInset?.left ?: 0,
                addedTop = topInset,
                addedRight = horizontalInset?.right ?: 0,
                addedBottom = bottomInset
            )
            insets
        }
        view.requestApplyInsetsWhenAttached()
    }

    fun applyTopInset(
        view: View,
        resizeHeight: Boolean = true
    ) {
        val initialState = view.initialState()
        val initial = initialState.padding
        val initialHeight = initialState.height
        ViewCompat.setOnApplyWindowInsetsListener(view) { target, insets ->
            val topInset = insets.getInsets(
                WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.displayCutout()
            ).top
            if (resizeHeight && initialHeight != null && initialHeight > 0) {
                target.updateHeight(initialHeight + topInset)
            }
            target.applyPadding(
                initial = initial,
                addedTop = topInset
            )
            insets
        }
        view.requestApplyInsetsWhenAttached()
    }

    fun applyBottomInset(
        view: View,
        resizeHeight: Boolean = false,
        includeIme: Boolean = false,
        extraBottom: Int = 0,
        includeGestureInset: Boolean = true
    ) {
        val initialState = view.initialState()
        val initial = initialState.padding
        val initialHeight = initialState.height
        ViewCompat.setOnApplyWindowInsetsListener(view) { target, insets ->
            val bottomInset = insets.resolveBottomInset(
                includeNavigationBar = true,
                includeIme = includeIme,
                includeGestureInset = includeGestureInset
            )
            if (resizeHeight && initialHeight != null && initialHeight > 0) {
                target.updateHeight(initialHeight + bottomInset)
            }
            target.applyPadding(
                initial = initial,
                addedBottom = bottomInset + extraBottom
            )
            insets
        }
        view.requestApplyInsetsWhenAttached()
    }

    fun applyScrollableBottomInset(
        view: View,
        extraBottom: Int = 0,
        includeIme: Boolean = false,
        includeGestureInset: Boolean = true
    ) {
        (view as? ViewGroup)?.clipToPadding = false
        applyBottomInset(
            view = view,
            resizeHeight = false,
            includeIme = includeIme,
            extraBottom = extraBottom,
            includeGestureInset = includeGestureInset
        )
    }

    fun applyBottomMarginInset(
        view: View,
        includeIme: Boolean = false,
        extraBottom: Int = 0,
        includeGestureInset: Boolean = true
    ) {
        val params = view.layoutParams as? ViewGroup.MarginLayoutParams ?: return
        val initialBottomMargin = view.initialState().bottomMargin ?: params.bottomMargin
        ViewCompat.setOnApplyWindowInsetsListener(view) { target, insets ->
            val targetParams = target.layoutParams as? ViewGroup.MarginLayoutParams
            if (targetParams != null) {
                val bottomMargin = initialBottomMargin + insets.resolveBottomInset(
                    includeNavigationBar = true,
                    includeIme = includeIme,
                    includeGestureInset = includeGestureInset
                ) + extraBottom
                if (targetParams.bottomMargin != bottomMargin) {
                    targetParams.bottomMargin = bottomMargin
                    target.layoutParams = targetParams
                }
            }
            insets
        }
        view.requestApplyInsetsWhenAttached()
    }

    /**
     * 为左右侧滑返回手势预留安全区，适合横向滑动组件或贴边操作按钮。
     *
     * 注意：不要全局无脑套在所有根布局上，否则内容会被过度收窄。
     */
    fun applyHorizontalGestureInset(
        view: View,
        applyLeft: Boolean = true,
        applyRight: Boolean = true,
        extraLeft: Int = 0,
        extraRight: Int = 0
    ) {
        val initial = view.initialState().padding
        ViewCompat.setOnApplyWindowInsetsListener(view) { target, insets ->
            val systemGestures = insets.getInsets(WindowInsetsCompat.Type.systemGestures())
            val mandatoryGestures =
                insets.getInsets(WindowInsetsCompat.Type.mandatorySystemGestures())
            target.applyPadding(
                initial = initial,
                addedLeft = if (applyLeft) {
                    maxOf(systemGestures.left, mandatoryGestures.left) + extraLeft
                } else {
                    0
                },
                addedRight = if (applyRight) {
                    maxOf(systemGestures.right, mandatoryGestures.right) + extraRight
                } else {
                    0
                }
            )
            insets
        }
        view.requestApplyInsetsWhenAttached()
    }

    private fun WindowInsetsCompat.resolveBottomInset(
        includeNavigationBar: Boolean,
        includeIme: Boolean,
        includeGestureInset: Boolean
    ): Int {
        val navigationBottom = if (includeNavigationBar) {
            getInsets(
                WindowInsetsCompat.Type.navigationBars() or
                    WindowInsetsCompat.Type.displayCutout()
            ).bottom
        } else {
            0
        }
        val gestureBottom = if (includeGestureInset) {
            maxOf(
                getInsets(WindowInsetsCompat.Type.systemGestures()).bottom,
                getInsets(WindowInsetsCompat.Type.mandatorySystemGestures()).bottom
            )
        } else {
            0
        }
        val imeBottom = if (includeIme) {
            getInsets(WindowInsetsCompat.Type.ime()).bottom
        } else {
            0
        }
        return maxOf(navigationBottom, gestureBottom, imeBottom)
    }

    private fun WindowInsetsCompat.resolveHorizontalInset(
        includeGestureInset: Boolean
    ): HorizontalInset {
        val systemBars = getInsets(
            WindowInsetsCompat.Type.navigationBars() or WindowInsetsCompat.Type.displayCutout()
        )
        if (!includeGestureInset) {
            return HorizontalInset(systemBars.left, systemBars.right)
        }
        val systemGestures = getInsets(WindowInsetsCompat.Type.systemGestures())
        val mandatoryGestures = getInsets(WindowInsetsCompat.Type.mandatorySystemGestures())
        return HorizontalInset(
            left = maxOf(systemBars.left, systemGestures.left, mandatoryGestures.left),
            right = maxOf(systemBars.right, systemGestures.right, mandatoryGestures.right)
        )
    }

    private fun View.initialState(): InitialViewState {
        return initialStates.getOrPut(this) {
            captureInitialState()
        }
    }

    private fun View.captureInitialState(): InitialViewState {
        val params = layoutParams
        return InitialViewState(
            padding = PaddingSnapshot(
                left = paddingLeft,
                top = paddingTop,
                right = paddingRight,
                bottom = paddingBottom,
                start = paddingStart,
                end = paddingEnd,
                isRelative = isPaddingRelative
            ),
            height = params?.height,
            bottomMargin = (params as? ViewGroup.MarginLayoutParams)?.bottomMargin
        )
    }

    private fun View.applyPadding(
        initial: PaddingSnapshot,
        addedLeft: Int = 0,
        addedTop: Int = 0,
        addedRight: Int = 0,
        addedBottom: Int = 0
    ) {
        if (initial.isRelative) {
            val addedStart = if (layoutDirection == View.LAYOUT_DIRECTION_RTL) {
                addedRight
            } else {
                addedLeft
            }
            val addedEnd = if (layoutDirection == View.LAYOUT_DIRECTION_RTL) {
                addedLeft
            } else {
                addedRight
            }
            setPaddingRelative(
                initial.start + addedStart,
                initial.top + addedTop,
                initial.end + addedEnd,
                initial.bottom + addedBottom
            )
        } else {
            setPadding(
                initial.left + addedLeft,
                initial.top + addedTop,
                initial.right + addedRight,
                initial.bottom + addedBottom
            )
        }
    }

    private fun View.requestApplyInsetsWhenAttached() {
        if (isAttachedToWindow) {
            ViewCompat.requestApplyInsets(this)
            return
        }
        if (pendingAttachListeners.containsKey(this)) return

        val listener = object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                pendingAttachListeners.remove(v)
                v.removeOnAttachStateChangeListener(this)
                ViewCompat.requestApplyInsets(v)
            }

            override fun onViewDetachedFromWindow(v: View) = Unit
        }
        pendingAttachListeners[this] = listener
        addOnAttachStateChangeListener(listener)
    }

    private fun View.updateHeight(height: Int) {
        val params = layoutParams ?: return
        if (params.height == height) return
        params.height = height
        layoutParams = params
    }

    private data class PaddingSnapshot(
        val left: Int,
        val top: Int,
        val right: Int,
        val bottom: Int,
        val start: Int,
        val end: Int,
        val isRelative: Boolean
    )

    private data class InitialViewState(
        val padding: PaddingSnapshot,
        val height: Int?,
        val bottomMargin: Int?
    )

    private data class HorizontalInset(
        val left: Int,
        val right: Int
    )
}
