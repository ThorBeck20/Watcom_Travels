package com.example.watcomtravels

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberMarkerState
import java.nio.file.Path
import kotlin.coroutines.coroutineContext

/**
 * This is a google maps composable, which creates Icons from a list of stopObjects
 */


@Composable
@GoogleMapComposable
fun StopMarkersMapContent (
    stopList: MutableList<StopObject>?,
    resource: Resources
) {

    val path : Path = Path.of("res/drawable")
    var stopIconBitmap = resourceToScaledBitMap(path, 8)
    val stopIcon = stopIconBitmap?.let { BitmapDescriptorFactory.fromBitmap(it) }

    stopList?.forEach { stop ->
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
            icon = stopIcon
        )
    }
}


