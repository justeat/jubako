package com.justeat.jubako.extensions

import android.os.Parcelable
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.IdRes
import androidx.lifecycle.*
import androidx.recyclerview.widget.RecyclerView
import com.justeat.jubako.ContentDescription
import com.justeat.jubako.JubakoViewHolder
import com.justeat.jubako.descriptionProvider
import com.justeat.jubako.viewHolderFactory
import com.justeat.jubako.widgets.JubakoCarouselRecyclerView

/**
 * Convenience function to add carousels into the list
 *
 * @param carouselView  An inflated view for your carousel must contain a [JubakoCarouselRecyclerView] as the root view
 * or ore specified by [carouselRecyclerViewId]. This is not used if you are providing your own custom [CarouselViewHolder]
 * with the [carouselViewHolder] argument.
 * @param carouselViewHolder Optionally provide your own custom [CarouselViewHolder] implementation.
 * @param carouselRecyclerViewId The id of a [JubakoCarouselRecyclerView] in your layout.
 * @param carouselViewBinder Optionally perform view binding in your [CarouselViewHolder].
 * @param data The [DATA] that holds your list of things to display in your carousel, use [InstantLiveData] for a simple
 * way to satisfy this argument if you already have something like a list of things you wish to display, otherwise
 * use your own [LiveData] if you want to load your carousel with [DATA] from a service layer, etc.
 * @param itemData Specify a lambda to retrieve the [ITEM_DATA] from your [DATA] by position.
 * @param itemCount Specify a lambda to retrieve the count of [ITEM_DATA] in your [DATA].
 * @param itemViewHolder Specify a custom [RecyclerView.ViewHolder] to use for your carousel items.
 * @param itemBinder Optionally specify a lambda where you can bind [ITEM_DATA] to [ITEM_HOLDER]
 */
fun <DATA, HOLDER : CarouselViewHolder<DATA, ITEM_DATA, ITEM_HOLDER>, ITEM_DATA, ITEM_HOLDER :
RecyclerView.ViewHolder> JubakoMutableList.addCarousel(
    carouselView: ((parent: ViewGroup) -> View)? = null,
    carouselViewHolder: (parent: ViewGroup) -> HOLDER = defaultCarouselViewHolder(carouselView),
    @IdRes carouselRecyclerViewId: Int = View.NO_ID,
    carouselViewBinder: (holder: HOLDER) -> Unit = {},
    data: LiveData<DATA>,
    itemData: (data: DATA, position: Int) -> ITEM_DATA,
    itemCount: (data: DATA) -> Int,
    itemViewHolder: (parent: ViewGroup) -> ITEM_HOLDER,
    itemBinder: (holder: ITEM_HOLDER, data: ITEM_DATA?) -> Unit = { _, _ -> }
) {
    add(descriptionProvider {
        carouselContentDescription(
            carouselView,
            carouselViewHolder,
            carouselRecyclerViewId,
            carouselViewBinder,
            data,
            itemData,
            itemCount,
            itemViewHolder,
            itemBinder
        )
    })
}

/**
 * Convenience function to create a carousel based content description
 *
 * @param carouselView  An inflated view for your carousel must contain a [JubakoCarouselRecyclerView] as the root view
 * or ore specified by [carouselRecyclerViewId]. This is not used if you are providing your own custom [CarouselViewHolder]
 * with the [carouselViewHolder] argument.
 * @param carouselViewHolder Optionally provide your own custom [CarouselViewHolder] implementation.
 * @param carouselRecyclerViewId The id of a [JubakoCarouselRecyclerView] in your layout.
 * @param carouselViewBinder Optionally perform view binding in your [CarouselViewHolder].
 * @param data The [DATA] that holds your list of things to display in your carousel, use [InstantLiveData] for a simple
 * way to satisfy this argument if you already have something like a list of things you wish to display, otherwise
 * use your own [LiveData] if you want to load your carousel with [DATA] from a service layer, etc.
 * @param itemData Specify a lambda to retrieve the [ITEM_DATA] from your [DATA] by position.
 * @param itemCount Specify a lambda to retrieve the count of [ITEM_DATA] in your [DATA].
 * @param itemViewHolder Specify a custom [RecyclerView.ViewHolder] to use for your carousel items.
 * @param itemBinder Optionally specify a lambda where you can bind [ITEM_DATA] to [ITEM_HOLDER]
 */
fun <DATA, HOLDER : CarouselViewHolder<DATA, ITEM_DATA, ITEM_HOLDER>, ITEM_DATA, ITEM_HOLDER : RecyclerView.ViewHolder> carouselContentDescription(
    carouselView: ((parent: ViewGroup) -> View)? = null,
    carouselViewHolder: (parent: ViewGroup) -> HOLDER = defaultCarouselViewHolder(carouselView),
    @IdRes carouselRecyclerViewId: Int = View.NO_ID,
    carouselViewBinder: (holder: HOLDER) -> Unit = {},
    data: LiveData<DATA>,
    itemData: (data: DATA, position: Int) -> ITEM_DATA,
    itemCount: (data: DATA) -> Int,
    itemViewHolder: (parent: ViewGroup) -> ITEM_HOLDER,
    itemBinder: (holder: ITEM_HOLDER, data: ITEM_DATA?) -> Unit = { _, _ -> }
): ContentDescription<DATA> {
    return ContentDescription(
        viewHolderFactory = viewHolderFactory { parent ->
            carouselViewHolder(parent).also { holder ->
                holder.carouselViewBinder = carouselViewBinder as (Any) -> Unit
                holder.itemBinder = itemBinder
                holder.itemViewHolderFactory = itemViewHolder
                holder.carouselRecyclerViewId = carouselRecyclerViewId
                holder.itemData = itemData
                holder.itemCount = itemCount
                holder.lifecycle =
                    (parent.context).takeIf { it is LifecycleOwner }?.let { (it as LifecycleOwner).lifecycle }
            }
        },
        data = data
    )
}

internal fun <DATA, HOLDER : CarouselViewHolder<DATA, ITEM_DATA, ITEM_HOLDER>, ITEM_DATA, ITEM_HOLDER : RecyclerView.ViewHolder> defaultCarouselViewHolder(
    carouselView: ((parent: ViewGroup) -> View)?
): (ViewGroup) -> HOLDER {
    return {
        CarouselViewHolder<DATA, ITEM_DATA, ITEM_HOLDER>(
            carouselView?.invoke(it) ?: throw Error("carouselView is required with default carouselViewHolder")
        ) as HOLDER
    }
}

open class CarouselViewHolder<DATA, ITEM_DATA, ITEM_HOLDER : RecyclerView.ViewHolder>(itemView: View) :
    JubakoViewHolder<DATA>(itemView) {

    @IdRes
    internal var carouselRecyclerViewId: Int = View.NO_ID
    internal lateinit var itemViewHolderFactory: (parent: ViewGroup) -> ITEM_HOLDER
    internal var carouselViewBinder: (Any) -> Unit = {}
    internal var itemBinder: (holder: ITEM_HOLDER, data: ITEM_DATA?) -> Unit = { _, _ -> }
    internal var lifecycle: Lifecycle? = null
    lateinit var itemData: (data: DATA, position: Int) -> ITEM_DATA
    lateinit var itemCount: (data: DATA) -> Int

    private val recycler: JubakoCarouselRecyclerView by lazy {
        when (carouselRecyclerViewId) {
            View.NO_ID -> itemView as JubakoCarouselRecyclerView
            else -> itemView.findViewById(carouselRecyclerViewId)
        }
    }

    @CallSuper
    override fun bind(data: DATA?) {
        carouselViewBinder(this)
        data?.apply {
            recycler.adapter = createAdapter(data)
            trackState()
        }
    }

    private fun trackState() {
        val cachedState: Parcelable? = (description?.cache?.get(CAROUSEL_RECYCLER_VIEW_STATE) as Parcelable?)
        cachedState?.let { inState ->
            recycler.layoutManager?.onRestoreInstanceState(inState)
        }

        lifecycle?.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestroy() {
                lifecycle?.removeObserver(this)
                recycler.layoutManager?.onSaveInstanceState()?.let { outState ->
                    description?.cache?.put(CAROUSEL_RECYCLER_VIEW_STATE, outState)
                }
            }
        })
    }

    private fun createAdapter(data: DATA): RecyclerView.Adapter<ITEM_HOLDER> {
        return object : RecyclerView.Adapter<ITEM_HOLDER>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = itemViewHolderFactory(parent)
            override fun getItemCount(): Int = itemCount(data)
            override fun onBindViewHolder(holder: ITEM_HOLDER, position: Int) {
                itemBinder(holder, itemData(data, position))
            }
        }
    }
}

private const val CAROUSEL_RECYCLER_VIEW_STATE = "jubako_carousel_recycler_view_state"
