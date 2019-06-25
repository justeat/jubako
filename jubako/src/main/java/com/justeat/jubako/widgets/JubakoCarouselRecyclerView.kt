package com.justeat.jubako.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class JubakoCarouselRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : JubakoRecyclerView(context, attrs, defStyleAttr) {

    init {
        layoutManager = LinearLayoutManager(context, HORIZONTAL, false)

        addOnItemTouchListener(object : OnItemTouchListener {
            override fun onTouchEvent(recyclerView: RecyclerView, event: MotionEvent) {}
            override fun onInterceptTouchEvent(recyclerView: RecyclerView, event: MotionEvent): Boolean {
                if (event.action == MotionEvent.ACTION_DOWN && recyclerView.scrollState == SCROLL_STATE_SETTLING) {
                    recyclerView.stopScroll()
                }
                return false
            }

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        })
    }

    override fun requestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
}
