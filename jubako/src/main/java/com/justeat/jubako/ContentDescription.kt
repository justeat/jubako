package com.justeat.jubako

import androidx.lifecycle.LiveData
import com.justeat.jubako.data.EmptyLiveData
import java.util.UUID

/**
 * Describes a generic piece of asynchronous content declaring its [data] and
 * UI via a [viewSpec].
 */
data class ContentDescription<T>(
    /**
     * A view spec that will be used to create a UI to
     * present your [data]
     */
    val viewSpec: JubakoViewSpec<T>,
    /**
     * Assign the [data] that will be observed and passed to [JubakoViewHolder.bind]
     */
    var data: LiveData<T> = EmptyLiveData(),

    /**
     * Will be called before a reload via [JubakoAdapter.reload] with an optional payload.
     *
     * A reload will cause [data] to be observed again effectively reloading this descriptions
     * corresponding data in the recycler, by assigning a function to [onReload] gives the opportunity
     * to reassign [data]. Optionally a payload can passed to [JubakoAdapter.reload] or
     * [JubakoViewHolder.reload] that can be evaluated when reassigning [data].
     */
    val onReload: (ContentDescription<T>.(payload: Any?) -> Unit) = {},

    /**
     * Automatically generated ID can be overridden with a specific ID which can be used
     * to reference content when one wishes to [JubakoAdapter.reload]
     */
    val id: String = UUID.randomUUID().toString(),

    /**
     * Map of anything to cache arbitrary data pertaining to this description, Jubako will
     * also use this map to store internal data using the prefix **jubako**, use with caution!
     */
    val cache: MutableMap<Any, Any> = mutableMapOf()
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val that = other as ContentDescription<*>?

        return id == that!!.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return id
    }
}
