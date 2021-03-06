package com.uwi.btmap.views.fragments.register

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.uwi.btmap.R
import com.uwi.btmap.viewmodels.RegisterCommuteViewModel
import com.uwi.btmap.views.activities.RegisterCommuteActivity
import kotlinx.android.synthetic.main.fragment_submit_commute.*


private const val TAG = "SubmitCommuteFragment"

@Suppress("DEPRECATION")
@SuppressLint("ResourceAsColor", "LogNotTimber")
class SubmitCommuteFragment : Fragment(R.layout.fragment_submit_commute) {

    private lateinit var viewModel: RegisterCommuteViewModel
    private lateinit var submitButton: Button
    private lateinit var prevButton: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(RegisterCommuteViewModel::class.java)

        submitButton = view.findViewById(R.id.submit_commute_button)
        submitButton.setOnClickListener {
            //showProgressBar()
            
            if (viewModel.isCommuteValid()) {
                Log.d(TAG, "onViewCreated: Is Valid: true")

                if (viewModel.commuteType.value == 0) {
//                if driver register commute
                    viewModel.registerDriverCommute()
                }
                if (viewModel.commuteType.value == 1) {
//                if passenger find pairs
                    viewModel.findSuitableCommutePairs()
                }
            } else {
                Toast.makeText(requireContext(), "Failed", Toast.LENGTH_SHORT).show()
            }
        }

        prevButton = view.findViewById(R.id.sub_prev_btn)
        prevButton.setOnClickListener {
            (activity as RegisterCommuteActivity?)?.setPrevPage()
        }

        viewModel.isLoading().observe(requireActivity(), Observer {
            if (it == true) {
                submitButton.isEnabled = false
                submitButton.setBackgroundColor(Color.GRAY)
                viewModel.isLoading()

                val handler = Handler()
                handler.postDelayed({
                    viewModel.commuteSaveSuccess.value = true
                }, 5000)
            }
        })
    }

    private fun showProgressBar() {
        submitFragProgressBar1.visibility = View.VISIBLE
        submitFragProgressBar2.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        submitFragProgressBar1.visibility = View.GONE
        submitFragProgressBar2.visibility = View.GONE
    }
}