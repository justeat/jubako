package com.justeat.jubako.recyclerviews.adapters

import android.content.Context
import android.os.Parcelable
import android.view.View
import androidx.annotation.CallSuper
import androidx.annotation.IdRes
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.justeat.jubako.data.PaginatedLiveData
import com.justeat.jubako.recyclerviews.CreateViewHolderDelegate
import com.justeat.jubako.recyclerviews.JubakoViewHolder
import com.justeat.jubako.recyclerviews.util.JubakoScreenFiller

/**
 * An implementation of [JubakoViewHolder] setup with a [RecyclerView] and easy callback functions for generic
 * view holder creation and data binding.
 *
 * Not normally used directly, use derived types with [addRecyclerView] or [recyclerViewContent] functions when
 * assembling content.
 */
open class JubakoRecyclerViewHolder<DATA, ITEM_DATA, ITEM_HOLDER : RecyclerView.ViewHolder>(itemView: View) :
    JubakoViewHolder<DATA>(itemView) {

    @IdRes
    internal var recyclerViewId: Int = View.NO_ID
    internal lateinit var itemViewHolder: CreateViewHolderDelegate<ITEM_HOLDER>
    internal var viewBinder: (Any) -> Unit = {}
    internal var itemBinder: (holder: ITEM_HOLDER, data: ITEM_DATA?) -> Unit = { _, _ -> }
    internal var lifecycleOwner: LifecycleOwner? = null
    lateinit var itemData: (data: DATA, position: Int) -> ITEM_DATA
    lateinit var itemCount: (data: DATA) -> Int
    lateinit var layoutManager: (context: Context) -> RecyclerView.LayoutManager
    internal var paginatedLiveData: PaginatedLiveData<*>? = null
    internal lateinit var progressViewHolder: CreateViewHolderDelegate<RecyclerView.ViewHolder>

    private val recycler: RecyclerView by lazy {
        when (recyclerViewId) {
            View.NO_ID -> itemView as RecyclerView
            else -> itemView.findViewById(recyclerViewId)
        }.also { recycler ->
            val layoutManager = layoutManager.invoke(recycler.context)
            checkLayoutManager(layoutManager)
            recycler.layoutManager = layoutManager
        }
    }

    @CallSuper
    override fun bind(data: DATA?) {
        viewBinder(this)
        data?.apply {
            if (recycler.adapter == null) {
                recycler.adapter = createAdapter(data)
            }
            trackState()
        }
    }

    private fun trackState() {
        val cachedState: Parcelable? = (description?.cache?.get(RECYCLER_VIEW_STATE) as Parcelable?)
        cachedState?.let { inState ->
            recycler.layoutManager?.onRestoreInstanceState(inState)
        }

        lifecycleOwner?.lifecycle?.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestroy() {
                lifecycleOwner?.let {
                    paginatedLiveData?.removeObservers(it)
                    it.lifecycle.removeObserver(this)
                }
                recycler.layoutManager?.onSaveInstanceState()?.let { outState ->
                    description?.cache?.put(RECYCLER_VIEW_STATE, outState)
                }
            }
        })
    }

    private fun createAdapter(data: DATA): RecyclerView.Adapter<RecyclerView.ViewHolder> {
        if (paginatedLiveData == null) {
            return StaticDataAdapter(
                data = data,
                logger = logger,
                itemViewHolder = itemViewHolder,
                itemBinder = itemBinder,
                itemData = itemData,
                itemCount = itemCount
            )
        } else {
            return PaginatedDataAdapter(
                logger = logger,
                itemViewHolder = itemViewHolder,
                itemBinder = itemBinder,
                lifecycleOwner = lifecycleOwner!!,
                itemData = itemData,
                itemCount = itemCount,
                paginatedLiveData = paginatedLiveData!!,
                progressViewHolder = progressViewHolder,
                orientation = JubakoScreenFiller.Orientation.HORIZONTAL
            )
        }
    }

    private fun checkLayoutManager(layoutManager: RecyclerView.LayoutManager) {
        paginatedLiveData?.let {
            if ((layoutManager !is LinearLayoutManager || layoutManager.orientation != LinearLayoutManager.HORIZONTAL)) {
                throw RuntimeException("You must use a LinearLayoutManager in HORIZONTAL orientation with PaginatedLiveData<T>")
            }
        }
    }
}

private const val RECYCLER_VIEW_STATE = "jubako_recycler_view_state"
