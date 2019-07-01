package com.justeat.jubako

interface JubakoAssembler {
    suspend fun assemble(): List<ContentDescriptionProvider<Any>>
    suspend fun hasMore(): Boolean = false
}