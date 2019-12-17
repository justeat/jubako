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
import com.justeat.jubako.ContentDescriptionProvider
import com.justeat.jubako.Jubako
import com.justeat.jubako.SimpleJubakoAssembler
import com.justeat.jubako.adapters.ProgressView
import com.justeat.jubako.data.PaginatedLiveData
import com.justeat.jubako.extensions.addRecyclerView
import com.justeat.jubako.extensions.pageSize
import com.justeat.jubako.extensions.withJubako
import kotlinx.android.synthetic.main.activity_grid_fill.errorRates
import kotlinx.android.synthetic.main.activity_grid_fill.reloadVButton
import kotlinx.android.synthetic.main.activity_grid_fill.tileSize
import kotlinx.android.synthetic.main.activity_jubako_recycler.recyclerView
import kotlin.math.min
import kotlin.random.Random
import kotlin.random.nextInt

class GridFillActivity : AppCompatActivity() {

    private lateinit var jubako: Jubako

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grid_fill)

        setupUi()

        Jubako.logger = Jubako.Logger(BuildConfig.DEBUG)

        jubako = recyclerView.withJubako(this, pageSize(PAGE_SIZE))

        load()
    }

    private fun load() {
        val crashRate = { (errorRates.selectedItem as String).split("/").first().toInt() }
        val tileSize = { (tileSize.selectedItem as String).toInt() }

        jubako.load(GridFillAssembler(crashRate, tileSize))
    }

    class GridFillAssembler(
        private val crashRate: () -> Int,
        private val tileSize: () -> Int
    ) :
        SimpleJubakoAssembler() {
        var counter = 0

        override fun hasMore() = counter < NUM_ROWS

        override suspend fun onAssemble(list: MutableList<ContentDescriptionProvider<Any>>) {

            throwRandomError(crashRate)

            list.addRecyclerView(
                //
                // Inflate a view for our carousel
                //
                view = { inflater, parent ->
                    inflater.inflate(R.layout.simple_carousel, parent, false)
                },
                //
                // Provide a lambda that will create our carousel item view holder
                //
                itemViewHolder = { inflater, parent, _ ->
                    SimpleCarouselItemViewHolder(inflater, parent, tileSize)
                },
                //
                // Specify the data that will be loaded into the carousel
                //
                data = getGridCell(counter, crashRate),
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
                progressViewHolder = { inflater, parent, _ ->
                    ProgressViewHolder(inflater, parent, tileSize)
                }
            )

            counter++
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
        tileSize.setSelection(7)
    }

    //
    // A simple holder for a carousel item
    //
    class SimpleCarouselItemViewHolder(inflater: LayoutInflater, parent: ViewGroup, tileSize: () -> Int) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.grid_cell, parent, false)) {
        init {
            itemView.layoutParams = FrameLayout.LayoutParams(tileSize(), tileSize())
        }
    }

    //
    // A custom holder to show progress and error-retry state
    //
    class ProgressViewHolder(inflater: LayoutInflater, parent: ViewGroup, tileSize: () -> Int) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.grid_fill_progress, parent, false)), ProgressView {
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

        fun getGridCell(offset: Int, crashRate: () -> Int): PaginatedLiveData<Boolean> {
            return PaginatedLiveData {
                hasMore = { loaded.size < NUM_COLUMNS }
                nextPage = {
                    throwRandomError(crashRate)
                    listOf(((offset + loaded.size) % 2 == 0))
                }
            }
        }
    }
}

private fun throwRandomError(crashRate: () -> Int) {
    if (random.nextInt(1..10) <= min(crashRate(), 10)) {
        throw RuntimeException("Error")
    }
}

private val random = Random(SystemClock.uptimeMillis())
private const val PAGE_SIZE = 1
private const val NUM_ROWS = 100
private const val NUM_COLUMNS = 100
