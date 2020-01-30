package com.justeat.jubako.platform

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.justeat.jubako.Jubako
import com.justeat.jubako.recyclerviews.adapters.JubakoAdapter
import com.justeat.jubako.test.R

class TestJubakoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_test_jubako)

        Jubako.logger.enabled = true
        Jubako.observe(this) { state ->
            when (state) {
                is Jubako.State.Assembled -> {
                    bindAdapter(state.data)
                }
            }
        }.load(TestContentAssembler())
    }

    private fun bindAdapter(data: Jubako.Data) {
        val contentAdapter = com.justeat.jubako.recyclerviews.adapters.JubakoAdapter(
            lifecycleOwner = this,
            data = data
        )

        findViewById<RecyclerView>(R.id.recyclerView).apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this@TestJubakoActivity)
            adapter = contentAdapter
        }

        findViewById<Button>(R.id.showPeekaBooButton).setOnClickListener {
            contentAdapter.reload(TestContentAssembler.ID_ROW_RAY)
        }
    }
}
