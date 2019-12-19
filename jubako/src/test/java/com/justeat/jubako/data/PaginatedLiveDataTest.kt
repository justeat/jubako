package com.justeat.jubako.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.Dispatchers
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class PaginatedLiveDataTest {

    private var state: PaginatedDataState<Item>? = null

    private val mocKError = Error()

    @Rule
    @JvmField
    var rule: TestRule = InstantTaskExecutorRule()

    @Test
    fun loadMorePaginates() {
        // arrange
        val repo = ItemRepository()
        val data = PaginatedLiveData<Item> {
            nextPage = {
                repo.getGetRestaurants(loaded.size).also {
                    hasMore = it.isNotEmpty()
                }
            }
        }
        data.io = Dispatchers.Unconfined

        // act & assert

        data.observeForever {
            state = it
        }

        state?.let { state ->
            // Page 0
            data.loadMore()
            assertEquals(10, state.loaded.size)
            assertEquals(10, state.page.size)

            // Page 1
            data.loadMore()
            assertEquals(20, state.loaded.size)
            assertEquals(10, state.page.size)

            // Page 2
            data.loadMore()
            assertEquals(30, state.loaded.size)
            assertEquals(10, state.page.size)

            // No more pages, loaded size same as last page (no more to load)
            data.loadMore()
            assertEquals(30, state.loaded.size)
            assertEquals(0, state.page.size)
        }
    }

    @Test
    fun loadMorePostsError() {
        // arrange
        val repo = ExceptionalItemRepository()
        val data = PaginatedLiveData<Item> {
            nextPage = {
                repo.getGetRestaurants().also {
                    hasMore = it.isNotEmpty()
                }
            }
        }
        data.io = Dispatchers.Unconfined

        data.observeForever {
            state = it
        }

        data.loadMore()

        state?.let { state ->
            assertEquals(state.error, mocKError)
        }
    }

    class ItemRepository {
        fun getGetRestaurants(startIndex: Int): List<Item> {
            if (startIndex >= 30) {
                return emptyList()
            }

            return (0..9).map { Item("item ${startIndex + it}") }
        }
    }

    inner class ExceptionalItemRepository {
        fun getGetRestaurants(): List<Item> {
            throw mocKError
        }
    }

    data class Item(val name: String)
}
