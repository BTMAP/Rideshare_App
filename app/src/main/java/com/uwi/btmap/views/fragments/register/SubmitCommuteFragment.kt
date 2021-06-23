package com.uwi.btmap.views.fragments.register

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.uwi.btmap.MainActivity
import com.uwi.btmap.R
import com.uwi.btmap.viewmodels.RegisterCommuteViewModel
import com.uwi.btmap.views.activities.RegisterCommuteActivity


private const val TAG = "SubmitCommuteFragment"

class SubmitCommuteFragment : Fragment(R.layout.fragment_submit_commute) {

    private lateinit var  viewModel: RegisterCommuteViewModel
    private lateinit var submitButton: Button
    private lateinit var prevButton: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(RegisterCommuteViewModel::class.java)

        submitButton = view.findViewById(R.id.submit_commute_button)
        submitButton.setOnClickListener{
            //check if commute is valid
            Log.d(TAG, "---------------------------------------------------------")
            if (viewModel.isCommuteValid()){
                Log.d(TAG, "onViewCreated: Is Valid: true")

                if (viewModel.commuteType.value == 0){
//                if driver register commute
                    viewModel.registerDriverCommute()
                }
                if (viewModel.commuteType.value == 1){
//                if passenger find pairs
                    viewModel.findSuitableCommutePairs()
                }
            }else{
                Log.d(TAG, "onViewCreated: Is Valid: false")
            }
        }

        prevButton = view.findViewById(R.id.sub_prev_btn)
        prevButton.setOnClickListener{
            (activity as RegisterCommuteActivity?)?.setPrevPage()
        }

        viewModel.commuteSaveSuccess().observe(requireActivity(), Observer{
            if(it){
                val intent = Intent(requireContext(),MainActivity::class.java)
                startActivity(intent)
            }
        })

    }
}