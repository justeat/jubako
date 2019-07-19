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

    fun loadMore() {
        if (loadingJob != null && loadingJob?.isCompleted == false) {
            Jubako.logger.log(TAG, "Busy", "already loading")
            return
        }

        if (pager.hasMore()) {
            Jubako.logger.log(TAG, "Loading More")
            paging(pager)

          //  postValue(State(loaded = loaded, page = emptyList(), loading = true))

            loadingJob = launch(io) {
                try {
                    val page = pager.nextPage()
                    loadingJob = null
                    loaded = loaded + page
                    Jubako.logger.log(TAG, "Page Loaded", "size: ${page.size}, total:${loaded.size}")
                    postValue(State(loaded = loaded, page = page))
                } catch (error: Throwable) {
                    loadingJob = null
                    Jubako.logger.log(TAG, "Page Error", "${Log.getStackTraceString(error)}")
                    postValue(State(loaded = loaded, page = emptyList(), error = error))
                }
            }
        }
    }

    class Pager<T>(private val _loaded: () -> List<T>) {
        var loaded: List<T> = _loaded()
            get() = _loaded()
        var hasMore: () -> Boolean = { true }
        lateinit var nextPage: suspend () -> List<T>
    }
}

private val TAG = PaginatedLiveData::class.java.simpleName