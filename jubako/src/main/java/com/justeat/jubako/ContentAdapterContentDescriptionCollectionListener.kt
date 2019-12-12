package com.justeat.jubako

class ContentAdapterContentDescriptionCollectionListener(
    private val contentAdapter: JubakoAdapter
) : ContentDescriptionCollection.Listener {
    var logger = Jubako.logger
    override fun notifyItemChanged(index: Int, payload: Any?) {
        logger.log(TAG, "Notify Item Changed", "index: $index")
        contentAdapter.notifyItemChanged(index, payload)
    }

    override fun notifyItemInserted(index: Int) {
        logger.log(TAG, "Notify Item Inserted", "index: $index")
        contentAdapter.notifyItemInserted(index)
    }

    override fun notifyItemRemoved(index: Int) {
        logger.log(TAG,"Notify Item Removed", "index: $index")
        contentAdapter.notifyItemRemoved(index)
    }

    override fun notifyDataSetChanged() {
        logger.log(TAG,"Notify Data Changed")
        contentAdapter.notifyDataSetChanged()
    }

    override fun notifyItemRangeInserted(positionStart: Int, itemCount: Int) {
        logger.log(TAG,
            "Notify Item Range Inserted",
            "positionStart: $positionStart, itemCount: $itemCount"
        )
        contentAdapter.notifyItemRangeInserted(positionStart, itemCount)
    }

    override fun notifyItemRangeRemoved(positionStart: Int, itemCount: Int) {
        logger.log(TAG,
            "Notify Item Range Removed",
            "positionStart: $positionStart, itemCount: $itemCount"
        )
        contentAdapter.notifyItemRangeRemoved(positionStart, itemCount)
    }
}

private val TAG = JubakoAdapter::class.java.simpleName