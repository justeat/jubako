package com.sample.jubako

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        sampleHelloButton.setOnClickListener {
            startActivity(Intent(this, HelloJubakoActivity::class.java))
        }

        sampleSimpleCarousels.setOnClickListener {
            startActivity(Intent(this, SimpleCarouselsActivity::class.java))
        }

        sampleLoadAsync.setOnClickListener {
            startActivity(Intent(this, LoadAsyncActivity::class.java))
        }

        sampleRowUpdates.setOnClickListener {
            startActivity(Intent(this, RowUpdatesActivity::class.java))
        }

        sampleSelfUpdatingItems.setOnClickListener {
            startActivity(Intent(this, SelfUpdatingItemsActivity::class.java))
        }

        sampleEnterpriseHello.setOnClickListener {
            startActivity(Intent(this, EnterpriseHelloJubakoActivity::class.java))
        }
    }
}

