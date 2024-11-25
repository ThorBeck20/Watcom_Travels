package com.example.watcomtravels

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
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
    stopList: MutableList<StopObject>,
    resource: Resources
) {

    var iconBitmap = reScaleResource(resource = resource, R.drawable.busmarker, 8)

    // This image is way too big
    val icon = BitmapDescriptorFactory.fromBitmap(iconBitmap)

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

fun reScaleResource(resource : Resources, id : Int, scalar : Int = 1) : Bitmap {
    var bitmap : Bitmap
    var bitmapOptions = BitmapFactory.Options()
    bitmapOptions.inJustDecodeBounds = true

    BitmapFactory.decodeResource(resource, id, bitmapOptions)

    if (bitmapOptions.outHeight >= 50) {
        Log.d("@@@", "Outheight = ${bitmapOptions.outHeight}")
    }

    var bitmapOptions_resized = BitmapFactory.Options()
    bitmapOptions_resized.inSampleSize = scalar
    bitmap = BitmapFactory.decodeResource(resource, id, bitmapOptions_resized)
    return bitmap
}
