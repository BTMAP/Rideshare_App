package com.uwi.btmap.views.activities

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.uwi.btmap.R
import com.uwi.btmap.models.Commute
import com.uwi.btmap.viewmodels.PreviewCommuteViewModel
import com.uwi.btmap.views.fragments.previewCommute.PreviewCommuteFragment

private const val TAG = "PreviewCommuteActivity"

class PreviewCommuteActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.setTitle("Preview Commute")
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
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
        transaction.replace(R.id.preview_commute_frame, fragment)
        transaction.commit()
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