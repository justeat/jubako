package com.justeat.jubako

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.justeat.jubako.data.EmptyLiveData
import com.justeat.jubako.data.PaginatedDataState
import com.justeat.jubako.data.PaginatedLiveData

open class PaginatedContentLoadingStrategy(private val pageSize: Int = DEFAULT_PAGE_SIZE) : ContentLoadingStrategy {
    private var reset: Boolean = false

    private val loadingData = hashMapOf<Cancellable, LiveData<Any>>()

    var logger = Jubako.logger

    override fun reset() {
        logger.log(TAG, "Reset", "$this")
        reset = true
        currentDescription = null
        loadingData.apply {
            forEach { it.key.cancelled = true }
            clear()
        }
        pagedDescriptions.clear()
    }

    private var currentDescription: ContentDescription<Any>? = null
    private var pagedDescriptions = mutableListOf<ContentDescription<Any>>()

    override fun load(
        lifecycleOwner: LifecycleOwner,
        data: Jubako.Data,
        callback: (state: PaginatedDataState<ContentDescription<Any>>, hasMore: Boolean) -> Boolean
    ) {
        logger.log(TAG, "Load", "onReload: $reset, loading observers: ${loadingData.size}")
//        if (BuildConfig.DEBUG) {
//            TEMP_integrityCheck(data.source, data.destination)
//        }

        if (loadingData.size > 0) {
            logger.log(TAG, "Busy", "already loading")
            return
        }
        reset = false
        loadDescriptions(lifecycleOwner, data, callback)
    }

    private fun TEMP_integrityCheck(
        source: MutableList<ContentDescription<Any>>,
        destination: ContentDescriptionCollection
    ) {
        if (destination.size() > 0) {
            for (i in 0 until destination.size()) {
                if (source[i] != destination[i]) {
                    throw RuntimeException("Failed integrity check, source size ${source.size}, dest size: ${destination.size()}, source: ${source[i]}, dest: ${destination[i]}")
                }
            }
        }
    }

    private fun loadDescriptions(
        lifecycleOwner: LifecycleOwner,
        data: Jubako.Data,
        callback: (state: PaginatedDataState<ContentDescription<Any>>, hasMore: Boolean) -> Boolean
    ) {
        logger.log(TAG, "Load Descriptions", "onReload: $reset")
        callback(
            PaginatedDataState(
                loaded = data.destination.mContentDescriptions.toList() as List<ContentDescription<Any>>,
                page = listOf(),
                loading = true,
                error = null,
                accepted = false
            ), true
        )

        if (data.destination.size() == data.source.size) {
            if (data.hasMore()) {
                val moreData = data.assembleMore()
                val observer = LoadMoreObserver(moreData, data, lifecycleOwner, this, callback)
                loadingData[observer] = moreData as LiveData<Any>
                moreData.observe(lifecycleOwner, observer)
            } else {
                logger.log(
                    TAG,
                    "Stop Loading",
                    "dest size: ${data.destination.size()}, source size: ${data.source.size}"
                )
                callback(
                    PaginatedDataState(
                        loaded = data.destination.mContentDescriptions.toList() as List<ContentDescription<Any>>,
                        page = listOf(),
                        loading = false,
                        error = null,
                        accepted = false
                    ), false
                )
            }
            return
        } else if (data.destination.size() > data.source.size) {
            throw RuntimeException("destination can not be great than source")
        }

        loadNext(data, lifecycleOwner, callback)
    }

    private fun loadNext(
        data: Jubako.Data,
        lifecycleOwner: LifecycleOwner,
        callback: (state: PaginatedDataState<ContentDescription<Any>>, hasMore: Boolean) -> Boolean
    ) {
        //
        // Lets get the next descriptionProvider to load...
        //
        currentDescription = data.source[data.destination.size() + pagedDescriptions.size]
        logger.log(TAG, "Load Description", "$currentDescription")

        currentDescription?.let { description ->
            description.data.let {
                when (it) {
                    is EmptyLiveData -> {
                        proceed(lifecycleOwner, data, callback)
                    }
                    else -> {
                        logger.log(TAG, "Observe Description", "$currentDescription")
                        val observer = LoadDescriptionObserver(it, lifecycleOwner, data, callback)
                        loadingData[observer] = it
                        it.observe(lifecycleOwner, observer)
                        if (it is PaginatedLiveData<*>) {
                            // Initial load (first page)
                            it.loadMore()
                        }
                    }
                }
            }
        }
    }

    private fun proceed(
        lifecycleOwner: LifecycleOwner,
        data: Jubako.Data,
        callback: (state: PaginatedDataState<ContentDescription<Any>>, hasMore: Boolean) -> Boolean
    ) {
        logger.log(TAG, "Proceed", "onReload: $reset")
        if (!reset) {
            pagedDescriptions.add(currentDescription!!)
            logger.log(
                TAG,
                "Proceed",
                "paged count:${pagedDescriptions.size}, dest size: ${data.destination.size()}, source size: ${data.source.size}"
            )

            when {
                hasPageWorth(data) -> {
                    data.destination.addAll(pagedDescriptions)
                    val page = pagedDescriptions.toList()
                    pagedDescriptions = mutableListOf()
                    val hasMore = data.destination.size() < data.source.size || data.hasMore()
                    if (callback(
                            PaginatedDataState(
                                data.destination.mContentDescriptions as List<ContentDescription<Any>>,
                                page,
                                false,
                                null,
                                false
                            ),
                            hasMore
                        ) && hasMore
                    ) {
                        loadDescriptions(lifecycleOwner, data, callback)
                    }
                }
                else -> loadDescriptions(lifecycleOwner, data, callback)
            }
        }
    }

    private fun hasPageWorth(data: Jubako.Data) =
        pagedDescriptions.size == pageSize ||
            (data.destination.size() + pagedDescriptions.size) == data.source.size

    override fun reload(lifecycleOwner: LifecycleOwner, position: Int, descriptions: ContentDescriptionCollection) {
        val item = descriptions[position]
        val data = item.data

        data.let {
            val observer = ReloadDescriptionObserver(it, position, descriptions)
            loadingData[observer] = it
            it.observe(lifecycleOwner, observer)
        }
    }

    inner class LoadDescriptionObserver(
        private val data: LiveData<Any>,
        private val lifecycleOwner: LifecycleOwner,
        private val jubakoData: Jubako.Data,
        private val callback: (state: PaginatedDataState<ContentDescription<Any>>, hasMore: Boolean) -> Boolean
    ) : Observer<Any>,
        Cancellable {
        override var cancelled = false
        override fun onChanged(data: Any?) {
            proceed()
        }

        private fun proceed() {
            loadingData.remove(this)
            this.data.removeObserver(this)
            if (!cancelled) {
                proceed(lifecycleOwner, jubakoData, callback)
            } else {
                logger.log(TAG, "Load Cancelled", "$currentDescription")
            }
        }
    }

    inner class ReloadDescriptionObserver(
        private val data: LiveData<Any>,
        private val position: Int,
        private val descriptions: ContentDescriptionCollection
    ) : Observer<Any>, Cancellable {
        override var cancelled = false
        override fun onChanged(t: Any?) {
            loadingData.remove(this)
            data.removeObserver(this)
            if (!cancelled) {
                if (position < descriptions.size()) {
                    descriptions.replace(position, descriptions[position])
                }
            } else {
                logger.log(TAG, "Reload Cancelled", "${descriptions[position]}")
            }
        }
    }

    inner class LoadMoreObserver(
        private val moreData: LiveData<Jubako.State>,
        private val data: Jubako.Data,
        private val lifecycleOwner: LifecycleOwner,
        private val strategy: PaginatedContentLoadingStrategy,
        private val callback: (state: PaginatedDataState<ContentDescription<Any>>, hasMore: Boolean) -> Boolean
    ) : Observer<Jubako.State>,
        Cancellable {
        override var cancelled = false
        override fun onChanged(state: Jubako.State?) {
            moreData.removeObserver(this)
            loadingData.remove(this)
            when (state) {
                is Jubako.State.Assembled, Jubako.State.Assembling -> {
                    if (!cancelled) {
                        strategy.loadNext(data, lifecycleOwner, callback)
                    } else {
                        logger.log(TAG, "Load More Cancelled", "$currentDescription")
                    }
                }
                is Jubako.State.AssembleError -> {
                    callback(
                        PaginatedDataState(
                            loaded = data.destination.mContentDescriptions.toList() as List<ContentDescription<Any>>,
                            page = listOf(),
                            loading = false,
                            error = state.error,
                            accepted = false
                        ),
                        false
                    )
                }
            }
        }
    }

    interface Cancellable {
        var cancelled: Boolean
    }
}

private const val DEFAULT_PAGE_SIZE = 10
private val TAG = PaginatedContentLoadingStrategy::class.java.simpleName
