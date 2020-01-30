package com.justeat.jubako

import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
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
        private val scope: CoroutineScope,
        private val assembler: JubakoAssembler,
        val source: MutableList<ContentDescription<Any>> = mutableListOf(),
        val destination: ContentDescriptionCollection = ContentDescriptionCollection(),
        val viewSpecs: MutableList<Any> = mutableListOf()
    ) {

        @VisibleForTesting
        internal var loadingJob: Job? = null

        fun getItem(position: Int) = destination[position]
        fun numItemsLoaded() = destination.size()
        fun byContentDescriptionId(contentDescriptionId: String): ContentDescription<Any>? =
            source.find { it.id == contentDescriptionId }

        fun indexOf(item: ContentDescription<Any>?): Int = source.indexOf(item)
        fun loaded(item: ContentDescription<Any>): Boolean = destination.contains(item)
        fun hasMore() = assembler.hasMore()

        fun assembleMore(): LiveData<State> {
            val liveData = MutableLiveData<State>()

            logger.log("Assembling More")
            loadingJob = scope.launch(Dispatchers.IO) {
                try {
                    load(assembler)
                    launch(Dispatchers.Main) {
                        liveData.value = State.Assembled(this@Data)
                    }
                } catch (exception: Throwable) {
                    logger.log("Assemble Error", Log.getStackTraceString(exception))
                    launch(Dispatchers.Main) {
                        liveData.value = State.AssembleError(exception)
                    }
                }
            }

            return liveData
        }
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
        // Only set assembler once (unless onReload)
        //
        if (data != null || loadingJob != null) return

        logger.log("Assembling")
        loadingState.value = State.Assembling

        loadingJob = launch(IO) {
            try {
                val data = completeAssemble(contentAssembler)
                launch(Dispatchers.Main) {
                    loadingState.value = State.Assembled(data)
                }
            } catch (exception: Throwable) {
                logger.log(logger.tag, "Assemble Error", "${Log.getStackTraceString(exception)}")
                launch(Dispatchers.Main) {
                    loadingState.value = State.AssembleError(exception)
                }
            } finally {
                loadingJob = null
            }
        }
    }

    private suspend fun completeAssemble(contentAssembler: JubakoAssembler) =
        Data(this, contentAssembler).apply {
            load(contentAssembler)
            data = this
        }

    /**
     * Allow content to be loaded again from the beginning...
     */
    fun reset() {
        logger.log("Reset")
        loadingJob?.cancel()
        loadingJob = null
        // TODO resetting fast causes an inconsistency clearing destination
        data?.destination?.clear()
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
    }

    open class Logger(var enabled: Boolean) {
        open val tag = Jubako::class.java.simpleName!!

        open fun log(state: String) {
            if (enabled) Log.d(tag, "[$state]")
        }

        open fun log(tag: String, state: String) {
            if (enabled) Log.d(tag, "[$state]")
        }

        open fun log(tag: String, state: String, message: String) {
            if (enabled) Log.d(tag, "[$state] $message")
        }
    }
}

private suspend fun Jubako.Data.load(contentAssembler: JubakoAssembler) {
    val descriptionProviders = contentAssembler.assemble()
    Jubako.logger.log("Assembled", "${descriptionProviders.size} more descriptions (total: ${source.size})")
    descriptionProviders.forEach {
        val description = it.createDescription()
        source.add(description)
        viewSpecs.add(description.viewSpec)
    }
}
