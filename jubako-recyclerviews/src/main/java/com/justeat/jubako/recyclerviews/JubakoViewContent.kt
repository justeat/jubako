package com.justeat.jubako.recyclerviews

import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import com.justeat.jubako.ContentDescription
import com.justeat.jubako.data.EmptyLiveData
import com.justeat.jubako.descriptionProvider
import com.justeat.jubako.extensions.JubakoMutableList
import com.justeat.jubako.extensions.add
import java.util.UUID

/**
 * Simple way to add an arbitrary view when assembling Jubako lists
 */
fun <VIEW : View> JubakoMutableList.addView(view: (parent: ViewGroup) -> VIEW) {
    addView<Unit, VIEW>(view = view)
}

/**
 * Add an arbitrary data bound view when assembling
 *
 * @param view An android view that represents the UI for this content
 * @param data The data we want to bind to the [view], defaults to [EmptyLiveData]
 * @param onReload Optional callback to handle a data reload
 * @param id Optional unique id for the description
 * @param viewBinder Optional view binder to perform binding from the loaded [data] into the [view]
 */
fun <DATA, VIEW : View> JubakoMutableList.addView(
    data: LiveData<DATA> = EmptyLiveData(),
    onReload: (ContentDescription<DATA>.(payload: Any?) -> Unit) = { },
    id: String = UUID.randomUUID().toString(),
    viewBinder: (DATA?, VIEW) -> Unit = { _, _ -> },
    view: (parent: ViewGroup) -> VIEW
) {
    add(descriptionProvider {
        viewContent(
            data = data,
            onReload = onReload,
            id = id,
            viewBinder = viewBinder,
            view = view
        )
    })
}

/**
 * Simple way to create a [data] bound [view] content description
 *
 * @param view An android view that represents the UI for this content
 * @param data The data we want to bind to the [view], defaults to [EmptyLiveData]
 * @param onReload Optional callback to handle a data reload
 * @param id Optional unique id for the description
 * @param viewBinder Optional view binder to perform binding from the loaded [data] into the [view]
 */
private fun <DATA, VIEW : View> viewContent(
    data: LiveData<DATA> = EmptyLiveData(),
    onReload: (ContentDescription<DATA>.(payload: Any?) -> Unit) = { },
    id: String = UUID.randomUUID().toString(),
    viewBinder: (DATA?, VIEW) -> Unit = { _, _ -> },
    view: (parent: ViewGroup) -> VIEW
): ContentDescription<DATA> {
    return ContentDescription(
        id = id,
        viewSpec = com.justeat.jubako.recyclerviews.viewHolderFactory {
            object : com.justeat.jubako.recyclerviews.JubakoViewHolder<DATA>(view.invoke(it)) {
                override fun bind(data: DATA?) {
                    viewBinder.invoke(data, itemView as VIEW)
                }
            }
        },
        data = data,
        onReload = onReload
    )
}
