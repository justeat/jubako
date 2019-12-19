package com.justeat.jubako.extensions

import com.justeat.jubako.ContentDescriptionProvider
import com.justeat.jubako.Jubako
import com.justeat.jubako.SimpleJubakoAssembler

/**
 * Simply load with the given descriptions then just call [add], [addView], [addHolder], etc
 * to specify how and what you wish to display conveniently or manually construct and add [ContentDescriptionProvider]'s
 */
fun Jubako.load(descriptionProviders: ListReceiver) {
    load(SimpleJubakoAssembler(descriptionProviders.apply { invoke(mutableListOf()) }))
}
/**
 * Same as [load] but happens on the IO dispatcher (use with caution!)
 */
fun Jubako.loadAsync(descriptionProviders: ListReceiver) {
    load(descriptionProviders.assemble())
}
