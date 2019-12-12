package com.justeat.jubako.data

import Samples
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import com.justeat.jubako.Jubako
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * A specialised [LiveData] that loads when [loadMore] is called.
 *
 * @sample Samples.samplePaginatedLiveData
 *
 */
class PaginatedLiveData<T>(private val paging: Pager<T>.() -> Unit) : LiveData<PaginatedLiveData.State<T>>(),
    CoroutineScope {
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    var loaded: List<T> = mutableListOf()

    @VisibleForTesting
    internal var io = Dispatchers.IO

    private val pager = Pager { loaded }

    val hasMore: Boolean
        get() = pager.hasMore()

    private var loadingJob: Job? = null

    data class State<T>(
        var loaded: List<T>,
        var page: List<T>,
        var loading: Boolean = false,
        var error: Throwable? = null,
        var accepted: Boolean = false
    ) {
        fun accept(): Boolean {
            if (!accepted) {
                accepted = true
                return true
            }
            return false
        }
    }

    val EMPTY_STATE = State<T>(listOf(), listOf(), false, null, false)
    var state: State<T> = EMPTY_STATE
    var previousState: State<T> = EMPTY_STATE

    fun loadMore() {
        if(state.loaded.isNotEmpty() && !state.accepted) return

        if (loadingJob != null && loadingJob?.isCompleted == false) {
            Jubako.logger.log(TAG, "Busy", "already loading")
            return
        }

        if (pager.hasMore()) {
            Jubako.logger.log(TAG, "Loading More")
            paging(pager)
            previousState = state
            state = State(loaded = loaded, page = emptyList(), loading = true).apply { value = this }

            loadingJob = launch(io) {
                try {
                    val page = pager.nextPage()
                    loaded = loaded + page
                    Jubako.logger.log(TAG, "Page Loaded", "size: ${page.size}, total:${loaded.size}")
                    launch(Dispatchers.Main) {
                        loadingJob = null
                        previousState = state
                        state = State(loaded = loaded, page = page).apply { value = this }
                    }
                } catch (error: Throwable) {
                    Jubako.logger.log(TAG, "Page Error", "${Log.getStackTraceString(error)}")
                    launch(Dispatchers.Main) {
                        loadingJob = null
                        previousState = state
                        state = State(loaded = loaded, page = emptyList(), error = error).apply { value = this }
                    }
                }
            }
        }
    }

    override fun onInactive() {
        super.onInactive()
        previousState = EMPTY_STATE
    }

    class Pager<T>(private val _loaded: () -> List<T>) {
        var loaded: List<T> = _loaded()
            get() = _loaded()
        var hasMore: () -> Boolean = { true }
        lateinit var nextPage: suspend () -> List<T>
    }
}

fun PaginatedLiveData.State<*>?.ready(): Boolean {
    return this != null && !loading && error == null
}

private val TAG = PaginatedLiveData::class.java.simpleName
