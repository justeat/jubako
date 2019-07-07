package com.justeat.jubako.extensions

import android.content.Context
import android.os.Parcelable
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.IdRes
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import androidx.recyclerview.widget.RecyclerView
import com.justeat.jubako.ContentDescription
import com.justeat.jubako.JubakoViewHolder
import com.justeat.jubako.descriptionProvider
import com.justeat.jubako.viewHolderFactory

/**
 * Convenience function to add a [RecyclerView] into the list to present data as carousels, grids, etc,
 * be default a [LinearLayoutManager] is configured in [HORIZONTAL] orientation
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
 * @param itemData Specify a lambda to retrieve the [ITEM_DATA] from your [DATA] by position.
 * @param itemCount Specify a lambda to retrieve the count of [ITEM_DATA] in your [DATA].
 * @param itemViewHolder Specify a custom [RecyclerView.ViewHolder] to use for your recycler view items.
 * @param itemBinder Optionally specify a lambda where you can bind [ITEM_DATA] to [ITEM_HOLDER]
 * @param layoutManager Defaults to a [LinearLayoutManager] in [HORIZONTAL] orientation, replace for a custom layout
 * @param onReload See [ContentDescription.onReload]
 */
fun <DATA, HOLDER : JubakoRecyclerViewHolder<DATA, ITEM_DATA, ITEM_HOLDER>, ITEM_DATA, ITEM_HOLDER :
RecyclerView.ViewHolder> JubakoMutableList.addRecyclerView(
    view: ((parent: ViewGroup) -> View)? = null,
    viewHolder: (parent: ViewGroup) -> HOLDER = defaultRecyclerViewHolder(view),
    @IdRes recyclerViewId: Int = View.NO_ID,
    viewBinder: (holder: HOLDER) -> Unit = {},
    data: LiveData<DATA>,
    itemData: (data: DATA, position: Int) -> ITEM_DATA,
    itemCount: (data: DATA) -> Int,
    itemViewHolder: (parent: ViewGroup) -> ITEM_HOLDER,
    itemBinder: (holder: ITEM_HOLDER, data: ITEM_DATA?) -> Unit = { _, _ -> },
    layoutManager: (context: Context) -> RecyclerView.LayoutManager = { LinearLayoutManager(it, HORIZONTAL, false) },
    onReload: (ContentDescription<DATA>.(payload: Any?) -> Unit) = {}
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
            onReload
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
 * @param itemData Specify a lambda to retrieve the [ITEM_DATA] from your [DATA] by position.
 * @param itemCount Specify a lambda to retrieve the count of [ITEM_DATA] in your [DATA].
 * @param itemViewHolder Specify a custom [RecyclerView.ViewHolder] to use for your recycler view items.
 * @param itemBinder Optionally specify a lambda where you can bind [ITEM_DATA] to [ITEM_HOLDER]
 * @param layoutManager Defaults to a [LinearLayoutManager] in [HORIZONTAL] orientation, replace for a custom layout
 * @param onReload See [ContentDescription.onReload]
 */
fun <DATA, HOLDER : JubakoRecyclerViewHolder<DATA, ITEM_DATA, ITEM_HOLDER>, ITEM_DATA, ITEM_HOLDER : RecyclerView.ViewHolder> recyclerViewContent(
    view: ((parent: ViewGroup) -> View)? = null,
    viewHolder: (parent: ViewGroup) -> HOLDER = defaultRecyclerViewHolder(view),
    @IdRes recyclerViewId: Int = View.NO_ID,
    viewBinder: (holder: HOLDER) -> Unit = {},
    data: LiveData<DATA>,
    itemData: (data: DATA, position: Int) -> ITEM_DATA,
    itemCount: (data: DATA) -> Int,
    itemViewHolder: (parent: ViewGroup) -> ITEM_HOLDER,
    itemBinder: (holder: ITEM_HOLDER, data: ITEM_DATA?) -> Unit = { _, _ -> },
    layoutManager: (context: Context) -> RecyclerView.LayoutManager = { LinearLayoutManager(it, HORIZONTAL, false) },
    onReload: (ContentDescription<DATA>.(payload: Any?) -> Unit) = {}
): ContentDescription<DATA> {
    return ContentDescription(
        viewHolderFactory = viewHolderFactory { parent ->
            viewHolder(parent).also { holder ->
                holder.viewBinder = viewBinder as (Any) -> Unit
                holder.itemBinder = itemBinder
                holder.itemViewHolder = itemViewHolder
                holder.recyclerViewId = recyclerViewId
                holder.itemData = itemData
                holder.itemCount = itemCount
                holder.lifecycle =
                    (parent.context).takeIf { it is LifecycleOwner }?.let { (it as LifecycleOwner).lifecycle }
                holder.layoutManager = layoutManager
            }
        },
        data = data,
        onReload = onReload
    )
}

internal fun <DATA, HOLDER : JubakoRecyclerViewHolder<DATA, ITEM_DATA, ITEM_HOLDER>, ITEM_DATA, ITEM_HOLDER : RecyclerView.ViewHolder> defaultRecyclerViewHolder(
    view: ((parent: ViewGroup) -> View)?
): (ViewGroup) -> HOLDER {
    return {
        JubakoRecyclerViewHolder<DATA, ITEM_DATA, ITEM_HOLDER>(
            view?.invoke(it) ?: throw Error("view is required with default viewHolder")
        ) as HOLDER
    }
}

/**
 * An implementation of [JubakoViewHolder] setup with a [RecyclerView] and easy callback functions for generic
 * view holder creation and data binding.
 */
open class JubakoRecyclerViewHolder<DATA, ITEM_DATA, ITEM_HOLDER : RecyclerView.ViewHolder>(itemView: View) :
    JubakoViewHolder<DATA>(itemView) {

    @IdRes
    internal var recyclerViewId: Int = View.NO_ID
    internal lateinit var itemViewHolder: (parent: ViewGroup) -> ITEM_HOLDER
    internal var viewBinder: (Any) -> Unit = {}
    internal var itemBinder: (holder: ITEM_HOLDER, data: ITEM_DATA?) -> Unit = { _, _ -> }
    internal var lifecycle: Lifecycle? = null
    lateinit var itemData: (data: DATA, position: Int) -> ITEM_DATA
    lateinit var itemCount: (data: DATA) -> Int
    lateinit var layoutManager: (context: Context) -> RecyclerView.LayoutManager

    private val recycler: RecyclerView by lazy {
        when (recyclerViewId) {
            View.NO_ID -> itemView as RecyclerView
            else -> itemView.findViewById(recyclerViewId)
        }.also { recycler ->
            recycler.layoutManager = layoutManager.invoke(recycler.context)
        }
    }

    @CallSuper
    override fun bind(data: DATA?) {
        viewBinder(this)
        data?.apply {
            recycler.adapter = createAdapter(data)
            trackState()
        }
    }

    private fun trackState() {
        val cachedState: Parcelable? = (description?.cache?.get(RECYCLER_VIEW_STATE) as Parcelable?)
        cachedState?.let { inState ->
            recycler.layoutManager?.onRestoreInstanceState(inState)
        }

        lifecycle?.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestroy() {
                lifecycle?.removeObserver(this)
                recycler.layoutManager?.onSaveInstanceState()?.let { outState ->
                    description?.cache?.put(RECYCLER_VIEW_STATE, outState)
                }
            }
        })
    }

    private fun createAdapter(data: DATA): RecyclerView.Adapter<ITEM_HOLDER> {
        return object : RecyclerView.Adapter<ITEM_HOLDER>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = itemViewHolder(parent)
            override fun getItemCount(): Int = itemCount(data)
            override fun onBindViewHolder(holder: ITEM_HOLDER, position: Int) {
                itemBinder(holder, itemData(data, position))
            }
        }
    }
}

private const val RECYCLER_VIEW_STATE = "jubako_recycler_view_state"
