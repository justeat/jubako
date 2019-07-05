package com.justeat.jubako.extensions

import android.os.Parcelable
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.lifecycle.*
import androidx.recyclerview.widget.RecyclerView
import com.justeat.jubako.ContentDescription
import com.justeat.jubako.JubakoViewHolder
import com.justeat.jubako.descriptionProvider
import com.justeat.jubako.viewHolderFactory
import com.justeat.jubako.widgets.JubakoCarouselRecyclerView

fun <HOLDER : CarouselViewHolder<ITEM, ITEM_HOLDER>, ITEM, ITEM_HOLDER : RecyclerView.ViewHolder> JubakoMutableList.addCarousel(
    carouselView: ((parent: ViewGroup) -> View)? = null,
    carouselViewHolder: (parent: ViewGroup) -> HOLDER = defaultCarouselViewHolder(
        carouselView
    ),
    @IdRes carouselRecyclerViewId: Int = View.NO_ID,
    carouselViewBinder: (holder: HOLDER) -> Unit = {},
    items: List<ITEM>? = null,
    itemData: LiveData<List<ITEM>> = DefaultCarouselItemsLiveData(items),
    itemViewHolder: (parent: ViewGroup) -> ITEM_HOLDER,
    itemBinder: (holder: ITEM_HOLDER, data: ITEM?) -> Unit = { _, _ -> }
) {
    add(descriptionProvider {
        carouselContentDescription(
            carouselView,
            carouselViewHolder,
            carouselRecyclerViewId,
            carouselViewBinder,
            items,
            itemData,
            itemViewHolder,
            itemBinder
        )
    })
}

fun <HOLDER : CarouselViewHolder<ITEM, ITEM_HOLDER>, ITEM, ITEM_HOLDER : RecyclerView.ViewHolder> carouselContentDescription(
    carouselView: ((parent: ViewGroup) -> View)? = null,
    carouselViewHolder: (parent: ViewGroup) -> HOLDER = defaultCarouselViewHolder(carouselView),
    @IdRes carouselRecyclerViewId: Int = View.NO_ID,
    carouselViewBinder: (holder: HOLDER) -> Unit = {},
    items: List<ITEM>? = null,
    itemData: LiveData<List<ITEM>> = DefaultCarouselItemsLiveData(items),
    itemViewHolder: (parent: ViewGroup) -> ITEM_HOLDER,
    itemBinder: (holder: ITEM_HOLDER, data: ITEM?) -> Unit = { _, _ -> }
): ContentDescription<List<ITEM>> {
    return ContentDescription(
        viewHolderFactory = viewHolderFactory { parent ->
            carouselViewHolder(parent).also { holder ->
                holder.carouselViewBinder = carouselViewBinder as (Any) -> Unit
                holder.itemBinder = itemBinder
                holder.itemViewHolderFactory = itemViewHolder
                holder.carouselRecyclerViewId = carouselRecyclerViewId
                holder.lifecycle =
                    (parent.context).takeIf { it is LifecycleOwner }?.let { (it as LifecycleOwner).lifecycle }
            }
        },
        data = itemData
    )
}

internal fun <HOLDER : CarouselViewHolder<ITEM, ITEM_HOLDER>, ITEM, ITEM_HOLDER : RecyclerView.ViewHolder> defaultCarouselViewHolder(
    carouselView: ((parent: ViewGroup) -> View)?
): (ViewGroup) -> HOLDER {
    return {
        CarouselViewHolder<ITEM, ITEM_HOLDER>(
            carouselView?.invoke(it) ?: throw Error("carouselView is required with default carouselViewHolder")
        ) as HOLDER
    }
}

internal class DefaultCarouselItemsLiveData<ITEM>(private val items: List<ITEM>?) : LiveData<List<ITEM>>() {
    override fun onActive() {
        if (items == null) throw Error("items is required with default itemData")
        postValue(items)
    }
}

open class CarouselViewHolder<ITEM, ITEM_HOLDER : RecyclerView.ViewHolder>(itemView: View) :
    JubakoViewHolder<List<ITEM>>(itemView) {

    @IdRes
    internal var carouselRecyclerViewId: Int = View.NO_ID
    internal lateinit var itemViewHolderFactory: (parent: ViewGroup) -> ITEM_HOLDER
    internal var carouselViewBinder: (Any) -> Unit = {}
    internal var itemBinder: (holder: ITEM_HOLDER, data: ITEM?) -> Unit = { _, _ -> }
    internal var lifecycle: Lifecycle? = null

    private val recycler: JubakoCarouselRecyclerView by lazy {
        when (carouselRecyclerViewId) {
            View.NO_ID -> itemView as JubakoCarouselRecyclerView
            else -> itemView.findViewById(carouselRecyclerViewId)
        }
    }

    override fun bind(data: List<ITEM>?) {
        carouselViewBinder(this)
        recycler.adapter = createAdapter(data ?: emptyList())
        trackState()
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

    private fun createAdapter(data: List<ITEM>): RecyclerView.Adapter<ITEM_HOLDER> {
        return object : RecyclerView.Adapter<ITEM_HOLDER>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = itemViewHolderFactory(parent)
            override fun getItemCount(): Int = data.size
            override fun onBindViewHolder(holder: ITEM_HOLDER, position: Int) {
                itemBinder(holder, data[position])
            }
        }
    }
}

private const val CAROUSEL_RECYCLER_VIEW_STATE = "jubako_carousel_recycler_view_state"
