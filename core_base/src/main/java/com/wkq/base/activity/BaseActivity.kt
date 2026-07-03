package com.wkq.base.activity

import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.viewbinding.ViewBinding
import com.wkq.base.insets.SystemBarInsets
import com.wkq.base.reflect.resolveGenericClass

abstract class BaseActivity<VB : ViewBinding> : PermissionsActivity() {

    protected lateinit var binding: VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initViewBinding()
        configureEdgeToEdge()
        setContentView(binding.root)

        initViewModel()
        initImmersionBar()
        initView()
        initData()
    }

    protected open fun configureEdgeToEdge() {
        val detectDarkMode: (Resources) -> Boolean = { _: Resources -> !setStatusBarDarkFont() }
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                Color.TRANSPARENT,
                Color.TRANSPARENT,
                detectDarkMode
            ),
            navigationBarStyle = SystemBarStyle.auto(
                Color.argb(0xe6, 0xFF, 0xFF, 0xFF),
                Color.argb(0x80, 0x1b, 0x1b, 0x1b),
                detectDarkMode
            )
        )
    }

    protected open fun initViewModel() {}

    @Suppress("UNCHECKED_CAST")
    protected open fun initViewBinding() {
        val clazz = resolveGenericClass<VB>(this, 0)
        val method = clazz.getMethod("inflate", LayoutInflater::class.java)
        binding = method.invoke(null, layoutInflater) as VB
    }

    protected open fun initImmersionBar() {
        initSystemBars()
    }

    protected open fun initSystemBars() {
        val useDarkIcons = setStatusBarDarkFont()
        WindowCompat.getInsetsController(window, window.decorView).run {
            isAppearanceLightStatusBars = useDarkIcons
            isAppearanceLightNavigationBars = useDarkIcons
        }
        applyDefaultSystemBarsInsets()
    }

    protected open fun applyDefaultSystemBarsInsets() {
        SystemBarInsets.applySystemBarsInset(binding.root)
    }

    protected open fun setViewBelowStatusBar(view: android.view.View) {
        SystemBarInsets.applyTopInset(view)
    }

    open fun setStatusBarDarkFont(): Boolean {
        val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags != Configuration.UI_MODE_NIGHT_YES
    }

    abstract fun initView()

    abstract fun initData()
}
