package com.justeat.jubako.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.justeat.jubako.Jubako
import com.justeat.jubako.data.PaginatedDataState
import com.justeat.jubako.data.ready
import com.justeat.jubako.util.IJubakoScreenFiller
import com.justeat.jubako.util.JubakoScreenFiller

@Suppress("UNCHECKED_CAST")
abstract class ProgressAdapter<ITEM_DATA, ITEM_HOLDER : RecyclerView.ViewHolder>(
    open var logger: Jubako.Logger,
    open val progressViewHolder: (parent: ViewGroup) -> RecyclerView.ViewHolder,
    open var orientation: JubakoScreenFiller.Orientation
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_PROGRESS = Int.MAX_VALUE
    }

    private var progressPos: Int = 0
    private var scrollListener: RecyclerView.OnScrollListener? = null
    private var screenFiller: IJubakoScreenFiller = IJubakoScreenFiller.NOOP

    final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        logger.log(TAG, "Create View Holder", "")
        return if (viewType == VIEW_TYPE_PROGRESS) {
            progressViewHolder(parent)
        } else {
            onCreateViewHolderItem(parent, viewType)
        }
    }

    abstract fun onCreateViewHolderItem(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder

    final override fun getItemCount(): Int {
        return getItemCountActual() + (if (getCurrentState().loading || getCurrentState().error != null) 1 else 0)
    }

    abstract fun getItemCountActual(): Int
    abstract fun getCurrentState(): PaginatedDataState<*>

    final override fun getItemViewType(position: Int): Int {
        return if ((getCurrentState().loading || getCurrentState().error != null) && position == getItemCountActual()) {
            VIEW_TYPE_PROGRESS
        } else {
            getItemViewTypeActual(position)
        }
    }

    open fun getItemViewTypeActual(position: Int): Int = super.getItemViewType(position)

    final override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        return when {
            getCurrentState().loading && position == getItemCountActual() -> {
                logger.log(TAG, "Bind View Holder (progress)", "position: $position")
                (holder as ProgressView).onProgress()
            }
            getCurrentState().error != null && position == getItemCountActual() -> {
                logger.log(TAG, "Bind View Holder (error)", "position: $position")
                (holder as ProgressView).onError(getCurrentState().error!!)
                (holder as ProgressView).setRetryCallback {
                    loadMore()
                }
            }
            else -> {
                logger.log(TAG, "Bind View Holder", "position: $position")
                bindItemToHolder(holder as ITEM_HOLDER, getItem(position))
            }
        }
    }

    abstract fun loadMore()
    abstract fun hasMoreToLoad(): Boolean
    abstract fun getItem(position: Int): ITEM_DATA
    abstract fun bindItemToHolder(holder: ITEM_HOLDER, item: ITEM_DATA)

    final override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        setupPaginatedLoading(recyclerView)
    }

    private fun setupPaginatedLoading(recyclerView: RecyclerView) {
        init(recyclerView)
        setupLoadMoreScrollTrigger(recyclerView)
        initialFillBegin(recyclerView)
    }

    abstract fun init(recyclerView: RecyclerView)

    fun onStateChanged(state: PaginatedDataState<*>, previousState: PaginatedDataState<*>) {
        if (state.accept()) {
            when {
                state.error != null -> {
                    logger.log(
                        TAG,
                        "Notify Item Changed (error)",
                        "position: $progressPos"
                    )
                    notifyItemChanged(progressPos)
                }
                (!previousState.ready() && state.ready()) -> {
                    progressPos = previousState.loaded.size
                    logger.log(
                        TAG,
                        "Notify Item Removed (progress)",
                        "position: $progressPos"
                    )
                    notifyItemRemoved(progressPos)
                    notifyAndContinue(state)
                }
                (previousState.ready() && !state.ready()) -> {
                    progressPos = state.loaded.size
                    logger.log(
                        TAG,
                        "Notify Item Inserted (progress)",
                        "item: $progressPos"
                    )
                    notifyItemInserted(progressPos)
                }
                else -> {
                    logger.log(TAG, "Case 5", "")
                    notifyAndContinue(state)
                }
            }
        }
    }

    private fun notifyAndContinue(state: PaginatedDataState<*>) {
        logger.log(TAG, "Carousel Page Loaded", "")
        val start = state.loaded.size - state.page.size
        logger.log(
            TAG,
            "Notify Item Range Inserted",
            "start: $start, count: ${state.page.size}"
        )
        notifyItemRangeInserted(start, state.page.size)
    }

    private fun setupLoadMoreScrollTrigger(recyclerView: RecyclerView) {
        if (scrollListener != null) {
            recyclerView.removeOnScrollListener(scrollListener!!)
            scrollListener = null
        }

        scrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                val offset = when (orientation) {
                    JubakoScreenFiller.Orientation.HORIZONTAL -> {
                        recyclerView.computeHorizontalScrollOffset()
                    }
                    JubakoScreenFiller.Orientation.VERTICAL -> {
                        recyclerView.computeVerticalScrollOffset()
                    }
                }

                val range = when (orientation) {
                    JubakoScreenFiller.Orientation.HORIZONTAL -> {
                        recyclerView.computeHorizontalScrollRange() - recyclerView.computeHorizontalScrollExtent()
                    }
                    JubakoScreenFiller.Orientation.VERTICAL -> {
                        recyclerView.computeVerticalScrollRange() - recyclerView.computeVerticalScrollExtent()
                    }
                }
                if (range != 0 && offset > range * 0.8f) {
                    logger.log(TAG, "Scroll Trigger Load", "")
                    if (getCurrentState().error == null && !getCurrentState().loading) {
                        loadMore()
                    }
                }
            }
        }

        recyclerView.addOnScrollListener(scrollListener!!)
    }

    private fun initialFillBegin(recyclerView: RecyclerView) {
        if (orientation == JubakoScreenFiller.Orientation.HORIZONTAL) {
            logger.log(TAG, "Initial Fill Across", "begin...")
        } else {
            logger.log(TAG, "Initial Fill Down", "begin...")
        }
        if (getCurrentState().error == null) {
            loadMore()
        }
        screenFiller = JubakoScreenFiller(orientation = orientation,
            logger = logger, log = true, hasMore = { hasMoreToLoad() }, loadMore = {
                if (getCurrentState().error == null) {
                    loadMore()
                }
            }, onFilled = ::onFilled
        )
        screenFiller.attach(recyclerView)
    }

    open fun onFilled() {

    }
}

private val TAG = ProgressAdapter::class.java.simpleName
