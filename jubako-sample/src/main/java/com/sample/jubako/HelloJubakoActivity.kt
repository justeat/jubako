package com.sample.jubako

import android.os.Bundle
import android.util.TypedValue
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.justeat.jubako.Jubako
import com.justeat.jubako.extensions.addView
import com.justeat.jubako.extensions.load
import com.justeat.jubako.extensions.withJubako
import kotlinx.android.synthetic.main.activity_jubako_recycler.*

class HelloJubakoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jubako_recycler)

        Jubako.logger.enabled = true

        jubakoRecycler.withJubako(this).load {
            for (i in 0..100) {
                addView { textView("Hello Jubako!") }
                addView { textView("こんにちはジュバコ") }
            }
        }
    }

    private fun textView(text: String): TextView {
        return TextView(this).apply {
            setText(text)
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 48f)
        }
    }
}
