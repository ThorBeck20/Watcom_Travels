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

    LaunchedEffect(true) {
        withContext(Dispatchers.IO) {
            viewModel.getRoutes()
            var route = Route(
                routeNum = 1,
                name = "Fairhaven&Downtown",
                color = "#ff0000",
                pattern = null
            )
            withContext(Dispatchers.Main) {
                viewModel.displayRoute(route)
                /**
                 * TODO() - Get all the routes and put them and their patterns in the database
                 *          Then do the same for the stops.
                  */
                viewModel.loaded()
            }
        }
    }

    val cameraPositionState : CameraPositionState = rememberCameraPositionState {
        position = uiState.cameraPosition
    }

    if (uiState.moveCamera) {
            uiState.latLongBounds?.let {
                CameraUpdateFactory.newLatLngBounds(it, 100)
            }?.let {
                cameraPositionState.move(
                    update = it
                )
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
            uiState.markers.forEach { (markerState, mOptions) ->
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


    // Moves the camera -- In progress
    fun moveCamera(scope: CoroutineScope, cameraPosState: CameraPositionState, newCameraPosition: CameraPosition) {
        TODO()
        scope.launch {
            cameraPosState.animate(
                update = CameraUpdateFactory.newCameraPosition(newCameraPosition),
                durationMs = 2000
            )
        }
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

/*
class TransitMap() {

    var stopList: MutableList<StopObject>? = mutableListOf<StopObject>()
    var polyLineList : MutableList<Polyline> = mutableListOf<Polyline>()
    var compPolyLine : @Composable () -> Unit = @Composable { Marker() }
    var cameraZoom : Float = 11f
    var cameraPosition : CameraPosition = CameraPosition.fromLatLngZoom(LatLng(48.769768, -122.485886), cameraZoom)
    var mapScope : CoroutineScope =

    private fun createPolyLineRoute(r : Route) : MutableList<@Composable () -> Unit> {
        var lines = mutableListOf<@Composable () -> Unit>()
        if (r.pattern?.isEmpty() == true) {
            return lines
        }
        for (i in (0..<(r.pattern!!.size))) {
            val linePoints = createPolyLinePoints(r.pattern!![i].pt)
            val p = @Composable {
                Polyline(
                    clickable = true,
                    onClick = {
//                        showRouteDetails(r)
                    },
                    points = linePoints,
                    visible = true,
                    tag = r.pattern!![i].lineNum,
                    color = Color(android.graphics.Color.parseColor(r.color))
                )
            }
            lines.add(p)
        }

        return lines
    }

    private fun createPolyLinePoints(list: List<Any>?): MutableList<LatLng> {
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
        stList: MutableList<StopObject>?,
        resources: Resources
    ) {

        val scope : CoroutineScope = rememberCoroutineScope()
        val cameraPositionState : CameraPositionState = rememberCameraPositionState {
            position = cameraPosition
        }

        stopList = stList
        var isLoaded by remember { mutableStateOf(false) }

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
//            resource = resources
//        )
            Polyline(
                points =
            )
        }
        Button(onClick = {
            // Move the camera to a new zoom level
//            displayRoute()
            val camPos = CameraPosition.fromLatLngZoom(LatLng(48.toDouble(), (-122).toDouble()), 10f)
            moveCamera(scope, cameraPositionState, camPos)
        }) {
            Text(text = "Do the thing")
        }

    }
}

 */