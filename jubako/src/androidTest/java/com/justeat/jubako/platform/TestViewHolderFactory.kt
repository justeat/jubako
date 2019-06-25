package com.justeat.jubako.platform

import android.view.LayoutInflater
import android.view.ViewGroup
import com.justeat.jubako.JubakoAdapter
import com.justeat.jubako.JubakoViewHolder
import com.justeat.jubako.test.R

class TestViewHolderFactory : JubakoAdapter.HolderFactory<String> {
    override fun createViewHolder(parent: ViewGroup): JubakoViewHolder<String> {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_simple, parent, false)
        return TestViewHolder(view)
    }
}