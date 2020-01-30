package com.justeat.jubako.platform

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.justeat.jubako.Jubako
import com.justeat.jubako.recyclerviews.adapters.JubakoAdapter
import com.justeat.jubako.test.R

class RobustnessTestJubakoActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private var liveData = MutableLiveData<() -> Unit>()
    private lateinit var content: Jubako

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_test_robustness_jubakoi)

        recyclerView = findViewById<RecyclerView>(R.id.recyclerView).apply {
            layoutManager = LinearLayoutManager(this@RobustnessTestJubakoActivity)
        }

        liveData.observe(this, Observer {
            it.invoke()
        })

        Jubako.logger.enabled = true

        content = Jubako.observe(this) { state ->
            when (state) {
                is Jubako.State.Assembled -> {
                    bindAdapter(state.data)
                }
            }
        }

        refreshContent(showDo = true, showRe = true, showMi = true)

        findViewById<Button>(R.id.showDo).setOnClickListener {
            liveData.postValue { refreshContent(showDo = true) }
            refreshContent(showDo = true)
        }

        findViewById<Button>(R.id.showRe).setOnClickListener {
            liveData.postValue { refreshContent(showRe = true) }
            refreshContent(showRe = true)
        }

        findViewById<Button>(R.id.showMi).setOnClickListener {
            liveData.postValue { refreshContent(showMi = true) }
            refreshContent(showMi = true)
        }
        findViewById<Button>(R.id.showMi).setOnClickListener {
            liveData.postValue { refreshContent(showDo = true, showRe = true, showMi = true) }
            refreshContent(showDo = true, showRe = true, showMi = true)
        }
    }

    private fun refreshContent(
        showDo: Boolean = false,
        showRe: Boolean = false,
        showMi: Boolean = false
    ) {
        content.reset()
        content.load(TestConditionalContentAssembler(showDo, showRe, showMi))
    }

    private fun bindAdapter(data: Jubako.Data) {
        val contentAdapter = com.justeat.jubako.recyclerviews.adapters.JubakoAdapter(
            lifecycleOwner = this,
            data = data
        )

        recyclerView.adapter = contentAdapter
    }
}
