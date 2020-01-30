package com.justeat.jubako.platform

import android.view.LayoutInflater
import android.view.ViewGroup
import com.justeat.jubako.recyclerviews.adapters.JubakoAdapter
import com.justeat.jubako.recyclerviews.JubakoViewHolder
import com.justeat.jubako.test.R

class TestViewHolderFactory : com.justeat.jubako.recyclerviews.adapters.JubakoAdapter.HolderFactory<String> {
    override fun createViewHolder(parent: ViewGroup): com.justeat.jubako.recyclerviews.JubakoViewHolder<String> {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_simple, parent, false)
        return TestViewHolder(view)
    }
}
