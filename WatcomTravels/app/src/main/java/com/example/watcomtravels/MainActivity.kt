package com.example.watcomtravels

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.AdvancedMarker
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.AdvancedMarker
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var stops: MutableList<StopObject> = remember { mutableStateListOf<StopObject>()}
            val currentLocation = 1
            val bham = LatLng(48.73, -122.49)

            LaunchedEffect(true) {
                withContext(Dispatchers.IO) {
                    stops = WTAApi.getStopObjets().toMutableList()
                    Log.d("@@@", "STOPS LOADED")
                }
            }
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
//                CoolMap(bham, stops)
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(bham, 15f)
                }
                val markerState = rememberMarkerState(position = LatLng(48.73, -122.49))
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState
                ) {
                    var numstops = 0
                    stops.forEach { stop ->
                        val state = rememberMarkerState(position = LatLng(stop.lat.toDouble(), stop.long.toDouble()))
                        AdvancedMarker(
                            state = state,
                            title = stop.name
                        )
                        numstops+=1

                    }
                    Log.d("@@@", "stops: ${numstops}")
                    AdvancedMarker(
                        state = markerState,
                        title = "Communications Facility"
                    )
                }
            }

        }
    }
}

@Composable
fun BasicMap() {
    val bham = LatLng(48.73, -122.47)
    val cameraPostionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(bham, 15f)
    }
    Box (
        modifier = Modifier.fillMaxSize()
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPostionState
        )

        Button(
            onClick = {
                cameraPostionState.move(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.fromLatLngZoom(
                            LatLng(48.0, -122.0),
                            10f
                        )
                    )
                )
            },
            modifier = Modifier.align(Alignment.BottomStart)
        ) {
            Text("Move")
        }
    }
}

@Composable
fun CoolMap(startingLocation: LatLng, stopList : MutableList<StopObject>) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(startingLocation, 15f)
    }
    val markerState = rememberMarkerState(position = LatLng(48.73, -122.49))
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        var numstops = 0
        stopList.forEach { stop ->
            val state = rememberMarkerState(position = LatLng(stop.lat.toDouble(), stop.long.toDouble()))
            AdvancedMarker(
                state = state,
                title = stop.name
            )
            numstops+=1

        }
        Log.d("@@@", "stops: ${numstops}")
        AdvancedMarker(
            state = markerState,
            title = "Communications Facility"
        )
    }
}