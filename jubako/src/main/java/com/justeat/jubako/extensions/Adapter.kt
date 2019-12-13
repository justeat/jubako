package com.justeat.jubako.extensions

import android.view.ViewGroup
import android.widget.Adapter
import androidx.recyclerview.widget.RecyclerView
import com.justeat.jubako.Jubako
import com.justeat.jubako.util.IJubakoScreenFiller
import com.justeat.jubako.util.JubakoScreenFiller

@Suppress("UNCHECKED_CAST")
abstract class ProgressAdapter<ITEM_DATA, ITEM_HOLDER : RecyclerView.ViewHolder>(
    var logger: Jubako.Logger,
    var itemBinder: (holder: ITEM_HOLDER, data: ITEM_DATA?) -> Unit = { _, _ -> },
    val progressViewHolder: (parent: ViewGroup) -> RecyclerView.ViewHolder,
    var screenFiller: IJubakoScreenFiller = IJubakoScreenFiller.NOOP
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_PROGRESS = Int.MAX_VALUE
    }

    data class State<T>(
        var loaded: List<T>,
        var page: List<T>,
        var loading: Boolean = false,
        var error: Throwable? = null,
        var accepted: Boolean = false
    ) {
        fun accept(): Boolean {
            if (!accepted) {
                accepted = true
                return true
            }
            return false
        }
    }

    private var progressPos: Int = 0
    private var scrollListener: RecyclerView.OnScrollListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        logger.log(TAG, "Create View Holder", "")
        return if (viewType == VIEW_TYPE_PROGRESS) {
            progressViewHolder(parent)
        } else {
            onCreateViewHolderItem(parent, viewType)
        }
    }

    abstract fun onCreateViewHolderItem(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder

    override fun getItemCount(): Int {
        return getItemCountActual() + (if (getCurrentState().loading || getCurrentState().error != null) 1 else 0)
    }

    abstract fun getItemCountActual(): Int
    abstract fun getCurrentState(): State<*>

    override fun getItemViewType(position: Int): Int {
        return if ((getCurrentState().loading || getCurrentState().error != null) && position == getItemCountActual()) {
            VIEW_TYPE_PROGRESS
        } else {
            super.getItemViewType(position)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
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
                itemBinder(holder as ITEM_HOLDER, getItem(position))
            }
        }
    }

    abstract fun loadMore()
    abstract fun hasMoreToLoad(): Boolean
    abstract fun getItem(position: Int): ITEM_DATA

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        setupPaginatedLoading(recyclerView)
    }

    private fun setupPaginatedLoading(recyclerView: RecyclerView) {
        // Implementations of init should setup change monitoring and call
        // consumeState()
        init()
//        lifecycleOwner?.let {
//            paginatedLiveData?.observe(it, Observer<PaginatedLiveData.State<*>> { state ->
//                logger.log(TAG, "Observe State", "$state")
//                consumeState(state)
//            })
//        }
        setupLoadMoreScrollTrigger(recyclerView)
        initialFillBegin(recyclerView)
    }

    abstract fun init()

    fun onStateChanged(state: State<*>, previousState: State<*>) {
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

    private fun notifyAndContinue(state: State<*>) {
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
                val offset = recyclerView.computeHorizontalScrollOffset()
                val range =
                    recyclerView.computeHorizontalScrollRange() - recyclerView.computeHorizontalScrollExtent()
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
        logger.log(TAG, "Initial Fill Across", "begin...")
        if (getCurrentState().error == null) {
            loadMore()
        }
        screenFiller = JubakoScreenFiller(orientation = JubakoScreenFiller.Orientation.HORIZONTAL,
            logger = logger, log = true, hasMore = { hasMoreToLoad() }, loadMore = {
                if (getCurrentState().error == null) {
                    loadMore()
                }
            })
        screenFiller.attach(recyclerView)
    }
}

fun ProgressAdapter.State<*>?.ready(): Boolean {
    return this != null && !loading && error == null
}

private val TAG = Adapter::class.java.simpleName
