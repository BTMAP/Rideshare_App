package com.uwi.btmap.views.fragments.commuteList

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
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
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.utils.BitmapUtils
import com.mapbox.navigation.base.internal.extensions.applyDefaultParams
import com.mapbox.navigation.core.MapboxNavigation
import com.uwi.btmap.R
import com.uwi.btmap.viewmodels.SelectPairViewModel

class PreviewCommutePairFragment : Fragment(R.layout.fragment_preview_commute_pair),
    OnMapReadyCallback{

//----------------------------------- source constants ---------------------------------------------

    private val drivingRouteSourceID = "DRIVING_ROUTE_SOURCE_ID"
    private val pickupRouteSourceID = "PICKUP_ROUTE_SOURCE_ID"
    private val dropoffRouteSourceID = "DROPOFF_ROUTE_SOURCE_ID"
    private val originSourceID = "ORIGIN_SOURCE_ID"
    private val destinationSourceID = "DESTINATION_SOURCE_ID"
    private val pickupPointSourceID = "PICKUP_SOURCE_ID"
    private val dropoffPointSourceID = "DROPOFF_SOURCE_ID"


    private val drivingRouteLayerID = "DRIVING_ROUTE_LAYER_ID"
    private val pickupRouteLayerID = "PICKUP_ROUTE_LAYER_ID"
    private val dropoffRouteLayerID = "DROPOFF_ROUTE_LAYER_ID"
    private val originLayerID = "ORIGIN_LAYER_ID"
    private val destinationLayerID = "DESTINATION_LAYER_ID"
    private val pickupPointLayerID = "PICKUP_POINT_LAYER_ID"
    private val dropoffPointLayerID = "DROPOFF_POINT_LAYER_ID"

    private val locationMarkerID = "LOCATION_MARKER_ID"

    //------------------------------ mapbox controllers --------------------------------------------

    private lateinit var mapView: MapView
    private lateinit var mapboxMap: MapboxMap
    private lateinit var mapboxNavigation: MapboxNavigation

    private lateinit var viewModel: SelectPairViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(SelectPairViewModel::class.java)
        Mapbox.getInstance(requireContext(),getString(R.string.mapbox_access_token))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //setup mapbox
        initMapView(view,savedInstanceState)
        initMapNavigation()
    }

    //----------------------- mapbox functions -------------------------

    override fun onMapReady(mapboxMap: MapboxMap) {
        mapboxMap.setStyle(Style.MAPBOX_STREETS) { style ->
            this.mapboxMap = mapboxMap
            centerMapCamera(mapboxMap)
            initLocationIcons(style)
            initRouteLayers(style)
            initIconLayers(style)
        }
        //display points
        //get routes
        //display routes
        //calculate passenger travel info
        //display travel info
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

    private fun initRouteLayers(style: Style){
        setupRouteLayer(style,drivingRouteSourceID,drivingRouteLayerID,"mapbox-location-shadow-layer","#2E4FC9")
        setupRouteLayer(style,pickupRouteSourceID,pickupRouteLayerID,drivingRouteLayerID,"#2E4FC9")
        setupRouteLayer(style,dropoffRouteSourceID,dropoffRouteLayerID,pickupRouteLayerID,"#2E4FC9")
    }

    private fun initIconLayers(style:Style){
        setupIconLayer(style,originSourceID,originLayerID,drivingRouteLayerID,locationMarkerID)
        setupIconLayer(style,destinationSourceID,destinationLayerID,originLayerID,locationMarkerID)
        setupIconLayer(style,pickupPointSourceID,pickupPointLayerID,destinationLayerID,locationMarkerID)
        setupIconLayer(style,dropoffPointSourceID,dropoffPointLayerID,pickupPointLayerID,locationMarkerID)

    }

    private fun setupRouteLayer(style:Style,routeSourceID:String,routeLayerID:String,belowLayer:String,color:String){
        //route layer
        style.addSource(
            GeoJsonSource(
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
                    PropertyFactory.lineColor(color)
                ),
            belowLayer
        )
    }

    private fun setupIconLayer(style:Style,pointSourceID:String,pointLayerID:String,aboveLayer:String,iconID:String){
        //origin layer
        style.addSource(GeoJsonSource(pointSourceID))
        style.addLayerAbove(
            SymbolLayer(pointLayerID,pointSourceID)
                .withProperties(PropertyFactory.iconImage(locationMarkerID)),aboveLayer)
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

    //route functions
    private fun getWalkingRoute(origin: Point, destination: Point){
        val routeOptions = RouteOptions.builder()
            .applyDefaultParams()
            .accessToken(getString(R.string.mapbox_access_token))
            .coordinates(listOf(origin,destination))
            .alternatives(false)
            .profile(DirectionsCriteria.PROFILE_WALKING)
            .voiceInstructions(false)
            .steps(false)
            .build()

        //mapboxNavigation.requestRoutes(routeOptions,routesReqCallback)
    }
    private fun getDrivingRoute(origin: Point, destination: Point){
        val routeOptions = RouteOptions.builder()
            .applyDefaultParams()
            .accessToken(getString(R.string.mapbox_access_token))
            .coordinates(listOf(origin,destination))
            .alternatives(false)
            .profile(DirectionsCriteria.PROFILE_DRIVING)
            .voiceInstructions(false)
            .steps(false)
            .build()

        //mapboxNavigation.requestRoutes(routeOptions,routesReqCallback)
    }

    //---------------------- lifecycle functions -----------------------
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