package com.example.watcomtravels

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.ktx.model.markerOptions
import com.google.maps.android.ktx.model.polylineOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext



data class TransitUiState(
    val context: Context? = null,
    val dbSearch: dbSearch,
    val dbStops: dbStops,
    val dbRoutes: dbRoutes,
    val cameraPosition: CameraPosition = CameraPosition.fromLatLngZoom(LatLng(48.769768, -122.485886), 11f),
    val displayedMarkers: MutableMap<MarkerState, MarkerOptions> = mutableMapOf<MarkerState, MarkerOptions>(),
    val selectedMarker: MarkerState? = null,
    val userMarker: Pair<MarkerState, MarkerOptions>? = null,
    val polylineOptions: PolylineOptions? = null,
    val routes: MutableList<Route> = mutableListOf<Route>(),
    val displayedRoute: Route? = null,
    val moveCamera: Boolean = false,
    val latLongBounds: LatLngBounds? = null,
    val isLoaded: Boolean = false
)

class TransitViewModel(context: Context, searchDb: dbSearch, stopsDb: dbStops, routesDb : dbRoutes) : ViewModel() {
    var _uiState = MutableStateFlow(TransitUiState(context, searchDb, stopsDb, routesDb))
    val uiState : StateFlow<TransitUiState> get() = _uiState

    var selectedRoute by mutableStateOf<Route?>(null)
        private set

    fun updateSelectedRoute(route: Route) {
        selectedRoute = route
        Log.d("TVM @@@", "Selected route updated: $selectedRoute")
    }

    /**
     *
     */

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
     * Removes the displayed [Route]
     * -- WARNING --
     * This function sets:
     *  -   [TransitUiState.displayedRoute] to null
     *  -   [TransitUiState.polylineOptions] to null
     *  -   [TransitUiState.displayedMarkers] to be an empty mutable map
     */
    fun rmDisplayedRoute() {
        _uiState.update {
            it.copy(
                displayedRoute = null,
                polylineOptions = null,
                displayedMarkers = mutableMapOf<MarkerState, MarkerOptions>()
            )
        }
    }

    /**
     * Updates the viewModel's [TransitUiState.displayedRoute] by passing the [route] to the [WTAApi].
     * @param route [Route]
     */
    fun displayRoute(route : Route) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val rtPatterns : RoutePattern? = WTAApi.getRoutePatterns(route.routeNum)
                val r = Route(
                    routeNum = route.routeNum,
                    name = route.name,
                    color = route.color,
                    pattern = rtPatterns
                )
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(displayedRoute = r) }
                    Log.d("@@@", "displayedRoute")
                    updatePolyLine(r)
                    zoomRoute()
                }
            } catch (e : Exception) {
                Log.d("@@@","There was an issue $e")
            }
        }

    }

    /**
     * Used to determine if the camera needs to be moved. calls [TransitUiState.moveCamera]
     * @return b [Boolean]
     */
    fun shouldMoveCamera() : Boolean {
        var b = uiState.value.moveCamera
        return b
    }

    /**
     * calls [TransitViewModel.addMarker] and then [TransitViewModel.deselectMarker],
     * [TransitViewModel.selectMarker]
     * [StopObject]
     * @param stopNum [StopObject]
     */
    fun displayStop(stop: StopObject) {
        val markerState = addMarker(stop)
        deselectMarker()
        selectMarker(markerState)
        Log.d("@@@", "displayedStop")
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
     * Updates the [TransitUiState.latLongBounds] and sets [TransitUiState.moveCamera] to true
     * Creates a bounds builder to include the points from the [TransitUiState.displayedMarkers] and
     * creates a [LatLngBounds.builder] that will trigger a zoom. This zoom must be done within a
     * composable that saves the [CameraPositionState].
     */
    private fun zoomMarkers() {
        val displayedMarkers : MutableMap<MarkerState, MarkerOptions> = uiState.value.displayedMarkers
        val boundsBuilder = LatLngBounds.builder()

        displayedMarkers.forEach { (markerState, mOptions) ->
            val pos = markerState.position
            boundsBuilder.include(pos)
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
     * Updates the viewModel's [TransitUiState.displayedMarkers] from [stop]
     * @param stop [StopObject]
     * @return markerState [MarkerState]
     */
    fun addMarker(stop : StopObject) : MarkerState {
        //TODO Check for stop in DB before calling API

        val iconBitmap = resourceToScaledBitMap(R.drawable.busmarker,8)
        val markerIcon = iconBitmap?.let { BitmapDescriptorFactory.fromBitmap(it) }
        val markerState = MarkerState(LatLng(stop!!.lat.toDouble(), stop.long.toDouble()))
        val mOptions = markerOptions { MarkerOptions()
            .position(LatLng(stop.lat.toDouble(), stop.long.toDouble()))
            .title(stop.name)
            .anchor(0.5f, 0.5f)
            .icon(markerIcon)
            .flat(true)
        }
        _uiState.update { it.copy(displayedMarkers = it.displayedMarkers.plus(mutableMapOf(markerState to mOptions)).toMutableMap()) }
        return markerState
    }

    /**
     * Updates the viewModel's [TransitUiState.displayedMarkers] from [stopNum]
     * @param stopNum [Int]
     * @return markerState [MarkerState]
     */
    fun addMarker(stopNum : Int) : MarkerState? {
        var stop = uiState.value.dbSearch.getSearch(stopNum)
        if (stop == null) {
            stop = WTAApi.getStop(stopNum)
        }
        if (stop == null) {
            Log.d("Transit View Model", "Could not find stop in database or API")
            return null
        }

        val iconBitmap = resourceToScaledBitMap(R.drawable.busmarker,8)
        val markerIcon = iconBitmap?.let { BitmapDescriptorFactory.fromBitmap(it) }
        val markerState = MarkerState(LatLng(stop.lat.toDouble(), stop.long.toDouble()))
        val mOptions = markerOptions { MarkerOptions()
            .position(LatLng(stop.lat.toDouble(), stop.long.toDouble()))
            .title(stop.name)
            .anchor(0.5f, 0.5f)
            .icon(markerIcon)
            .flat(true)
        }
        _uiState.update { it.copy(displayedMarkers = it.displayedMarkers.plus(mutableMapOf(markerState to mOptions)).toMutableMap()) }
        return markerState
    }


    /**
     * Updates the viewModel's [TransitUiState.displayedMarkers] from [latLng]
     * @param latLng [LatLng]
     * @return markerState [MarkerState]
     */
    fun addMarker(latLng: LatLng) : MarkerState {
        val iconBitmap = resourceToScaledBitMap(R.drawable.busmarker,8)
        val markerIcon = iconBitmap?.let { BitmapDescriptorFactory.fromBitmap(it) }
        val markerState = MarkerState(latLng)
        val mOptions = markerOptions { MarkerOptions()
            .position(latLng)
            .title("Marker!")
            .anchor(0.5f, 0.5f)
            .icon(markerIcon)
            .flat(true)
        }
        _uiState.update { it.copy(displayedMarkers = it.displayedMarkers.plus(mutableMapOf(markerState to mOptions)).toMutableMap()) }
        return markerState
    }

    /**
     * Displays a marker of the users current location using [latLng]
     * @param latLng [LatLng]
     * @return markerState [MarkerState]
     */
    fun displayUser(latLng: LatLng) : MarkerState {
        val markerState = MarkerState(latLng)
        val mOptions = markerOptions { MarkerOptions()
            .position(latLng)
            .title("Marker!")
            .anchor(0.5f, 0.5f)
            .flat(true)
        }
        val userMarkerPair = Pair<MarkerState, MarkerOptions>(markerState, mOptions)
        _uiState.update { it.copy(userMarker = userMarkerPair) }
        return markerState
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
     * Updates the viewModel's [TransitUiState.polylineOptions] from changes in [r]
     * Also displays the passed [Route]'s [StopObject] dependent on the parameter [displayStops]
     *
     * @param r [Route]
     * @param displayStops [Boolean]
     */
    private fun updatePolyLine(r : Route, displayStops : Boolean = true) {
        val points : MutableList<LatLng> = mutableListOf<LatLng>()
        val pLOptions = polylineOptions { PolylineOptions()
            .color(Color(android.graphics.Color.parseColor((r.color))).toArgb())
            .clickable(false)
        }
        r.pattern?.pt?.forEach { patternObj ->
            if (displayStops) {
                patternObj.stop?.let { addMarker(it) }
            }
                val latLng = LatLng(patternObj.lat.toDouble(), patternObj.long.toDouble())
                points.add(latLng)
                pLOptions.add(latLng)
            }

        _uiState.update { it -> it.copy(polylineOptions = pLOptions) }
    }

    /**
     * Updates the [TransitUiState.isLoaded] to true
     */
    fun loaded() {
        _uiState.update { it.copy(isLoaded = true) }
    }

    /**
     * @return [TransitUiState.isLoaded]
     */
    fun isLoaded() : Boolean {
        return _uiState.value.isLoaded
    }
}