package com.uwi.btmap.views.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mapbox.geojson.Point
import com.uwi.btmap.MainActivity
import com.uwi.btmap.R
import com.uwi.btmap.models.CommuteOptions
import com.uwi.btmap.viewmodels.SelectPairViewModel
import com.uwi.btmap.views.fragments.commuteList.PreviewCommutePairFragment
import com.uwi.btmap.views.fragments.selectPair.ListCommutePairFragment

private const val TAG = "ListCommutePairs"

class SelectPairActivity : AppCompatActivity() {

    lateinit var viewModel:SelectPairViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.setTitle("Pair Select")
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        setContentView(R.layout.activity_select_pair)

        noCommutes()

        viewModel = ViewModelProvider(this).get(SelectPairViewModel::class.java)

        viewModel.commuteOptions.value = intent.getSerializableExtra("CommuteOptions") as CommuteOptions
        viewModel.origin.value = intent.getSerializableExtra("PassengerOrigin") as Point
        viewModel.destination.value = intent.getSerializableExtra("PassengerDestination") as Point
        viewModel.originAddress.value = intent.getSerializableExtra("OriginAddress") as String
        viewModel.destinationAddress.value = intent.getSerializableExtra("DestinationAddress") as String

        val fragment = ListCommutePairFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.pair_select_fragment,fragment)
        transaction.commit()

        viewModel.pairSuccess().observe(this,Observer{
            if(it){
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        })
    }

    private fun noCommutes(){
        val commuteNo = viewModel.commuteOptions.value
        if (commuteNo == null){
            val builder = AlertDialog.Builder(this)
            builder.setCancelable(false)
            builder.setTitle("No Commutes")
            builder.setMessage("There are no current pairable commutes. Do you wish to change your commute details?")

            builder.setPositiveButton(android.R.string.yes) { dialog, which ->
                val intent = Intent(this@SelectPairActivity, RegisterCommuteActivity::class.java)
                startActivity(intent)
                finish()
            }

            builder.setNegativeButton(android.R.string.no) { dialog, which ->
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }

            builder.show()
        }
    }

    override fun onBackPressed() {
        if (viewModel.currentFragment.value == 0) {
            super.onBackPressed()
        }else{
            val fragment = ListCommutePairFragment()
            replaceFragment(fragment)
        }
    }

    private fun replaceFragment(fragment: Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.pair_select_fragment,fragment)
        fragmentTransaction.commit()
    }

}