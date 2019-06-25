package com.justeat.jubako.utils

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

fun <T> T.exhaust() = this

fun <T> LiveData<T>.observeNotNull(lifecycleOwner: LifecycleOwner, observer: (T) -> Unit) {
    observe(lifecycleOwner, Observer { value -> value?.let(observer) })
}
