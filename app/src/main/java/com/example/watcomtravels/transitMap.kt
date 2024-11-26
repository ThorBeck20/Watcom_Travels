package com.example.watcomtravels

import android.content.res.Resources
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

class TransitMap() {

    var stopList: MutableList<StopObject>? = mutableListOf<StopObject>()
    var routePatterns: MutableList<RoutePattern>? = mutableListOf<RoutePattern>()

    private fun createPolyLineList(list: List<Any>?): MutableList<LatLng> {
        if (list?.isEmpty() == true) {
            return mutableListOf<LatLng>()
        }
        val pointsList = mutableListOf<LatLng>()
        for (i in (0..<list!!.size)) {
            if (list[i] is StopObject) {
                val stop: StopObject = list[i] as StopObject
                pointsList.add(LatLng(stop.lat.toDouble(), stop.long.toDouble()))
            } else if (list[i] is PatternObject) {
                val point: PatternObject = list[i] as PatternObject
                pointsList.add(LatLng(point.lat.toDouble(), point.long.toDouble()))
            } else {
                Log.d("@@@", "Unknown Type passed.")
            }
        }
        return pointsList
    }

    @Composable
    fun transitMap(
        startingLocation: LatLng,
        stList: MutableList<StopObject>?,
        rtPattern: MutableList<RoutePattern>?,
        resources: Resources
    ) {
        stopList = stList
        routePatterns = rtPattern

        var isLoaded by remember { mutableStateOf(false) }

        var pointList: MutableList<LatLng> = remember { mutableListOf<LatLng>() }
        if (routePatterns?.isNotEmpty() == true) {
            pointList = createPolyLineList(routePatterns!![0].pt.toList())
        }
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(LatLng(48.720355999987, -122.510690000002), 15f)
        }

        var mapProperties by remember {
            mutableStateOf(
                MapProperties(
                    isTrafficEnabled = true,
                )
            )
        }
        val mapUiSettings by remember {
            mutableStateOf(
                MapUiSettings(
                    mapToolbarEnabled = false
                )
            )
        }



        var map = GoogleMap(
            properties = mapProperties,
            uiSettings = mapUiSettings,
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMapLoaded = { isLoaded = true },
            googleMapOptionsFactory = {
                GoogleMapOptions().mapId("map1")
            }
        )
        {
//        StopMarkersMapContent(
//            stopList,
//            routePattern,
//            resource = resources
//        )

            Polyline(pointList)
        }



        fun viewStopDetails(stop: StopObject) {
            // Gets the details of the stop from the API and displays them
        }
    }
}