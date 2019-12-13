package com.justeat.jubako

import android.os.Handler
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.justeat.jubako.JubakoViewHolder.Event
import com.justeat.jubako.util.IJubakoScreenFiller
import com.justeat.jubako.util.JubakoScreenFiller
import com.justeat.jubako.widgets.JubakoRecyclerView

open class JubakoAdapter(
    private val lifecycleOwner: LifecycleOwner,
    private val data: Jubako.Data,
    private val loadingStrategy: ContentLoadingStrategy = PaginatedContentLoadingStrategy()
) : RecyclerView.Adapter<JubakoViewHolder<Any>>() {

    var onInitialFill: () -> Unit = {}
    var onViewHolderBound: (contentViewHolder: JubakoViewHolder<Any>) -> Unit = {}

    private val handler = Handler()
    var logger = Jubako.logger
    var hasMore: Boolean = true

    var screenFiller = IJubakoScreenFiller.NOOP

    interface HolderFactory<T> {
        fun createViewHolder(parent: ViewGroup): JubakoViewHolder<T>
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        logger.log(TAG, "On Attach", "total rows: ${data.source.size}")
        (recyclerView as JubakoRecyclerView).onDrawComplete = {}
        setupLoadMoreScrollTrigger(recyclerView)
        initialFillOnLayoutChanged(recyclerView)
        listenForContentChanges()
    }

    private fun listenForContentChanges() {
        //
        // Listens for changes in ContentDescriptionCollection and relays
        // then to the adapter via the Adapter notify methods
        //
        data.destination.listener =
            ContentAdapterContentDescriptionCollectionListener(this)
    }

    private fun initialFillOnLayoutChanged(recyclerView: RecyclerView) {
        logger.log(TAG, "Initial Fill Down", "begin...")
        screenFiller = JubakoScreenFiller(
            orientation = JubakoScreenFiller.Orientation.VERTICAL,
            logger = logger,
            log = true,
            hasMore = { hasMore },
            loadMore = this::load,
            onFilled = {
                onInitialFill()
            }
        )
        screenFiller.attach(recyclerView)
        load()
    }

    private var inLoadingError = false

    private fun load() {
        if(!inLoadingError) {
            loadingStrategy.load(lifecycleOwner, data,
                onLoaded = {
                    hasMore = it
                    false
                },
                onError = {
                    inLoadingError = true
                })
        }
    }

    override fun getItemCount(): Int {
        return data.numItemsLoaded()
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): JubakoViewHolder<Any> {

        val factory = data.viewHolderFactories[viewType]

        val holder = factory.createViewHolder(viewGroup)
        holder.logger = logger

        logger.log(
            TAG,
            "Create View Holder",
            "id: ${data.viewTypes[viewType]}, viewType: $viewType, type: ${holder.javaClass.simpleName}"
        )
        return holder
    }

    override fun onBindViewHolder(holder: JubakoViewHolder<Any>, position: Int) {
        logger.log(TAG, "Bind ViewHolder")

        val item = data.getItem(position)

        holder.onClickDelegate = { postViewHolderEvent(Event.Click(item.id, it)) }

        attachReloader(item, holder)

        bindWhenNewOrDefault(item, holder)

        onViewHolderBound(holder)
    }

    private fun bindWhenNewOrDefault(item: ContentDescription<Any>, holder: JubakoViewHolder<Any>) {
        val data = item.data.value
        if (data !== holder.data || data == null) {
            holder.description = item
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
                        if (destination.contains(item)) {
                            loadingStrategy.reload(
                                lifecycleOwner,
                                position, destination
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
                    logger.log(TAG, "Scroll Trigger Load")
                    loadingStrategy.load(lifecycleOwner, data, onLoaded = {
                        logger.log(TAG, "Scroll Trigger Load Complete")
                        false
                    })
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
                logger.log(TAG, "Reload", "description: $contentDescriptionId, position: $position")
                loadingStrategy.reload(lifecycleOwner, position, data.destination)
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

private val TAG = JubakoAdapter::class.java.simpleName
