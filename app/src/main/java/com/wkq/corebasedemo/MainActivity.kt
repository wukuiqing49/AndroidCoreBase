package com.wkq.corebasedemo

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.wkq.base.activity.BaseTitleActivity
import com.wkq.corebasedemo.databinding.ActivityMainBinding

class MainActivity : BaseTitleActivity<ActivityMainBinding>() {

    override fun initView() {
        setPageTitle(getString(R.string.demo_home_title))
        setLeftVisible(false)

        bindDemoRow(
            row = contentBinding.rowTitle,
            iconView = contentBinding.ivTitleIcon,
            titleView = contentBinding.tvTitleName,
            summaryView = contentBinding.tvTitleDesc,
            iconRes = R.drawable.ic_md_title,
            title = getString(R.string.demo_title_page_name),
            summary = getString(R.string.demo_title_page_desc),
            target = TitleDemoActivity::class.java
        )
        bindDemoRow(
            row = contentBinding.rowVm,
            iconView = contentBinding.ivVmIcon,
            titleView = contentBinding.tvVmName,
            summaryView = contentBinding.tvVmDesc,
            iconRes = R.drawable.ic_md_state,
            title = getString(R.string.demo_vm_page_name),
            summary = getString(R.string.demo_vm_page_desc),
            target = VmStateDemoActivity::class.java
        )
        bindDemoRow(
            row = contentBinding.rowList,
            iconView = contentBinding.ivListIcon,
            titleView = contentBinding.tvListName,
            summaryView = contentBinding.tvListDesc,
            iconRes = R.drawable.ic_md_list,
            title = getString(R.string.demo_list_page_name),
            summary = getString(R.string.demo_list_page_desc),
            target = ListDemoActivity::class.java
        )
        bindDemoRow(
            row = contentBinding.rowWidget,
            iconView = contentBinding.ivWidgetIcon,
            titleView = contentBinding.tvWidgetName,
            summaryView = contentBinding.tvWidgetDesc,
            iconRes = R.drawable.ic_md_widgets,
            title = getString(R.string.demo_widget_page_name),
            summary = getString(R.string.demo_widget_page_desc),
            target = WidgetDemoActivity::class.java
        )
    }

    override fun initData() = Unit

    private fun bindDemoRow(
        row: View,
        iconView: ImageView,
        titleView: TextView,
        summaryView: TextView,
        iconRes: Int,
        title: String,
        summary: String,
        target: Class<out Activity>
    ) {
        iconView.setImageResource(iconRes)
        titleView.text = title
        summaryView.text = summary
        row.setOnClickListener {
            startActivity(Intent(this, target))
        }
    }
}
