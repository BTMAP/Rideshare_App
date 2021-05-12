package com.uwi.btmap

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color.parseColor
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdate
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
import com.mapbox.mapboxsdk.style.expressions.Expression.*
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.utils.BitmapUtils
import com.mapbox.navigation.base.internal.extensions.applyDefaultParams
import com.mapbox.navigation.base.internal.extensions.coordinates
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.directions.session.RoutesRequestCallback
import com.mapbox.navigation.core.reroute.RerouteController
import kotlin.math.log

class MainActivity :
    AppCompatActivity(),
    OnMapReadyCallback,
    PermissionsListener,
    MapboxMap.OnMapLongClickListener,
    AdapterView.OnItemSelectedListener {

    private val TAG = "Main Activity"

    private var mapView: MapView? = null
    private lateinit var mapboxMap: MapboxMap
    private var mapboxNavigation: MapboxNavigation? = null

    private var permissionsManager: PermissionsManager = PermissionsManager(this)

    //Locations
    private var commute: Commute = Commute()
    private var currentSelectedLocation = 0

    //Widgets
    private lateinit var routeButton: Button
    private lateinit var locationSpinner : Spinner

    private lateinit var locationBottomSheet: FrameLayout
    private lateinit var submitButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))

        setContentView(R.layout.activity_main)

        setupLocationSpinner()
        setupRouteButton()

        setupMapView(savedInstanceState)
        setupNavigationObject()

        locationBottomSheet = findViewById(R.id.location_bottom_sheet)
        BottomSheetBehavior.from(locationBottomSheet).apply{
            peekHeight = 200
        }

        submitButton = findViewById(R.id.submit_button)

        submitButton.setOnClickListener{
            var navActivityIntent = Intent(this, NavActivity::class.java)
                    .putExtra("commute", commute)
            startActivity(navActivityIntent)
        }
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        mapboxMap.setStyle(Style.MAPBOX_STREETS) {
            this.mapboxMap = mapboxMap
//            show user location
            enableLocationComponent(it)

            setupMapIcons(it)

            setupRouteLayer(it)
            setupLocationMarkerLayers(it)
        }
        mapboxMap.addOnMapLongClickListener(this)
    }

    private fun setupMapIcons(style: Style){
        style.addImage("ICON_ID",
                BitmapUtils.getBitmapFromDrawable(
                        ContextCompat.getDrawable(
                                this,
                                R.drawable.mapbox_marker_icon_default
                        )
                )!!
        )
    }

    private fun setupRouteLayer(style: Style){
        style.addSource(GeoJsonSource(
                "ROUTE_LINE_SOURCE_ID",
                GeoJsonOptions().withLineMetrics(true)
        ))
        style.addLayerBelow(
                LineLayer("ROUTE_LAYER_ID", "ROUTE_LINE_SOURCE_ID")
                        .withProperties(
                                lineCap(Property.LINE_CAP_ROUND),
                                lineJoin(Property.LINE_JOIN_ROUND),
                                lineWidth(6f),
                                lineOpacity(1f),
                                lineColor("#2E4FC9")
                        ),
                "mapbox-location-shadow-layer"
        )

        style.addSource(GeoJsonSource(
                "PASSENGER_ROUTE_SOURCE_ID",
                GeoJsonOptions().withLineMetrics(true)
        ))
        style.addLayerBelow(
                LineLayer("PASSENGER_ROUTE_LAYER_ID", "PASSENGER_ROUTE_SOURCE_ID")
                        .withProperties(
                                lineCap(Property.LINE_CAP_ROUND),
                                lineJoin(Property.LINE_JOIN_ROUND),
                                lineWidth(6f),
                                lineOpacity(1f),
                                lineColor("#5E8F5E")
                        ),
                "ROUTE_LAYER_ID"
        )
    }

    private fun setupLocationMarkerLayers(style: Style){
        setupIconLayerAbove(style,"ORIGIN_SOURCE","ORIGIN_LAYER","ROUTE_LAYER_ID")
        setupIconLayerBelow(style,"DESTINATION_SOURCE","DESTINATION_LAYER","ORIGIN_LAYER")
        setupIconLayerBelow(style,"PASSENGER_SOURCE","PASSENGER_LAYER","DESTINATION_LAYER")
        setupIconLayerBelow(style,"DROP_OFF_SOURCE","DROP_OFF_LAYER","PASSENGER_LAYER")
        setupIconLayerBelow(style,"PICKUP_POINT","PICKUP_POINT_LAYER","PASSENGER_LAYER")
    }

    private fun setupIconLayerAbove(style: Style, sourceId : String, layerId : String, aboveLayer : String){
        style.addSource(GeoJsonSource(sourceId))
        style.addLayerAbove(SymbolLayer(layerId,sourceId)
                .withProperties(iconImage("ICON_ID")),aboveLayer)
    }

    private fun setupIconLayerBelow(style: Style, sourceId : String, layerId : String, aboveLayer : String){
        style.addSource(GeoJsonSource(sourceId))
        style.addLayerBelow(SymbolLayer(layerId,sourceId)
                .withProperties(iconImage("ICON_ID")),aboveLayer)
    }

    @SuppressLint("MissingPermission")
    private fun enableLocationComponent(loadedMapStyle: Style){
        mapboxMap.getStyle {
            if(PermissionsManager.areLocationPermissionsGranted(this)){
                val customLocationComponentOptions = LocationComponentOptions.builder(this)
                        .trackingGesturesManagement(true)
                        .accuracyColor(ContextCompat.getColor(this,R.color.mapbox_blue))
                        .build()

                val locationComponentActivationOptions =
                        LocationComponentActivationOptions.builder(this, loadedMapStyle)
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
                            .target(LatLng(lat!!,lng!!))
                            .build()

                    mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position))
                }
            }else{
                permissionsManager = PermissionsManager(this)
                permissionsManager.requestLocationPermissions(this)
            }
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        Toast.makeText(this, "We need location services >:/",Toast.LENGTH_LONG).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if(granted){
            enableLocationComponent(mapboxMap.style!!)
        }else{
            Toast.makeText(this, "permissions not granted",Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onMapLongClick(point: LatLng): Boolean {
        val selectedPoint: Point = Point.fromLngLat(point.longitude,point.latitude)
        var sourceId = ""

        if(this.currentSelectedLocation == 0){
            Toast.makeText(this,"Set origin location",Toast.LENGTH_SHORT).show()
            commute.setOrigin(selectedPoint)
            sourceId = "ORIGIN_SOURCE"
        }
        if(this.currentSelectedLocation == 1){
            Toast.makeText(this,"Set destination location",Toast.LENGTH_SHORT).show()
            commute.setDestination(selectedPoint)
            sourceId = "DESTINATION_SOURCE"
        }
        if(this.currentSelectedLocation == 2){
            Toast.makeText(this,"Set passenger location",Toast.LENGTH_SHORT).show()
            commute.setPassenger(selectedPoint)
            sourceId = "PASSENGER_SOURCE"
        }
        if(this.currentSelectedLocation == 3){
            Toast.makeText(this,"Set dropoff location",Toast.LENGTH_SHORT).show()
            commute.setDropOff(selectedPoint)
            sourceId = "DROP_OFF_SOURCE"
        }


        mapboxMap?.getStyle {
            updateSource(it,sourceId,selectedPoint)
        }

        return true
    }

    private fun updateSource(style: Style, source_id : String, point: Point){
        var source = style.getSourceAs<GeoJsonSource>(source_id)
        source?.setGeoJson(point)
    }


    private fun getRoute(){
        if (commute.isValid()){
            Log.d(TAG, "getRoute: id valid")
            val routeOptions : RouteOptions = commute.pairedRouteOptions(getString(R.string.mapbox_access_token))
            mapboxNavigation?.requestRoutes(
                    routeOptions,
                    routesReqCallback
            )
        }
        Log.d(TAG, "getRoute: after isValid check")
    }

    private val routesReqCallback = object : RoutesRequestCallback{
        override fun onRoutesReady(routes: List<DirectionsRoute>) {
            if (routes.isNotEmpty()){
                commute.setDriverRoute(routes[0])
                mapboxMap?.getStyle {
                    val clickPointSource = it.getSourceAs<GeoJsonSource>("ROUTE_LINE_SOURCE_ID")
                    val routeLineString = LineString.fromPolyline(
                            routes[0].geometry()!!,
                            6
                    )
//                    add the returned route to the route line source
                    clickPointSource?.setGeoJson(routeLineString)
                }
            }else{
                Log.d(TAG, "onRoutesReady: No routes found")
            }
        }

        override fun onRoutesRequestCanceled(routeOptions: RouteOptions) {
            Log.d(TAG, "onRoutesRequestCanceled: routes request cancelled")
        }

        override fun onRoutesRequestFailure(throwable: Throwable, routeOptions: RouteOptions) {
            Log.e(TAG, "onRoutesRequestFailure: routes request failed: ", throwable)
        }

    }

    private fun getPassengerRoute(){
        //Request passenger directions
        val routeOptions : RouteOptions = commute.passengerRouteOptions(getString(R.string.mapbox_access_token))

        mapboxNavigation?.requestRoutes(
                routeOptions,
                passengerRoutesReqCallback
        )
    }

    private val passengerRoutesReqCallback = object : RoutesRequestCallback{
        override fun onRoutesReady(routes: List<DirectionsRoute>) {
            if (routes.isNotEmpty()){
                commute.setPassengerRoute(routes[0])
                mapboxMap?.getStyle {
                    val clickPointSource = it.getSourceAs<GeoJsonSource>("PASSENGER_ROUTE_SOURCE_ID")
                    val routeLineString = LineString.fromPolyline(
                            routes[0].geometry()!!,
                            6
                    )
//                    add the returned route to the route line source
                    clickPointSource?.setGeoJson(routeLineString)
                }
            }else{
                Log.d(TAG, "onRoutesReady: No routes found")
            }
        }

        override fun onRoutesRequestCanceled(routeOptions: RouteOptions) {
            Log.d(TAG, "onRoutesRequestCanceled: routes request cancelled")
        }

        override fun onRoutesRequestFailure(throwable: Throwable, routeOptions: RouteOptions) {
            Log.e(TAG, "onRoutesRequestFailure: routes request failed: ", throwable)
        }

    }

    //------------mapbox lifecycle functions------------------
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
    //----------mapbox lifecycle functions end----------------

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

    override fun onNothingSelected(p0: AdapterView<*>?) {
    }

    override fun onItemSelected(parent : AdapterView<*>?,
                                view : View, position: Int, id : Long){
//        set variable to represent what item is selected
        currentSelectedLocation = position
    }

    private fun setupMapView(savedInstanceState: Bundle?){
        this.mapView = findViewById(R.id.mapView)
        this.mapView?.onCreate(savedInstanceState)
        this.mapView?.getMapAsync(this)
    }

    private fun setupNavigationObject(){
        val mapboxNavigationOptions = MapboxNavigation
                .defaultNavigationOptionsBuilder(this, getString(R.string.mapbox_access_token))
                .build()

        this.mapboxNavigation = MapboxNavigation(mapboxNavigationOptions)
    }

    private fun setupRouteButton(){
        Log.d(TAG, "setupRouteButton: Called")
        this.routeButton = findViewById<Button>(R.id.route_button)
        this.routeButton.setOnClickListener{
            commute.generatePickupPoint()
            if (commute.isValid()) {
                Log.d(TAG, "setupRouteButton: commute valid")

                mapboxMap?.getStyle {
                    updateSource(it,"PICKUP_POINT", commute.getPickup())
                }

                getRoute()
                getPassengerRoute()
            }
        }
    }

    private fun setupLocationSpinner(){
        var locations = arrayOf<String>("driver origin", "driver destination", "passenger origin", "passenger destination")
        this.locationSpinner = findViewById<Spinner>(R.id.location_spinner)

        this.locationSpinner.onItemSelectedListener = this

        val adapter : ArrayAdapter<*> = ArrayAdapter<Any?>(this, android.R.layout.simple_spinner_item, locations)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        this.locationSpinner.adapter = adapter
    }
}