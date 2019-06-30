package com.justeat.jubako.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

open class JubakoRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    private var scrollPointerId = -1
    private var pointTouchX = 0
    private var pointTouchY = 0
    private var touchSlop = 0

    init {
        touchSlop = ViewConfiguration.get(context).scaledTouchSlop
        layoutManager = LinearLayoutManager(context)
    }

    override fun setScrollingTouchSlop(slopConstant: Int) {
        super.setScrollingTouchSlop(slopConstant)

        val viewConfiguration = ViewConfiguration.get(context)
        when (slopConstant) {
            TOUCH_SLOP_DEFAULT -> touchSlop = viewConfiguration.scaledTouchSlop
            TOUCH_SLOP_PAGING -> touchSlop = viewConfiguration.scaledPagingTouchSlop
        }
    }

    override fun onInterceptTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) {
            return false
        }

        val action = event.actionMasked
        val actionIndex = event.actionIndex
        val scale = 0.5f

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                scrollPointerId = event.getPointerId(0)
                pointTouchX = Math.round(event.x + scale)
                pointTouchY = Math.round(event.y + scale)
                return super.onInterceptTouchEvent(event)
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                scrollPointerId = event.getPointerId(actionIndex)
                pointTouchX = Math.round(event.getX(actionIndex) + scale)
                pointTouchY = Math.round(event.getY(actionIndex) + scale)
                return super.onInterceptTouchEvent(event)
            }

            MotionEvent.ACTION_MOVE -> {
                val index = event.findPointerIndex(scrollPointerId)
                if (index < 0) {
                    return false
                }

                val x = Math.round(event.getX(index) + scale)
                val y = Math.round(event.getY(index) + scale)
                if (scrollState != SCROLL_STATE_DRAGGING) {
                    val dx = x - pointTouchX
                    val dy = y - pointTouchY
                    var startScroll = false
                    if (layoutManager?.canScrollHorizontally() == true && Math.abs(dx) > touchSlop && (layoutManager?.canScrollVertically() == true || Math.abs(
                            dx
                        ) > Math.abs(dy))
                    ) {
                        startScroll = true
                    }
                    if (layoutManager?.canScrollVertically() == true && Math.abs(dy) > touchSlop && (layoutManager?.canScrollHorizontally() == true || Math.abs(
                            dy
                        ) > Math.abs(dx))
                    ) {
                        startScroll = true
                    }
                    return startScroll && super.onInterceptTouchEvent(event)
                }
                return super.onInterceptTouchEvent(event)
            }
            else -> {
                return super.onInterceptTouchEvent(event)
            }
        }
    }
}
