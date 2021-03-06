package com.uwi.btmap.views.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.mapbox.android.core.location.*
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.api.directions.v5.models.BannerInstructions
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.api.directions.v5.models.VoiceInstructions
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
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
import com.mapbox.navigation.base.trip.model.RouteLegProgress
import com.mapbox.navigation.base.trip.model.RouteProgress
import com.mapbox.navigation.base.trip.model.RouteProgressState
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.arrival.ArrivalObserver
import com.mapbox.navigation.core.trip.session.*
import com.mapbox.navigation.ui.camera.NavigationCamera
import com.mapbox.navigation.ui.camera.NavigationCamera.NAVIGATION_TRACKING_MODE_GPS
import com.mapbox.navigation.ui.instruction.InstructionView
import com.mapbox.navigation.ui.map.NavigationMapboxMap
import com.mapbox.navigation.ui.puck.PuckDrawableSupplier
import com.mapbox.navigation.ui.summary.SummaryBottomSheet
import com.uwi.btmap.MainActivity
import com.uwi.btmap.R
import com.uwi.btmap.utils.TTS
import com.uwi.btmap.models.DriverLiveLocation
import java.lang.ref.WeakReference

private const val TAG = "NAV_ACTIVITY"

@Suppress("DEPRECATION")
class NavActivity :
    AppCompatActivity(), PermissionsListener,
    OnMapReadyCallback {

    private lateinit var accessToken: String

    //mapbox views
    private lateinit var mapView: MapView
    private lateinit var instructionView: InstructionView
    private lateinit var summaryBottomSheet: SummaryBottomSheet

    private lateinit var recenterButton: Button
    private lateinit var muteButton: Button

    //mapbox controllers
    private lateinit var mapboxMap: MapboxMap

    private lateinit var mapboxNavigation: MapboxNavigation
    private lateinit var navigationMap: NavigationMapboxMap

    private lateinit var mapCamera: NavigationCamera

    //custom commute object
    private lateinit var commute: DirectionsRoute

    private lateinit var permissionsManager: PermissionsManager
    private lateinit var locationEngine: LocationEngine
    private lateinit var callback: LocationChangeListeningCallback

    private lateinit var database: DatabaseReference
    var mAuth: FirebaseAuth? = null


    /*--------------------------------------------------------------------------------------------*/
    /*-------------------------- Location and route progress observer ---------------------------*/
    @SuppressLint("LogNotTimber")
    private val locationObserver = object : LocationObserver {
        override fun onEnhancedLocationChanged(
            enhancedLocation: Location,
            keyPoints: List<Location>
        ) {
            Log.d(TAG, "onEnhancedLocationChanged: $keyPoints")
            if (keyPoints.isEmpty()) {
                updateLocation(enhancedLocation)
            } else {
                updateLocation(keyPoints)
            }
        }

        override fun onRawLocationChanged(rawLocation: Location) {
            //not handled
        }

    }

    @SuppressLint("LogNotTimber")
    private fun updateLocation(location: Location) {
        mAuth = FirebaseAuth.getInstance()

        Log.d(TAG, "updateLocation: Single location Called: $location")
        updateLocation(listOf(location))

//        database = FirebaseDatabase.getInstance().getReference("UserCommuteType")
//        val currUserType = database.child(mAuth?.currentUser?.uid!!).child("userType")

        database = FirebaseDatabase.getInstance().getReference("DriverLiveLocation")
        val lat = location.latitude.toString()
        val lng = location.longitude.toString()
        val bearing = location.bearing.toString()

//        val currLat = DriverLiveLocation(DriverLatitude = "$lat")
//        val currLng = DriverLiveLocation(DriverLongitude = "$lng")
//        val currBearing = DriverLiveLocation(DriverBearing = "$bearing")

        val driverCurrentLocation = DriverLiveLocation(lat, lng, bearing)

        database.child(mAuth?.currentUser?.uid!!).setValue(driverCurrentLocation)


        database.child(mAuth?.currentUser?.uid!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(p0: DataSnapshot) {
//                    val currentLatitude = p0.child("driverLatitude").getValue(String::class.java)
//                    val currentLongitude = p0.child("driverLongitude").getValue(String::class.java)
//
//                    val selectedPoint: Point = Point.fromLngLat(
//                        currentLongitude!!.toDouble(),
//                        currentLatitude!!.toDouble()
//                    )
//
//                    val sourceId = "PASSENGER_LOCATION"
//
//                    mapboxMap.getStyle {
//                        updateSource(it, sourceId, selectedPoint)
//                    }
                }
            })
    }

    private fun updateSource(style: Style, source_id: String, point: Point) {
        val source = style.getSourceAs<GeoJsonSource>(source_id)
        source?.setGeoJson(point)
    }

    @SuppressLint("LogNotTimber")
    internal fun updateLocation(locations: List<Location>) {
        Log.d(TAG, "updateLocation: List of locations Called: $locations")

        //location update is the recommended method to update the location component
        //but caused jerky movement
        mapboxMap.locationComponent.forceLocationUpdate(locations, false)
    }


    private val routeProgressObserver = object : RouteProgressObserver {
        override fun onRouteProgressChanged(routeProgress: RouteProgress) {
            instructionView.updateDistanceWith(routeProgress)
            summaryBottomSheet.update(routeProgress)

            routeProgress.currentState.let { currentState ->
                //TODO add voice instruction notifying driver that they are approaching waypoints
            }
        }
    }

    @SuppressLint("LogNotTimber")
    private val arrivalObserver = object : ArrivalObserver {
        override fun onFinalDestinationArrival(routeProgress: RouteProgress) {
            finalDestinationAlertDialog()
            //TODO update database
        }

        override fun onNextRouteLegStart(routeLegProgress: RouteLegProgress) {
            if (routeLegProgress.legIndex == 1){
                Log.d(TAG, "Pickup Point")
                pickUpAlertDialog()
            }
            if (routeLegProgress.legIndex == 2){
                Log.d(TAG, "Drop-off Point")
                dropOffAlertDialog()
            }
        }
    }

    @SuppressLint("LogNotTimber")
    fun pickUpAlertDialog() {
        //Instantiate builder variable
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(false)
        builder.setTitle("Pickup Point")
        builder.setMessage("Please wait for the passenger.")

        builder.setPositiveButton(
            "Continue"
        ) { dialog, id ->
            // User clicked Update Now button
            Log.d(TAG, "Collected")
            dialog.dismiss()
        }

        builder.setNegativeButton(
            "Cancel"
        ) { dialog, id ->
            // User cancelled the dialog
            Log.d(TAG, "Cancel")
            dialog.dismiss()
        }
        builder.show()
    }

    @SuppressLint("LogNotTimber")
    fun dropOffAlertDialog() {
        //Instantiate builder variable
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(false)
        builder.setTitle("Drop-off Point")
        builder.setMessage("Fare = $5.30")

        builder.setPositiveButton(
            "Continue"
        ) { dialog, id ->
            // User clicked Update Now button
            Log.d(TAG, "Confirm")
            dialog.dismiss()
        }
        builder.setNegativeButton(
            "Cancel"
        ) { dialog, id ->
            // User cancelled the dialog
            Log.d(TAG, "Cancel")
            dialog.dismiss()
        }
        builder.show()
    }

    @SuppressLint("LogNotTimber")
    fun finalDestinationAlertDialog() {
        val builder = AlertDialog.Builder(this)

        builder.setCancelable(false)
        builder.setTitle("Final Destination")
        builder.setMessage("Rating")


        builder.setPositiveButton(
            "Continue"
        ) { dialog, id ->
            // User clicked Update Now button
            Log.d(TAG, "Confirm")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(Intent(this, MainActivity::class.java))
            finish()

            dialog.dismiss()
        }
        builder.show()
    }


    /*--------------------------------------------------------------------------------------------*/
    /*------------------------------------ Off Route Detection -----------------------------------*/

    private val offRouteObserver = object : OffRouteObserver {
        override fun onOffRouteStateChanged(offRoute: Boolean) {
            //nav component automatically creates new route when user goes off route
            //use the route in the nav component to update the map
            val currentRoutes = mapboxNavigation.getRoutes()

            if (currentRoutes.count() > 0) {
                navigationMap.drawRoute(currentRoutes[0])
            }
        }
    }


    /*--------------------------------------------------------------------------------------------*/
    /*----------------------------------- Maneuver Instructions ----------------------------------*/
    private val tripSessionStateObserver = object : TripSessionStateObserver {
        override fun onSessionStateChanged(tripSessionState: TripSessionState) {
            when (tripSessionState) {
                TripSessionState.STARTED -> {
                    instructionView.visibility = View.VISIBLE
                    instructionView.retrieveSoundButton().show()
                    instructionView.retrieveFeedbackButton().show()
                    summaryBottomSheet.visibility = View.VISIBLE
                }
                TripSessionState.STOPPED -> {
                    instructionView.visibility = View.GONE
                    summaryBottomSheet.visibility = View.GONE
                    //camera??
                }
            }
        }
    }

    private val bannerInstructionsObserver = object : BannerInstructionsObserver {
        override fun onNewBannerInstructions(bannerInstructions: BannerInstructions) {
            instructionView.updateBannerInstructionsWith(bannerInstructions)
            instructionView.toggleGuidanceView(bannerInstructions)
        }
    }

    /*--------------------------------- Voice Instructions ---------------------------------------*/

    private var isVoiceMuted = false
    private lateinit var ttsPlayer: TTS

    private val voiceInstructionsObserver = object : VoiceInstructionsObserver {
        override fun onNewVoiceInstructions(voiceInstructions: VoiceInstructions) {
            if (voiceInstructions.announcement() != null && !isVoiceMuted) {
                ttsPlayer.play(voiceInstructions.announcement()!!)
            }
        }
    }


    /*--------------------------------------------------------------------------------------------*/
    /*---------------------------------- Source Functions ----------------------------------------*/
    private fun initializeDriverRouteLayer(style: Style) {
        style.addSource(
            GeoJsonSource(
                "ROUTE_LINE_SOURCE_ID",
                GeoJsonOptions().withLineMetrics(true)
            )
        )
        style.addLayerBelow(
            LineLayer("ROUTE_LAYER_ID", "ROUTE_LINE_SOURCE_ID")
                .withProperties(
                    PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                    PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                    PropertyFactory.lineWidth(6f),
                    PropertyFactory.lineOpacity(1f),
                    PropertyFactory.lineColor("#2E4FC9")
                ),
            "mapbox-location-shadow-layer"
        )
    }

    private fun setupMapIcons(style: Style) {
        style.addImage(
            "ICON_ID",
            BitmapUtils.getBitmapFromDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.map_default_map_marker
                )
            )!!
        )
    }

    private fun setupIconLayerBelow(
        style: Style,
        sourceId: String,
        layerId: String,
        belowLayer: String
    ) {
        style.addSource(GeoJsonSource(sourceId))
        style.addLayerBelow(
            SymbolLayer(layerId, sourceId)
                .withProperties(PropertyFactory.iconImage("ICON_ID")), belowLayer
        )
    }

    private fun setupLocationMarkerLayers(style: Style) {
        setupIconLayerBelow(
            style,
            "PASSENGER_LOCATION",
            "PASSENGER_LAYER",
            "mapbox-location-shadow-layer"
        )
    }
    /*--------------------------------------------------------------------------------------------*/
    /*-------------------------------------- OnCreate --------------------------------------------*/
    @SuppressLint("LogNotTimber")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nav)

        //get commute
        this.commute = intent.getSerializableExtra("DirectionsRoute")!! as DirectionsRoute

        //set access token
        this.accessToken = getString(R.string.mapbox_access_token)

        //SPEECH
        ttsPlayer = TTS(this)

        //initialize mapView
        this.mapView = findViewById(R.id.nav_mapView)
        this.mapView.onCreate(savedInstanceState)
        this.mapView.getMapAsync(this)

        this.instructionView = findViewById(R.id.nav_instructionView)
        this.summaryBottomSheet = findViewById(R.id.nav_summary_sheet)
        this.recenterButton = findViewById(R.id.recenter_button)
        this.muteButton = findViewById(R.id.mute_button)
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(mapboxMap: MapboxMap) {
        //initialize mapboxMap
        this.mapboxMap = mapboxMap
        mapboxMap.setStyle(Style.MAPBOX_STREETS) {

            //initialize the mapboxNavigation
            val mapboxNavigationOptions = MapboxNavigation
                .defaultNavigationOptionsBuilder(this, accessToken)
                .build()
            this.mapboxNavigation = MapboxNavigation(mapboxNavigationOptions)
            this.navigationMap = NavigationMapboxMap.Builder(mapView, mapboxMap, this)
                .vanishRouteLineEnabled(true)
                .build().also {
                    //it.addProgressChangeListener(mapboxNavigation)
                }

            //Register Observers
            this.mapboxNavigation.registerLocationObserver(locationObserver)
            this.mapboxNavigation.registerArrivalObserver(arrivalObserver)
            this.mapboxNavigation.registerRouteProgressObserver(routeProgressObserver)
            this.mapboxNavigation.registerBannerInstructionsObserver(bannerInstructionsObserver)
            this.mapboxNavigation.registerVoiceInstructionsObserver(voiceInstructionsObserver)
            this.mapboxNavigation.registerOffRouteObserver(offRouteObserver)

            //Camera init
            mapCamera = NavigationCamera(mapboxMap)
            mapCamera.addProgressChangeListener(mapboxNavigation)
            //init camera zoom level
            mapboxMap.moveCamera(CameraUpdateFactory.zoomTo(25.0))

            this.recenterButton.setOnClickListener {
                //recenter camera position and re-enable tracking
                mapCamera.resetCameraPositionWith(NAVIGATION_TRACKING_MODE_GPS)
            }

            this.muteButton.setOnClickListener {
                Toast.makeText(this, "Pressed", Toast.LENGTH_SHORT).show()
            }

            //get last location with custom location engine callback
            val myLocationEngineCallback = LocationEngineCallback(this)
            mapboxNavigation.navigationOptions.locationEngine.getLastLocation(
                myLocationEngineCallback
            )

            //add route to map
            Log.d(TAG, "onMapReady: $commute")
            this.navigationMap.drawRoute(commute)
            this.navigationMap.updateCameraTrackingMode(NAVIGATION_TRACKING_MODE_GPS)

            this.navigationMap.setPuckDrawableSupplier(MyPuckDrawableSupplier())

            //add route to navigation object
            this.mapboxNavigation.setRoutes(listOf(commute))

            //start trip
            //camera start route
            mapCamera.updateCameraTrackingMode(NAVIGATION_TRACKING_MODE_GPS)
            mapCamera.start(commute)

            //set location puck render mode
            mapboxMap.locationComponent.renderMode = RenderMode.GPS

            //start session
            this.mapboxNavigation.startTripSession()

            //Add icon
            setupMapIcons(it)
            setupLocationMarkerLayers(it)
        }

    }


    /*--------------------------------------------------------------------------------------------*/
    /*-------------------------------------- Life Cycle ------------------------------------------*/

    override fun onStart() {
        super.onStart()
        mapView.onStart()

        if (this::mapboxNavigation.isInitialized) {
            mapboxNavigation.registerLocationObserver(locationObserver)
            mapboxNavigation.registerTripSessionStateObserver(tripSessionStateObserver)
            mapboxNavigation.registerOffRouteObserver(offRouteObserver)
            mapboxNavigation.registerArrivalObserver(arrivalObserver)
        }
        if (this::mapCamera.isInitialized) {
            mapCamera.onStart()
        }
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
        mapCamera.onStop()
        mapboxNavigation.unregisterOffRouteObserver(offRouteObserver)
        mapboxNavigation.unregisterArrivalObserver(arrivalObserver)
        mapboxNavigation.unregisterLocationObserver(locationObserver)
        mapboxNavigation.unregisterBannerInstructionsObserver(bannerInstructionsObserver)
        mapboxNavigation.unregisterTripSessionStateObserver(tripSessionStateObserver)
        mapboxNavigation.unregisterVoiceInstructionsObserver(voiceInstructionsObserver)
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
        mapboxNavigation.unregisterRouteProgressObserver(routeProgressObserver)
        mapboxNavigation.stopTripSession()
        mapboxNavigation.onDestroy()
        mapView.onDestroy()
    }


/*------------------------------------------------------------------------------------------------*/
/*------------------------------------------------------------------------------------------------*/

    @SuppressLint("MissingPermission")
    private fun enableLocationComponent(loadedMapStyle: Style) {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            val locationComponentActivationOptions =
                LocationComponentActivationOptions.builder(this, loadedMapStyle)
                    .useDefaultLocationEngine(false)
                    .build()
            mapboxMap.locationComponent.apply {
                activateLocationComponent(locationComponentActivationOptions)
                isLocationComponentEnabled = true // Enable to make component visible
                cameraMode = CameraMode.TRACKING  // Set the component's camera mode
                renderMode = RenderMode.COMPASS   // Set the component's render mode
            }
            initLocationEngine()
        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager.requestLocationPermissions(this)
        }
    }

    @SuppressLint("MissingPermission")
    private fun initLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(this)
        val request = LocationEngineRequest
            .Builder(1000L)
            .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
            .setMaxWaitTime(1000L * 5)
            .build()
        locationEngine.requestLocationUpdates(request, callback, mainLooper)
        locationEngine.getLastLocation(callback)

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private inner class LocationChangeListeningCallback :
        LocationEngineCallback<LocationEngineResult> {

        override fun onSuccess(result: LocationEngineResult?) {
            result?.lastLocation
                ?: return //BE CAREFULL HERE, IF NAME LOCATION UPDATE DON'T USER -> val resLoc = result.lastLocation ?: return
            if (result.lastLocation != null) {
                val lat = result.lastLocation?.latitude!!
                val lng = result.lastLocation?.longitude!!
                val latLng = LatLng(lat, lng)

                if (result.lastLocation != null) {
                    mapboxMap.locationComponent.forceLocationUpdate(result.lastLocation)
                    val position = CameraPosition.Builder()
                        .target(latLng)
                        .zoom(13.0) //disable this for not follow zoom
                        .tilt(10.0)
                        .build()
                    mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position))
                    Toast.makeText(
                        this@NavActivity,
                        "Location update : $latLng",
                        Toast.LENGTH_SHORT
                    ).show()

                    //TODO drivers location in database for driver live location updates
//                    database = FirebaseDatabase.getInstance().getReference("CurrentLocation")
//                    val coordinates = Coords(latLng)
//                    Log.d(TAG, "Coordinates: $latLng")
//                    database.child(mAuth?.currentUser?.uid!!).setValue(coordinates)
                }

            }

        }

        override fun onFailure(exception: Exception) {
            //not handled
        }
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        Toast.makeText(this, "This app requires location services to provide directions while navigating.", Toast.LENGTH_SHORT).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            mapboxMap.getStyle {
                enableLocationComponent(it)
            }
        } else {
            Toast.makeText(this, "Permission not granted!! app will be EXIT", Toast.LENGTH_LONG)
                .show()
            Handler().postDelayed({
                finish()
            }, 3000)
        }
    }


}


/*------------------------------------------------------------------------------------------------*/
/*------------------------------------------------------------------------------------------------*/

class LocationEngineCallback(activity: NavActivity) : LocationEngineCallback<LocationEngineResult> {
    private var activityRef: WeakReference<NavActivity>? = null

    init {
        this.activityRef = WeakReference(activity)
    }

    @SuppressLint("LogNotTimber")
    override fun onSuccess(result: LocationEngineResult?) {
        if (result != null) {
            //initialize location puck position
            activityRef?.get()?.updateLocation(result.locations)
        } else {
            Log.e(TAG, "onSuccess: Failed to update location (result == null)")
        }
    }
    @SuppressLint("LogNotTimber")
    override fun onFailure(exception: Exception) {
        Log.e(TAG, "onFailure: Failed to update location", exception)
    }

}

class MyPuckDrawableSupplier : PuckDrawableSupplier {
    override fun getPuckDrawable(routeProgressState: RouteProgressState): Int {
        return R.drawable.mapbox_ic_user_puck
    }
}