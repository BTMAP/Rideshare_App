package com.uwi.btmap.views.fragments.commuteList

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.LineString
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
import com.mapbox.navigation.core.directions.session.RoutesRequestCallback
import com.uwi.btmap.R
import com.uwi.btmap.models.PassengerCommuteEstimate
import com.uwi.btmap.viewmodels.MainViewModel
import com.uwi.btmap.viewmodels.SelectPairViewModel
import com.uwi.btmap.views.activities.RegisterCommuteActivity
import com.uwi.btmap.views.fragments.selectPair.ListCommutePairFragment
import kotlinx.android.synthetic.main.commute_pair_list_item.*
import java.text.SimpleDateFormat
import java.util.*


const val TAG = "RoutePreview"

class PreviewCommutePairFragment : Fragment(R.layout.fragment_preview_commute_pair),
    OnMapReadyCallback{

    private lateinit var pairButton: Button

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

        val currentIndex = arguments?.getInt("CommuteIndex")
        viewModel.currentCommuteIndex.value = currentIndex

        viewModel.currentFragment.value = 1

        val currentCommute = viewModel.commuteOptions.value?.pairs?.get(currentIndex!!)

        viewModel.getPairEstimates(currentCommute!!.commuteId,
            viewModel.origin.value!!,
            viewModel.destination.value!!,
            currentCommute.pickupPoints[0],
            currentCommute.dropoffPoints[0]
        )
        Mapbox.getInstance(requireContext(),getString(R.string.mapbox_access_token))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //setup mapbox
        initMapView(view,savedInstanceState)
        initMapNavigation()

        getPassengerCommuteEstimate()

        pairButton = view.findViewById(R.id.pair_button)

        pairButton.setOnClickListener {
            val currentIndex = viewModel.currentCommuteIndex.value
            val currentCommute = viewModel.commuteOptions.value?.pairs?.get(currentIndex!!)
            viewModel.pair(currentCommute!!.commuteId,
                viewModel.origin.value!!,
                viewModel.destination.value!!,
                currentCommute.pickupPoints[0],
                currentCommute.dropoffPoints[0]
            )
        }
    }

    private fun getPassengerCommuteEstimate(){
        val eta = view?.findViewById<TextView>(R.id.preview_destETA)
        val time = view?.findViewById<TextView>(R.id.preview_destTime)
        val mDate = view?.findViewById<TextView>(R.id.preview_date)
        val walkingDistance = view?.findViewById<TextView>(R.id.preview_walkingDistance)
        val walkingDuration = view?.findViewById<TextView>(R.id.preview_walkingTime)

        viewModel.getPairEstimatesSuccess().observe(requireActivity(), androidx.lifecycle.Observer {
            if (it) {
                val estimates = viewModel.commuteEstimates.value
                Log.d(TAG, "Estimates: $estimates")

                mDate?.text = estimates?.getDate()
                eta?.text = estimates?.getETA()
                time?.text = estimates?.getStartTime()
                walkingDistance?.text = estimates?.getDistanceInKm().toString()
                walkingDuration?.text = estimates?.getDurationInMinutes().toString()

            }else{
                Toast.makeText(requireContext(), "Failed", Toast.LENGTH_SHORT).show()
            }
        })
    }



    //----------------------- mapbox functions -------------------------

    override fun onMapReady(mapboxMap: MapboxMap) {
        mapboxMap.setStyle(Style.MAPBOX_STREETS) { style ->
            this.mapboxMap = mapboxMap
            centerMapCamera(mapboxMap)
            initLocationIcons(style)
            initRouteLayers(style)
            initIconLayers(style)

            //display points
            val originSource = style.getSourceAs<GeoJsonSource>(originSourceID)
            val destinationSource = style.getSourceAs<GeoJsonSource>(destinationSourceID)
            val pickupSource = style.getSourceAs<GeoJsonSource>(pickupPointSourceID)
            val dropoffSource = style.getSourceAs<GeoJsonSource>(dropoffPointSourceID)

            originSource?.setGeoJson(viewModel.origin.value)
            val geoJson = destinationSource?.setGeoJson(viewModel.destination.value)


            val currentCommute = viewModel.commuteOptions.value?.pairs?.get(viewModel.currentCommuteIndex.value!!)
            val pickup = currentCommute?.pickupPoints?.get(0)
            val dropoff = currentCommute?.dropoffPoints?.get(0)

            val pickupPoint = Point.fromLngLat(pickup!!.lng?.toDouble(), pickup!!.lat?.toDouble())
            val dropoffPoint = Point.fromLngLat(dropoff!!.lng?.toDouble(), dropoff!!.lat?.toDouble())

            pickupSource?.setGeoJson(pickupPoint)
            dropoffSource?.setGeoJson(dropoffPoint)

            //get routes
            getWalkingRoute(viewModel.origin.value!!,pickupPoint,firstLegRouteCallback)
            getWalkingRoute(dropoffPoint,viewModel.destination.value!!,lastLegRouteCallback)
            getDrivingRoute(pickupPoint,dropoffPoint)
        }
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
        setupRouteLayer(style,pickupRouteSourceID,pickupRouteLayerID,drivingRouteLayerID,"#25BE7A")
        setupRouteLayer(style,dropoffRouteSourceID,dropoffRouteLayerID,pickupRouteLayerID,"#25BE7A")
    }

    private fun initIconLayers(style:Style){
        setupIconLayer(style,pickupPointSourceID,pickupPointLayerID,drivingRouteLayerID,locationMarkerID)
        setupIconLayer(style,dropoffPointSourceID,dropoffPointLayerID,pickupPointLayerID,locationMarkerID)
        setupIconLayer(style,originSourceID,originLayerID,dropoffPointLayerID,locationMarkerID)
        setupIconLayer(style,destinationSourceID,destinationLayerID,originLayerID,locationMarkerID)
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

    private val firstLegRouteCallback = object: RoutesRequestCallback{
        override fun onRoutesReady(routes: List<DirectionsRoute>) {
            if (routes.isNotEmpty()){
                val routeLineString = LineString.fromPolyline(
                    routes[0].geometry()!!,6)
                viewModel.firstLegRoute.value=routes[0]
                mapboxMap.getStyle {
                    val routeSource = it.getSourceAs<GeoJsonSource>(pickupRouteSourceID)
                    routeSource?.setGeoJson(routeLineString)
                }
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

    private val lastLegRouteCallback = object: RoutesRequestCallback{
        override fun onRoutesReady(routes: List<DirectionsRoute>) {
            if (routes.isNotEmpty()){
                val routeLineString = LineString.fromPolyline(
                    routes[0].geometry()!!,6)
                viewModel.lastLegRoute.value=routes[0]
                mapboxMap.getStyle {
                    val routeSource = it.getSourceAs<GeoJsonSource>(dropoffRouteSourceID)
                    routeSource?.setGeoJson(routeLineString)
                }
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

    private val drivingLegRouteCallback = object: RoutesRequestCallback{
        override fun onRoutesReady(routes: List<DirectionsRoute>) {
            if (routes.isNotEmpty()){
                val routeLineString = LineString.fromPolyline(
                    routes[0].geometry()!!,6)
                viewModel.drivingLegRoute.value=routes[0]
                mapboxMap.getStyle {
                    val routeSource = it.getSourceAs<GeoJsonSource>(drivingRouteSourceID)
                    routeSource?.setGeoJson(routeLineString)
                }
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

    //route functions
    private fun getWalkingRoute(origin: Point, destination: Point,callback:RoutesRequestCallback){
        val routeOptions = RouteOptions.builder()
            .applyDefaultParams()
            .accessToken(getString(R.string.mapbox_access_token))
            .coordinates(listOf(origin,destination))
            .alternatives(false)
            .profile(DirectionsCriteria.PROFILE_WALKING)
            .voiceInstructions(false)
            .steps(false)
            .build()

        mapboxNavigation.requestRoutes(routeOptions,callback)
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

        mapboxNavigation.requestRoutes(routeOptions,drivingLegRouteCallback)
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