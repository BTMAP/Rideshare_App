package com.uwi.btmap

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineResult
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationUpdate
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.ui.camera.NavigationCamera
import java.lang.ref.WeakReference


class NavActivity :
    AppCompatActivity(),
    OnMapReadyCallback{

    private val TAG = "NAV_ACTIVITY"
    private lateinit var accessToken: String

    private lateinit var mapView: MapView

    private lateinit var mapboxMap: MapboxMap

    private lateinit var mapboxNavigation: MapboxNavigation

    private lateinit var mapCamera: NavigationCamera

    private lateinit var commute: Commute

    /*--------------------------------------------------------------------------------------------*/
    /*-------------------------- Location and route progress callbacks ---------------------------*/

    private val locationObserver = object : LocationObserver {
        override fun onEnhancedLocationChanged(
            enhancedLocation: Location,
            keyPoints: List<Location>
        ) {
            Log.d(TAG, "onEnhancedLocationChanged: Called")
            if (keyPoints.isEmpty()) {
                updateLocation(enhancedLocation)
            } else {
                updateLocation(keyPoints)
            }

            //update camera position
        }

        override fun onRawLocationChanged(rawLocation: Location) {
            //not handled
        }

    }

    private fun updateLocation(location: Location) {
        Log.d(TAG, "updateLocation: Single location Called: $location")
        updateLocation(listOf(location))
    }

    internal fun updateLocation(locations: List<Location>) {
        Log.d(TAG, "updateLocation: List of locations Called: $locations")

        //location update is the recommended method to update the location component
        //but causes jerky movement

//        var locationUpdateBuilder = LocationUpdate.Builder()
//        locationUpdateBuilder
//            .location(locations[0])
//            .animationDuration(300)
//        if(locations.count()>1){
//            locationUpdateBuilder.intermediatePoints(locations.subList(1,locations.count()-1))
//        }
//        var locationUpdate = locationUpdateBuilder.build()
//        mapboxMap.locationComponent.forceLocationUpdate(locationUpdate)
        mapboxMap.locationComponent.forceLocationUpdate(locations,false)
    }

    //TODO routeProgressObserver

    /*--------------------------------------------------------------------------------------------*/
    /*--------------------------- Maneuver instruction callback ----------------------------------*/

    //TODO maneuver instructions

    /*--------------------------------------------------------------------------------------------*/
    /*-------------------------------------- OnCreate --------------------------------------------*/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nav)

        //get commute
        //TODO notify user and return to previous screen if no commute is passed
        this.commute = intent.getSerializableExtra("commute")!! as Commute

        //set access token
        this.accessToken = getString(R.string.mapbox_access_token)

        //initialize mapView
        this.mapView = findViewById(R.id.nav_mapView)
        this.mapView?.onCreate(savedInstanceState)
        this.mapView?.getMapAsync(this)

    }

    //TODO address missing permissions checks
    @SuppressLint("MissingPermission")
    override fun onMapReady(mapboxMap: MapboxMap) {
        //initialize mapboxMap
        this.mapboxMap = mapboxMap
        mapboxMap.setStyle(Style.MAPBOX_STREETS) {

            initializeLocationComponent(mapboxMap, it)

            //initialize the mapboxNavigation
            val mapboxNavigationOptions = MapboxNavigation
                .defaultNavigationOptionsBuilder(this, accessToken)
                .build()
            this.mapboxNavigation = MapboxNavigation(mapboxNavigationOptions)
            Log.d(TAG, "onMapReady: register locationObserver")
            this.mapboxNavigation.registerLocationObserver(locationObserver)

            //TODO register route progress observer

            //Camera init
            mapCamera = NavigationCamera(mapboxMap)
            mapCamera.addProgressChangeListener(mapboxNavigation)
            //init camera zoom level
            mapboxMap.moveCamera(CameraUpdateFactory.zoomTo(25.0))

            //get last location with custom location engine callback
            val myLocationEngineCallback = com.uwi.btmap.LocationEngineCallback(this)
            mapboxNavigation.navigationOptions.locationEngine.getLastLocation(myLocationEngineCallback)

            //add route to map


            //add route to navigation object
            this.mapboxNavigation.setRoutes(listOf(commute.getDriverRoute()))

            //start trip
            //camera start route
            mapCamera.updateCameraTrackingMode(NavigationCamera.NAVIGATION_TRACKING_MODE_GPS)
            mapCamera.start(commute.getDriverRoute())
            this.mapboxNavigation.startTripSession()

        }

    }

    @SuppressLint("MissingPermission")
    private fun initializeLocationComponent(
        mapboxMap: MapboxMap,
        style: Style
    ) {
        val activationOptions =
            LocationComponentActivationOptions.builder(this, style)
                .useDefaultLocationEngine(false)
                .build()

        mapboxMap.locationComponent.apply {
            this.activateLocationComponent(activationOptions)
            isLocationComponentEnabled = true
            cameraMode = CameraMode.TRACKING
            renderMode = RenderMode.GPS
        }
    }

    /*--------------------------------------------------------------------------------------------*/
    /*-------------------------------------- Life Cycle ------------------------------------------*/

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
        if(this::mapboxNavigation.isInitialized){
            Log.d(TAG, "onStart: register locationObserver")
            mapboxNavigation.registerLocationObserver(locationObserver)
        }
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
        mapboxNavigation.unregisterLocationObserver(locationObserver)
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
        mapboxNavigation.onDestroy()
        mapView?.onDestroy()
    }

}


/*------------------------------------------------------------------------------------------------*/
/*------------------------------------------------------------------------------------------------*/

class LocationEngineCallback(activity: NavActivity) : LocationEngineCallback<LocationEngineResult> {
    private var activityRef: WeakReference<NavActivity>? = null
    private val TAG = "LocationEngineCallback"

    init {
        this.activityRef = WeakReference(activity)
    }

    override fun onSuccess(result: LocationEngineResult?) {
        if (result != null) {
            //initialize location puck position
            activityRef?.get()?.updateLocation(result.locations)
            Log.d(TAG, "onSuccess: $result.locations")
            //initialize camera position

        }else{
            Log.e(TAG, "onSuccess: Failed to update location (result == null)")
        }
    }

    override fun onFailure(exception: Exception) {
        Log.e(TAG, "onFailure: Failed to update location", exception)
    }

}
