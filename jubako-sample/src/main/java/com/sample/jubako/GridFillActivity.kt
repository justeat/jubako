package com.sample.jubako

import android.graphics.Color
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.justeat.jubako.Jubako
import com.justeat.jubako.data.PaginatedLiveData
import com.justeat.jubako.extensions.*
import kotlinx.android.synthetic.main.activity_grid_fill.*
import kotlinx.android.synthetic.main.activity_jubako_recycler.recyclerView
import kotlin.random.Random
import kotlin.random.nextInt

class GridFillActivity : AppCompatActivity() {

    private lateinit var jubako: Jubako

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grid_fill)

        setupUi()

        Jubako.logger = Jubako.Logger(BuildConfig.DEBUG)

        jubako = recyclerView.withJubako(this, pageSize(1))

        load()
    }

    private fun load() {
        val crashRate = { (errorRates.selectedItem as String).split("/").first().toInt() }
        val tileSize = { (tileSize.selectedItem as String).toInt() }

        jubako.load {
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
                    itemViewHolder = { SimpleCarouselItemViewHolder(it, tileSize) },
                    //
                    // Specify the data that will be loaded into the carousel
                    //
                    data = getGridCell(index, crashRate),
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
                    },
                    //
                    // A custom progress view holder for this carousel
                    //
                    progressViewHolder = { ProgressViewHolder(it, tileSize) }
                )
            }
        }
    }

    private fun setupUi() {
        setupErrorRatesSpinner()
        setupPageSizeSpinner()

        reloadVButton.setOnClickListener {
            jubako.reset()
            load()
        }
    }

    private fun setupErrorRatesSpinner() {
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item).apply {
            for (i in 0..10) {
                addAll("$i/10")
            }
        }

        errorRates.adapter = adapter
        errorRates.setSelection(1)
    }

    private fun setupPageSizeSpinner() {
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item).apply {
            for (i in 25..500 step 25) {
                addAll("$i")
            }
        }

        tileSize.adapter = adapter
        tileSize.setSelection(3)
    }

    //
    // A simple holder for a carousel item
    //
    inner class SimpleCarouselItemViewHolder(parent: ViewGroup, tileSize: () -> Int) :
        RecyclerView.ViewHolder(LayoutInflater.from(this).inflate(R.layout.grid_cell, parent, false)) {
        init {
            itemView.layoutParams = FrameLayout.LayoutParams(tileSize(), tileSize())
        }
    }

    //
    // A custom holder to show progress and error-retry state
    //
    class ProgressViewHolder(parent: ViewGroup, tileSize: () -> Int) :
        RecyclerView.ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.grid_fill_progress, parent, false)
        ), ProgressView {
        init {
            itemView.layoutParams = FrameLayout.LayoutParams(tileSize(), tileSize())
        }

        override fun setRetryCallback(retry: () -> Unit) {
            itemView.findViewById<View>(R.id.button_retry).setOnClickListener {
                retry.invoke()
            }
        }

        override fun onProgress() {
            itemView.findViewById<View>(R.id.progress).visibility = View.VISIBLE
            itemView.findViewById<View>(R.id.button_retry).visibility = View.GONE
        }

        override fun onError(error: Throwable) {
            itemView.findViewById<View>(R.id.progress).visibility = View.GONE
            itemView.findViewById<View>(R.id.button_retry).visibility = View.VISIBLE
        }
    }

    companion object {
        val random = Random(SystemClock.uptimeMillis())
        fun getGridCell(offset: Int, crashRate: () -> Int): PaginatedLiveData<Boolean> {
            return PaginatedLiveData {
                hasMore = { loaded.size < 100 }
                nextPage = {
                    if (random.nextInt(1..10) <= Math.min(crashRate(), 10)) {
                        throw RuntimeException("Error")
                    }
                    listOf(((offset + loaded.size) % 2 == 0))
                }
            }
        }
    }
}
