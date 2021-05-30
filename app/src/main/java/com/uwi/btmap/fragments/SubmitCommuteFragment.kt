package com.uwi.btmap.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import androidx.lifecycle.ViewModelProvider
import com.uwi.btmap.MainActivity
import com.uwi.btmap.R
import com.uwi.btmap.bll.CommuteViewModel

private const val TAG = "SubmitCommuteFragment"

class SubmitCommuteFragment : Fragment(R.layout.fragment_submit_commute) {

    private lateinit var  viewModel:CommuteViewModel
    private lateinit var submitButton: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(CommuteViewModel::class.java)

        submitButton = view.findViewById(R.id.submit_commute_button)
        submitButton.setOnClickListener{
            viewModel.saveCommute()
            val intent = Intent(requireContext(),MainActivity::class.java)
            startActivity(intent)
        }
    }
}