package com.wkq.corebasedemo

import android.widget.Toast
import com.wkq.base.activity.BaseTitleActivity
import com.wkq.corebasedemo.databinding.ActivityBaseTitleDemoBinding

class TitleDemoActivity : BaseTitleActivity<ActivityBaseTitleDemoBinding>() {

    private var compactTitle = false

    override fun initView() {
        setPageTitle(getString(R.string.demo_title_page_name))
        setRightText(getString(R.string.demo_action_save)) {
            showToast(getString(R.string.demo_saved))
        }

        contentBinding.tvIntro.text = getString(R.string.demo_title_intro)
        contentBinding.btnChangeTitle.setOnClickListener {
            compactTitle = !compactTitle
            setPageTitle(
                if (compactTitle) {
                    getString(R.string.demo_title_compact)
                } else {
                    getString(R.string.demo_title_page_name)
                }
            )
        }
        contentBinding.btnRightText.setOnClickListener {
            setRightText(getString(R.string.demo_action_done)) {
                showToast(getString(R.string.demo_done_clicked))
            }
            showToast(getString(R.string.demo_right_text_changed))
        }
        contentBinding.btnLeftText.setOnClickListener {
            setLeftText(getString(R.string.demo_action_close)) {
                finish()
            }
            showToast(getString(R.string.demo_left_text_changed))
        }
    }

    override fun initData() = Unit

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
