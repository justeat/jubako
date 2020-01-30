package com.justeat.jubako

import android.view.ViewGroup
import org.junit.Assert.assertEquals
import org.junit.Test

class ContentDescriptionCollectionTest {
    @Test
    @Throws(Exception::class)
    fun addAllPersistsEntries() {
        val collection = ContentDescriptionCollection()

        val descriptions = mutableListOf<ContentDescription<*>>()
        descriptions.add(ContentDescription(object : com.justeat.jubako.recyclerviews.adapters.JubakoAdapter.HolderFactory<String> {
            override fun createViewHolder(parent: ViewGroup): com.justeat.jubako.recyclerviews.JubakoViewHolder<String> = null!!
        }))
        descriptions.add(ContentDescription(object : com.justeat.jubako.recyclerviews.adapters.JubakoAdapter.HolderFactory<String> {
            override fun createViewHolder(parent: ViewGroup): com.justeat.jubako.recyclerviews.JubakoViewHolder<String> = null!!
        }))
        descriptions.add(ContentDescription(object : com.justeat.jubako.recyclerviews.adapters.JubakoAdapter.HolderFactory<String> {
            override fun createViewHolder(parent: ViewGroup): com.justeat.jubako.recyclerviews.JubakoViewHolder<String> = null!!
        }))

        collection.addAll(descriptions)

        assertEquals(3, collection.size())
    }
}
