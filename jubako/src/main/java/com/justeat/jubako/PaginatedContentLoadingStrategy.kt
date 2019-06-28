package com.justeat.jubako

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

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
        source: MutableList<ContentDescription<Any>>,
        destination: ContentDescriptionCollection,
        onLoaded: (hasMore: Boolean) -> Boolean
    ) {
        logger.log(TAG, "Load", "onReload: $reset, loading observers: ${loadingData.size}")
        if(BuildConfig.DEBUG) {
            TEMP_integrityCheck(destination, source)
        }

        if (loadingData.size > 0) {
            logger.log(TAG, "Busy", "already loading")
            return
        }
        reset = false
        loadDescriptions(lifecycleOwner, source, destination, onLoaded)
    }

    private fun TEMP_integrityCheck(
        destination: ContentDescriptionCollection,
        source: MutableList<ContentDescription<Any>>
    ) {
        if (destination.size() > 0) {
            for (i in 0 until destination.size()) {
                if (source[i] != destination[i]) {
                    throw RuntimeException("Failed integrity check")
                }
            }
        }
    }

    private fun loadDescriptions(
        lifecycleOwner: LifecycleOwner,
        source: MutableList<ContentDescription<Any>>,
        destination: ContentDescriptionCollection,
        onLoaded: (hasMore: Boolean) -> Boolean
    ) {
        logger.log(TAG, "Load Descriptions", "onReload: $reset")
        if (destination.size() == source.size) {
            logger.log(TAG, "Stop Loading", "dest size: ${destination.size()}, source size: ${source.size}")
            onLoaded(false)
            return
        } else if (destination.size() > source.size) {
            throw RuntimeException("destination can not be great than source")
        }

        //
        // Lets get the next descriptionProvider to load...
        //
        currentDescription = source[destination.size() + pagedDescriptions.size]
        logger.log(TAG, "Load Description", "$currentDescription")

        currentDescription?.data?.let {
            logger.log(TAG, "Observe Description", "$currentDescription")
            val observer = LoadDescriptionObserver(it, lifecycleOwner, source, destination, onLoaded)
            loadingData[observer] = it
            it.observe(lifecycleOwner, observer)
        } ?: proceed(lifecycleOwner, source, destination, onLoaded)
    }

    private fun proceed(
        lifecycleOwner: LifecycleOwner,
        source: MutableList<ContentDescription<Any>>,
        destination: ContentDescriptionCollection,
        onLoaded: (hasMore: Boolean) -> Boolean
    ) {
        logger.log(TAG, "Proceed", "onReload: $reset")
        if (!reset) {
            pagedDescriptions.add(currentDescription!!)
            logger.log(
                TAG,
                "Proceed",
                "paged count:${pagedDescriptions.size}, dest size: ${destination.size()}, source size: ${source.size}"
            )
            if (pagedDescriptions.size == pageSize || (destination.size() + pagedDescriptions.size) == source.size) {
                dispatchPage(destination)
                val hasMore = destination.size() < source.size
                if (onLoaded(hasMore) && hasMore) {
                    loadDescriptions(lifecycleOwner, source, destination, onLoaded)
                }
            } else {
                loadDescriptions(lifecycleOwner, source, destination, onLoaded)
            }
        }
    }

    private fun dispatchPage(destination: ContentDescriptionCollection) {
        logger.log(TAG, "Dispatch Page", "size: ${pagedDescriptions.size}")
        destination.addAll(pagedDescriptions)
        pagedDescriptions.clear()
    }

    override fun reload(lifecycleOwner: LifecycleOwner, position: Int, descriptions: ContentDescriptionCollection) {
        val item = descriptions[position]
        val data = item.data

        data?.let {
            val observer = ReloadDescriptionObserver(it, position, descriptions)
            loadingData[observer] = it
            it.observe(lifecycleOwner, observer)
        }
    }

    inner class LoadDescriptionObserver(
        private val data: LiveData<Any>,
        private val lifecycleOwner: LifecycleOwner,
        private val source: MutableList<ContentDescription<Any>>,
        private val destination: ContentDescriptionCollection,
        private val onLoaded: (hasMore: Boolean) -> Boolean
    ) : Observer<Any>,
        Cancellable {
        override var cancelled = false
        override fun onChanged(t: Any?) {
            loadingData.remove(this)
            data.removeObserver(this)
            if (!cancelled) {
                proceed(lifecycleOwner, source, destination, onLoaded)
            } else {
                logger.log(TAG, "Reload Cancelled", "$currentDescription")
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

    interface Cancellable {
        var cancelled: Boolean
    }
}

private const val DEFAULT_PAGE_SIZE = 10
private val TAG = PaginatedContentLoadingStrategy::class.java.simpleName
