package com.justeat.jubako.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.justeat.jubako.Jubako

@Suppress("UNCHECKED_CAST")
class StaticDataAdapter<DATA, ITEM_DATA, ITEM_HOLDER : RecyclerView.ViewHolder>(
    private val data: DATA,
    var logger: Jubako.Logger,
    var itemViewHolder: (layoutInflater: LayoutInflater, parent: ViewGroup) -> ITEM_HOLDER,
    var itemBinder: (holder: ITEM_HOLDER, data: ITEM_DATA?) -> Unit = { _, _ -> },
    var itemData: (data: DATA, position: Int) -> ITEM_DATA,
    var itemCount: (data: DATA) -> Int
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        logger.log(TAG, "Create View Holder", "")
        return itemViewHolder(LayoutInflater.from(parent.context), parent)
    }

    override fun getItemCount(): Int {
        return itemCount(data)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        itemBinder(holder as ITEM_HOLDER, itemData(data, position))
    }
}

private val TAG = StaticDataAdapter::class.java.simpleName
