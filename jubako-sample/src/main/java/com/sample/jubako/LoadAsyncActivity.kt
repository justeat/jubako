package com.sample.jubako

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.recyclerview.widget.RecyclerView
import com.justeat.jubako.Jubako
import com.justeat.jubako.extensions.addCarousel
import com.justeat.jubako.extensions.loadAsync
import kotlinx.android.synthetic.main.activity_jubako_recycler.*

class LoadAsyncActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jubako_recycler)

        Jubako.logger.enabled = true

        Jubako.into(
            activity = this,
            recycler = jubakoRecycler,
            onAssembling = ::showLoading,
            onAssembled = { showContent() }).loadAsync {

            // Call sleeps (by 4s) to simulate latency
            val compartments = JubakoRepository().getCompartments()

            for (i in 0..100) {
                compartments.forEach { compartment ->
                    addCarousel(
                        layout = { layoutInflater.inflate(R.layout.simple_carousel_with_heading, it, false) },
                        carouselRecyclerViewId = R.id.jubakoCarousel,
                        priority = compartment.priority + i,
                        layoutBinder = { layout ->
                            layout.itemView.findViewById<TextView>(R.id.heading).apply {
                                text = compartment.title
                            }
                        },
                        items = compartment.items,
                        itemViewHolderFactory = { SimpleCarouselItemViewHolder(it) },
                        itemBinder = { holder, data -> holder.bind(data) })
                }
            }
        }
    }

    private fun showContent() {
        loadingIndicator.isGone = true
        jubakoRecycler.isGone = false
    }

    private fun showLoading() {
        loadingIndicator.isGone = false
        jubakoRecycler.isGone = true
    }

    inner class SimpleCarouselItemViewHolder(parent: ViewGroup) :
        RecyclerView.ViewHolder(LayoutInflater.from(this).inflate(R.layout.simple_carousel_item_text, parent, false)) {
        fun bind(data: String?) {
            itemView.findViewById<TextView>(R.id.text).apply {
                text = data
            }
        }
    }

    data class Compartment(val priority: Int, val title: String, val items: List<String>)

    class JubakoRepository {
        fun getCompartments(): List<Compartment> {

            // Simulate network delay (calling retrofit API for instance)
            Thread.sleep(4000)

            return listOf(
                Compartment(
                    1, "Sushi",
                    listOf("Nigiri", "Sashimi", "Maki", "Uramaki", "Temaki")
                ),
                Compartment(
                    2, "Sashimi",
                    listOf("Ahi", "Aji", "Amaebi", "Anago", "Aoyagi", "Bincho", "Katsuo")
                )
            )
        }
    }
}
