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
    private var touchSlopType = 0

    init {
        touchSlopType = ViewConfiguration.get(context).scaledTouchSlop
        layoutManager = LinearLayoutManager(context)
    }

    override fun setScrollingTouchSlop(slopConstant: Int) {
        super.setScrollingTouchSlop(slopConstant)

        val viewConfiguration = ViewConfiguration.get(context)
        when (slopConstant) {
            TOUCH_SLOP_DEFAULT -> touchSlopType = viewConfiguration.scaledTouchSlop
            TOUCH_SLOP_PAGING -> touchSlopType = viewConfiguration.scaledPagingTouchSlop
        }
    }

    override fun onInterceptTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) {
            return false
        }

        val action = event.actionMasked
        val actionIndex = event.actionIndex

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                scrollPointerId = event.getPointerId(0)
                pointTouchX = Math.round(event.x + 0.5f)
                pointTouchY = Math.round(event.y + 0.5f)
                return super.onInterceptTouchEvent(event)
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                scrollPointerId = event.getPointerId(actionIndex)
                pointTouchX = Math.round(event.getX(actionIndex) + 0.5f)
                pointTouchY = Math.round(event.getY(actionIndex) + 0.5f)
                return super.onInterceptTouchEvent(event)
            }

            MotionEvent.ACTION_MOVE -> {
                val index = event.findPointerIndex(scrollPointerId)
                if (index < 0) {
                    return false
                }

                val x = Math.round(event.getX(index) + 0.5f)
                val y = Math.round(event.getY(index) + 0.5f)
                if (scrollState != SCROLL_STATE_DRAGGING) {
                    val dx = x - pointTouchX
                    val dy = y - pointTouchY
                    var startScroll = false
                    if (layoutManager?.canScrollHorizontally() == true && Math.abs(dx) > touchSlopType && (layoutManager?.canScrollVertically() == true || Math.abs(
                            dx
                        ) > Math.abs(dy))
                    ) {
                        startScroll = true
                    }
                    if (layoutManager?.canScrollVertically() == true && Math.abs(dy) > touchSlopType && (layoutManager?.canScrollHorizontally() == true || Math.abs(
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
