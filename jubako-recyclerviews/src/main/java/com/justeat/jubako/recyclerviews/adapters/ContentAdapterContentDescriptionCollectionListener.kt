package com.justeat.jubako.recyclerviews.adapters

import com.justeat.jubako.ContentDescriptionCollection
import com.justeat.jubako.Jubako

class ContentAdapterContentDescriptionCollectionListener(
    private val contentAdapter: JubakoAdapter
) : ContentDescriptionCollection.Listener {
    var logger = Jubako.logger
    override fun notifyItemChanged(index: Int, payload: Any?) {
        logger.log(TAG, "Notify Item Changed", "index: $index")
        contentAdapter.notifyItemChanged(index, payload)
    }

    override fun notifyItemRangeRemoved(positionStart: Int, itemCount: Int) {
        logger.log(
            TAG,
            "Notify Item Range Removed",
            "positionStart: $positionStart, itemCount: $itemCount"
        )
        contentAdapter.notifyItemRangeRemoved(positionStart, itemCount)
    }
}

private val TAG = JubakoAdapter::class.java.simpleName
