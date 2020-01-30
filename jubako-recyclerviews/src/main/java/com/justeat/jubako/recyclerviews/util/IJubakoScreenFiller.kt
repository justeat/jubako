package com.justeat.jubako.recyclerviews.util

import androidx.recyclerview.widget.RecyclerView

interface IJubakoScreenFiller {
    fun attach(recyclerView: RecyclerView)

    companion object {
        val NOOP = object: IJubakoScreenFiller {
            override fun attach(recyclerView: RecyclerView) {}
        }
    }
}
