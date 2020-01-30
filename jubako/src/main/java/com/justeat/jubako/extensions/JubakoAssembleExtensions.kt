package com.justeat.jubako.extensions

import androidx.lifecycle.LiveData
import com.justeat.jubako.ContentDescription
import com.justeat.jubako.ContentDescriptionProvider
import com.justeat.jubako.JubakoAssembler
import com.justeat.jubako.JubakoViewSpec
import com.justeat.jubako.data.EmptyLiveData
import com.justeat.jubako.descriptionProvider
import java.util.UUID

typealias ListReceiver = MutableList<ContentDescriptionProvider<Any>>.() -> Unit
typealias JubakoMutableList = MutableList<ContentDescriptionProvider<Any>>

fun ListReceiver.assemble(): JubakoAssembler {
    return object : JubakoAssembler {
        override suspend fun assemble(): List<ContentDescriptionProvider<Any>> =
            mutableListOf<ContentDescriptionProvider<Any>>().apply {
                invoke(this)
            }
    }
}

fun JubakoMutableList.add(provider: ContentDescriptionProvider<*>) {
    add(provider as ContentDescriptionProvider<Any>)
}

fun <T> JubakoMutableList.add(delegate: () -> ContentDescription<T>) {
    add(descriptionProvider { delegate.invoke() } as ContentDescriptionProvider<Any>)
}

fun <T> JubakoMutableList.addDescription(
    viewSpec: JubakoViewSpec<T>,
    data: LiveData<T> = EmptyLiveData(),
    onReload: (ContentDescription<T>.(payload: Any?) -> Unit) = { },
    id: String = UUID.randomUUID().toString()
) {
    add(descriptionProvider {
        ContentDescription(
            viewSpec = viewSpec,
            data = data,
            onReload = onReload,
            id = id
        )
    })
}

