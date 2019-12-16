package com.sample.jubako

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.recyclerview.widget.RecyclerView
import com.justeat.jubako.Jubako
import com.justeat.jubako.adapters.JubakoRecyclerViewHolder
import com.justeat.jubako.data.InstantLiveData
import com.justeat.jubako.extensions.addRecyclerView
import com.justeat.jubako.extensions.loadAsync
import com.justeat.jubako.extensions.withJubako
import kotlinx.android.synthetic.main.activity_jubako_recycler.loadingIndicator
import kotlinx.android.synthetic.main.activity_jubako_recycler.recyclerView

class LoadAsyncActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jubako_recycler)

        Jubako.logger.enabled = true

        recyclerView.withJubako(
            activity = this,
            onAssembling = ::showLoading,
            onAssembled = { showContent() }).loadAsync {

            // Call sleeps (by 4s) to simulate latency
            val compartments = JubakoRepository().getCompartments()

            for (i in 0..100) {
                compartments.forEach { compartment ->
                    addRecyclerView(
                        //
                        // You can specify a custom holder like this, or instead
                        // provide the view parameter instead to just
                        // use the base [JubakoRecyclerViewHolder] with an inflated view
                        // (hint: custom view holders are better for strongly typed binding
                        //  with the viewBinder parameter)
                        //
                        viewHolder = {
                            CustomCarouselHolder(
                                layoutInflater.inflate(R.layout.simple_carousel_with_heading, it, false)
                            )
                        },
                        //
                        // The id of the recycler view in the holder
                        // (with no id it will expect itemView itself is the recycler!)
                        //
                        recyclerViewId = R.id.jubakoCarousel,
                        //
                        // Perform any binding on the view itself
                        //
                        viewBinder = { holder ->
                            holder.heading.text = compartment.title
                        },
                        //
                        // The items (data) that will  bound to the recyclers adapter
                        //
                        data = InstantLiveData(compartment),

                        //
                        // Get the item data at position in the data
                        //
                        itemData = { data, position -> data.items[position] },

                        //
                        // Get the number of items in data
                        //
                        itemCount = { data -> data.items.size },

                        //
                        // A factory to produce item view holders
                        //
                        itemViewHolder = { inflater, parent ->
                            SimpleCarouselItemViewHolder(inflater, parent)
                        },
                        //
                        // Perform binding of the item data to the holder
                        //
                        itemBinder = { holder, data -> holder.bind(data) })
                }
            }
        }
    }

    private fun showContent() {
        loadingIndicator.isGone = true
        recyclerView.isGone = false
    }

    private fun showLoading() {
        loadingIndicator.isGone = false
        recyclerView.isGone = true
    }

    class SimpleCarouselItemViewHolder(inflater: LayoutInflater, parent: ViewGroup) : RecyclerView.ViewHolder(
        inflater.inflate(
            R.layout.simple_carousel_item_text,
            parent,
            false
        )
    ) {
        fun bind(data: String?) {
            itemView.findViewById<TextView>(R.id.text).apply {
                text = data
            }
        }
    }

    data class Compartment(val title: String, val items: List<String>)

    class JubakoRepository {
        fun getCompartments(): List<Compartment> {

            // Simulate network delay (calling retrofit API for instance)
            Thread.sleep(4000)

            return listOf(
                Compartment(
                    "Sushi", listOf("Nigiri", "Sashimi", "Maki", "Uramaki", "Temaki")
                ),
                Compartment(
                    "Sashimi", listOf("Ahi", "Aji", "Amaebi", "Anago", "Aoyagi", "Bincho", "Katsuo")
                )
            )
        }
    }

    class CustomCarouselHolder(itemView: View) :
        JubakoRecyclerViewHolder<Compartment, String, SimpleCarouselItemViewHolder>(itemView) {
        val heading: TextView = itemView.findViewById(R.id.heading)
    }
}
