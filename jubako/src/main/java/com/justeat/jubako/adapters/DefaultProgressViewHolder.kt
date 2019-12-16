package com.justeat.jubako.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.justeat.jubako.R

class DefaultProgressViewHolder(parent: ViewGroup, layoutId: Int = R.layout.default_progress) :
    RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
    ), ProgressView {
    override fun setRetryCallback(retry: () -> Unit) {
        itemView.findViewById<View>(R.id.button_retry).setOnClickListener {
            retry.invoke()
        }
    }

    override fun onProgress() {
        itemView.findViewById<View>(R.id.progress).visibility =
            View.VISIBLE
        itemView.findViewById<View>(R.id.button_retry).visibility =
            View.GONE
    }

    override fun onError(error: Throwable) {
        itemView.findViewById<View>(R.id.progress).visibility = View.GONE
        itemView.findViewById<View>(R.id.button_retry).visibility =
            View.VISIBLE
    }
}