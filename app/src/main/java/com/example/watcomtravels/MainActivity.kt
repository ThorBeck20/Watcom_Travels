package com.example.watcomtravels

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private lateinit var fusedLocationClient: FusedLocationProviderClient
var showStopInfo by mutableStateOf<StopObject?>(null)
val showRouteInfo by mutableStateOf<Route?>(null)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        super.onCreate(savedInstanceState)
        setContent {
            var stops: MutableList<StopObject> = remember { mutableStateListOf<StopObject>()}
            var loaded by remember { mutableStateOf(false) }
            val currentLocation = 1
            val bham = LatLng(48.73, -122.49)

            val favTrips = dbTrips(this)
            val favStops = dbStops(this)
            val recents = dbRecent(this)

            LaunchedEffect(Unit) {
                withContext(Dispatchers.IO) {
                    val fetchedStops = WTAApi.getStopObjets()
                    withContext(Dispatchers.Main){
                        fetchedStops?.let { stops.addAll(it) }
                        loaded = true
                        Log.d("@@@", "STOPS LOADED")
                    }

                }
            }

            val transitViewModel = TransitViewModel(context = this@MainActivity)
            val uiState by transitViewModel.uiState.collectAsState()

            val mapComposable = @Composable { TransitMap(transitViewModel) }
            var location: LatLng? = null

            LaunchedEffect(true) {
                withContext(Dispatchers.IO){
                    location = getLastKnownLocation()
                }
            }

            val defaultStops = getDefaultStops()
            val nearbyStops: MutableList<StopObject>? = getNearbyStops(stops, location)

            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                if(nearbyStops != null){
                    LandscapeUI(mapComposable, nearbyStops)
                }else{
                    LandscapeUI(mapComposable, defaultStops)
                }

            } else {
                if(nearbyStops != null){
                    PortraitUI(mapComposable, nearbyStops, "Nearby stops")
                }else{
                    PortraitUI(mapComposable, defaultStops, "Popular stops")
                }
            }


        }
    }
    fun denDix(dp : Int) : Int {
        return (dp * resources.displayMetrics.density).toInt()
    }


    @Composable
    //a short list of default stops to display if nearby are not available
    private fun getDefaultStops(): MutableList<StopObject> {
        val defaultList: MutableList<StopObject> = remember { mutableStateListOf<StopObject>()}

        val bhamStation = StopObject(id = 597, name = "Bellingham Station", lat = 48.75039.toFloat(), long = (-122.475612).toFloat(), stopNum = 2001)
        defaultList.add(bhamStation)

        val cordataStation = StopObject(704, "Cordata Station", 48.79293100097.toFloat(),
            (-122.491083399237).toFloat(), 2000)
        defaultList.add(cordataStation)

        val vikingUnion = StopObject(914, "High St at Viking Union", 48.738656.toFloat(),
            (-122.48557).toFloat(), 2052)
        defaultList.add(vikingUnion)

        val bellisFair = StopObject(598, "Bellis Fair Pkwy at Macy's", 48.787738.toFloat(),
            (-122.488511).toFloat(), 3438)
        defaultList.add(bellisFair)

        return defaultList

    }

    private suspend fun getLastKnownLocation(): LatLng? {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                2
            )
            return null // Return null due to missing permissions
        }

        return suspendCoroutine { continuation ->
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        continuation.resume(LatLng(location.latitude, location.longitude))
                    } else {
                        continuation.resume(null)
                    }
                }
                .addOnFailureListener {
                    continuation.resume(null) // Handle failure case
                }
        }
    }


}



@Composable
private fun getNearbyStops(allStops: MutableList<StopObject>, location: LatLng?): MutableList<StopObject>? {

    if(location == null) {
        return null
    }

    val maxDistance = 20
    val nearbyStops: MutableList<StopObject> = remember { mutableStateListOf<StopObject>()}

    for(stop in allStops){
        if(((stop.lat - location.latitude) < maxDistance) && ((stop.long - location.longitude) < maxDistance)){
            nearbyStops.add(stop)
        }
    }

    return nearbyStops
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PortraitUI(mapComposable: @Composable () -> Unit, stops: MutableList<StopObject>, stopType: String) {

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        val scope = rememberCoroutineScope()
        val scaffoldState = rememberBottomSheetScaffoldState()
        val searchText = rememberSaveable { mutableStateOf("") }

        if (showStopInfo != null) {
            StopInfoPage(showStopInfo!!)
        } else if (showRouteInfo != null) {

        } else {
            BottomSheetScaffold(
                scaffoldState = scaffoldState,
                sheetPeekHeight = 256.dp,
                sheetShadowElevation = 24.dp,
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Row {

                                TextField(
                                    value = searchText.value,
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    onValueChange = {
                                        searchText.value = it
                                    },
                                    placeholder = {
                                        Text("Where to?")
                                    },
                                    shape = RoundedCornerShape(50.dp),
                                    singleLine = true

                                )

                            }

                        }
                    )
                },
                sheetContent = {
                    Column(
                        Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        StopRow(stops, stopType)

                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {


                    mapComposable.invoke()

                }
            }

        }

    }
}

@Composable
fun StopRow(stopList: MutableList<StopObject>, stopType: String) {

    Column(
        modifier = Modifier
            .padding(16.dp)
    ){
        Text(
            stopType,
            fontSize = 32.sp,
            textAlign = TextAlign.Left,
            fontWeight = FontWeight.SemiBold
        )

        val scrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .horizontalScroll(scrollState)
                .padding(16.dp)
                .fillMaxWidth()
        ) {

            for(stop in stopList){
                StopCard(stop)
                Spacer(
                    modifier = Modifier
                        .size(16.dp, 100.dp)
                )
            }
        }
    }

}

@Composable
fun StopCard(stop: StopObject) {

    Box(
        modifier = Modifier
            .size(width = 150.dp, height = 100.dp)
            .background(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(20)).border(width = 2.dp, color= MaterialTheme.colorScheme.primaryContainer , shape = RoundedCornerShape(20))
            .padding(16.dp)
            .clickable(
                enabled = true,
                onClick = {
                    showStopInfo = stop
                    Log.d("@@@", "stop clicked!!")
                }
            ),
        contentAlignment = Alignment.Center,



    ){

            stop.name?.let {
                Text(
                    it,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold
                )
            }


    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LandscapeUI(mapComposable: @Composable () -> Unit, stops: MutableList<StopObject>?) {

    val searchText = rememberSaveable { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxSize()
    ) {
        if (showStopInfo != null) {
            StopInfoPage(showStopInfo!!)
        } else if (showRouteInfo != null) {

        } else {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Row {
                                TextField(
                                    value = searchText.value,
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    onValueChange = {
                                        searchText.value = it
                                    },
                                    placeholder = {
                                        Text("Where to?")
                                    },
                                    shape = RoundedCornerShape(50.dp),
                                    singleLine = true

                                )

                            }
                        }
                    )
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier.padding(innerPadding)
                ) {

                }

            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StopInfoPage(stop: StopObject) {

    val scaffoldState = rememberBottomSheetScaffoldState()


    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 256.dp,
        sheetShadowElevation = 24.dp,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically

                    ){
                        Text(
                            stop.name!!,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.SemiBold
                        )
                    }


                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            showStopInfo = null
                        },
                        content = {
                          Icon(
                              Icons.AutoMirrored.Filled.ArrowBack,
                              "Back",
                              tint = MaterialTheme.colorScheme.primary
                          )
                        }
                    )
                },
                colors = TopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent,
                    actionIconContentColor = Color.Transparent

                ),


            )

        },
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    "Stop Information",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 32.sp,
                    modifier = Modifier.padding(4.dp)
                )
                Text(
                    stop.name!!,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(4.dp),
                    fontSize = 24.sp,

                )
            }

        }
    ) { innerPadding ->


    }
}






