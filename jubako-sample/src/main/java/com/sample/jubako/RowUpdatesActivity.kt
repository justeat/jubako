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
import com.justeat.jubako.extensions.withJubako
import com.justeat.jubako.viewHolderFactory
import kotlinx.android.synthetic.main.activity_jubako_recycler.recyclerView

class RowUpdatesActivity : AppCompatActivity() {

    private lateinit var updater: RowUpdaterHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jubako_recycler)

        Jubako.logger.enabled = true

        recyclerView.withJubako(this).load {
            for (i in 0 until ROWS) {
                var hello = "Hello Jubako! $i "
                addDescription(
                    id = HELLO_ID + i,
                    data = object : LiveData<String>() {
                        override fun onActive() {
                            postValue(hello)
                        }
                    },
                    onReload = {
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

        updater = RowUpdaterHandler(recyclerView).apply {
            postUpdates()
        }
    }

    class RowUpdaterHandler(private val recycler: RecyclerView) : Handler() {
        private var running: Boolean = false
        var currentRow = 0

        fun postUpdates() {
            running = true
            sendMessageDelayed(Message.obtain().apply { what = UPDATE }, 24)
        }

        fun stop() {
            running = false
            removeMessages(UPDATE)
        }

        override fun handleMessage(msg: Message) {
            if (running) {
                if (msg.what == UPDATE) {
                    (recycler.adapter as? JubakoAdapter)?.reload(HELLO_ID + currentRow++)
                    if (currentRow == ROWS) {
                        currentRow = 0
                    }
                    postUpdates()
                }
            }
        }
    }

    private fun textView(text: String = ""): TextView {
        return TextView(this).apply {
            setText(text)
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 48f)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        updater.stop()
    }
}

private const val HELLO_ID = "hello"
private const val UPDATE = 1
private const val ROWS = 20
