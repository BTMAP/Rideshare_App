package com.uwi.btmap

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineResult
import com.mapbox.api.directions.v5.models.BannerInstructions
import com.mapbox.api.directions.v5.models.VoiceInstructions
import com.mapbox.api.speech.v1.MapboxSpeech
import com.mapbox.geojson.LineString
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
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.navigation.base.trip.model.RouteProgress
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.trip.session.*
import com.mapbox.navigation.ui.NavigationViewOptions
import com.mapbox.navigation.ui.camera.NavigationCamera
import com.mapbox.navigation.ui.camera.NavigationCamera.NAVIGATION_TRACKING_MODE_GPS
import com.mapbox.navigation.ui.instruction.InstructionView
import com.mapbox.navigation.ui.map.NavigationMapboxMap
import com.mapbox.navigation.ui.route.NavigationMapRoute
import com.mapbox.navigation.ui.summary.SummaryBottomSheet
import com.mapbox.navigation.ui.voice.NavigationSpeechPlayer
import com.mapbox.navigation.ui.voice.SpeechPlayer
import com.mapbox.navigation.ui.voice.SpeechPlayerProvider
import com.mapbox.navigation.ui.voice.VoiceInstructionLoader
import okhttp3.Cache
import java.io.File
import java.lang.ref.WeakReference
import java.util.*


class NavActivity :
    AppCompatActivity(),
    OnMapReadyCallback{

    private val TAG = "NAV_ACTIVITY"
    private lateinit var accessToken: String

    //mapbox views
    private lateinit var mapView: MapView
    private lateinit var instructionView: InstructionView
    private lateinit var summaryBottomSheet: SummaryBottomSheet

    //mapbox controllers
    private lateinit var mapboxMap: MapboxMap

    private lateinit var mapboxNavigation: MapboxNavigation
//    private lateinit var navigationMapRoute: NavigationMapRoute
    private lateinit var navigationMap: NavigationMapboxMap

    private lateinit var mapCamera: NavigationCamera

    //custom commute object
    private lateinit var commute: Commute

    //more view references
    private lateinit var recenterButton : Button

    /*--------------------------------------------------------------------------------------------*/
    /*-------------------------- Location and route progress observer ---------------------------*/

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

            //update camera position? (might not be needed)
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


    private val routeProgressObserver = object : RouteProgressObserver{
        override fun onRouteProgressChanged(routeProgress: RouteProgress) {
            Log.d(TAG, "onRouteProgressChanged: Changed!!!!!!!!!!!!!!!!!!")
            //TODO update progress card info
            instructionView.updateDistanceWith(routeProgress)
            summaryBottomSheet.update(routeProgress)
        }

    }

    /*--------------------------------------------------------------------------------------------*/
    /*--------------------------- Maneuver Instructions ----------------------------------*/
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
    private lateinit var ttsPlayer : TTS

    private val voiceInstructionsObserver = object : VoiceInstructionsObserver {
        override fun onNewVoiceInstructions(voiceInstructions: VoiceInstructions) {
            Log.d(TAG, "onNewVoiceInstructions: ${voiceInstructions.announcement()}")
            //speechPlayer.play(voiceInstructions)
            if(voiceInstructions.announcement() != null){
                ttsPlayer.play(voiceInstructions.announcement()!!)
            }
        }

    }


    /*--------------------------------------------------------------------------------------------*/
    /*---------------------------------- Source Functions ----------------------------------------*/
    private fun initializeDriverRouteLayer(style: Style){
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

        //SPEECH
        ttsPlayer = TTS(this)

        //initialize mapView
        this.mapView = findViewById(R.id.nav_mapView)
        this.mapView?.onCreate(savedInstanceState)
        this.mapView?.getMapAsync(this)

        this.instructionView = findViewById(R.id.nav_instructionView)
        this.summaryBottomSheet = findViewById(R.id.nav_summary_sheet)
        this.recenterButton = findViewById(R.id.recenter_button)
    }

    //TODO address missing permissions checks
    @SuppressLint("MissingPermission")
    override fun onMapReady(mapboxMap: MapboxMap) {
        //initialize mapboxMap
        this.mapboxMap = mapboxMap
        mapboxMap.setStyle(Style.MAPBOX_STREETS) {

            initializeLocationComponent(mapboxMap, it)
            initializeDriverRouteLayer(it)

            //initialize the mapboxNavigation
            val mapboxNavigationOptions = MapboxNavigation
                .defaultNavigationOptionsBuilder(this, accessToken)
                .build()
            this.mapboxNavigation = MapboxNavigation(mapboxNavigationOptions)
            this.navigationMap = NavigationMapboxMap.Builder(mapView,mapboxMap,this)
                .vanishRouteLineEnabled(true)
                .build().also{
                    //it.addProgressChangeListener(mapboxNavigation)
                }

            //Register Observers
            this.mapboxNavigation.registerLocationObserver(locationObserver)
            this.mapboxNavigation.registerRouteProgressObserver(routeProgressObserver)
            this.mapboxNavigation.registerBannerInstructionsObserver(bannerInstructionsObserver)
            this.mapboxNavigation.registerVoiceInstructionsObserver(voiceInstructionsObserver)

            //Camera init
            mapCamera = NavigationCamera(mapboxMap)
            mapCamera.addProgressChangeListener(mapboxNavigation)
            //init camera zoom level
            mapboxMap.moveCamera(CameraUpdateFactory.zoomTo(25.0))

            this.recenterButton.setOnClickListener {
                //recenter camera position and re-enable tracking
                mapCamera.resetCameraPositionWith(NAVIGATION_TRACKING_MODE_GPS)
            }

            //get last location with custom location engine callback
            val myLocationEngineCallback = com.uwi.btmap.LocationEngineCallback(this)
            mapboxNavigation.navigationOptions.locationEngine.getLastLocation(myLocationEngineCallback)

            //add route to map
//            val lineSource = it.getSourceAs<GeoJsonSource>("ROUTE_LINE_SOURCE_ID")
//            val routeLineString = LineString.fromPolyline(
//                commute.getDriverRoute().geometry()!!,
//                6
//            )

//            lineSource!!.setGeoJson(routeLineString)

            this.navigationMap?.drawRoute(commute.getDriverRoute())
            this.navigationMap?.updateCameraTrackingMode(NAVIGATION_TRACKING_MODE_GPS)

            //add route to navigation object
            this.mapboxNavigation.setRoutes(listOf(commute.getDriverRoute()))

            //start trip
            //camera start route
            mapCamera.updateCameraTrackingMode(NAVIGATION_TRACKING_MODE_GPS)
            mapCamera.start(commute.getDriverRoute())
            //start session
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
//            Log.d(TAG, "onStart: register locationObserver")
//            mapboxNavigation.registerLocationObserver(locationObserver)
            mapboxNavigation.registerTripSessionStateObserver(tripSessionStateObserver)
        }
        if(this::mapCamera.isInitialized){
            mapCamera.onStart()
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
        mapCamera.onStop()
        mapboxNavigation.unregisterLocationObserver(locationObserver)
        mapboxNavigation.unregisterBannerInstructionsObserver(bannerInstructionsObserver)
        mapboxNavigation.unregisterTripSessionStateObserver(tripSessionStateObserver)
        mapboxNavigation.unregisterVoiceInstructionsObserver(voiceInstructionsObserver)
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
        mapboxNavigation.unregisterRouteProgressObserver(routeProgressObserver)
        mapboxNavigation.stopTripSession()
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
