package com.justeat.jubako.extensions

import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.justeat.jubako.*
import com.justeat.jubako.widgets.JubakoCarouselRecyclerView

typealias ListReceiver = MutableList<ContentDescriptionProvider<Any>>.() -> Unit

/**
 * Simply load with the given descriptions then just call [add], [withView], [withHolder], etc
 * to specify how and what you wish to display conveniently or manually construct and add [ContentDescriptionProvider]'s
 */
fun Jubako.load(descriptionProviders: ListReceiver) {
    load(SimpleJubakoAssembler(descriptionProviders.apply { invoke(mutableListOf()) }))
}

/**
 * Same as [load] but happens on the IO dispatcher (use with caution!)
 */
fun Jubako.loadAsync(descriptionProviders: ListReceiver) {
    load(object : JubakoAssembler {
        override suspend fun assemble(): List<ContentDescriptionProvider<Any>> =
            mutableListOf<ContentDescriptionProvider<Any>>().apply {
                descriptionProviders.invoke(this)
            }
    })
}

/**
 * Simple way to construct a [JubakoAdapter.HolderFactory]
 */
fun <T> viewHolderFactory(delegate: () -> JubakoViewHolder<T>): JubakoAdapter.HolderFactory<T> {
    return object : JubakoAdapter.HolderFactory<T> {
        override fun createViewHolder(parent: ViewGroup) = delegate()
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

fun <T> MutableList<ContentDescriptionProvider<Any>>.withHolder(delegate: () -> JubakoViewHolder<T>) {
    add(descriptionProvider {
        ContentDescription(
            viewHolderFactory = viewHolderFactory { delegate.invoke() })
    } as ContentDescriptionProvider<Any>)
}

fun MutableList<ContentDescriptionProvider<Any>>.withView(delegate: () -> View) {
    add(descriptionProvider {
        ContentDescription(
            viewHolderFactory = viewHolderFactory {
                object : JubakoViewHolder<Any>(delegate.invoke()) {
                    override fun bind(data: Any?) {}
                }
            })
    })
}

fun <T, VH : RecyclerView.ViewHolder> MutableList<ContentDescriptionProvider<Any>>.withCarousel(
    layout: View,
    @IdRes carouselRecyclerViewId: Int,
    list: List<T>,
    priority: Int = 0,
    viewHolderFactory: () -> VH,
    binder: (holder: VH, data: T?) -> Unit
) {
    add(descriptionProvider {
        ContentDescription(
            viewHolderFactory = viewHolderFactory {
                CarouselViewHolder<T, VH>(
                    layout,
                    carouselRecyclerViewId,
                    viewHolderFactory,
                    binder
                )
            },
            data = object : LiveData<List<T>>() {
                override fun onActive() {
                    postValue(list)
                }
            },
            priority = priority
        )
    } as ContentDescriptionProvider<Any>)
}

class CarouselViewHolder<T, VH : RecyclerView.ViewHolder>(
    itemView: View,
    @IdRes val carouselRecyclerViewId: Int,
    val viewHolderFactory: () -> VH,
    val binder: (holder: VH, data: T?) -> Unit
) : JubakoViewHolder<List<T>>(itemView) {
    override fun bind(data: List<T>?) {
        itemView.findViewById<JubakoCarouselRecyclerView>(carouselRecyclerViewId).adapter =
            createAdapter(data ?: emptyList())
    }

    private fun createAdapter(data: List<T>): RecyclerView.Adapter<VH> {
        return object : RecyclerView.Adapter<VH>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = viewHolderFactory()
            override fun getItemCount(): Int = data.size
            override fun onBindViewHolder(holder: VH, position: Int) {
                binder(holder, data[position])
            }
        }
    }
}