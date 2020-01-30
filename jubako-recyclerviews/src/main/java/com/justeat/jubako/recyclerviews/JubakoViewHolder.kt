package com.justeat.jubako.recyclerviews

import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.recyclerview.widget.RecyclerView
import com.justeat.jubako.ContentDescription
import com.justeat.jubako.Jubako

/**
 * Like a standard [RecyclerView.ViewHolder] with some extra Jubako features
 */
abstract class JubakoViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {

    sealed class Event(open val contentId: String) {
        data class Click(override val contentId: String, @IdRes val viewId: Int) : Event(contentId)
    }

    internal var reloader: ((position: Int, payload: Any?) -> Unit)? = null
    internal var onClickDelegate: ((id: Int) -> Unit)? = null
    internal var description: ContentDescription<T>? = null
    internal var data: T? = null

    var logger = Jubako.logger

    /**
     * Reload the backing [ContentDescription] with an optional [payload]
     */
    fun reload(payload: Any? = null) {
        val position = adapterPosition
        logger.log("ViewHolder Reload", "position:$position, type: ${this.javaClass.simpleName}")
        reloader?.takeIf { position != RecyclerView.NO_POSITION }?.invoke(position, payload)
    }

    /**
     * Post an event down to [JubakoAdapter.onViewHolderEvent] (which can be set with a callback function,
     * passing the [id] that represents the view that was clicked (although could be any id)
     */
    fun postClickEvent(@IdRes id: Int) {
        onClickDelegate?.invoke(id)
    }

    /**
     * Will be called when the corresponding [ContentDescription] is loaded and bound by the adapter,
     * will not be called if the previous call data is the same as the given [data]
     */
    abstract fun bind(data: T?)
}

/**
 * Simple way to construct a [JubakoAdapter.HolderFactory]
 */
fun <T> viewHolderFactory(delegate: (parent: ViewGroup) -> JubakoViewHolder<T>): com.justeat.jubako.recyclerviews.adapters.JubakoAdapter.HolderFactory<T> {
    return object : com.justeat.jubako.recyclerviews.adapters.JubakoAdapter.HolderFactory<T> {
        override fun createViewHolder(parent: ViewGroup) = delegate(parent)
    }
}
