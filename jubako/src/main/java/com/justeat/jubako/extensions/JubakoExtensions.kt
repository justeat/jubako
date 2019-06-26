package com.justeat.jubako.extensions

import android.view.View
import android.view.View.NO_ID
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.justeat.jubako.*
import com.justeat.jubako.widgets.JubakoCarouselRecyclerView

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

fun MutableList<ContentDescriptionProvider<Any>>.addView(delegate: () -> View) {
    add(descriptionProvider {
        ContentDescription(
            viewHolderFactory = viewHolderFactory {
                object : JubakoViewHolder<Any>(delegate.invoke()) {
                    override fun bind(data: Any?) {}
                }
            })
    })
}

fun <CAROUSEL : CarouselViewHolder<ITEM, ITEM_HOLDER>, ITEM, ITEM_HOLDER : RecyclerView.ViewHolder> MutableList<ContentDescriptionProvider<Any>>.addCarousel(
    layout: (parent: ViewGroup) -> View,
    @IdRes carouselRecyclerViewId: Int = NO_ID,
    items: List<ITEM>,
    priority: Int = 0,
    layoutBinder: (CAROUSEL) -> Unit = {},
    itemViewHolderFactory: (parent: ViewGroup) -> ITEM_HOLDER,
    itemBinder: (holder: ITEM_HOLDER, data: ITEM?) -> Unit = { _, _ -> }
) {
    add(descriptionProvider {
        ContentDescription(
            viewHolderFactory = viewHolderFactory {
                CarouselViewHolder(
                    layout(it),
                    carouselRecyclerViewId,
                    layoutBinder as ((CarouselViewHolder<ITEM, ITEM_HOLDER>) -> Unit),
                    itemViewHolderFactory,
                    itemBinder
                )
            },
            data = object : LiveData<List<ITEM>>() {
                override fun onActive() {
                    postValue(items)
                }
            },
            priority = priority
        )
    } as ContentDescriptionProvider<Any>)
}

class CarouselViewHolder<T, VH : RecyclerView.ViewHolder>(
    itemView: View,
    @IdRes val carouselRecyclerViewId: Int = NO_ID,
    val layoutBinder: (CarouselViewHolder<T, VH>) -> Unit = {},
    val itemViewHolderFactory: (parent: ViewGroup) -> VH,
    val itemBinder: (holder: VH, data: T?) -> Unit = { _, _ -> }
) : JubakoViewHolder<List<T>>(itemView) {

    private val recycler: JubakoCarouselRecyclerView by lazy {
        when (carouselRecyclerViewId) {
            NO_ID -> itemView as JubakoCarouselRecyclerView
            else -> itemView.findViewById(carouselRecyclerViewId)
        }
    }

    override fun bind(data: List<T>?) {
        layoutBinder(this)
        recycler.adapter =
            createAdapter(data ?: emptyList())
    }

    private fun createAdapter(data: List<T>): RecyclerView.Adapter<VH> {
        return object : RecyclerView.Adapter<VH>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = itemViewHolderFactory(parent)
            override fun getItemCount(): Int = data.size
            override fun onBindViewHolder(holder: VH, position: Int) {
                itemBinder(holder, data[position])
            }
        }
    }
}

fun pageSize(pageSize: Int) = PaginatedContentLoadingStrategy(pageSize)