package com.justeat.jubako

/**
 * Wrapper round list to manage the insertion/deletion of loadedContentDescriptions blocks
 * and a convenient listener for adapter integrations
 */
class ContentDescriptionCollection(var listener: Listener? = null) {

    private val mContentDescriptions = mutableListOf<ContentDescription<*>>()

    interface Listener {
        fun notifyItemChanged(index: Int, payload: Any?)

        fun notifyItemInserted(index: Int)

        fun notifyItemRemoved(index: Int)

        fun notifyDataSetChanged()
        fun notifyItemRangeInserted(positionStart: Int, itemCount: Int)
        fun notifyItemRangeRemoved(positionStart: Int, itemCount: Int)
    }

    operator fun get(index: Int): ContentDescription<Any> {
        return mContentDescriptions.get(index) as ContentDescription<Any>
    }

    fun size(): Int {
        return mContentDescriptions.size
    }

    fun clear() {
        val size = mContentDescriptions.size
        mContentDescriptions.clear()
        notifyItemRangeRemoved(0, size)
    }

    /**
     * Add a descriptionProvider to the end of this collection
     */
    fun add(block: ContentDescription<*>) {
        mContentDescriptions.add(block)
        notifyItemInserted(mContentDescriptions.size - 1)
    }

    /**
     * Add many descriptions to the end of this collection
     */
    fun addAll(descriptions: List<ContentDescription<*>>) {
        if (descriptions.isEmpty()) return

        val positionStart = mContentDescriptions.size
        val itemCount = descriptions.size
        mContentDescriptions.addAll(descriptions)

        notifyItemRangeInserted(positionStart, itemCount)
    }

    fun replace(index: Int, description: ContentDescription<*>) {
        mContentDescriptions[index] = description
        notifyItemChanged(index)
    }

    fun contains(description: ContentDescription<*>): Boolean {
        return mContentDescriptions.contains(description)
    }

    private fun notifyItemInserted(index: Int) {
        listener?.notifyItemInserted(index)
    }

    private fun notifyItemRangeInserted(positionStart: Int, itemCount: Int) {
        listener?.notifyItemRangeInserted(positionStart, itemCount)
    }

    private fun notifyItemChanged(index: Int) {
        listener?.notifyItemChanged(index, mContentDescriptions[index])
    }

    private fun notifyItemRangeRemoved(positionStart: Int, itemCount: Int) {
        listener?.notifyItemRangeRemoved(positionStart, itemCount)
    }
}