package com.uwi.btmap.views.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.uwi.btmap.R
import com.uwi.btmap.models.Commute
import com.uwi.btmap.viewmodels.PreviewCommuteViewModel
import com.uwi.btmap.viewmodels.RegisterCommuteViewModel
import com.uwi.btmap.views.fragments.previewCommute.PreviewCommuteFragment
import com.uwi.btmap.views.fragments.selectPair.ListCommutePairFragment

private const val TAG = "PreviewCommuteActivity"

class PreviewCommuteActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview_commute)
        val viewModel = ViewModelProvider(this).get(PreviewCommuteViewModel::class.java)
        //get commute data from intent
        val commute = intent.getSerializableExtra("Commute") as Commute
        Log.d(TAG, "onCreate: $commute")
        //set data in viewModel
        viewModel.commute.value = commute

        //set default fragment
        val fragment = PreviewCommuteFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.preview_commute_frame,fragment)
        transaction.commit()
    }
}