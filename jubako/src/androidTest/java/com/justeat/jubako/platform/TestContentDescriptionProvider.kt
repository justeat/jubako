package com.justeat.jubako.platform

import androidx.lifecycle.LiveData
import com.justeat.jubako.ContentDescription
import com.justeat.jubako.ContentDescriptionProvider
import java.util.*

class TestContentDescriptionProvider(
    val data: String,
    val id: String = UUID.randomUUID().toString()
) : ContentDescriptionProvider<String> {
    override fun createDescription(): ContentDescription<String> {
        return ContentDescription(
            id = id,
            viewHolderFactory = TestViewHolderFactory(),
            data = object : LiveData<String>() {
                override fun onActive() {
                    postValue(data)
                }
            },
            onReload = { a, _ ->
                a.data = object : LiveData<String>() {
                    override fun onActive() {
                        postValue("Peek-a-Boo!")
                    }
                }
            })
    }
}