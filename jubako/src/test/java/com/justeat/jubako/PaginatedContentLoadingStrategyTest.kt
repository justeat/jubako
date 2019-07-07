package com.justeat.jubako

import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class PaginatedContentLoadingStrategyTest {

    @Mock
    lateinit var lifecycleOwner: LifecycleOwner

    @Mock
    lateinit var scope: CoroutineScope

    @Mock
    lateinit var assembler: JubakoAssembler

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    @Throws(Exception::class)
    fun load_loads_page_at_a_time() {
        val paginatedLoadingStrategy = PaginatedContentLoadingStrategy(5)

        val data = Jubako.Data(scope, assembler)

        repeat((0..21).count()) {
            data.source.add(ContentDescription(object : JubakoAdapter.HolderFactory<String> {
                override fun createViewHolder(parent: ViewGroup): JubakoViewHolder<String> = null!!
            }) as ContentDescription<Any>)
        }

        // First five
        paginatedLoadingStrategy.load(
            lifecycleOwner,
            data,
            onLoaded = { hasMore: Boolean ->
                assertEquals(5, data.destination.size())
                assertTrue(hasMore)
                false
            })

        // Second five (now 10)
        paginatedLoadingStrategy.load(
            lifecycleOwner,
            data,
            onLoaded = { hasMore: Boolean ->
                assertEquals(10, data.destination.size())
                assertTrue(hasMore)
                false
            })

        // Third five (now 15)
        paginatedLoadingStrategy.load(
            lifecycleOwner,
            data,
            onLoaded = { hasMore: Boolean ->
                assertEquals(15, data.destination.size())
                assertTrue(hasMore)
                false
            })

        // Fourth five (now 20)
        paginatedLoadingStrategy.load(
            lifecycleOwner,
            data,
            onLoaded = { hasMore: Boolean ->
                assertEquals(20, data.destination.size())
                assertTrue(hasMore)
                false
            })

        // Last two (now 22)
        paginatedLoadingStrategy.load(
            lifecycleOwner,
            data,
            onLoaded = { hasMore: Boolean ->
                assertEquals(22, data.destination.size())
                assertFalse(hasMore)
                false
            })

        // Calling again should do nothing but invoke the callback
        paginatedLoadingStrategy.load(
            lifecycleOwner,
            data,
            onLoaded = { hasMore: Boolean ->
                assertEquals(22, data.destination.size())
                assertFalse(hasMore)
                false
            })
    }
}