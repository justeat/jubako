package com.sample.jubako

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.TypedValue
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.justeat.jubako.Jubako
import com.justeat.jubako.JubakoAdapter
import com.justeat.jubako.JubakoViewHolder
import com.justeat.jubako.extensions.addDescription
import com.justeat.jubako.extensions.load
import com.justeat.jubako.extensions.viewHolderFactory
import kotlinx.android.synthetic.main.activity_jubako_recycler.*

class RowUpdatesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jubako_recycler)

        Jubako.logger.enabled = true

        Jubako.into(this, jubakoRecycler).load {
            for (i in 0 until ROWS) {
                var hello = "Hello Jubako!"
                addDescription(
                    id = HELLO_ID + i,
                    data = object : LiveData<String>() {
                        override fun onActive() {
                            postValue(hello)
                        }
                    },
                    onReload = { _, _ ->
                        hello = hello.plus(hello.first()).drop(1)
                    },
                    viewHolderFactory = viewHolderFactory {
                        object : JubakoViewHolder<String>(textView()) {
                            override fun bind(data: String?) {
                                (itemView as TextView).text = data
                            }
                        }
                    }
                )
            }
        }

        RowUpdaterHandler(jubakoRecycler).postUpdates()
    }

    class RowUpdaterHandler(private val recycler: RecyclerView) : Handler() {
        var currentRow = 0

        fun postUpdates() {
            sendMessageDelayed(Message.obtain().apply { what = UPDATE }, 5)
        }

        override fun handleMessage(msg: Message) {
            if (msg.what == UPDATE) {
                (recycler.adapter as? JubakoAdapter)?.reload(HELLO_ID + currentRow++)
                if (currentRow == ROWS) {
                    currentRow = 0
                }
                postUpdates()
            }
        }
    }

    private fun textView(text: String = ""): TextView {
        return TextView(this).apply {
            setText(text)
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 48f)
        }
    }
}

private const val HELLO_ID = "hello"
private const val UPDATE = 1
private const val ROWS = 10
