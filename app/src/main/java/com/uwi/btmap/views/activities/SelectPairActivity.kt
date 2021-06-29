package com.uwi.btmap.views.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.mapbox.geojson.Point
import com.uwi.btmap.MainActivity
import com.uwi.btmap.R
import com.uwi.btmap.models.CommuteOptions
import com.uwi.btmap.viewmodels.SelectPairViewModel
import com.uwi.btmap.views.fragments.selectPair.ListCommutePairFragment

private const val TAG = "ListCommutePairs"

class SelectPairActivity : AppCompatActivity() {

    lateinit var viewModel: SelectPairViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.setTitle("Pair Select")
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        setContentView(R.layout.activity_select_pair)

        viewModel = ViewModelProvider(this).get(SelectPairViewModel::class.java)

        viewModel.commuteOptions.value =
            intent.getSerializableExtra("CommuteOptions") as CommuteOptions
        viewModel.origin.value = intent.getSerializableExtra("PassengerOrigin") as Point
        viewModel.destination.value = intent.getSerializableExtra("PassengerDestination") as Point
        viewModel.originAddress.value = intent.getSerializableExtra("OriginAddress") as String
        viewModel.destinationAddress.value =
            intent.getSerializableExtra("DestinationAddress") as String


        val fragment = ListCommutePairFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.pair_select_fragment, fragment)
        transaction.commit()

        viewModel.pairSuccess().observe(this, Observer {
            if (it) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        })
    }


    override fun onBackPressed() {

        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}