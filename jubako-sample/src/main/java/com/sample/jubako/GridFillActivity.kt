package com.sample.jubako

import android.graphics.Color
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.justeat.jubako.Jubako
import com.justeat.jubako.data.PaginatedLiveData
import com.justeat.jubako.extensions.addRecyclerView
import com.justeat.jubako.extensions.load
import com.justeat.jubako.extensions.pageSize
import com.justeat.jubako.extensions.withJubako
import kotlinx.android.synthetic.main.activity_jubako_recycler.*
import kotlin.random.Random
import kotlin.random.nextInt

class GridFillActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jubako_recycler)

        Jubako.logger = Jubako.Logger(BuildConfig.DEBUG)

        // Set page size to 1 so we can see it loading (descriptions are delayed by 500ms)
        recyclerView.withJubako(this, pageSize(1)).load {
            (0 until 100).forEach { index ->
                addRecyclerView(
                    //
                    // Inflate a view for our carousel
                    //
                    view = {
                        LayoutInflater.from(this@GridFillActivity).inflate(R.layout.simple_carousel, it, false)
                    },
                    //
                    // Provide a lambda that will create our carousel item view holder
                    //
                    itemViewHolder = { SimpleCarouselItemViewHolder(it) },
                    //
                    // Specify the data that will be loaded into the carousel
                    //
                    data = getGridCell(index),
                    //
                    // Provide a lambda that will fetch carousel item data by position
                    //
                    itemData = { data, position -> data.loaded[position] },
                    //
                    // Specify a lambda that will provide the count of item data in our carousel
                    //
                    itemCount = { data -> data.loaded.size },
                    //
                    // Specify a viewBinder that will allow binding between data and item holder
                    //
                    itemBinder = { holder, data ->
                        holder.itemView.setBackgroundColor(
                            when (data) {
                                true -> Color.BLACK
                                else -> Color.WHITE
                            }
                        )
                    }
                )
            }
        }
    }

    inner class SimpleCarouselItemViewHolder(parent: ViewGroup) :
        RecyclerView.ViewHolder(LayoutInflater.from(this).inflate(R.layout.grid_cell, parent, false))

    companion object {
        val random = Random(SystemClock.uptimeMillis())
        val crashRate = 0 // crashes 1/10 times

        fun getGridCell(offset: Int): PaginatedLiveData<Boolean> {
            return PaginatedLiveData {
                hasMore = { loaded.size < 100 }
                nextPage = {
//                    if (random.nextInt(0..10) <= Math.min(crashRate, 10)) {
//                        throw RuntimeException("Error")
//                    }
                    listOf(((offset + loaded.size) % 2 == 0))
                }
            }
        }
    }
}
