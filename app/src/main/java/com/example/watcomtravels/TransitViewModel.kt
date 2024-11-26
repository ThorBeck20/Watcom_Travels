package com.example.watcomtravels

import android.content.Context
import android.content.res.Resources
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.ktx.model.markerOptions
import com.google.maps.android.ktx.model.polylineOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.nio.file.Path

data class TransitUiState(
    val context : Context? = null,
    val cameraPosition: CameraPosition = CameraPosition.fromLatLngZoom(LatLng(48.769768, -122.485886), 11f),
    val markers : MutableMap<MarkerState, MarkerOptions> = mutableMapOf<MarkerState, MarkerOptions>(),
    val selectedMarker : MarkerState? = null,
    val polylineOptions: PolylineOptions? = null,
    val routes : MutableList<Route> = mutableListOf<Route>(),
    val displayedRoute : Route? = null,
    val moveCamera : Boolean = false,
    val latLongBounds : LatLngBounds? = null,
    val isLoaded : Boolean = false
)

class TransitViewModel(context: Context) : ViewModel() {
    private val _uiState = MutableStateFlow(TransitUiState(context))
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
        val path : Path = Path.of("res/drawable/busicon.jpg")
        val iconBitmap = resourceToScaledBitMap(path,8)
        val markerIcon = iconBitmap?.let { BitmapDescriptorFactory.fromBitmap(it) }
        val marker = MarkerState(latLng)
        val mOptions = markerOptions { MarkerOptions()
            .position(latLng)
            .title("Marker!")
            .anchor(0.5f, 0.5f)
            .icon(markerIcon)
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

    /**
     * Updates the [TransitUiState.isLoaded] to true
     */
    fun loaded() {
        _uiState.update { it.copy(isLoaded = true) }
    }
}