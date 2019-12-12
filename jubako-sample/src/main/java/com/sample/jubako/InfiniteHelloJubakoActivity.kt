package com.sample.jubako

import android.os.Bundle
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.justeat.jubako.ContentDescriptionProvider
import com.justeat.jubako.Jubako
import com.justeat.jubako.SimpleJubakoAssembler
import com.justeat.jubako.extensions.addView
import com.justeat.jubako.extensions.withJubako
import kotlinx.android.synthetic.main.activity_jubako_recycler.*

class InfiniteHelloJubakoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jubako_recycler)

        Jubako.logger.enabled = true

        recyclerView.withJubako(this).load(HelloAssembler())
    }

    class HelloAssembler : SimpleJubakoAssembler() {
        var counter = 0
        //
        // Always has more == infinite!
        // Normally you should calculate this value, for instance
        // start returning false when you get to the end
        // of your data source
        //
        override fun hasMore() = true

        override fun onAssemble(list: MutableList<ContentDescriptionProvider<Any>>) {
            // pages of ten
            for (i in 0 until 10) {
                val num = ++counter
                list.addView { textView(it, "Hello Jubako! $num") }
            }
        }

        private fun textView(parent: ViewGroup, text: String): TextView {
            return TextView(parent.context).apply {
                setText(text)
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, 48f)
            }
        }
    }
}
