package com.example.watcomtravels

import android.content.res.Resources
import android.graphics.ColorSpace
import android.os.Bundle
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.ktx.model.markerOptions
import com.google.maps.android.ktx.model.polylineOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TransitUiState(
    val cameraPosition: CameraPosition = CameraPosition.fromLatLngZoom(LatLng(48.769768, -122.485886), 11f),
    val markers : MutableMap<MarkerState, MarkerOptions> = mutableMapOf<MarkerState, MarkerOptions>(),
    val selectedMarker : MarkerState? = null,
    val polylineOptions: PolylineOptions? = null,
    val routes : MutableList<Route> = mutableListOf<Route>(),
    val displayedRoute : Route? = null,
    val moveCamera : Boolean = false,
    val latLongBounds : LatLngBounds? = null
)

class TransitViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(TransitUiState())
    val uiState : StateFlow<TransitUiState> get() = _uiState


    /**
     * Updates the viewModel by calling [WTAApi.getRoutes] to update the viewModel's
     * [TransitUiState.routes]
     */
    fun getRoutes() {
        viewModelScope.launch {
            try {
                val rts : MutableList<Route> = WTAApi.getRoutes()?.toMutableList() ?: mutableListOf<Route>()
                _uiState.update { it.copy(routes = rts) }
            } catch (e : Exception) {
                Log.d("@@@","There was an issue $e")
            }
        }
    }


    /**
     * Updates the viewModel's [TransitUiState.displayRoute] by passing the [route] to the [WTAApi].
     * @param route [Route]
     */
    fun displayRoute(route : Route) {
        viewModelScope.launch {
            try {
                val rtPatterns : MutableList<RoutePattern> = WTAApi.getRoutePatterns(route.routeNum)?.toMutableList() ?: mutableListOf<RoutePattern>()
                val r = Route(
                    routeNum = route.routeNum,
                    name = route.name,
                    color = route.color,
                    pattern = rtPatterns
                )
                _uiState.update { it.copy(displayedRoute = r) }
                Log.d("@@@", "displayedRoute")
                updatePolyLine(r)
                zoomRoute()
            } catch (e : Exception) {
                Log.d("@@@","There was an issue $e")
            }
        }

    }

    /**
     * Creates a bounds builder to include the points from the [TransitUiState.polylineOptions] and
     * creates a [LatLngBounds.builder] that will trigger a zoom. This zoom must be done with a
     * [CameraPositionState], so [TransitUiState.moveCamera] is set to true.
     */
    private fun zoomRoute() {
        val points : List<LatLng> = uiState.value.polylineOptions?.points as List<LatLng>
        val boundsBuilder = LatLngBounds.builder()
        for (point in points) {
            boundsBuilder.include(point)
        }
        val bounds = boundsBuilder.build()
        _uiState.update { it.copy(latLongBounds = bounds) }
        _uiState.update { it.copy(moveCamera = true) }
    }


    /**
     * Updates the viewModel's [TransitUiState.cameraPosition] using [position]
     * @param position [CameraPosition]
     */
    fun updateCameraPosition(position: CameraPosition) {
        _uiState.update { it.copy(cameraPosition = position) }
    }

    /**
     * Updates the viewModel's [TransitUiState.markers] from [stop]
     * @param stop [StopObject]
     */
    fun addMarker(stop : StopObject) {
        val marker = MarkerState(LatLng(stop.lat.toDouble(), stop.long.toDouble()))
        val mOptions = markerOptions { MarkerOptions()
            .position(LatLng(stop.lat.toDouble(), stop.long.toDouble()))
            .title(stop.name)
            .anchor(0.5f, 0.5f)
            .icon(getIcon())
            .flat(true)
        }
        _uiState.update { it.copy(markers = it.markers.plus(mutableMapOf(marker to mOptions)).toMutableMap()) }
    }


    /**
     * Updates the viewModel's [TransitUiState.markers] from [latLng]
     * @param latLng [LatLng]
     */
    fun addMarker(latLng: LatLng) {
        val marker = MarkerState(latLng)
        val mOptions = markerOptions { MarkerOptions()
            .position(latLng)
            .title("Marker!")
            .anchor(0.5f, 0.5f)
            .icon(getIcon())
            .flat(true)
        }
        _uiState.update { it.copy(markers = it.markers.plus(mutableMapOf(marker to mOptions)).toMutableMap()) }
    }

    /**
     * Updates the viewModel's [TransitUiState.selectedMarker] with [marker].
     * @param marker [MarkerState]
     */
    fun selectMarker(marker: MarkerState) {
        _uiState.update { it.copy(selectedMarker = marker) }
    }

    /**
     * Updates the viewModel's [TransitUiState.selectedMarker] by setting it to null.
     */
    fun deselectMarker() {
        _uiState.update { it.copy(selectedMarker = null) }
    }

    /**
     * Updates the viewModel's [TransitUiState.polylineOptions] from changes in [r]\
     * @param r [Route]
     */
    private fun updatePolyLine(r : Route) {
        val points : MutableList<LatLng> = mutableListOf<LatLng>()
        r.pattern?.forEach { routePattern ->
            routePattern.pt.forEach { patternObj ->
                points.add(LatLng(patternObj.lat.toDouble(), patternObj.long.toDouble()))
            }
        }
        _uiState.update { it ->
            it.copy(polylineOptions =
                polylineOptions { PolylineOptions()
                    .addAll(points)
                    .color(Color(android.graphics.Color.parseColor((it.displayedRoute?.color ?: Color.Black).toString())).toArgb())
                    .clickable(true)
                }
            )
        }
    }
}

@Composable
fun TransitMap(viewModel: TransitViewModel = TransitViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val scope : CoroutineScope = rememberCoroutineScope()



    LaunchedEffect(true) {
        viewModel.getRoutes()
        var route = Route(
            routeNum = 1,
            name = "Fairhaven&Downtown",
            color = "#ff0000",
            pattern = null
        )
        viewModel.displayRoute(route)
        // TODO()
        // viewModel.getStops()
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

    GoogleMap(
        properties = mapProperties,
        uiSettings = mapUiSettings,
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
//        onMapLoaded = { isLoaded = true },
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

        // Render Selected Marker
        uiState.selectedMarker?.let { selected ->
            val camPos = CameraPosition.fromLatLngZoom(selected.position, 10f)
            viewModel.updateCameraPosition(camPos)
            Box (
                modifier = Modifier,
                contentAlignment = Alignment.BottomCenter
            ) {
                Text(text = "Hi")
            }
        }

        uiState.polylineOptions?.let { polylineOptions ->
            Polyline(
                points = polylineOptions.points,
                color = Color.hsl(
                    hue = polylineOptions.color.toFloat(),
                    saturation = 1f,
                    lightness = 0.5f,
                )
            )

        }
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