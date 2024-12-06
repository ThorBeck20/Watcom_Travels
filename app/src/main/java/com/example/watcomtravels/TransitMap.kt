package com.example.watcomtravels

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@Composable
fun TransitMap(viewModel: TransitViewModel = TransitViewModel(LocalContext.current)) {
    val uiState by viewModel.uiState.collectAsState()
    val scope : CoroutineScope = rememberCoroutineScope()

    viewModel.loaded()

    val cameraPositionState : CameraPositionState = rememberCameraPositionState {
        position = uiState.cameraPosition
    }

    if (uiState.moveCamera) {
        uiState.latLongBounds?.let {
            animateCamera(scope, cameraPositionState, it)
        }
    }

    val mapProperties by remember {
        mutableStateOf(
            MapProperties(
                isTrafficEnabled = true,
            )
        )
    }
    val mapUiSettings by remember {
        // NO WEIRD GESTURES >:)
        mutableStateOf(
            MapUiSettings(
                mapToolbarEnabled = false,
                rotationGesturesEnabled = true,
                scrollGesturesEnabled= true,
                scrollGesturesEnabledDuringRotateOrZoom = false,
                tiltGesturesEnabled = false,
            )
        )
    }

    if (uiState.isLoaded) {
        GoogleMap(
            properties = mapProperties,
            uiSettings = mapUiSettings,
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMapLoaded = { viewModel.loaded() },
            googleMapOptionsFactory = {
                GoogleMapOptions().mapId("map1")
            },
            onMapClick = { latLng ->
                viewModel.addMarker(latLng)
            }
        ) {
            // Render Markers
            uiState.displayedMarkers.forEach { (markerState, mOptions) ->
                Marker(
                    state = markerState,
                    title = mOptions.title,
                    icon = mOptions.icon,
                    anchor = Offset(mOptions.anchorU, mOptions.anchorV),
                    flat = true,
                    onClick = {
                        viewModel.selectMarker(markerState)
                        true
                    },
                    onInfoWindowClose = {
                        viewModel.deselectMarker()
                    }
                )
            }

            // Renders Selected Marker
            uiState.selectedMarker?.let { selected ->
                val camPos = CameraPosition.fromLatLngZoom(selected.position, 10f)
                viewModel.updateCameraPosition(camPos)
                Box(
                    modifier = Modifier,
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Text(text = "Hi")
                }
            }

            // Updates the polyLine
            uiState.polylineOptions?.let { polylineOptions ->
                Polyline(
                    points = polylineOptions.points,
                    color = Color.Black
                )

            }
        }
    } else {
        CircularProgressIndicator()
    }


}
// Moves the camera -- In progress
fun animateCamera(scope: CoroutineScope, cameraPosState: CameraPositionState, newCameraPosition: CameraPosition) {
    scope.launch {
        cameraPosState.animate(
            update = CameraUpdateFactory.newCameraPosition(newCameraPosition),
            durationMs = 2000
        )
    }
}

// Moves the camera -- In progress
fun animateCamera(scope: CoroutineScope, cameraPosState: CameraPositionState, newLatLngBounds: LatLngBounds) {
    scope.launch {
        cameraPosState.animate(
            update = CameraUpdateFactory.newLatLngBounds(newLatLngBounds, 100),
            durationMs = 2000
        )
    }
}


fun resourceToScaledBitMap(@DrawableRes id: Int, size : Int = 10) : Bitmap? {
//    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    val bitmap : Bitmap? = BitmapFactory.decodeResource(Resources.getSystem(), id)
    if (bitmap == null) {
        return null
    }
//    options.inSampleSize = calculateInSampleSize()
    val resizedBitmap = Bitmap.createScaledBitmap(
        bitmap,
        size,
        size,
        true
    )
//    val canvas = Canvas(bitmap)
//    drawable.setBounds(0,0, canvas.width, canvas.height)
//    drawable.draw(canvas)
    return resizedBitmap
}