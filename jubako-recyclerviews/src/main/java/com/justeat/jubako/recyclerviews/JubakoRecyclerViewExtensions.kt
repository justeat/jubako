package com.justeat.jubako.recyclerviews

import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.justeat.jubako.Jubako
import com.justeat.jubako.recyclerviews.adapters.JubakoAdapter
import com.justeat.jubako.recyclerviews.adapters.PaginatedContentLoadingStrategy
import com.justeat.jubako.recyclerviews.widgets.JubakoRecyclerView

fun RecyclerView.withJubako(
    activity: FragmentActivity,
    loadingStrategy: ContentLoadingStrategy = PaginatedContentLoadingStrategy(10),
    onAssembled: (data: Jubako.Data) -> Unit = {},
    onAssembling: () -> Unit = {},
    onAssembleError: () -> Unit = {},
    onInitialFill: () -> Unit = {},
    onViewHolderEvent: (com.justeat.jubako.recyclerviews.JubakoViewHolder.Event) -> Unit = {}
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
