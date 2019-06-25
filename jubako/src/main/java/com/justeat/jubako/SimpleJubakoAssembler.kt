package com.justeat.jubako

/**
 * Construct a list of [ContentDescriptionProvider] by simply providing a delegate or override
 * the [onAssemble] method to add descriptions to the given list
 */
open class SimpleJubakoAssembler(private val delegate: (MutableList<ContentDescriptionProvider<Any>>.() -> Unit)? = null) :
    JubakoAssembler {
    override suspend fun assemble(): List<ContentDescriptionProvider<Any>> {
        return mutableListOf<ContentDescriptionProvider<Any>>().apply {
            delegate?.invoke(this)
            onAssemble(this)
        }
    }

    /**
     * Just add [ContentDescriptionProvider]s to the given list, easy
     */
    open fun onAssemble(list: MutableList<ContentDescriptionProvider<Any>>) {}

    open fun MutableList<ContentDescriptionProvider<Any>>.add(provider: ContentDescriptionProvider<*>) {
        add(provider as ContentDescriptionProvider<Any>)
    }
}

