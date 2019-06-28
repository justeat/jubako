package com.justeat.jubako

import android.os.Handler
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.justeat.jubako.JubakoViewHolder.Event

open class JubakoAdapter(
    private val lifecycleOwner: LifecycleOwner,
    private val data: Jubako.Data,
    private val loadingStrategy: ContentLoadingStrategy = PaginatedContentLoadingStrategy()
) : RecyclerView.Adapter<JubakoViewHolder<Any>>() {

    var onInitialFill: () -> Unit = {}
    var onViewHolderBound: (contentViewHolder: JubakoViewHolder<Any>) -> Unit = {}

    private val handler = Handler()
    var logger = Jubako.logger

    interface HolderFactory<T> {
        fun createViewHolder(parent: ViewGroup): JubakoViewHolder<T>
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        logger.log("On Attach", "total rows: ${data.contentDescriptions.size}")
        setupLoadMoreScrollTrigger(recyclerView)
        initialFillOnLayoutChanged(recyclerView)
        listenForContentChanges()
    }

    private fun listenForContentChanges() {
        //
        // Listens for changes in ContentDescriptionCollection and relays
        // then to the adapter via the Adapter notify methods
        //
        data.loadedContentDescriptions.listener =
            ContentAdapterContentDescriptionCollectionListener(this)
    }

    private fun initialFillOnLayoutChanged(recyclerView: RecyclerView) {
        initialFill(recyclerView)

        //
        // Initial loading the adapter performed on the first layout change to avoid inconsistency
        // when an update occurs during a layout phase
        //
        recyclerView.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
            override fun onLayoutChange(
                v: View?,
                left: Int,
                top: Int,
                right: Int,
                bottom: Int,
                oldLeft: Int,
                oldTop: Int,
                oldRight: Int,
                oldBottom: Int
            ) {
                recyclerView.removeOnLayoutChangeListener(this)
                logger.log("Initial Fill", "On Layout")

                initialFill(recyclerView)
            }
        })
    }

    private fun initialFill(recyclerView: RecyclerView) {
        var lastTimeVisibleItemPos = Int.MIN_VALUE

        data.apply {
            loadingStrategy.load(
                lifecycleOwner,
                contentDescriptions,
                loadedContentDescriptions
            )

            { hasMore ->
                val lastPos =
                    (recyclerView.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition()
                logger.log(
                    "Initial Fill",
                    "last visible item pos: $lastPos, previously: $lastTimeVisibleItemPos"
                )
                when {
                    canLoadMoreDescriptions(hasMore, lastPos, lastTimeVisibleItemPos) -> {
                        logger.log("Initial Fill", "filling screen...")
                        lastTimeVisibleItemPos = lastPos
                        true
                    }
                    else -> {
                        logger.log("Initial Fill", "Complete")
                        onInitialFill()
                        false
                    }
                }
            }
        }
    }

    private fun canLoadMoreDescriptions(hasMore: Boolean, lastPos: Int, lastTimeVisibleItemPos: Int) =
        hasMore && lastPos != lastTimeVisibleItemPos

    override fun getItemCount(): Int {
        return data.numItemsLoaded()
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): JubakoViewHolder<Any> {

        val factory = data.viewHolderFactories.get(viewType)

        val holder = factory.createViewHolder(viewGroup)
        holder.logger = logger

        logger.log(
            "Create View Holder",
            "id: ${data.viewTypes[viewType]}, viewType: $viewType, type: ${holder.javaClass.simpleName}"
        )
        return holder
    }

    override fun onBindViewHolder(holder: JubakoViewHolder<Any>, position: Int) {
        logger.log("Bind ViewHolder")

        val item = data.getItem(position)

        holder.onClickDelegate = { postViewHolderEvent(Event.Click(item.id, it)) }

        attachReloader(item, holder)

        bindWhenNewOrDefault(item, holder)

        onViewHolderBound(holder)
    }

    private fun bindWhenNewOrDefault(item: ContentDescription<Any>, holder: JubakoViewHolder<Any>) {
        val data = item.data?.value
        if (data !== holder.data || data == null) {
            holder.bind(data)
            holder.data = data
        }
    }

    private fun attachReloader(item: ContentDescription<Any>, holder: JubakoViewHolder<Any>) {
        holder.reloader = { position, payload ->
            handler.post {
                item.onReload.apply {
                    invoke(item, payload)
                    data.apply {
                        if (loadedContentDescriptions.contains(item)) {
                            loadingStrategy.reload(
                                lifecycleOwner,
                                position, loadedContentDescriptions
                            )
                        }
                    }
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return data.getItemViewType(position)
    }

    private var scrollListener: RecyclerView.OnScrollListener? = null

    private fun setupLoadMoreScrollTrigger(recyclerView: RecyclerView) {
        if (scrollListener != null) {
            recyclerView.removeOnScrollListener(scrollListener!!)
            scrollListener = null
        }

        scrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                val offset = recyclerView.computeVerticalScrollOffset()
                val range = recyclerView.computeVerticalScrollRange() - recyclerView.computeVerticalScrollExtent()
                if (range != 0 && offset > range * 0.8f) {
                    logger.log("Scroll Trigger Load")
                    data.apply {
                        loadingStrategy.load(
                            lifecycleOwner,
                            contentDescriptions,
                            loadedContentDescriptions
                        ) {
                            logger.log("Scroll Trigger Load Complete")
                            false
                        }
                    }
                    //      }
                }
            }
        }

        recyclerView.addOnScrollListener(scrollListener!!)
    }

    fun reload(contentDescriptionId: String, payload: Any? = null) {
        val item = data.byContentDescriptionId(contentDescriptionId)
        val position: Int = data.indexOf(item)
        item?.onReload?.apply {
            invoke(item, payload)
            if (data.loaded(item)) {
                logger.log("Reload", "description: $contentDescriptionId, position: $position")
                loadingStrategy.reload(lifecycleOwner, position, data.loadedContentDescriptions)
            }
        }
    }

    override fun getItemId(position: Int): Long {
        return data.getItemId(position)
    }

    private fun postViewHolderEvent(event: Event) {
        onViewHolderEvent(event)
    }

    var onViewHolderEvent: (event: Event) -> Unit = { }
}

private const val SCROLL_DIRECTION_DOWN = 1
