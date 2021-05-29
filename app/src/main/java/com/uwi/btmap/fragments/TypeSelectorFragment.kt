package com.uwi.btmap.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.lifecycle.ViewModelProvider
import com.uwi.btmap.bll.CommuteViewModel
import com.uwi.btmap.R

private const val TAG = "TypeSelectorFragment"

class TypeSelectorFragment : Fragment() {
    private lateinit var driverButton: Button
    private lateinit var passengerButton: Button

    private lateinit var viewModel: CommuteViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            //set vars from bundle
        }

        viewModel = ViewModelProvider(requireActivity()).get(CommuteViewModel::class.java)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_type_selector, container, false)

        driverButton = view.findViewById(R.id.driver_button)
        passengerButton = view.findViewById(R.id.passenger_button)

        driverButton.setOnClickListener {
            viewModel.setCommuteType(0)
            Log.d(TAG, "onCreateView: DriverType Set: ${viewModel.commuteType}")
        }

        passengerButton.setOnClickListener {
            viewModel.setCommuteType(1)
            Log.d(TAG, "onCreateView: DriverType Set: ${viewModel.commuteType}")
        }

        return view
    }

}