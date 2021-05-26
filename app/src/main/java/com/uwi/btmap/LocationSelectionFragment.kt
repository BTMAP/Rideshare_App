package com.uwi.btmap

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.lifecycle.ViewModelProvider
import com.uwi.btmap.BLL.CommuteViewModel

private const val TAG = "LocationSelectionFragment"

class LocationSelectionFragment : Fragment(R.layout.fragment_location_selection) {

    private lateinit var viewModel: CommuteViewModel

    private lateinit var originButton: Button
    private lateinit var destinationButton: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(CommuteViewModel::class.java)

        originButton = view.findViewById(R.id.origin_selection_button)
        destinationButton = view.findViewById(R.id.destination_selection_button)

        originButton.setOnClickListener {
            if (viewModel.locationSelectionMode == 1){
                viewModel.locationSelectionMode = 0
            }else{
                viewModel.locationSelectionMode = 1
            }

        }

        destinationButton.setOnClickListener {
            if (viewModel.locationSelectionMode == 2){
                viewModel.locationSelectionMode = 0
            }else{
                viewModel.locationSelectionMode = 2
            }
        }
    }

}