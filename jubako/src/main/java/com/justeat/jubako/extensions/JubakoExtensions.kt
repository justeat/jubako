package com.justeat.jubako.extensions

import androidx.lifecycle.LiveData
import com.justeat.jubako.PaginatedContentLoadingStrategy

fun pageSize(pageSize: Int) = PaginatedContentLoadingStrategy(pageSize)

class InstantLiveData<DATA>(private val data: DATA) : LiveData<DATA>() {
    override fun onActive() {
        postValue(data)
    }
}