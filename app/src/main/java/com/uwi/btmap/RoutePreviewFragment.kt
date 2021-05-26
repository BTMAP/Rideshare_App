package com.uwi.btmap

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.mapbox.geojson.GeoJson
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.utils.BitmapUtils
import com.uwi.btmap.BLL.CommuteViewModel

private const val TAG = "MapboxPreviewFragment"

class RoutePreviewFragment : Fragment(R.layout.fragment_route_preview), 
    OnMapReadyCallback{

    val routeSourceID = "ROUTE _SOURCE_ID"
    val originSourceID = "ORIGIN_SOURCE_ID"
    val destinationSourceID = "DESTINATION_SOURCE_ID"

    val routeLayerID = "ROUTE_LAYER_ID"
    val originLayerID = "ORIGIN_LAYER_ID"
    val destinationLayerID = "DESTINATION_LAYER_ID"

    val locationMarkerID = "LOCATION_MARKER_ID"

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

            initLocationIcons(it)
            initMapLayers(it)

            mapboxMap.addOnMapLongClickListener{
                onMapClick(it)
            }

            centerMapCamera(mapboxMap)
        }

        //add observer for origin and destination
        viewModel.origin().observe(requireActivity(), Observer {
            //add/update source
            if (it != null){
                var source = mapboxMap.style?.getSourceAs<GeoJsonSource>(originSourceID)
                source?.setGeoJson(it)
            }
            //reset selection mode
            viewModel.locationSelectionMode = 0
        })

        viewModel.destination().observe(requireActivity(), Observer {
            //add/update source
            if (it != null){
                var source = mapboxMap.style?.getSourceAs<GeoJsonSource>(destinationSourceID)
                source?.setGeoJson(it)
            }
            //reset selection mode
            viewModel.locationSelectionMode = 0
        })
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
        if(viewModel.locationSelectionMode == 0){
            Log.d(TAG, "onMapClick: Don't add location")
        }
        if(viewModel.locationSelectionMode == 1){
            Log.d(TAG, "onMapClick: Add origin location: $point")
            viewModel.origin.value = Point.fromLngLat(point.longitude,point.latitude)
//            var source = mapboxMap.style?.getSourceAs<GeoJsonSource>(originSourceID)
//            source?.setGeoJson(viewModel.origin.value)
        }
        if(viewModel.locationSelectionMode == 2){
            Log.d(TAG, "onMapClick: Add destination location: $point")
            viewModel.destination.value = Point.fromLngLat(point.longitude,point.latitude)
        }

        return true
    }

    //functions to setup sources
    private fun initMapLayers(style: Style){

        //route layer
        style.addSource(GeoJsonSource(
            routeSourceID,
            GeoJsonOptions().withLineMetrics(true)
        ))
        style.addLayerBelow(
            LineLayer(routeLayerID, routeSourceID)
                .withProperties(
                    PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                    PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                    PropertyFactory.lineWidth(6f),
                    PropertyFactory.lineOpacity(1f),
                    PropertyFactory.lineColor("#2E4FC9")
                ),
            "mapbox-location-shadow-layer"
        )

        //origin layer
        style.addSource(GeoJsonSource(originSourceID))
        style.addLayerAbove(SymbolLayer(originLayerID,originSourceID)
            .withProperties(iconImage(locationMarkerID)),routeLayerID)

        //destination layer
        style.addSource(GeoJsonSource(destinationSourceID))
        style.addLayerBelow(SymbolLayer(destinationLayerID,destinationSourceID)
            .withProperties(iconImage(locationMarkerID)),originLayerID)
    }


    private fun initLocationIcons(style: Style){
        style.addImage(locationMarkerID,
            BitmapUtils.getBitmapFromDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.mapbox_marker_icon_default
                )
            )!!
        )
    }

    //---------------------- mapbox lifecycle functions -----------------------
    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }


    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }
}