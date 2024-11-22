package com.example.watcomtravels

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var stops: MutableList<StopObject> = remember { mutableStateListOf<StopObject>()}
            var loaded by remember { mutableStateOf(false) }
            val currentLocation = 1
            val bham = LatLng(48.73, -122.49)

            LaunchedEffect(true) {
                withContext(Dispatchers.IO) {
                    stops.addAll(WTAApi.getStopObjets())
                    loaded = true
                    Log.d("@@@", "STOPS LOADED")
                }
            }
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                if (loaded) {

                    Log.d("@@@", "Calling map")
                    CoolMap(bham, stops)
                }
            }
        }
    }
}


@Composable
fun CoolMap(startingLocation: LatLng, stopList : MutableList<StopObject>) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(startingLocation, 15f)
    }
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        var numstops = 0
        stopList.forEach { stop ->
            val pos = LatLng(stop.lat.toDouble(), stop.long.toDouble())
            Marker(
                state = rememberMarkerState(position = pos),
                title = stop.name
            )
            numstops+=1

        }
        Log.d("@@@", "stops: $numstops")
    }
}