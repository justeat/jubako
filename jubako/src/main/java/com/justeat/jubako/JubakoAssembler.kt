package com.justeat.jubako

interface JubakoAssembler {
    suspend fun assemble(): List<ContentDescriptionProvider<Any>>
    fun hasMore(): Boolean = false
}