package com.uwi.btmap

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.api.directions.v5.models.DirectionsRoute
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
import com.mapbox.navigation.core.MapboxNavigation
import org.json.JSONObject

class NavigationActivity :
        AppCompatActivity(),
        OnMapReadyCallback,
        PermissionsListener {

    private lateinit var mapView: MapView
    private lateinit var mapboxMap: MapboxMap
    private lateinit var mapboxNavigation: MapboxNavigation

    private var permissionsManager: PermissionsManager = PermissionsManager(this)

    private lateinit var commute: Commute

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)

        //required mapbox mapview setup stuff
        setupMapView(savedInstanceState)
        //requires mapbox navigation setup stuff
        setupNavigationObject()
        //get commute to navigate from intent
        commute = intent.getSerializableExtra("commute")!! as Commute
    }

    private fun setupMapView(savedInstanceState: Bundle?){
        //required mapbox mapview setup stuff
        this.mapView = findViewById(R.id.mapView)
        this.mapView?.onCreate(savedInstanceState)
        this.mapView?.getMapAsync(this)
    }

    private fun setupNavigationObject(){
        //requires mapbox navigation setup stuff
        val mapboxNavigationOptions = MapboxNavigation
            .defaultNavigationOptionsBuilder(this, getString(R.string.mapbox_access_token))
            .build()

        this.mapboxNavigation = MapboxNavigation(mapboxNavigationOptions)
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        mapboxMap.setStyle(Style.MAPBOX_STREETS) {
            this.mapboxMap = mapboxMap
//            show user location
            enableLocationComponent(it)

            setupMapIcons(it)

            setupRouteLayer(it)
            setupLocationMarkerLayers(it)

            val clickPointSource = it.getSourceAs<GeoJsonSource>("ROUTE_LINE_SOURCE_ID")
            val routeLineString = LineString.fromPolyline(
                    commute.getDriverRoute().geometry()!!,
                    6
            )
//                    add the returned route to the route line source
            clickPointSource?.setGeoJson(routeLineString)

            updateSource(it, "ORIGIN_SOURCE",commute.getOrigin())
            updateSource(it, "DESTINATION_SOURCE",commute.getDestination())
            updateSource(it, "PICKUP_POINT",commute.getPickup())
            updateSource(it, "DROP_OFF_SOURCE",commute.getDropOff())
        }
    }

    private fun updateSource(style: Style, source_id : String, point: Point){
        var source = style.getSourceAs<GeoJsonSource>(source_id)
        source?.setGeoJson(point)
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
                    cameraMode = CameraMode.TRACKING_GPS
                    renderMode = RenderMode.GPS

                    val lat = mapboxMap.locationComponent.lastKnownLocation?.latitude
                    val lng = mapboxMap.locationComponent.lastKnownLocation?.longitude

                    //call map matching api
                    //val client = MapboxMapMatching.builder()
                    //get new lat and lng
                    //set icon for driver to position of lat & lng
                    //set set camera to position of lat & lng

                    val position = CameraPosition.Builder()
                        .zoom(12.0)
                        .tilt(60.0)
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
                                PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                                PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                                PropertyFactory.lineWidth(6f),
                                PropertyFactory.lineOpacity(1f),
                                PropertyFactory.lineColor("#2E4FC9")
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
                                PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                                PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                                PropertyFactory.lineWidth(6f),
                                PropertyFactory.lineOpacity(1f),
                                PropertyFactory.lineColor("#5E8F5E")
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
                .withProperties(PropertyFactory.iconImage("ICON_ID")),aboveLayer)
    }

    private fun setupIconLayerBelow(style: Style, sourceId : String, layerId : String, aboveLayer : String){
        style.addSource(GeoJsonSource(sourceId))
        style.addLayerBelow(SymbolLayer(layerId,sourceId)
                .withProperties(PropertyFactory.iconImage("ICON_ID")),aboveLayer)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        Toast.makeText(this, "We need location services >:/", Toast.LENGTH_LONG).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if(granted){
            enableLocationComponent(mapboxMap.style!!)
        }else{
            Toast.makeText(this, "permissions not granted", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}