package com.justeat.jubako.recyclerviews

import com.justeat.jubako.ContentDescription
import com.justeat.jubako.descriptionProvider
import com.justeat.jubako.extensions.JubakoMutableList
import com.justeat.jubako.extensions.add

fun <T> JubakoMutableList.addHolder(delegate: () -> JubakoViewHolder<T>) {
    add(descriptionProvider {
        ContentDescription<T>(
            viewSpec = viewHolderFactory { delegate.invoke() })
    })
}
