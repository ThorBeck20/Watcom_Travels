package com.example.watcomtravels

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var stops: MutableList<StopObject> = remember { mutableStateListOf<StopObject>()}
            var loaded by remember { mutableStateOf(false) }
            val currentLocation = 1
            val bham = LatLng(48.73, -122.49)

            val favTrips = dbTrips(this)
            val favStops = dbStops(this)
            val recents = dbRecent(this)

            LaunchedEffect(true) {
                withContext(Dispatchers.IO) {
                    stops.addAll(WTAApi.getStopObjets())
                    loaded = true
                    Log.d("@@@", "STOPS LOADED")
                }
            }

            val mapComposable = @Composable { CoolMap(bham, stops) }

            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                MainActivity_BottomSheet(mapComposable)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainActivity_BottomSheet(scaffoldContent : @Composable () -> Unit){
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState()

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 256.dp,
        sheetShadowElevation = 24.dp,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Whatcom Travels") }
            )
        },
        sheetContent = {
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(128.dp)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center,

                    ) {
                    Text("Swipe up to expand sheet. It will automatically expand to fit the content within it")
                }
                Text("Sheet content")
                Button(
                    modifier = Modifier
                        .padding(bottom = 64.dp),
                    onClick = {
                        scope.launch {
                            scaffoldState.bottomSheetState.partialExpand()
                        }
                    }
                ) {
                    Text("Click to collapse sheet")
                }

            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            scaffoldContent.invoke()
        }
    }
}



@Composable
fun CoolMap(startingLocation: LatLng, stopList : MutableList<StopObject>) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(startingLocation, 15f)
    }
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        var numstops = 0
        stopList.forEach { stop ->
            val pos = LatLng(stop.lat.toDouble(), stop.long.toDouble())
            Marker(
                state = rememberMarkerState(position = pos),
                title = stop.name
            )
            numstops+=1

        }
        Log.d("@@@", "stops: $numstops")
    }
}