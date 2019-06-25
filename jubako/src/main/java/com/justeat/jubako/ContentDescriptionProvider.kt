package com.justeat.jubako

interface ContentDescriptionProvider<T> {
    fun createDescription(): ContentDescription<T>
}