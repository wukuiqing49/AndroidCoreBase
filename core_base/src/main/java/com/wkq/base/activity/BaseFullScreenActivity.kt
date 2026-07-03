package com.wkq.base.activity

import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.viewbinding.ViewBinding

abstract class BaseFullScreenActivity<VB : ViewBinding> : BaseActivity<VB>() {

    override fun applyDefaultSystemBarsInsets() = Unit

    override fun initImmersionBar() {
        WindowCompat.getInsetsController(window, window.decorView).run {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        findViewById<android.view.ViewGroup>(android.R.id.content)?.getChildAt(0)?.let {
            it.fitsSystemWindows = false
        }
    }
}
