package com.wkq.base.activity

import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
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

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        refreshSystemBarAppearance()
    }

    protected open fun configureEdgeToEdge() {
        val detectStatusBarDarkMode: (Resources) -> Boolean =
            { _: Resources -> !setStatusBarDarkFont() }
        val detectNavigationBarDarkMode: (Resources) -> Boolean =
            { _: Resources -> !setNavigationBarDarkFont() }
        val navigationBarStyle = if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            shouldEnforceNavigationBarContrast()
        ) {
            /*
             * SystemBarStyle.auto keeps gesture navigation transparent while asking the platform
             * to add contrast protection for button navigation.
             */
            SystemBarStyle.auto(
                Color.TRANSPARENT,
                Color.TRANSPARENT,
                detectNavigationBarDarkMode
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            /*
             * API 29+ supports both light and dark navigation icons. Select an explicit style so
             * AndroidX keeps the bar transparent without enabling the platform's gray three-button
             * contrast scrim. The icon appearance is matched to the page by
             * setNavigationBarDarkFont(), which is the safe fallback for button visibility.
             */
            if (setNavigationBarDarkFont()) {
                SystemBarStyle.light(
                    Color.TRANSPARENT,
                    Color.argb(0x80, 0x1b, 0x1b, 0x1b)
                )
            } else {
                SystemBarStyle.dark(Color.TRANSPARENT)
            }
        } else {
            /*
             * API 24-28 cannot reliably distinguish gesture/button navigation or provide the
             * platform contrast layer. Retain a version-safe scrim because API 24-25 cannot use
             * dark navigation icons and a fully transparent light background would hide buttons.
             */
            SystemBarStyle.auto(
                Color.argb(0xe6, 0xFF, 0xFF, 0xFF),
                Color.argb(0x80, 0x1b, 0x1b, 0x1b),
                detectNavigationBarDarkMode
            )
        }
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                Color.TRANSPARENT,
                Color.TRANSPARENT,
                detectStatusBarDarkMode
            ),
            navigationBarStyle = navigationBarStyle
        )
    }

    protected open fun initViewModel() {}

    @Suppress("UNCHECKED_CAST")
    protected open fun initViewBinding() {
        val clazz = resolveGenericClass<VB>(
            instance = this,
            genericBaseClass = BaseActivity::class.java,
            index = 0,
            expectedSupertype = ViewBinding::class.java
        )
        val method = clazz.getMethod("inflate", LayoutInflater::class.java)
        binding = method.invoke(null, layoutInflater) as VB
    }

    protected open fun initImmersionBar() {
        initSystemBars()
    }

    protected open fun initSystemBars() {
        refreshSystemBarAppearance()
        applyDefaultSystemBarsInsets()
    }

    /**
     * Refreshes icon appearance without reinstalling WindowInsets listeners.
     */
    protected fun refreshSystemBarAppearance() {
        WindowCompat.getInsetsController(window, window.decorView).run {
            isAppearanceLightStatusBars = setStatusBarDarkFont()
            isAppearanceLightNavigationBars = setNavigationBarDarkFont()
        }
    }

    protected open fun applyDefaultSystemBarsInsets() {
        SystemBarInsets.applySystemBarsInset(
            view = binding.root,
            includeTop = shouldApplyStatusBarInset(),
            includeBottom = shouldApplyNavigationBarInset(),
            includeHorizontal = shouldApplyHorizontalInset(),
            includeIme = shouldApplyImeInset(),
            includeGestureInset = shouldApplyGestureInset()
        )
    }

    protected open fun shouldApplyStatusBarInset(): Boolean = true

    protected open fun shouldApplyNavigationBarInset(): Boolean = true

    protected open fun shouldApplyImeInset(): Boolean = false

    /**
     * Controls whether gesture-safe bottom insets are included with navigation bar adaptation.
     * This has no bottom effect when [shouldApplyNavigationBarInset] is false.
     */
    protected open fun shouldApplyGestureInset(): Boolean = true

    /**
     * Keeps regular content clear of landscape navigation bars and display cutouts. Override with
     * false only when the page owns its horizontal edge-to-edge layout and inset handling.
     */
    protected open fun shouldApplyHorizontalInset(): Boolean = true

    /**
     * Enable for pages whose content behind a three-button navigation bar cannot guarantee icon
     * contrast, such as photos or dynamically changing colors. The platform may add a gray scrim.
     */
    protected open fun shouldEnforceNavigationBarContrast(): Boolean = false

    protected open fun setViewBelowStatusBar(view: android.view.View) {
        SystemBarInsets.applyTopInset(view)
    }

    open fun setStatusBarDarkFont(): Boolean {
        val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags != Configuration.UI_MODE_NIGHT_YES
    }

    /**
     * Returns true for dark navigation icons on a light bottom background.
     * Override this when the bottom bar color does not follow the page theme.
     */
    protected open fun setNavigationBarDarkFont(): Boolean = setStatusBarDarkFont()

    abstract fun initView()

    abstract fun initData()
}
