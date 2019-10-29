package com.justeat.jubako.util

import androidx.recyclerview.widget.RecyclerView

interface IJubakoScreenFiller {
    fun attach(recyclerView: RecyclerView)

    companion object {
        val NOOP = object: IJubakoScreenFiller {
            override fun attach(recyclerView: RecyclerView) {}
        }
    }
}