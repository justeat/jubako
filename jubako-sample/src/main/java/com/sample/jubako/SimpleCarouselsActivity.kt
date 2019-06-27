package com.sample.jubako

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.justeat.jubako.Jubako
import com.justeat.jubako.extensions.addCarousel
import com.justeat.jubako.extensions.load
import com.justeat.jubako.extensions.pageSize
import kotlinx.android.synthetic.main.activity_jubako_recycler.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SimpleCarouselsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jubako_recycler)

        Jubako.logger = Jubako.Logger(BuildConfig.DEBUG)

        // Set page size to 1 so we can see it loading (descriptions are delayed by 500ms)
        Jubako.into(this, jubakoRecycler, pageSize(1)).load {
            (0 until 100).forEach { i ->
                addCarousel(
                    priority = i,
                    carouselView = {
                        LayoutInflater.from(this@SimpleCarouselsActivity).inflate(R.layout.simple_carousel, it, false)
                    },
                    itemViewHolder = { SimpleCarouselItemViewHolder(it) },
                    itemData = when {
                        i % 2 == 0 -> getNumbersEnglish()
                        else -> getNumbersJapanese()
                    },
                    itemBinder = { holder, data ->
                        holder.itemView.findViewById<TextView>(R.id.text).text = data
                    }
                )
            }
        }
    }

    inner class SimpleCarouselItemViewHolder(parent: ViewGroup) :
        RecyclerView.ViewHolder(LayoutInflater.from(this).inflate(R.layout.simple_carousel_item_text, parent, false))

    companion object {
        fun getNumbersEnglish(): LiveData<List<String>> {
            return object : LiveData<List<String>>() {
                override fun onActive() {
                    GlobalScope.launch(Dispatchers.IO) {
                        delay(500)
                        postValue(listOf("One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight"))
                    }
                }
            }
        }

        fun getNumbersJapanese(): LiveData<List<String>> {
            return object : LiveData<List<String>>() {
                override fun onActive() {
                    GlobalScope.launch(Dispatchers.IO) {
                        delay(500)
                        postValue(listOf("ひとつ", "ふたつ", "みっつ", "よっつ", "いつつ", "むっつ", "ななつ", "やっつ"))
                    }
                }
            }
        }
    }
}
