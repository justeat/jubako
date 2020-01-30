package com.justeat.jubako

/**
 * List of rendered content descriptions
 */
class ContentDescriptionCollection(var listener: Listener? = null) {

    private var descriptions = mutableListOf<ContentDescription<*>>()

    interface Listener {
        fun notifyItemChanged(index: Int, payload: Any?)
        fun notifyItemRangeRemoved(positionStart: Int, itemCount: Int)
    }

    operator fun get(index: Int): ContentDescription<Any> = descriptions[index] as ContentDescription<Any>

    fun size(): Int = descriptions.size

    fun clear() {
        val size = descriptions.size
        descriptions.clear()
        listener?.notifyItemRangeRemoved(0, size)
    }

    /**
     * Add many descriptions to the end of this collection
     */
    fun addAll(descriptions: List<ContentDescription<*>>) {
        if (descriptions.isEmpty()) return
        this.descriptions = (this.descriptions + descriptions).toMutableList()
    }

    fun replace(index: Int, description: ContentDescription<*>) {
        descriptions[index] = description
        notifyItemChanged(index)
    }

    fun contains(description: ContentDescription<*>): Boolean = descriptions.contains(description)

    private fun notifyItemChanged(index: Int) {
        listener?.notifyItemChanged(index, descriptions[index])
    }

    fun asList(): List<ContentDescription<*>> = descriptions.toList()
}
