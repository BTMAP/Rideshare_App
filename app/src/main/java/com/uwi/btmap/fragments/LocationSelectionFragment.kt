package com.uwi.btmap.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.mapbox.api.geocoding.v5.GeocodingCriteria
import com.mapbox.api.geocoding.v5.MapboxGeocoding
import com.mapbox.api.geocoding.v5.models.GeocodingResponse
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions
import com.uwi.btmap.bll.CommuteViewModel
import com.uwi.btmap.R
import retrofit2.Callback
import retrofit2.Call
import retrofit2.Response

private const val TAG = "LocationSelectFragment"

class LocationSelectionFragment : Fragment(R.layout.fragment_location_selection) {

    private lateinit var viewModel: CommuteViewModel

    private lateinit var originButton: Button
    private lateinit var destinationButton: Button

    private lateinit var originEditText: TextView
    private lateinit var destinationEditText: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(CommuteViewModel::class.java)

        originButton = view.findViewById(R.id.origin_selection_button)
        destinationButton = view.findViewById(R.id.destination_selection_button)

        originEditText = view.findViewById(R.id.origin_edit_text)
        destinationEditText = view.findViewById(R.id.destination_edit_text)

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

        originEditText.setOnClickListener{
            var intent = PlaceAutocomplete.IntentBuilder()
                .accessToken(getString(R.string.mapbox_access_token))
                .placeOptions(PlaceOptions.builder()
                    .backgroundColor(Color.parseColor("#EEEEEE"))
                    .limit(10)
                    .build(PlaceOptions.MODE_CARDS))
                .build(requireActivity())

            startActivityForResult(intent,1)
        }

        destinationEditText.setOnClickListener{
            var intent = PlaceAutocomplete.IntentBuilder()
                .accessToken(getString(R.string.mapbox_access_token))
                .placeOptions(PlaceOptions.builder()
                    .backgroundColor(Color.parseColor("#EEEEEE"))
                    .limit(10)
                    .build(PlaceOptions.MODE_CARDS))
                .build(requireActivity())

            startActivityForResult(intent,2)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 1) {
            val feature = PlaceAutocomplete.getPlace(data)
            viewModel.origin.value = feature.center()
        }

        if (resultCode == Activity.RESULT_OK && requestCode == 2) {
            val feature = PlaceAutocomplete.getPlace(data)
            viewModel.destination.value = feature.center()
        }
    }

    private fun geoCodeRequest(){
        Log.d(TAG, "geoCodeRequest: Called.")
        val mapboxGeocoding = MapboxGeocoding.builder()
            .accessToken(getString(R.string.mapbox_access_token))
            .query("Barbados Bridgetown")
            .geocodingTypes(GeocodingCriteria.TYPE_ADDRESS)
            .build()

        mapboxGeocoding.enqueueCall(object:
            Callback<GeocodingResponse> {
            override fun onResponse(
                call: Call<GeocodingResponse>,
                response: Response<GeocodingResponse>
            ) {
                Log.d(TAG, "onResponse: Geocoder response called.")
                val results = response.body()!!.features()

                if (results.size > 0) {

                    for(result in results){
                        Log.d(TAG, "onResponse: Result: $result")
                    }

                }else{
                    Log.d(TAG, "onResponse: No result found.")
                }
            }

            override fun onFailure(call: Call<GeocodingResponse>, t: Throwable) {
                t.printStackTrace()
            }

        })
    }
}