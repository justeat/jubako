package com.justeat.jubako.extensions

import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.justeat.jubako.*
import com.justeat.jubako.widgets.JubakoRecyclerView

fun RecyclerView.withJubako(
    activity: FragmentActivity,
    loadingStrategy: ContentLoadingStrategy = PaginatedContentLoadingStrategy(10),
    onAssembled: (data: Jubako.Data) -> Unit = {},
    onAssembling: () -> Unit = {},
    onAssembleError: () -> Unit = {},
    onInitialFill: () -> Unit = {},
    onViewHolderEvent: (JubakoViewHolder.Event) -> Unit = {}
): Jubako {
    assert(this is JubakoRecyclerView)

    return Jubako.observe(activity) { state ->
        when (state) {
            is Jubako.State.Assembled -> {
                onAssembled(state.data)
                adapter = JubakoAdapter(activity, state.data, loadingStrategy).apply {
                    this.onInitialFill = onInitialFill
                    this.onViewHolderEvent = onViewHolderEvent
                }
            }
            is Jubako.State.Assembling -> onAssembling()
            is Jubako.State.AssembleError -> onAssembleError()
        }
    }
}
