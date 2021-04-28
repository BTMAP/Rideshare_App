package com.uwi.btmap

import android.content.Context
import android.os.Bundle
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.navigation.core.MapboxNavigation

class MapboxHandler(token: String, context: Context){
    private var token = token
    private var context = context

    private lateinit var mapboxNavigation: MapboxNavigation
    private lateinit var mapboxMap: MapboxMap


    init{
        setupNavigationObject()
    }


    private fun setupNavigationObject(){
        val mapboxNavigationOptions = MapboxNavigation
                .defaultNavigationOptionsBuilder(context, token)
                .build()

        this.mapboxNavigation = MapboxNavigation(mapboxNavigationOptions)
    }

    fun setupMap(){

    }

}