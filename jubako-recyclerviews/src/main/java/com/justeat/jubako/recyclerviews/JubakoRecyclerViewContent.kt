package com.justeat.jubako.recyclerviews

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import androidx.recyclerview.widget.RecyclerView
import com.justeat.jubako.ContentDescription
import com.justeat.jubako.data.PaginatedDataState
import com.justeat.jubako.data.PaginatedLiveData
import com.justeat.jubako.descriptionProvider
import com.justeat.jubako.extensions.JubakoMutableList
import com.justeat.jubako.extensions.add
import com.justeat.jubako.recyclerviews.adapters.DefaultProgressViewHolder
import com.justeat.jubako.recyclerviews.adapters.JubakoRecyclerViewHolder

typealias CreateViewDelegate = ((layoutInflater: LayoutInflater, parent: ViewGroup) -> View)
typealias CreateViewHolderDelegate<T> = (layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int) -> T

/**
 * Convenience function to add a [RecyclerView] into the list to present data as carousels, grids, etc,
 * by default a [LinearLayoutManager] is configured in [HORIZONTAL] orientation.
 *
 * @param view  An inflated view for your holder must contain a [RecyclerView] as the root view
 * unless specified by [recyclerViewId]. This is not used if you are providing your own custom [JubakoRecyclerViewHolder]
 * with the [viewHolder] argument.
 * @param viewHolder Optionally provide your own custom [JubakoRecyclerViewHolder] implementation.
 * @param recyclerViewId The id of a [RecyclerView] in your layout, if omitted then [view] is expected
 * to be the recycler itself
 * @param viewBinder Optionally perform view binding in your [JubakoRecyclerViewHolder].
 * @param data The [DATA] that holds your list of things to display in your recycler view, use [InstantLiveData] for a simple
 * way to satisfy this argument if you already have something like a list of things you wish to display, otherwise
 * use your own [LiveData] if you want to load your [JubakoRecyclerViewHolder] with [DATA] from a service layer, etc.
 *
 * For paginated data use [PaginatedLiveData]
 * @param itemData Specify a lambda to retrieve the [ITEM_DATA] from your [DATA] by position.
 * @param itemCount Specify a lambda to retrieve the count of [ITEM_DATA] in your [DATA].
 * @param itemViewHolder Specify a custom [RecyclerView.ViewHolder] to use for your recycler view items.
 * @param itemBinder Optionally specify a lambda where you can bind [ITEM_DATA] to [ITEM_HOLDER]
 * @param layoutManager Defaults to a [LinearLayoutManager] in [HORIZONTAL] orientation, replace for a custom layout
 * @param onReload See [ContentDescription.onReload]
 * @param progressViewHolder Optional custom [RecyclerView.ViewHolder], must implement interface [ProgressView]
 * @param DATA The type of data you want to display, the data that contains all the items (see [ITEM_DATA]), simplest thing could be a [List]
 * @param HOLDER Optional custom type of [JubakoRecyclerViewHolder]
 * @param ITEM_DATA The type of item data that [DATA] provides
 * @param ITEM_HOLDER A standard implementation of [RecyclerView.ViewHolder] to display [ITEM_DATA]
 */
fun <DATA, HOLDER : JubakoRecyclerViewHolder<DATA, ITEM_DATA, ITEM_HOLDER>, ITEM_DATA, ITEM_HOLDER :
RecyclerView.ViewHolder> JubakoMutableList.addRecyclerView(
    view: CreateViewDelegate? = null,
    viewHolder: (parent: ViewGroup) -> HOLDER = defaultRecyclerViewHolder(view),
    @IdRes recyclerViewId: Int = View.NO_ID,
    viewBinder: (holder: HOLDER) -> Unit = {},
    data: LiveData<DATA>,
    itemData: (data: DATA, position: Int) -> ITEM_DATA = defaultItemData(),
    itemCount: (data: DATA) -> Int = defaultItemCount(),
    itemViewHolder: CreateViewHolderDelegate<ITEM_HOLDER>,
    itemBinder: (holder: ITEM_HOLDER, data: ITEM_DATA?) -> Unit = { _, _ -> },
    layoutManager: (context: Context) -> RecyclerView.LayoutManager = { LinearLayoutManager(it, HORIZONTAL, false) },
    onReload: (ContentDescription<DATA>.(payload: Any?) -> Unit) = {},
    progressViewHolder: CreateViewHolderDelegate<RecyclerView.ViewHolder> = { inflater, parent, _ ->
        DefaultProgressViewHolder(inflater, parent)
    }
) {
    add(descriptionProvider {
        recyclerViewContent(
            view,
            viewHolder,
            recyclerViewId,
            viewBinder,
            data,
            itemData,
            itemCount,
            itemViewHolder,
            itemBinder,
            layoutManager,
            onReload,
            progressViewHolder
        )
    })
}

fun <HOLDER : JubakoRecyclerViewHolder<PaginatedDataState<ITEM_DATA>, ITEM_DATA, ITEM_HOLDER>, ITEM_DATA, ITEM_HOLDER :
RecyclerView.ViewHolder> JubakoMutableList.addRecyclerView(
    view: CreateViewDelegate? = null,
    viewHolder: (parent: ViewGroup) -> HOLDER = defaultRecyclerViewHolder(view),
    @IdRes recyclerViewId: Int = View.NO_ID,
    viewBinder: (holder: HOLDER) -> Unit = {},
    data: PaginatedLiveData<ITEM_DATA>,
    itemData: (data: PaginatedDataState<ITEM_DATA>, position: Int) -> ITEM_DATA = { data, position ->
        data.loaded[position]
    },
    itemCount: (data: PaginatedDataState<ITEM_DATA>) -> Int = { data ->
        data.loaded.size
    },
    itemViewHolder: CreateViewHolderDelegate<ITEM_HOLDER>,
    itemBinder: (holder: ITEM_HOLDER, data: ITEM_DATA?) -> Unit = { _, _ -> },
    layoutManager: (context: Context) -> RecyclerView.LayoutManager = { LinearLayoutManager(it, HORIZONTAL, false) },
    onReload: (ContentDescription<PaginatedDataState<ITEM_DATA>>.(payload: Any?) -> Unit) = {},
    progressViewHolder: CreateViewHolderDelegate<RecyclerView.ViewHolder> = { inflater, parent, _ ->
        DefaultProgressViewHolder(inflater, parent)
    }
) {
    add(descriptionProvider {
        recyclerViewContent(
            view,
            viewHolder,
            recyclerViewId,
            viewBinder,
            data,
            itemData,
            itemCount,
            itemViewHolder,
            itemBinder,
            layoutManager,
            onReload,
            progressViewHolder
        )
    })
}

/**
 * Convenience function to create a recycler view based content description
 *
 * @param view  An inflated view for your holder must contain a [RecyclerView] as the root view
 * unless specified by [recyclerViewId]. This is not used if you are providing your own custom [JubakoRecyclerViewHolder]
 * with the [viewHolder] argument.
 * @param viewHolder Optionally provide your own custom [JubakoRecyclerViewHolder] implementation.
 * @param recyclerViewId The id of a [RecyclerView] in your layout, if omitted then [view] is expected
 * to be the recycler itself
 * @param viewBinder Optionally perform view binding in your [JubakoRecyclerViewHolder].
 * @param data The [DATA] that holds your list of things to display in your recycler view, use [InstantLiveData] for a simple
 * way to satisfy this argument if you already have something like a list of things you wish to display, otherwise
 * use your own [LiveData] if you want to load your [JubakoRecyclerViewHolder] with [DATA] from a service layer, etc.
 *
 * For paginated data use [PaginatedLiveData]
 * @param itemData Specify a lambda to retrieve the [ITEM_DATA] from your [DATA] by position.
 * @param itemCount Specify a lambda to retrieve the count of [ITEM_DATA] in your [DATA].
 * @param itemViewHolder Specify a custom [RecyclerView.ViewHolder] to use for your recycler view items.
 * @param itemBinder Optionally specify a lambda where you can bind [ITEM_DATA] to [ITEM_HOLDER]
 * @param layoutManager Defaults to a [LinearLayoutManager] in [HORIZONTAL] orientation, replace for a custom layout
 * @param onReload See [ContentDescription.onReload]
 * @param progressViewHolder Optional custom [RecyclerView.ViewHolder], must implement interface [ProgressView]
 * @param DATA The type of data you want to display, the data that contains all the items (see [ITEM_DATA]), simplest thing could be a [List]
 * @param HOLDER Optional custom type of [JubakoRecyclerViewHolder]
 * @param ITEM_DATA The type of item data that [DATA] provides
 * @param ITEM_HOLDER A standard implementation of [RecyclerView.ViewHolder] to display [ITEM_DATA]
 */
fun <DATA, HOLDER : JubakoRecyclerViewHolder<DATA, ITEM_DATA, ITEM_HOLDER>, ITEM_DATA, ITEM_HOLDER : RecyclerView.ViewHolder> recyclerViewContent(
    view: CreateViewDelegate? = null,
    viewHolder: (parent: ViewGroup) -> HOLDER = defaultRecyclerViewHolder(view),
    @IdRes recyclerViewId: Int = View.NO_ID,
    viewBinder: (holder: HOLDER) -> Unit = {},
    data: LiveData<DATA>,
    itemData: (data: DATA, position: Int) -> ITEM_DATA,
    itemCount: (data: DATA) -> Int,
    itemViewHolder: CreateViewHolderDelegate<ITEM_HOLDER>,
    itemBinder: (holder: ITEM_HOLDER, data: ITEM_DATA?) -> Unit = { _, _ -> },
    layoutManager: (context: Context) -> RecyclerView.LayoutManager = { LinearLayoutManager(it, HORIZONTAL, false) },
    onReload: (ContentDescription<DATA>.(payload: Any?) -> Unit) = {},
    progressViewHolder: CreateViewHolderDelegate<RecyclerView.ViewHolder> = { inflater, parent, _ ->
        DefaultProgressViewHolder(inflater, parent)
    }
): ContentDescription<DATA> {

    if (data is PaginatedLiveData<*>) {
        assert(layoutManager is LinearLayoutManager && layoutManager.orientation == HORIZONTAL) {
            "PaginatedLiveData<T> is only compatible with a HORIZONTAL LinearLayoutManagers"
        }
    }

    return ContentDescription(
        viewSpec = com.justeat.jubako.recyclerviews.viewHolderFactory { parent ->
            viewHolder(parent).also { holder ->
                @Suppress("UNCHECKED_CAST")
                holder.viewBinder = viewBinder as (Any) -> Unit
                holder.itemBinder = itemBinder
                holder.itemViewHolder = itemViewHolder
                holder.recyclerViewId = recyclerViewId
                holder.itemData = itemData
                holder.itemCount = itemCount
                holder.lifecycleOwner = (parent.context).takeIf { it is LifecycleOwner }?.let { it as LifecycleOwner }
                holder.layoutManager = layoutManager
                holder.paginatedLiveData = if (data is PaginatedLiveData<*>) data else null
                holder.progressViewHolder = progressViewHolder
            }
        },
        data = data,
        onReload = onReload
    )
}

internal fun <DATA, HOLDER : JubakoRecyclerViewHolder<DATA, ITEM_DATA, ITEM_HOLDER>, ITEM_DATA, ITEM_HOLDER : RecyclerView.ViewHolder> defaultRecyclerViewHolder(
    view: CreateViewDelegate?
): (ViewGroup) -> HOLDER {
    return {
        @Suppress("UNCHECKED_CAST")
        JubakoRecyclerViewHolder<DATA, ITEM_DATA, ITEM_HOLDER>(
            view?.invoke(LayoutInflater.from(it.context), it) ?: throw Error("view is required with default viewHolder")
        ) as HOLDER
    }
}

private fun <DATA> defaultItemCount(): (DATA) -> Int {
    return { data: DATA ->
        if (data is PaginatedDataState<*>) {
            data.loaded.size
        } else {
            throw Error("You must specify itemCount argument")
        }
    }
}

private fun <DATA, ITEM_DATA> defaultItemData(): (DATA, Int) -> ITEM_DATA {
    return { data, position ->
        if (data is PaginatedDataState<*>) {
            data.loaded[position] as ITEM_DATA
        } else {
            throw Error("You must specify itemData argument")
        }
    }
}
