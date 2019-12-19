package com.sample.jubako

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.lifecycle.MutableLiveData
import com.justeat.jubako.ContentDescription
import com.justeat.jubako.ContentDescriptionProvider
import com.justeat.jubako.Jubako
import com.justeat.jubako.Jubako.State.AssembleError
import com.justeat.jubako.Jubako.State.Assembled
import com.justeat.jubako.Jubako.State.Assembling
import com.justeat.jubako.JubakoAdapter
import com.justeat.jubako.JubakoViewHolder
import com.justeat.jubako.SimpleJubakoAssembler
import com.sample.jubako.EnterpriseHelloJubakoActivity.HelloContentDescriptionProvider.Language.ENGLISH
import com.sample.jubako.EnterpriseHelloJubakoActivity.HelloContentDescriptionProvider.Language.JAPANESE
import kotlinx.android.synthetic.main.activity_jubako_recycler.error
import kotlinx.android.synthetic.main.activity_jubako_recycler.loadingIndicator
import kotlinx.android.synthetic.main.activity_jubako_recycler.recyclerView
import kotlinx.android.synthetic.main.activity_jubako_recycler.retryButton
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class EnterpriseHelloJubakoActivity : AppCompatActivity() {

    private lateinit var jubako: Jubako

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jubako_recycler)

        Jubako.logger.enabled = true

        jubako = Jubako.observe(this) { state ->
            when (state) {
                is Assembling -> {
                    showLoading()
                }
                is AssembleError -> {
                    showError()
                }
                is Assembled -> {
                    recyclerView.adapter = JubakoAdapter(this, state.data)
                    showContent()
                }
            }
        }

        retryButton.setOnClickListener {
            jubako.apply {
                reset()
                load(HelloJubakoAssembler())
            }
        }

        jubako.load(HelloJubakoAssembler())
    }

    class HelloJubakoAssembler : SimpleJubakoAssembler() {
        override suspend fun onAssemble(list: MutableList<ContentDescriptionProvider<Any>>) {
            if (randomTime() > FAILURE_THRESHOLD_MS) {
                throw Error("Something went wrong")
            }

            for (i in 0..100) {
                list.add(HelloContentDescriptionProvider(ENGLISH))
                list.add(HelloContentDescriptionProvider(JAPANESE))
            }
        }
    }

    class HelloContentDescriptionProvider(private val language: Language) : ContentDescriptionProvider<String> {
        enum class Language { ENGLISH, JAPANESE }

        private val service = HelloService()

        override fun createDescription(): ContentDescription<String> {
            return ContentDescription(
                data = when (language) {
                    ENGLISH -> service.getHelloEnglish()
                    JAPANESE -> service.getHelloJapanese()
                },
                viewHolderFactory = HelloViewHolderFactory()
            )
        }

    }

    class HelloViewHolderFactory : JubakoAdapter.HolderFactory<String> {
        override fun createViewHolder(parent: ViewGroup): JubakoViewHolder<String> {
            return HelloViewHolder(parent)
        }
    }

    class HelloViewHolder(parent: ViewGroup) : JubakoViewHolder<String>(
        LayoutInflater.from(parent.context).inflate(R.layout.simple_item_text, parent, false)
    ) {

        private val textView = itemView.findViewById<TextView>(R.id.text)

        override fun bind(data: String?) {
            textView.text = data
        }
    }

    class HelloService {
        fun getHelloEnglish() = object : MutableLiveData<String>() {
            override fun onActive() {
                post("Hello Jubako!")
            }
        }

        fun getHelloJapanese() = object : MutableLiveData<String>() {
            override fun onActive() {
                post("こんにちはジュバコ")
            }
        }

        val post: MutableLiveData<String>.(String) -> Unit = { message ->
            GlobalScope.launch(IO) {
                randomTime().let {
                    delay(it) // random delay
                }
                postValue(message)
            }
        }
    }

    private fun showError() {
        error.isGone = false
        loadingIndicator.isGone = true
        recyclerView.isGone = true
    }

    private fun showLoading() {
        error.isGone = true
        loadingIndicator.isGone = false
        recyclerView.isGone = true
    }

    private fun showContent() {
        error.isGone = true
        loadingIndicator.isGone = true
        recyclerView.isGone = false
    }
}

//
// Simulate for some random delay
//
private const val MAX_DELAY_MS = 300L
private const val FAILURE_THRESHOLD_MS = 150L
private val random = Random(System.currentTimeMillis())
val randomTime: () -> Long = { random.nextLong(MAX_DELAY_MS) }
