package com.uwi.btmap.views.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.uwi.btmap.R
import com.uwi.btmap.viewmodels.SelectPairViewModel

class SelectPairActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_pair)

        val viewModel = ViewModelProvider(this).get(SelectPairViewModel::class.java)

        val a = intent.getSerializableExtra("CommuteOptions")
    }
}