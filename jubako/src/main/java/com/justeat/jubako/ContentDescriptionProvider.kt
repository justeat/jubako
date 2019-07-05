package com.justeat.jubako

/**
 * Produces an instances of [ContentDescription]
 */
interface ContentDescriptionProvider<T> {
    fun createDescription(): ContentDescription<T>
}

/**
 * Simple way to construct a [ContentDescriptionProvider]
 */
fun <T> descriptionProvider(delegate: () -> ContentDescription<T>): ContentDescriptionProvider<T> {
    return object : ContentDescriptionProvider<T> {
        override fun createDescription() = delegate()
    }
}
