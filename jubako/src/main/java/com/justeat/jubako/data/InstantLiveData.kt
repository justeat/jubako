package com.justeat.jubako.data

import androidx.lifecycle.LiveData

class InstantLiveData<DATA>(private val data: DATA) : LiveData<DATA>() {
    override fun onActive() {
        postValue(data)
    }
}
