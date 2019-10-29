package com.justeat.jubako.util

import android.graphics.Rect
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.justeat.jubako.Jubako
import com.justeat.jubako.widgets.JubakoRecyclerView

open class JubakoScreenFiller(
    val orientation: Orientation,
    val logger: Jubako.Logger,
    val hasMore: () -> Boolean,
    val loadMore: () -> Unit
) : IJubakoScreenFiller {

    enum class Orientation {
        HORIZONTAL,
        VERTICAL
    }

    override fun attach(recyclerView: RecyclerView) {
        (recyclerView as JubakoRecyclerView).onDrawComplete = {
            val lm = (recyclerView.layoutManager as LinearLayoutManager)
            val lastVisibleItemPos = lm.findLastVisibleItemPosition()
            logger.log(TAG, "Recycler child count: ${recyclerView.childCount}")
            logger.log(TAG, "Initial Fill $orientation", "On Draw (lastVisibleItemPos: $lastVisibleItemPos)")
            if (lastVisibleItemPos != RecyclerView.NO_POSITION) {
                val view = lm.findViewByPosition(lastVisibleItemPos)
                if (view != null) {
                    val rect = Rect()
                    view.getLocalVisibleRect(rect)

                    val filled = when (orientation) {
                        Orientation.HORIZONTAL -> {
                            rect.right >= recyclerView.measuredWidth
                        }
                        else -> {
                            rect.bottom >= recyclerView.measuredHeight
                        }
                    }

                    val extent = when (orientation) {
                        Orientation.HORIZONTAL -> {
                            recyclerView.measuredWidth
                        }
                        else -> {
                            recyclerView.measuredHeight
                        }
                    }

                    val hasMore = hasMore()
                    if (!hasMore || filled) {
                        logger.log(
                            TAG,
                            "Initial Fill $orientation",
                            "Complete pos: $lastVisibleItemPos, extent: $extent, rect: $rect, hasMore: $hasMore"
                        )
                    } else {
                        logger.log(
                            TAG,
                            "Initial Fill $orientation",
                            "pos: $lastVisibleItemPos, extent: $extent, rect: $rect"
                        )
                        loadMore()
                    }
                } else {
                    loadMore()
                }
            } else {
                loadMore()
            }
        }
    }
}

private val TAG = JubakoScreenFiller::class.java.simpleName