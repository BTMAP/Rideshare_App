package com.uwi.btmap

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.uwi.btmap.BLL.CommuteViewModel

private const val TAG = "MapboxPreviewFragment"

class RoutePreviewFragment : Fragment(R.layout.fragment_route_preview), 
    OnMapReadyCallback{

    private lateinit var mapView: MapView
    private lateinit var mapboxMap: MapboxMap

    private lateinit var viewModel: CommuteViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(CommuteViewModel::class.java)
        setupMapView(view,savedInstanceState)
    }

    private fun setupMapView(view: View, savedInstanceState: Bundle?){
        this.mapView = view.findViewById(R.id.route_preview_map_view)
        this.mapView?.onCreate(savedInstanceState)
        this.mapView?.getMapAsync(this)
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        mapboxMap.setStyle(Style.MAPBOX_STREETS){
            this.mapboxMap = mapboxMap
            mapboxMap.addOnMapLongClickListener{
                onMapClick(it)
            }

            centerMapCamera(mapboxMap)
        }
    }
    
    private fun centerMapCamera(mapboxMap: MapboxMap){
        //bim coords: 13.1939° N, 59.5432° W
        // lat: 13.1939, long: -59.5432
        val position = CameraPosition.Builder()
            .zoom(10.0)
            .tilt(0.0)
            .target(LatLng(13.1939,-59.5432))
            .build()

        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position))
    }

    private fun onMapClick(point: LatLng): Boolean {

        return true
    }
}