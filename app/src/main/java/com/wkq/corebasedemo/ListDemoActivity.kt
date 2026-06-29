package com.wkq.corebasedemo

import android.content.Context
import com.wkq.base.adapter.BaseRecyclerViewAdapter
import com.wkq.base.activity.BaseListActivity
import com.wkq.base.widget.CommonTitleBar
import com.wkq.corebasedemo.databinding.ItemBaseListDemoBinding

class ListDemoActivity : BaseListActivity<DemoListItem>() {

    override fun initView() {
        super.initView()
        setHeaderView(
            CommonTitleBar(this).apply {
                setTitle(getString(R.string.demo_list_page_name))
                setLeftIcon(com.wkq.base.R.mipmap.ic_toolbar_back_black)
                onLeftClickListener = { finish() }
            }
        )
        setEmptyText(getString(R.string.demo_list_empty))
    }

    override fun createAdapter(): BaseRecyclerViewAdapter<*, DemoListItem> {
        return DemoListAdapter(this)
    }

    override fun loadListData(page: Int) {
        binding.root.postDelayed({
            val items = if (page <= 3) {
                (1..8).map { index ->
                    val number = (page - 1) * 8 + index
                    DemoListItem(
                        title = getString(R.string.demo_list_item_title, number),
                        summary = getString(R.string.demo_list_item_desc, page)
                    )
                }
            } else {
                emptyList()
            }
            finishLoad(items, hasMore = page < 3)
        }, 350)
    }
}

data class DemoListItem(
    val title: String,
    val summary: String
)

class DemoListAdapter(context: Context) :
    BaseRecyclerViewAdapter<ItemBaseListDemoBinding, DemoListItem>(
        context,
        ItemBaseListDemoBinding::inflate
    ) {

    override fun convert(binding: ItemBaseListDemoBinding, item: DemoListItem, position: Int) {
        binding.tvItemTitle.text = item.title
        binding.tvItemDesc.text = item.summary
        binding.tvItemIndex.text = (position + 1).toString()
    }
}
