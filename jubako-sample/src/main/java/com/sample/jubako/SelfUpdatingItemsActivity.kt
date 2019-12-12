package com.sample.jubako

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import com.justeat.jubako.Jubako
import com.justeat.jubako.JubakoViewHolder
import com.justeat.jubako.extensions.addDescription
import com.justeat.jubako.extensions.load
import com.justeat.jubako.extensions.withJubako
import com.justeat.jubako.viewHolderFactory
import kotlinx.android.synthetic.main.activity_jubako_recycler.*

class SelfUpdatingItemsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jubako_recycler)

        Jubako.logger.enabled = true

        recyclerView.withJubako(this).load {
            for (i in 0..100) {
                var counter = 0
                addDescription(
                    viewHolderFactory = viewHolderFactory { CountingViewHolder(it) },
                    data = object : LiveData<Int>() {
                        override fun onActive() {
                            postValue(counter)
                        }
                    },
                    onReload = { counter++ }
                )
            }
        }
    }

    class CountingViewHolder(parent: ViewGroup) : JubakoViewHolder<Int>(
        LayoutInflater.from(parent.context).inflate(R.layout.simple_item_text, parent, false)
    ) {
        private val textView = itemView.findViewById<TextView>(R.id.text).apply {
            text = "Press Me!"
            setOnClickListener {
                reload()
            }
        }

        override fun bind(data: Int?) {
            textView.text = "Press Me! $data"
        }
    }
}
