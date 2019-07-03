package com.justeat.jubako.extensions

import android.os.Parcelable
import android.view.View
import android.view.View.NO_ID
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import androidx.recyclerview.widget.RecyclerView
import com.justeat.jubako.*
import com.justeat.jubako.widgets.JubakoCarouselRecyclerView
import com.justeat.jubako.widgets.JubakoRecyclerView
import java.util.*

typealias ListReceiver = MutableList<ContentDescriptionProvider<Any>>.() -> Unit

/**
 * Simply load with the given descriptions then just call [add], [addView], [addHolder], etc
 * to specify how and what you wish to display conveniently or manually construct and add [ContentDescriptionProvider]'s
 */
fun Jubako.load(descriptionProviders: ListReceiver) {
    load(SimpleJubakoAssembler(descriptionProviders.apply { invoke(mutableListOf()) }))
}

/**
 * Same as [load] but happens on the IO dispatcher (use with caution!)
 */
fun Jubako.loadAsync(descriptionProviders: ListReceiver) {
    load(assemble(descriptionProviders))
}

fun assemble(descriptionProviders: ListReceiver): JubakoAssembler {
    return object : JubakoAssembler {
        override suspend fun assemble(): List<ContentDescriptionProvider<Any>> =
            mutableListOf<ContentDescriptionProvider<Any>>().apply {
                descriptionProviders.invoke(this)
            }
    }
}

fun RecyclerView.withJubako(
    activity: FragmentActivity,
    loadingStrategy: ContentLoadingStrategy = PaginatedContentLoadingStrategy(10),
    onAssembled: (data: Jubako.Data) -> Unit = {},
    onAssembling: () -> Unit = {},
    onAssembleError: () -> Unit = {},
    onInitialFill: () -> Unit = {},
    onViewHolderEvent: (JubakoViewHolder.Event) -> Unit = {}
): Jubako {
    assert(this is JubakoRecyclerView)

    return Jubako.observe(activity) { state ->
        when (state) {
            is Jubako.State.Assembled -> {
                onAssembled(state.data)
                adapter = JubakoAdapter(activity, state.data, loadingStrategy).apply {
                    this.onInitialFill = onInitialFill
                    this.onViewHolderEvent = onViewHolderEvent
                }
            }
            is Jubako.State.Assembling -> onAssembling()
            is Jubako.State.AssembleError -> onAssembleError()
        }
    }
}

/**
 * Simple way to construct a [JubakoAdapter.HolderFactory]
 */
fun <T> viewHolderFactory(delegate: (parent: ViewGroup) -> JubakoViewHolder<T>): JubakoAdapter.HolderFactory<T> {
    return object : JubakoAdapter.HolderFactory<T> {
        override fun createViewHolder(parent: ViewGroup) = delegate(parent)
    }
}

/**
 * Simple way to construct a [ContentDescriptionProvider]
 */
fun <T> descriptionProvider(delegate: () -> ContentDescription<T>): ContentDescriptionProvider<T> {
    return object : ContentDescriptionProvider<T> {
        override fun createDescription() = delegate()
    }
}

fun MutableList<ContentDescriptionProvider<Any>>.add(provider: ContentDescriptionProvider<*>) {
    add(provider as ContentDescriptionProvider<Any>)
}

fun <T> MutableList<ContentDescriptionProvider<Any>>.add(delegate: () -> ContentDescription<T>) {
    add(descriptionProvider { delegate.invoke() } as ContentDescriptionProvider<Any>)
}

fun <T> MutableList<ContentDescriptionProvider<Any>>.addHolder(delegate: () -> JubakoViewHolder<T>) {
    add(descriptionProvider {
        ContentDescription(
            viewHolderFactory = viewHolderFactory { delegate.invoke() })
    } as ContentDescriptionProvider<Any>)
}

fun MutableList<ContentDescriptionProvider<Any>>.addView(delegate: (parent: ViewGroup) -> View) {
    add(descriptionProvider {
        ContentDescription(
            viewHolderFactory = viewHolderFactory {
                object : JubakoViewHolder<Any>(delegate.invoke(it)) {
                    override fun bind(data: Any?) {}
                }
            })
    })
}

fun <HOLDER : CarouselViewHolder<ITEM, ITEM_HOLDER>, ITEM, ITEM_HOLDER : RecyclerView.ViewHolder> MutableList<ContentDescriptionProvider<Any>>.addCarousel(
    carouselView: ((parent: ViewGroup) -> View)? = null,
    carouselViewHolder: (parent: ViewGroup) -> HOLDER = defaultCarouselViewHolder(carouselView),
    @IdRes carouselRecyclerViewId: Int = NO_ID,
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

private fun <HOLDER : CarouselViewHolder<ITEM, ITEM_HOLDER>, ITEM, ITEM_HOLDER : RecyclerView.ViewHolder> carouselContentDescription(
    carouselView: ((parent: ViewGroup) -> View)? = null,
    carouselViewHolder: (parent: ViewGroup) -> HOLDER = defaultCarouselViewHolder(carouselView),
    @IdRes carouselRecyclerViewId: Int = NO_ID,
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
    internal var carouselRecyclerViewId: Int = NO_ID
    internal lateinit var itemViewHolderFactory: (parent: ViewGroup) -> ITEM_HOLDER
    internal var carouselViewBinder: (Any) -> Unit = {}
    internal var itemBinder: (holder: ITEM_HOLDER, data: ITEM?) -> Unit = { _, _ -> }
    internal var lifecycle: Lifecycle? = null

    private val recycler: JubakoCarouselRecyclerView by lazy {
        when (carouselRecyclerViewId) {
            NO_ID -> itemView as JubakoCarouselRecyclerView
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

fun pageSize(pageSize: Int) = PaginatedContentLoadingStrategy(pageSize)

fun <T> MutableList<ContentDescriptionProvider<Any>>.addDescription(
    viewHolderFactory: JubakoAdapter.HolderFactory<T>,
    data: LiveData<T>? = null,
    onReload: (ContentDescription<T>.(payload: Any?) -> Unit) = { },
    id: String = UUID.randomUUID().toString()
) {
    add(descriptionProvider {
        ContentDescription(
            viewHolderFactory = viewHolderFactory,
            data = data,
            onReload = onReload,
            id = id
        )
    })
}

private const val CAROUSEL_RECYCLER_VIEW_STATE = "jubako_carousel_recycler_view_state"
