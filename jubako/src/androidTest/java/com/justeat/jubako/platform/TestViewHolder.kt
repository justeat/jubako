package com.justeat.jubako.platform

import android.view.View
import android.widget.TextView
import com.justeat.jubako.recyclerviews.JubakoViewHolder
import com.justeat.jubako.test.R

class TestViewHolder(view: View) : com.justeat.jubako.recyclerviews.JubakoViewHolder<String>(view) {
    override fun bind(data: String?) {
        itemView.findViewById<TextView>(R.id.itemText).apply {
            text = data
            setOnClickListener {
                reload()
            }
        }
    }
}
