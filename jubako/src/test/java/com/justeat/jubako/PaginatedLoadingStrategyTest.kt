package com.justeat.jubako

import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class PaginatedLoadingStrategyTest {

    @Mock
    lateinit var lifecycleOwner: LifecycleOwner

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    @Throws(Exception::class)
    fun load_loads_page_at_a_time() {
        val paginatedLoadingStrategy = PaginatedContentLoadingStrategy(5)

        val source = mutableListOf<ContentDescription<Any>>()
        val destination = ContentDescriptionCollection()
        (0..21).forEach {
            source.add(ContentDescription(object : JubakoAdapter.HolderFactory<String> {
                override fun createViewHolder(parent: ViewGroup): JubakoViewHolder<String> = null!!
            }) as ContentDescription<Any>)
        }

        // First five
        paginatedLoadingStrategy.load(
            lifecycleOwner,
            source = source,
            destination = destination,
            onLoaded = { hasMore: Boolean ->
                assertEquals(5, destination.size())
                assertTrue(hasMore)
                false
            })

        // Second five (now 10)
        paginatedLoadingStrategy.load(
            lifecycleOwner,
            source = source,
            destination = destination,
            onLoaded = { hasMore: Boolean ->
                assertEquals(10, destination.size())
                assertTrue(hasMore)
                false
            })

        // Third five (now 15)
        paginatedLoadingStrategy.load(
            lifecycleOwner,
            source = source,
            destination = destination,
            onLoaded = { hasMore: Boolean ->
                assertEquals(15, destination.size())
                assertTrue(hasMore)
                false
            })

        // Fourth five (now 20)
        paginatedLoadingStrategy.load(
            lifecycleOwner,
            source = source,
            destination = destination,
            onLoaded = { hasMore: Boolean ->
                assertEquals(20, destination.size())
                assertTrue(hasMore)
                false
            })

        // Last two (now 22)
        paginatedLoadingStrategy.load(
            lifecycleOwner,
            source = source,
            destination = destination,
            onLoaded = { hasMore: Boolean ->
                assertEquals(22, destination.size())
                assertFalse(hasMore)
                false
            })

        // Calling again should do nothing but invoke the callback
        paginatedLoadingStrategy.load(
            lifecycleOwner,
            source = source,
            destination = destination,
            onLoaded = { hasMore: Boolean ->
                assertEquals(22, destination.size())
                assertFalse(hasMore)
                false
            })
    }
}