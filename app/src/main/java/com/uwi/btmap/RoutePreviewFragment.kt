package com.uwi.btmap

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.LineString
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
import com.mapbox.navigation.base.internal.extensions.applyDefaultParams
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.directions.session.RoutesRequestCallback
import com.uwi.btmap.BLL.CommuteViewModel

private const val TAG = "MapboxPreviewFragment"

class RoutePreviewFragment : Fragment(R.layout.fragment_route_preview), 
    OnMapReadyCallback{

    private val routeSourceID = "ROUTE _SOURCE_ID"
    private val originSourceID = "ORIGIN_SOURCE_ID"
    private val destinationSourceID = "DESTINATION_SOURCE_ID"

    private val routeLayerID = "ROUTE_LAYER_ID"
    private val originLayerID = "ORIGIN_LAYER_ID"
    private val destinationLayerID = "DESTINATION_LAYER_ID"

    private val locationMarkerID = "LOCATION_MARKER_ID"

    private lateinit var mapView: MapView
    private lateinit var mapboxMap: MapboxMap
    private lateinit var mapboxNavigation: MapboxNavigation

    private lateinit var viewModel: CommuteViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(CommuteViewModel::class.java)
        initMapView(view,savedInstanceState)
        initMapNavigation()
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        mapboxMap.setStyle(Style.MAPBOX_STREETS){ style ->
            this.mapboxMap = mapboxMap

            initLocationIcons(style)
            initMapLayers(style)

            mapboxMap.addOnMapLongClickListener{
                onMapClick(it)
            }

            centerMapCamera(mapboxMap)
        }

        //add observer for origin and destination
        viewModel.origin().observe(requireActivity(), Observer {
            //add/update source
            if (it != null){
                val source = mapboxMap.style?.getSourceAs<GeoJsonSource>(originSourceID)
                source?.setGeoJson(it)
            }
            //reset selection mode
            viewModel.locationSelectionMode = 0
        })

        viewModel.destination().observe(requireActivity(), Observer {
            //add/update source
            if (it != null){
                val source = mapboxMap.style?.getSourceAs<GeoJsonSource>(destinationSourceID)
                source?.setGeoJson(it)
            }
            //reset selection mode
            viewModel.locationSelectionMode = 0
        })

        viewModel.routePreview().observe(requireActivity(), Observer {
            if (it != null){
                //draw route to map
               val style =  mapboxMap.style

                val routeSource = style?.getSourceAs<GeoJsonSource>(routeSourceID)
                val routeLineString = LineString.fromPolyline(
                    it.geometry()!!,6)

                routeSource?.setGeoJson(routeLineString)
            }
        })
    }
    
    private fun centerMapCamera(mapboxMap: MapboxMap){
        //bim coordinates: 13.1939° N, 59.5432° W
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
            //if both points set get route
            //update viewModel routePreview
            if (viewModel.origin.value != null && viewModel.destination.value != null) {
                getRoute(viewModel.origin.value!!, viewModel.destination.value!!)
            }
        }
        if(viewModel.locationSelectionMode == 2){
            Log.d(TAG, "onMapClick: Add destination location: $point")
            viewModel.destination.value = Point.fromLngLat(point.longitude,point.latitude)
            //if both points set get route
            //update viewModel routePreview
            if (viewModel.origin.value != null && viewModel.destination.value != null) {
                getRoute(viewModel.origin.value!!, viewModel.destination.value!!)
            }
        }

        return true
    }

    //mapbox object setup functions
    private fun initMapView(view: View, savedInstanceState: Bundle?){
        this.mapView = view.findViewById(R.id.route_preview_map_view)
        this.mapView.onCreate(savedInstanceState)
        this.mapView.getMapAsync(this)
    }

    private fun initMapNavigation(){
        val mapboxNavigationOptions = MapboxNavigation
            .defaultNavigationOptionsBuilder(requireContext(), getString(R.string.mapbox_access_token))
            .build()
        this.mapboxNavigation = MapboxNavigation(mapboxNavigationOptions)
    }

    //route functions
    private fun getRoute(origin:Point,destination:Point){
        val routeOptions = RouteOptions.builder()
            .applyDefaultParams()
            .accessToken(getString(R.string.mapbox_access_token))
            .coordinates(listOf(origin,destination))
            .alternatives(false)
            .profile(DirectionsCriteria.PROFILE_DRIVING)
            .voiceInstructions(false)
            .steps(false)
            .build()

        mapboxNavigation.requestRoutes(routeOptions,routesReqCallback)
    }

    private val routesReqCallback = object: RoutesRequestCallback{
        override fun onRoutesReady(routes: List<DirectionsRoute>) {
            if (routes.isNotEmpty()){
                //save to view model
                viewModel.routePreview.value = routes[0]

            }else{
                Log.d(TAG, "onRoutesReady: No routes found.")
            }
        }

        override fun onRoutesRequestCanceled(routeOptions: RouteOptions) {
            Log.d(TAG, "onRoutesRequestCanceled: Route request cancelled.")
        }

        override fun onRoutesRequestFailure(throwable: Throwable, routeOptions: RouteOptions) {
            Log.d(TAG, "onRoutesRequestFailure: Route request failed.")
        }

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
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }


    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }
}