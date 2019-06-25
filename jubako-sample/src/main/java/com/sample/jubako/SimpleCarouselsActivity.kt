package com.sample.jubako

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.justeat.jubako.*
import com.justeat.jubako.widgets.JubakoCarouselRecyclerView
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

        Jubako.observe(this) { state ->
            when (state) {
                is Jubako.State.Assembled -> {
                    jubakoRecycler.adapter = JubakoAdapter(
                        lifecycleOwner = this,
                        data = state.data,
                        // set the page size of one so we can watch each row appear
                        loadingStrategy = PaginatedContentLoadingStrategy(1)
                    )
                }
            }
        }.load {
            for (i in 0 until 100) {
                add {
                    ContentDescription(
                        viewHolderFactory { SimpleCarouselViewHolder() },
                        data = when {
                            i % 2 == 0 -> getNumbersEnglish()
                            else -> getNumbersJapanese()
                        },
                        priority = i
                    )
                }
            }
        }
    }

    inner class SimpleCarouselViewHolder :
        JubakoViewHolder<List<String>>(LayoutInflater.from(this).inflate(R.layout.simple_carousel, null)) {
        override fun bind(data: List<String>?) {
            (itemView as JubakoCarouselRecyclerView).adapter = createAdapter(data ?: emptyList())
        }

        private fun createAdapter(data: List<String>): RecyclerView.Adapter<SimpleCarouselItemViewHolder> {
            return object : RecyclerView.Adapter<SimpleCarouselItemViewHolder>() {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = SimpleCarouselItemViewHolder()
                override fun getItemCount(): Int = data.size
                override fun onBindViewHolder(holder: SimpleCarouselItemViewHolder, position: Int) {
                    holder.itemView.findViewById<TextView>(R.id.text).text = data[position]
                }
            }
        }
    }

    inner class SimpleCarouselItemViewHolder :
        RecyclerView.ViewHolder(LayoutInflater.from(this).inflate(R.layout.simple_carousel_item_text, null))

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
