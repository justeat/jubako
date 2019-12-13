package com.justeat.jubako

import androidx.lifecycle.LifecycleOwner

interface ContentLoadingStrategy {
    /**
     * @param onLoaded A callback invoked when the strategy has loaded, clients should return true if they wish to load more
     */
    fun load(
        lifecycleOwner: LifecycleOwner,
        data: Jubako.Data,
        onLoaded: (hasMore: Boolean) -> Boolean = { false },
        onError: (error: Throwable) -> Unit = {}
    )

    fun reload(lifecycleOwner: LifecycleOwner, position: Int, descriptions: ContentDescriptionCollection)
    fun reset()
}
