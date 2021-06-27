package com.uwi.btmap.views.fragments.passengerNav

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
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
import com.mapbox.navigation.core.directions.session.RoutesRequestCallback
import com.uwi.btmap.R
import com.uwi.btmap.viewmodels.PreviewCommuteViewModel

private const val TAG = "PassengerNavMapFragment"

class PassengerNavMapFragment : Fragment(R.layout.fragment_passenger_nav_map),
    OnMapReadyCallback, PermissionsListener {

    private lateinit var viewModel:PreviewCommuteViewModel
    private var permissionsManager: PermissionsManager = PermissionsManager(this)

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

    private lateinit var mapView: MapView
    private lateinit var mapboxMap: MapboxMap
    private lateinit var mapboxNavigation: MapboxNavigation


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(PreviewCommuteViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initMapView(view,savedInstanceState)
        initMapNavigation()
    }
    override fun onMapReady(mapboxMap: MapboxMap) {
        mapboxMap.setStyle(Style.MAPBOX_STREETS){ style ->
            this.mapboxMap = mapboxMap

            initLocationIcons(style)
            initRouteLayers(style)
            initIconLayers(style)

            enableLocationComponent(style)

            val commute = viewModel.commute.value
            val lastLocation = mapboxMap?.locationComponent?.lastKnownLocation
            val origin = lastLocation?.let { Point.fromLngLat(it.longitude, it.latitude) }

            if(commute != null){
                val originSource = style?.getSourceAs<GeoJsonSource>(originSourceID)
                originSource?.setGeoJson(origin)
                val destinationSource = style?.getSourceAs<GeoJsonSource>(destinationSourceID)
                destinationSource?.setGeoJson(commute.getDestinationPoint())

                if (commute.isPaired && commute.getPickupPoint() != null && commute.getDropoffPoint() != null){
                    val pickupSource = style?.getSourceAs<GeoJsonSource>(pickupPointSourceID)
                    pickupSource?.setGeoJson(commute.getPickupPoint())
                    val dropoffSource = style?.getSourceAs<GeoJsonSource>(dropoffPointSourceID)
                    dropoffSource?.setGeoJson(commute.getDropoffPoint())
                }

//                updateRoutes(style)
            }

            //display routes
            val firstLeg = viewModel.firstLegDirectionsRoute.value
            if (firstLeg != null){
                val firstLegSource = style?.getSourceAs<GeoJsonSource>(pickupRouteSourceID)
                val routeLineString = LineString.fromPolyline(firstLeg.geometry()!!,6)
                firstLegSource?.setGeoJson(routeLineString)
            }
            val drivingLeg = viewModel.drivingDirectionsRoute.value
            if (drivingLeg != null){
                val drivingLegSource = style?.getSourceAs<GeoJsonSource>(drivingRouteSourceID)
                val routeLineString = LineString.fromPolyline(drivingLeg.geometry()!!,6)
                drivingLegSource?.setGeoJson(routeLineString)
            }
            val lastLeg = viewModel.lastLegDirectionsRoute.value
            if (lastLeg != null){
                val lastLegSource = style?.getSourceAs<GeoJsonSource>(dropoffRouteSourceID)
                val routeLineString = LineString.fromPolyline(lastLeg.geometry()!!,6)
                lastLegSource?.setGeoJson(routeLineString)
            }
        }
    }


    private fun updateRoutes(style:Style){
        viewModel.firstLegDirectionsRoute().observe(requireActivity(),Observer{
            val firstLeg = viewModel.firstLegDirectionsRoute.value
            if (firstLeg != null){
                val firstLegSource = style?.getSourceAs<GeoJsonSource>(pickupRouteSourceID)
                val routeLineString = LineString.fromPolyline(firstLeg.geometry()!!,6)
                firstLegSource?.setGeoJson(routeLineString)
            }
        })
        viewModel.drivingDirectionsRoute().observe(requireActivity(),Observer{
            val drivingLeg = viewModel.drivingDirectionsRoute.value
            if (drivingLeg != null){
                val drivingLegSource = style?.getSourceAs<GeoJsonSource>(drivingRouteSourceID)
                val routeLineString = LineString.fromPolyline(drivingLeg.geometry()!!,6)
                drivingLegSource?.setGeoJson(routeLineString)
            }
        })
        viewModel.lastLegDirectionsRoute().observe(requireActivity(),Observer{
            val lastLeg = viewModel.lastLegDirectionsRoute.value
            if (lastLeg != null){
                val lastLegSource = style?.getSourceAs<GeoJsonSource>(dropoffRouteSourceID)
                val routeLineString = LineString.fromPolyline(lastLeg.geometry()!!,6)
                lastLegSource?.setGeoJson(routeLineString)
            }
        })
    }

    //mapbox object setup functions
    private fun initMapView(view: View, savedInstanceState: Bundle?){
        this.mapView = view.findViewById(R.id.passenger_nav_map_view)
        this.mapView.onCreate(savedInstanceState)
        this.mapView.getMapAsync(this)
    }

    private fun initMapNavigation(){
        val mapboxNavigationOptions = MapboxNavigation
            .defaultNavigationOptionsBuilder(requireContext(), getString(R.string.mapbox_access_token))
            .build()
        this.mapboxNavigation = MapboxNavigation(mapboxNavigationOptions)
    }


    //functions to setup sources
    private fun initRouteLayers(style: Style){
        setupRouteLayer(style,drivingRouteSourceID,drivingRouteLayerID,"mapbox-location-shadow-layer","#2E4FC9")
        setupRouteLayer(style,pickupRouteSourceID,pickupRouteLayerID,drivingRouteLayerID,"#25BE7A")
        setupRouteLayer(style,dropoffRouteSourceID,dropoffRouteLayerID,pickupRouteLayerID,"#25BE7A")
    }

    private fun initIconLayers(style: Style){
        setupIconLayer(style,pickupPointSourceID,pickupPointLayerID,drivingRouteLayerID,locationMarkerID)
        setupIconLayer(style,dropoffPointSourceID,dropoffPointLayerID,pickupPointLayerID,locationMarkerID)
        setupIconLayer(style,originSourceID,originLayerID,dropoffPointLayerID,locationMarkerID)
        setupIconLayer(style,destinationSourceID,destinationLayerID,originLayerID,locationMarkerID)
    }

    private fun setupRouteLayer(style: Style, routeSourceID:String, routeLayerID:String, belowLayer:String, color:String){
        //route layer
        style.addSource(
            GeoJsonSource(
                routeSourceID,
                GeoJsonOptions().withLineMetrics(true)
            )
        )
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

    private fun setupIconLayer(style: Style, pointSourceID:String, pointLayerID:String, aboveLayer:String, iconID:String){
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

    @SuppressLint("MissingPermission")
    private fun enableLocationComponent(loadedMapStyle: Style) {
        mapboxMap.getStyle {
            if (PermissionsManager.areLocationPermissionsGranted(requireContext())) {
                val customLocationComponentOptions = LocationComponentOptions.builder(requireContext())
                    .trackingGesturesManagement(true)
                    .accuracyColor(ContextCompat.getColor(requireContext(), R.color.mapbox_blue))
                    .build()

                val locationComponentActivationOptions =
                    LocationComponentActivationOptions.builder(requireContext(), loadedMapStyle)
                        .locationComponentOptions(customLocationComponentOptions)
                        .build()

                mapboxMap.locationComponent.apply {
                    activateLocationComponent(locationComponentActivationOptions)
                    isLocationComponentEnabled = true
                    cameraMode = CameraMode.TRACKING
                    renderMode = RenderMode.COMPASS

                    val lat = mapboxMap.locationComponent.lastKnownLocation?.latitude
                    val lng = mapboxMap.locationComponent.lastKnownLocation?.longitude

                    val position = CameraPosition.Builder()
                        .zoom(12.0)
                        .tilt(0.0)
                        .target(LatLng(lat!!, lng!!))
                        .build()

                    mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position))
                }
            } else {
                permissionsManager = PermissionsManager(this)
                permissionsManager.requestLocationPermissions(requireActivity())
            }
        }

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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        Toast.makeText(requireContext(), "This app requires location services in order to provide navigation to requested destination.", Toast.LENGTH_LONG).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            enableLocationComponent(mapboxMap.style!!)
        } else {
            Toast.makeText(requireContext(), "permissions not granted", Toast.LENGTH_LONG).show()
        }
    }
}
