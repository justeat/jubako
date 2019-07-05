package com.justeat.jubako.extensions

import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import com.justeat.jubako.*
import java.util.*

typealias ListReceiver = MutableList<ContentDescriptionProvider<Any>>.() -> Unit
typealias JubakoMutableList = MutableList<ContentDescriptionProvider<Any>>

fun ListReceiver.assemble(): JubakoAssembler {
    return object : JubakoAssembler {
        override suspend fun assemble(): List<ContentDescriptionProvider<Any>> =
            mutableListOf<ContentDescriptionProvider<Any>>().apply {
                invoke(this)
            }
    }
}

fun JubakoMutableList.add(provider: ContentDescriptionProvider<*>) {
    add(provider as ContentDescriptionProvider<Any>)
}

fun <T> JubakoMutableList.add(delegate: () -> ContentDescription<T>) {
    add(descriptionProvider { delegate.invoke() } as ContentDescriptionProvider<Any>)
}

fun <T> JubakoMutableList.addHolder(delegate: () -> JubakoViewHolder<T>) {
    add(descriptionProvider {
        ContentDescription(
            viewHolderFactory = viewHolderFactory { delegate.invoke() })
    })
}

fun JubakoMutableList.addView(delegate: (parent: ViewGroup) -> View) {
    add(descriptionProvider {
        ContentDescription(
            viewHolderFactory = viewHolderFactory {
                object : JubakoViewHolder<Any>(delegate.invoke(it)) {
                    override fun bind(data: Any?) {}
                }
            })
    })
}

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

