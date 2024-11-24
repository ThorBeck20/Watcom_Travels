package com.example.watcomtravels

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberMarkerState

/**
 * This is a google maps composable, which creates Icons from a list of stopObjects
 */


@Composable
@GoogleMapComposable
fun StopMarkersMapContent (
    stopList: MutableList<StopObject>
) {

    // This image is way too big
    val icon = BitmapDescriptorFactory.fromResource(R.drawable.file)

    stopList.forEach { stop ->
        val pos = LatLng(stop.lat.toDouble(), stop.long.toDouble())
        Marker(
            state = rememberMarkerState(position = pos),
            title = stop.name,
            tag = stop,
//            onClick = { marker ->
//                onStopClick(marker)
//                false
//            },
            anchor = Offset(0.5f, 0.5f),
            icon = icon
        )
    }
}