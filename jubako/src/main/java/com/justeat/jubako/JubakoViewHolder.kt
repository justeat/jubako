package com.justeat.jubako

import android.view.View
import androidx.annotation.IdRes
import androidx.recyclerview.widget.RecyclerView

abstract class JubakoViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {

    sealed class Event(open val contentId: String) {
        data class Click(override val contentId: String, @IdRes val viewId: Int) : Event(contentId)
    }

    internal var reloader: ((position: Int, payload: Any?) -> Unit)? = null
    internal var onClickDelegate: ((id: Int) -> Unit)? = null
    internal var description: ContentDescription<T>? = null
    internal var data: T? = null

    var logger = Jubako.logger

    fun reload(payload: Any? = null) {
        val position = adapterPosition
        logger?.log("ViewHolder Reload", "position:$position, type: ${this.javaClass.simpleName}")
        reloader?.takeIf { position != RecyclerView.NO_POSITION }?.invoke(position, payload)
    }

    fun postClickEvent(@IdRes id: Int) {
        onClickDelegate?.invoke(id)
    }

    abstract fun bind(data: T?)
}
