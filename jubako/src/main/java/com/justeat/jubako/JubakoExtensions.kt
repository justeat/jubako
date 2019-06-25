package com.justeat.jubako

import android.view.View
import android.view.ViewGroup

fun <T> viewHolderFactory(delegate: () -> JubakoViewHolder<T>): JubakoAdapter.HolderFactory<T> {
    return object : JubakoAdapter.HolderFactory<T> {
        override fun createViewHolder(parent: ViewGroup) = delegate()
    }
}


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
    } as ContentDescriptionProvider<Any>)
}