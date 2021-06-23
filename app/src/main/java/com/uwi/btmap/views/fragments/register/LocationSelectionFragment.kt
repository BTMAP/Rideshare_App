package com.uwi.btmap.views.fragments.register

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions
import com.uwi.btmap.R
import com.uwi.btmap.viewmodels.RegisterCommuteViewModel
import com.uwi.btmap.views.activities.RegisterCommuteActivity


private const val TAG = "LocationSelectFragment"

class LocationSelectionFragment : Fragment(R.layout.fragment_location_selection) {

    private lateinit var viewModel: RegisterCommuteViewModel

    private lateinit var originButton: ImageButton
    private lateinit var destinationButton: ImageButton

    private lateinit var nextButton: Button
    private lateinit var prevButton: Button

    private lateinit var originAddressText: TextView
    private lateinit var destinationAddressText: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(RegisterCommuteViewModel::class.java)

        originButton = view.findViewById(R.id.origin_selection_button)
        destinationButton = view.findViewById(R.id.destination_selection_button)

        nextButton = view.findViewById(R.id.loc_next_btn)
        prevButton = view.findViewById(R.id.loc_prev_btn)

        originAddressText = view.findViewById(R.id.origin_edit_text)
        destinationAddressText = view.findViewById(R.id.destination_edit_text)

        originButton.setOnClickListener {
            if (viewModel.locationSelectionMode == 1) {
                viewModel.locationSelectionMode = 0
                originButton.isSelected = false
            } else {
                viewModel.locationSelectionMode = 1
                originButton.isSelected = true
            }
        }


        destinationButton.setOnClickListener {
            if (viewModel.locationSelectionMode == 2) {
                viewModel.locationSelectionMode = 0
                destinationButton.isSelected = false
            } else {
                viewModel.locationSelectionMode = 2
                destinationButton.isSelected = true
            }
        }

        nextButton.setOnClickListener {
            (activity as RegisterCommuteActivity?)?.setNextPage()
        }

        prevButton.setOnClickListener {
            (activity as RegisterCommuteActivity?)?.setPrevPage()
        }

        originAddressText.setOnClickListener {
            var intent = PlaceAutocomplete.IntentBuilder()
                .accessToken(getString(R.string.mapbox_access_token))
                .placeOptions(
                    PlaceOptions.builder()
                        .country("BB")
                        .backgroundColor(Color.parseColor("#EEEEEE"))
                        .limit(10)
                        .build(PlaceOptions.MODE_CARDS)
                )
                .build(requireActivity())

            startActivityForResult(intent, 1)

            originButton.isSelected = true
        }

        destinationAddressText.setOnClickListener {
            var intent = PlaceAutocomplete.IntentBuilder()
                .accessToken(getString(R.string.mapbox_access_token))
                .placeOptions(
                    PlaceOptions.builder()
                        .country("BB")
                        .backgroundColor(Color.parseColor("#EEEEEE"))
                        .limit(10)
                        .build(PlaceOptions.MODE_CARDS)
                )
                .build(requireActivity())

            startActivityForResult(intent, 2)

            destinationButton.isSelected = true
        }

        viewModel.originAddress().observe(requireActivity(), Observer {
            originAddressText.text = it
        })

        viewModel.destinationAddress().observe(requireActivity(), Observer {
            destinationAddressText.text = it
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 1) {
            val feature = PlaceAutocomplete.getPlace(data)
            viewModel.origin.value = feature.center()
            //set text using placeName
            //feature.text is shorter (could use instead)
            //could use address if it is not null
            viewModel.originAddress.value = feature.placeName()
        }

        if (resultCode == Activity.RESULT_OK && requestCode == 2) {
            val feature = PlaceAutocomplete.getPlace(data)
            viewModel.destination.value = feature.center()
            //set text using placeName
            //feature.text is shorter (could use instead)
            //could use address if it is not null
            viewModel.destinationAddress.value = feature.placeName()
        }
    }


}