package com.sample.jubako

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import com.justeat.jubako.*
import com.justeat.jubako.extensions.withJubako
import com.sample.jubako.EnterpriseHelloJubakoActivity.HelloContentDescriptionProvider.Language.ENGLISH
import com.sample.jubako.EnterpriseHelloJubakoActivity.HelloContentDescriptionProvider.Language.JAPANESE
import kotlinx.android.synthetic.main.activity_jubako_recycler.*

class EnterpriseHelloJubakoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jubako_recycler)

        Jubako.logger.enabled = true

        recyclerView.withJubako(this).load(HelloJubakoAssembler())
    }

    class HelloJubakoAssembler : SimpleJubakoAssembler() {
        override fun onAssemble(list: MutableList<ContentDescriptionProvider<Any>>) {
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

        val textView = itemView.findViewById<TextView>(R.id.text)

        override fun bind(data: String?) {
            textView.text = data
        }
    }

    class HelloService {
        fun getHelloEnglish() = object : LiveData<String>() {
            override fun onActive() {
                postValue("Hello Jubako!")
            }
        }

        fun getHelloJapanese() = object : LiveData<String>() {
            override fun onActive() {
                postValue("こんにちはジュバコ")
            }
        }
    }
}
