package com.justeat.jubako

import android.view.ViewGroup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ContentDescriptionTest {
    @Test
    @Throws(Exception::class)
    fun are_equal_when_id_the_same() {
        val description1 = ContentDescription(viewSpec = object : com.justeat.jubako.recyclerviews.adapters.JubakoAdapter.HolderFactory<String> {
            override fun createViewHolder(parent: ViewGroup): com.justeat.jubako.recyclerviews.JubakoViewHolder<String> = null!!
        }, id = "abc")

        val description2 = ContentDescription(viewSpec = object : com.justeat.jubako.recyclerviews.adapters.JubakoAdapter.HolderFactory<String> {
            override fun createViewHolder(parent: ViewGroup): com.justeat.jubako.recyclerviews.JubakoViewHolder<String> = null!!
        }, id = "abc")

        assertEquals(description1, description2)
    }

    @Test
    @Throws(Exception::class)
    fun are_not_equal_when_id_not_specified() {
        val description1 = ContentDescription(object : com.justeat.jubako.recyclerviews.adapters.JubakoAdapter.HolderFactory<String> {
            override fun createViewHolder(parent: ViewGroup): com.justeat.jubako.recyclerviews.JubakoViewHolder<String> = null!!
        })

        val description2 = ContentDescription(object : com.justeat.jubako.recyclerviews.adapters.JubakoAdapter.HolderFactory<String> {
            override fun createViewHolder(parent: ViewGroup): com.justeat.jubako.recyclerviews.JubakoViewHolder<String> = null!!
        })

        assertNotEquals(description1, description2)
    }
}
