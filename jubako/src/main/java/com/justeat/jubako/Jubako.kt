package com.justeat.jubako

import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import com.justeat.jubako.widgets.JubakoRecyclerView
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

open class Jubako : ViewModel(), CoroutineScope {
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    @VisibleForTesting
    internal var loadingJob: Job? = null

    @VisibleForTesting
    internal var IO = Dispatchers.IO

    data class Data(
        val contentDescriptions: MutableList<ContentDescription<Any>> = mutableListOf(),
        val loadedContentDescriptions: ContentDescriptionCollection = ContentDescriptionCollection(),
        val viewHolderFactories: MutableList<JubakoAdapter.HolderFactory<Any>> = mutableListOf(),
        val viewTypes: MutableList<String> = mutableListOf()
    ) {
        fun getItemViewType(position: Int): Int =
            viewTypes.indexOf(loadedContentDescriptions[position].id)

        fun getItemId(position: Int): Long =
            viewTypes.indexOf(loadedContentDescriptions[position].id).toLong()

        fun getItem(position: Int) = loadedContentDescriptions[position]
        fun numItemsLoaded() = loadedContentDescriptions.size()
        fun byContentDescriptionId(contentDescriptionId: String): ContentDescription<Any>? =
            contentDescriptions.find { it.id == contentDescriptionId }

        fun indexOf(item: ContentDescription<Any>?): Int = contentDescriptions.indexOf(item)
        fun loaded(item: ContentDescription<Any>): Boolean = loadedContentDescriptions.contains(item)
    }

    private var data: Data? = null
    var loadingState: MutableLiveData<State> = MutableLiveData()

    sealed class State {
        object Assembling : State()
        data class Assembled(val data: Data) : State()
        data class AssembleError(val error: Throwable) : State()
    }

    /**
     * Load content using the given [JubakoAssembler], calling this
     * function again will do nothing until you call [reset]
     */
    open fun load(contentAssembler: JubakoAssembler) {
        //
        // Only set assembler once (unless reset)
        //
        if (data != null || loadingJob != null) return

        logger.log("Assembling")
        loadingState.value = State.Assembling

        loadingJob = launch(IO) {
            try {
                loadingState.postValue(
                    State.Assembled(
                        completeAssemble(contentAssembler.assemble())
                    )
                )
            } catch (exception: Exception) {
                logger.log("Assemble Error", "${Log.getStackTraceString(exception)}")
                loadingState.postValue(State.AssembleError(exception))
            } finally {
                loadingJob = null
            }
        }
    }

    private fun completeAssemble(descriptionProviders: List<ContentDescriptionProvider<Any>>) =
        Data().apply {
            logger.log("Assembled", "${descriptionProviders.size} descriptions")
            descriptionProviders.forEach {
                val description = it.createDescription()
                contentDescriptions.add(description)
                viewHolderFactories.add(description.viewHolderFactory)
                viewTypes.add(description.id)
            }

            contentDescriptions.sortWith(
                compareBy(ContentDescription<Any>::priority)
            )
            data = this
        }

    /**
     * Allow content to be loaded again from the beginning...
     */
    fun reset() {
        logger.log("Reset")
        loadingJob?.cancel()
        loadingJob = null
        data?.loadedContentDescriptions?.clear()
        data = null
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }

    companion object {
        var logger: Logger = Logger(false)

        fun observe(activity: FragmentActivity, changed: (State) -> Unit): Jubako {
            val viewModel = ViewModelProviders.of(activity).get(Jubako::class.java)
            viewModel.loadingState.observe(activity, Observer { changed(it) })
            return viewModel
        }

        fun into(
            activity: FragmentActivity,
            recycler: JubakoRecyclerView,
            onAssembled: (data: Data) -> Unit = {},
            onAssembling: () -> Unit = {},
            onAssembleError: () -> Unit = {}
        ): Jubako {
            return observe(activity) { state ->
                when (state) {
                    is State.Assembled -> {
                        onAssembled(state.data)
                        recycler.adapter = JubakoAdapter(activity, state.data)
                    }
                    is State.Assembling -> onAssembling()
                    is State.AssembleError -> onAssembleError()
                }
            }
        }

        fun into(
            activity: FragmentActivity,
            recycler: JubakoRecyclerView,
            loadingStrategy: ContentLoadingStrategy,
            onAssembled: (data: Data) -> Unit = {},
            onAssembling: () -> Unit = {},
            onAssembleError: () -> Unit = {}
        ): Jubako {
            return observe(recycler.context as FragmentActivity) { state ->
                when (state) {
                    is State.Assembled -> {
                        onAssembled(state.data)
                        recycler.adapter = JubakoAdapter(
                            lifecycleOwner = activity,
                            data = state.data,
                            loadingStrategy = loadingStrategy
                        )
                    }
                    is State.Assembling -> onAssembling()
                    is State.AssembleError -> onAssembleError()
                }
            }
        }
    }

    open class Logger(var enabled: Boolean) {
        private val tag = Jubako::class.java.simpleName!!

        open fun log(state: String, message: String = "") {
            if (enabled) log(tag, state, message)
        }

        open fun log(tag: String, state: String, message: String = "") {
            if (enabled) Log.d(tag, "[$state] $message")
        }
    }
}