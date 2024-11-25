package com.example.watcomtravels

import android.content.res.Resources
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState


@Composable
fun CoolMap(
    startingLocation: LatLng,
    stopList : MutableList<StopObject>
) {
    var isLoaded by remember { mutableStateOf(false) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(startingLocation, 15f)
    }

    var map: Unit = GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        onMapLoaded = { isLoaded = true },
        googleMapOptionsFactory = {
            GoogleMapOptions().mapId("map1")
        }
    ) {
        StopMarkersMapContent(
            stopList
        )
//        var pointList = remember { mutableListOf<LatLng>() }
//        var point1 = LatLng(stopList[0].lat.toDouble(), stopList[0].long.toDouble())
//        var point2 = LatLng(stopList[2].lat.toDouble(), stopList[2].long.toDouble())
//        pointList.add(point1)
//        pointList.add(point2)
//
//        Polyline(pointList)
    }

    fun viewStopDetails(stop: StopObject) {
        // Gets the details of the stop from the API and displays them
    }
}