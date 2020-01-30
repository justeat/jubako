package com.justeat.jubako.recyclerviews.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.justeat.jubako.Jubako
import com.justeat.jubako.data.PaginatedDataState
import com.justeat.jubako.data.PaginatedLiveData
import com.justeat.jubako.recyclerviews.CreateViewHolderDelegate
import com.justeat.jubako.recyclerviews.util.JubakoScreenFiller

@Suppress("UNCHECKED_CAST")
class PaginatedDataAdapter<DATA, ITEM_DATA, ITEM_HOLDER : RecyclerView.ViewHolder>(
    override var logger: Jubako.Logger,
    var itemViewHolder: CreateViewHolderDelegate<ITEM_HOLDER>,
    var itemBinder: (holder: ITEM_HOLDER, data: ITEM_DATA?) -> Unit = { _, _ -> },
    var lifecycleOwner: LifecycleOwner,
    var itemData: (data: DATA, position: Int) -> ITEM_DATA,
    var itemCount: (data: DATA) -> Int,
    var paginatedLiveData: PaginatedLiveData<*>,
    override val progressViewHolder: CreateViewHolderDelegate<RecyclerView.ViewHolder>,
    override var orientation: JubakoScreenFiller.Orientation
) : ProgressAdapter<ITEM_DATA, ITEM_HOLDER>(
    logger, progressViewHolder, orientation
) {
    override fun onCreateViewHolderItem(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        itemViewHolder(LayoutInflater.from(parent.context), parent, viewType)

    override fun getItemCountActual(): Int = itemCount(paginatedLiveData.state as DATA)
    override fun getCurrentState(): PaginatedDataState<*> = paginatedLiveData.state
    override fun loadMore() = paginatedLiveData.loadMore()
    override fun hasMoreToLoad(): Boolean = paginatedLiveData.hasMore
    override fun getItem(position: Int): ITEM_DATA = itemData(paginatedLiveData.state as DATA, position)
    override fun bindItemToHolder(holder: ITEM_HOLDER, item: ITEM_DATA) = itemBinder(holder, item)

    override fun init(recyclerView: RecyclerView) =
        paginatedLiveData.observe(lifecycleOwner, Observer<PaginatedDataState<*>> { state ->
            onStateChanged(state, paginatedLiveData.previousState)
        })
}
