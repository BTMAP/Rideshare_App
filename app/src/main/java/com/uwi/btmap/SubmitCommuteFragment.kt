package com.uwi.btmap

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

private const val TAG = "SubmitCommuteFragment"

class SubmitCommuteFragment : Fragment(R.layout.fragment_submit_commute) {

    private lateinit var submitButton: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        submitButton = view.findViewById(R.id.submit_commute_button)
    }
}