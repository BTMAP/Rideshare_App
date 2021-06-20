package com.uwi.btmap.views.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.uwi.btmap.R
import com.uwi.btmap.models.CommuteOptions
import com.uwi.btmap.viewmodels.SelectPairViewModel

const val TAG = "ListCommutePairs"

class SelectPairActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_pair)

        val viewModel = ViewModelProvider(this).get(SelectPairViewModel::class.java)

        viewModel.commuteOptions.value = intent.getSerializableExtra("CommuteOptions") as CommuteOptions
        Log.d(TAG, "onCreate: ${viewModel.commuteOptions.value}")
    }
}