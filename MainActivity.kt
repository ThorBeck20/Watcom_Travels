package com.example.watcomtravels

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var stopIds : List<StopObject> = emptyList()
            val currentLocation = 1
            val bham = LatLng(48.73, -122.49)

            LaunchedEffect(true) {
                withContext(Dispatchers.IO) {
                    stopIds = WTAApi.getStopObjets()
                }
            }
            Column (
                modifier = Modifier.fillMaxSize()
            ) {
                BasicMap()
//                CoolMap(startingLocation = bham)
//                for (stop in stopIds) {
//                    Text(text = stop.id.toString())
//                }
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
    fun CoolMap(startingLocation: LatLng) {
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(startingLocation, 15f)
        }
        val markerState = rememberMarkerState(position = LatLng(48.73, -122.49))
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        ) {
            AdvancedMarker(
                state = markerState,
                title = "Communications Facility"
            )
        }
    }
}