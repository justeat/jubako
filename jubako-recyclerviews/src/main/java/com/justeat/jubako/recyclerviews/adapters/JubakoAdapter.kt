package com.justeat.jubako.recyclerviews.adapters

import android.os.Handler
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.justeat.jubako.ContentDescription
import com.justeat.jubako.Jubako
import com.justeat.jubako.data.PaginatedDataState
import com.justeat.jubako.recyclerviews.ContentLoadingStrategy
import com.justeat.jubako.recyclerviews.CreateViewHolderDelegate
import com.justeat.jubako.recyclerviews.JubakoViewHolder
import com.justeat.jubako.recyclerviews.util.JubakoScreenFiller
import com.justeat.jubako.recyclerviews.widgets.JubakoRecyclerView

open class JubakoAdapter(
    private val lifecycleOwner: LifecycleOwner,
    private val data: Jubako.Data,
    private val loadingStrategy: ContentLoadingStrategy = PaginatedContentLoadingStrategy(),
    override val progressViewHolder: CreateViewHolderDelegate<RecyclerView.ViewHolder> = { inflater, parent, _ ->
        DefaultProgressViewHolder(inflater, parent)
    }
) : ProgressAdapter<ContentDescription<Any>, JubakoViewHolder<Any>>(
    Jubako.logger, progressViewHolder, JubakoScreenFiller.Orientation.VERTICAL
) {
    private val handler = Handler()
    var onInitialFill: () -> Unit = {}
    var onViewHolderBound: (contentViewHolder: JubakoViewHolder<Any>) -> Unit = {}

    var hasMore: Boolean = true
    var state = PaginatedDataState<ContentDescription<Any>>(listOf(), listOf(), true, null, false)

    interface HolderFactory<T> {
        fun createViewHolder(parent: ViewGroup): JubakoViewHolder<T>
    }

    override fun init(recyclerView: RecyclerView) {
        load()
        (recyclerView as JubakoRecyclerView).onDrawComplete = {} // TODO why?
        data.destination.listener = ContentAdapterContentDescriptionCollectionListener(this)
    }

    override fun onCreateViewHolderItem(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val factory = data.viewSpecs[viewType]

        val holder = (factory as HolderFactory<*>).createViewHolder(parent)
        holder.logger = logger

        logger.log(
            TAG,
            "Create View Holder",
            "id: ${data.viewTypes[viewType]}, viewType: $viewType, type: ${holder.javaClass.simpleName}"
        )
        return holder
    }

    override fun getItemCountActual(): Int = data.numItemsLoaded()
    override fun getCurrentState(): PaginatedDataState<*> = state
    override fun loadMore() = load()
    override fun hasMoreToLoad(): Boolean = hasMore

    override fun getItem(position: Int): ContentDescription<Any> = data.getItem(position)

    override fun bindItemToHolder(holder: JubakoViewHolder<Any>, item: ContentDescription<Any>) {
        holder.onClickDelegate = { postViewHolderEvent(JubakoViewHolder.Event.Click(item.id, it)) }
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

    override fun onFilled() = onInitialFill()

    private fun load() = loadingStrategy.load(
        lifecycleOwner, data
    ) { newState, more ->
        hasMore = more
        onStateChanged(newState, state)
        state = newState
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

    override fun getItemViewTypeActual(position: Int): Int {
        return data.getItemViewType(position)
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

    override fun getItemId(position: Int): Long = data.getItemId(position)
    private fun postViewHolderEvent(event: JubakoViewHolder.Event) = onViewHolderEvent(event)

    var onViewHolderEvent: (event: JubakoViewHolder.Event) -> Unit = { }
}

private val TAG = JubakoAdapter::class.java.simpleName
