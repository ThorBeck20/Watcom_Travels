package com.example.watcomtravels

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberDrawerState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private lateinit var fusedLocationClient: FusedLocationProviderClient
var showStopInfo by mutableStateOf<StopObject?>(null)
var showRouteInfo by mutableStateOf<Route?>(null)
var showSettings by mutableStateOf(false)
var showFavorites by mutableStateOf(false)

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
                    val fetchedStops = WTAApi.getStopObjects()
                    withContext(Dispatchers.Main){
                        fetchedStops?.let { stops.addAll(it) }
                        loaded = true
                        Log.d("@@@", "STOPS LOADED: ${stops.size}")
                    }

                }
            }

            val transitViewModel = TransitViewModel(context = this@MainActivity)
            val uiState by transitViewModel.uiState.collectAsState()

            val mapComposable = @Composable { TransitMap(transitViewModel, transitViewModel.selectedRoute) }
            var location: LatLng? = null

            LaunchedEffect(true) {
                withContext(Dispatchers.IO){
                    location = getLastKnownLocation()
                }
            }

            val defaultStops = getDefaultStops()
            val nearbyStops: MutableList<StopObject>? = getNearbyStops(stops, location)
            val items =
                listOf(
                    Icons.Default.Settings,
                    Icons.Default.Favorite
                )
            val selectedItem = remember { mutableStateOf(items[0]) }
            val drawerState = rememberDrawerState(DrawerValue.Closed)

            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(drawerState){
                        Column(

                        ){
                            Spacer(
                                modifier = Modifier
                                    .height(4.dp)
                            )

                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.Settings, contentDescription = null)},
                                label = { Text("Settings") },
                                selected = Icons.Default.Settings == selectedItem.value,
                                onClick = {
                                    showSettings = true
                                          },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )

                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.Favorite, contentDescription = null)},
                                label = { Text("Favorites") },
                                selected = Icons.Default.Favorite == selectedItem.value,
                                onClick = {
                                    showFavorites = true
                                },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )

                        }
                    }
                },
                content = {

                    if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        if(nearbyStops != null){
                            LandscapeUI(mapComposable, nearbyStops, drawerState)
                        }else{
                            LandscapeUI(mapComposable, defaultStops, drawerState)
                        }

                    } else {
                        if(nearbyStops != null){
                            PortraitUI(mapComposable, nearbyStops, "Nearby stops", transitViewModel, drawerState)
                        }else{
                            PortraitUI(mapComposable, defaultStops, "Popular stops", transitViewModel, drawerState)
                        }
                    }
                }
            )




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
private fun PortraitUI(
    mapComposable: @Composable () -> Unit,
    stops: MutableList<StopObject>,
    stopType: String,
    transitViewModel: TransitViewModel,
    drawerState: DrawerState
) {
    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        val scope = rememberCoroutineScope()
        val scaffoldState = rememberBottomSheetScaffoldState()
        val searchText = rememberSaveable { mutableStateOf("") }

        if (showStopInfo != null) {
            StopInfoPage(showStopInfo!!)
        } else if (showRouteInfo != null) {
            RouteInfoPage(showRouteInfo!!, mapComposable)
        }else if (showSettings){
            LaunchedEffect(Unit){
                scope.launch {
                    drawerState.close()
                }
            }
            SettingsPage()
        }else if(showFavorites){
            LaunchedEffect(Unit){
                scope.launch {
                    drawerState.close()
                }
            }

            FavoritesPage()
        } else {

                val scrollState = rememberScrollState()
                BottomSheetScaffold(
                    scaffoldState = scaffoldState,
                    sheetPeekHeight = 300.dp,
                    sheetShadowElevation = 24.dp,
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Row(

                                ) {

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

                            },
                            navigationIcon = {
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            drawerState.open()
                                        }
                                    },
                                    content = {
                                        Icon(
                                            Icons.Filled.Menu,
                                            "Menu",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                )
                            }
                        )
                    },
                    sheetContent = {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .verticalScroll(scrollState),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            RoutesMain(transitViewModel)
                            StopRow(stops, stopType)
                            BulletinsMain()

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LandscapeUI(
    mapComposable: @Composable () -> Unit,
    stops: MutableList<StopObject>?,
    drawerState: DrawerState
) {

    val searchText = rememberSaveable { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxSize()
    ) {
        if (showStopInfo != null) {
            StopInfoPage(showStopInfo!!)
        } else if (showRouteInfo != null) {
            RouteInfoPage(showRouteInfo!!, mapComposable)
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
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(20)
            )
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(20)
            )
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
fun StopInfoPage(stop: StopObject) {

    val scaffoldState = rememberBottomSheetScaffoldState()

    val scrollState = rememberScrollState()
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
                    .verticalScroll(scrollState)
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

                Text(
                    "Stop number: ${stop.stopNum}",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(4.dp),
                    fontSize = 24.sp,
                )


                val predictions = remember { mutableStateListOf<Prediction>() }
                LaunchedEffect(Unit) {
                    withContext(Dispatchers.IO) {
                        val fetchedPredictions = WTAApi.getPredictions(stop.stopNum)
                        withContext(Dispatchers.Main) {
                            fetchedPredictions?.let { predictions.addAll(it) }

                        }
                    }
                }

                Text(
                    "Upcoming",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 32.sp,
                    modifier = Modifier.padding(4.dp)
                )

                for(prediction in predictions){

                    Box(
                        modifier = Modifier
                            .wrapContentSize()
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(20)
                            )
                            .border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(20)
                            )
                            .padding(8.dp)
                            .clickable(
                                enabled = true,
                                onClick = {
                                    //showRouteInfo = route
                                }
                            )
                    ){
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                        ){
                            Text(
                                prediction.bus,
                                textAlign = TextAlign.Left,
                                modifier = Modifier
                                    .padding(4.dp)
                                    .wrapContentWidth(Alignment.Start),
                                fontSize = 24.sp,
                            )
                            Text(
                                "${if (prediction.hour.toInt() > 12) (prediction.hour.toInt() - 12).toString() else prediction.hour
                                }:${prediction.min}",
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .padding(4.dp)
                                    .wrapContentWidth(Alignment.End),
                                fontSize = 24.sp,
                            )
                        }

                    }

                    Spacer(
                        modifier = Modifier
                            .height(4.dp)
                    )

                }

                val bulletins = remember { mutableStateListOf<ServiceBulletin>() }
                LaunchedEffect(Unit) {
                    withContext(Dispatchers.IO) {
                        val fetchedBulletins = WTAApi.getBulletins(stop.stopNum)
                        withContext(Dispatchers.Main) {
                            fetchedBulletins.let { bulletins.addAll(it) }

                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text("Service Bulletins",
                        fontSize = 32.sp,
                        textAlign = TextAlign.Left,
                        fontWeight = FontWeight.SemiBold
                    )

                    Column {
                        if(bulletins.size == 0){
                            Text(
                                "No bulletins at this time",
                                fontSize = 20.sp,
                                textAlign = TextAlign.Left
                            )
                        }
                        for (bulletin in bulletins) {
                            Box(
                                modifier = Modifier
                                    .wrapContentHeight()
                                    .fillMaxWidth()
                                    .background(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(20)
                                    )
                                    .border(
                                        width = 2.dp,
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(20)
                                    )
                                    .padding(16.dp)
                                    .clickable(
                                        enabled = true,
                                        onClick = {

                                        }
                                    )
                            ){
                                Column(){
                                    Text(
                                        "Priority: ${bulletin.priority}",
                                        fontSize = 20.sp
                                    )
                                    Text(
                                        "Subject: ${bulletin.subject}",
                                        fontSize = 20.sp
                                    )
                                    Text(
                                        "Effect: ${bulletin.effect}",
                                        fontSize = 20.sp
                                    )
                                    Text(
                                        bulletin.brief,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        "Services effected: ",
                                        fontSize = 20.sp
                                    )
                                    DisplayServiceBulletinInfo(bulletin.service)
                                }

                            }
                            Spacer(
                                modifier = Modifier
                                    .height(4.dp)
                            )
                        }
                    }




                }
            }

        }
    ) { innerPadding ->


    }
}

@Composable
fun RoutesMain(transitViewModel: TransitViewModel) {
    val routes = remember { mutableStateListOf<Route>() }
    var mExpanded by remember { mutableStateOf(false) }
    var mSelectedText by rememberSaveable { mutableStateOf(transitViewModel.selectedRoute?.name ?: "Select a route") }

    LaunchedEffect(transitViewModel.selectedRoute) {
        mSelectedText = transitViewModel.selectedRoute?.name ?: "Select a route"
    }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val fetchedRoutes = WTAApi.getRoutes()
            Log.d("@@@", "Fetched Routes: ${fetchedRoutes?.size ?: 0}")
            withContext(Dispatchers.Main) {
                fetchedRoutes?.let { routes.addAll(it) }
                Log.d("@@@", "Routes LOADED: ${routes.size}")
            }
        }
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Text(
            "Routes",
            fontSize = 32.sp,
            textAlign = TextAlign.Left,
            fontWeight = FontWeight.SemiBold
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .wrapContentSize(Alignment.TopStart)
        ) {
            Text(
                mSelectedText,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        mExpanded = true
                        Log.d("@@@", "Dropdown Triggered")
                    }
                    .background(Color.LightGray)
                    .padding(8.dp),
                fontSize = 20.sp
            )

            DropdownMenu(
                expanded = mExpanded,
                onDismissRequest = { mExpanded = false },
                modifier = Modifier.wrapContentHeight()
            ) {
                if (routes.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No routes available") },
                        onClick = {}
                    )
                } else {
                    routes.forEach { route ->
                        DropdownMenuItem(
                            text = { Text("${route.routeNum} ${route.name}", fontSize = 18.sp) },
                            onClick = {
                                mSelectedText = route.name
                                transitViewModel.updateSelectedRoute(route)
                                mExpanded = false
                                Log.d("@@@", "Route Selected: ${route.name}")
                            }
                        )
                    }
                }
            }
        }
        Spacer(
            modifier = Modifier
                .height(4.dp)
        )

        Box(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(20)
                )
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(20)
                )
                .padding(16.dp)
                .clickable(
                    enabled = true,
                    onClick = {
                        showRouteInfo = transitViewModel.selectedRoute
                        Log.d("@@@", "Route info clicked!!")
                    }
                ),
            contentAlignment = Alignment.Center,



            ){

                Text(
                    "View Route Info",
                    fontSize = 20.sp
                )


        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteInfoPage(route: Route, mapComposable: @Composable () -> Unit) {
    val scaffoldState = rememberBottomSheetScaffoldState()

    val scrollState = rememberScrollState()
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
                            "${route.routeNum} ${route.name}",
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.SemiBold
                        )
                    }


                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            showRouteInfo = null
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
                    .verticalScroll(scrollState)
            ) {
                Text(
                    "Route Information",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 32.sp,
                    modifier = Modifier.padding(4.dp)
                )
                Text(
                    "${route.routeNum} ${route.name}",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(4.dp),
                    fontSize = 24.sp,

                    )

                Spacer(
                    Modifier.height(4.dp)
                )

                val context = LocalContext.current
                val url = "https://schedules.ridewta.com/#route-details?routeNum=${route.routeNum}"
                Box(

                    modifier = Modifier
                        .wrapContentHeight()
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(20)
                        )
                        .border(
                            width = 2.dp, color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(20)
                        )
                        .padding(16.dp)
                        .clickable(
                            enabled = true,
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            }
                        )
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Text(
                            "View Route Schedule",
                            fontSize = 20.sp,
                            modifier = Modifier
                                .wrapContentWidth(Alignment.Start)
                        )

                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            "Routes Link",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .wrapContentWidth(Alignment.End)
                        )
                    }
                }

                val bulletins = remember { mutableStateListOf<ServiceBulletin>() }
                LaunchedEffect(Unit) {
                    withContext(Dispatchers.IO) {
                        val fetchedBulletins = WTAApi.getRouteBulletins(route.routeNum)
                        withContext(Dispatchers.Main) {
                            fetchedBulletins.let { bulletins.addAll(it) }

                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        "Service Bulletins",
                        fontSize = 32.sp,
                        textAlign = TextAlign.Left,
                        fontWeight = FontWeight.SemiBold
                    )

                    Column {
                        if (bulletins.size == 0) {
                            Text(
                                "No bulletins at this time",
                                fontSize = 20.sp,
                                textAlign = TextAlign.Left
                            )
                        }
                        for (bulletin in bulletins) {
                            Box(
                                modifier = Modifier
                                    .wrapContentHeight()
                                    .fillMaxWidth()
                                    .background(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(20)
                                    )
                                    .border(
                                        width = 2.dp,
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(20)
                                    )
                                    .padding(16.dp)
                                    .clickable(
                                        enabled = true,
                                        onClick = {

                                        }
                                    )
                            ) {
                                Column() {
                                    Text(
                                        "Priority: ${bulletin.priority}",
                                        fontSize = 20.sp
                                    )
                                    Text(
                                        "Subject: ${bulletin.subject}",
                                        fontSize = 20.sp
                                    )
                                    Text(
                                        "Effect: ${bulletin.effect}",
                                        fontSize = 20.sp
                                    )
                                    Text(
                                        bulletin.brief,
                                        fontSize = 16.sp
                                    )

                                }

                            }
                            Spacer(
                                modifier = Modifier
                                    .height(4.dp)
                            )
                        }
                    }
                }
            }

        }
    ) { innerPadding ->

        //mapComposable.invoke()
    }
}

@Composable
fun BulletinsMain(){

    val bulletins = remember { mutableStateListOf<ServiceBulletin>() }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val fetchedBulletins = WTAApi.getAllBulletins()
            Log.d("@@@", "Fetched Bulletins: ${fetchedBulletins?.size ?: 0}")
            withContext(Dispatchers.Main) {
                fetchedBulletins?.let { bulletins.addAll(it) }
                Log.d("@@@", "Bulletins LOADED: ${bulletins.size}")
            }
        }
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Text("Service Bulletins",
            fontSize = 32.sp,
            textAlign = TextAlign.Left,
            fontWeight = FontWeight.SemiBold
        )

        Column {
                if(bulletins.size == 0){
                    Text(
                        "No bulletins at this time",
                        fontSize = 20.sp,
                        textAlign = TextAlign.Left
                    )
                }
                for (bulletin in bulletins) {
                    Box(
                        modifier = Modifier
                            .wrapContentHeight()
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(20)
                            )
                            .border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(20)
                            )
                            .padding(16.dp)
                            .clickable(
                                enabled = true,
                                onClick = {

                                }
                            )
                    ){
                        Column(){
                            Text(
                                "Priority: ${bulletin.priority}",
                                fontSize = 20.sp
                            )
                            Text(
                                "Subject: ${bulletin.subject}",
                                fontSize = 20.sp
                            )
                            Text(
                                "Effect: ${bulletin.effect}",
                                fontSize = 20.sp
                            )
                            Text(
                                bulletin.brief,
                                fontSize = 16.sp
                            )
                            Text(
                                "Services effected: ",
                                fontSize = 20.sp
                            )
                            DisplayServiceBulletinInfo(bulletin.service)
                        }

                    }
                    Spacer(
                        modifier = Modifier
                        .height(4.dp)
                    )
                }
        }




    }

}

@Composable
fun DisplayServiceBulletinInfo(service: JSONArray) {

    val affectedServices = remember { mutableListOf<JSONObject>() }

    for (i in 0..<service.length()){
        affectedServices.add(service.getJSONObject(i))
    }

    for (item in affectedServices){
        Text("Route: ${item.getString("rt")} ${item.getString("rtdir")}",
            fontSize = 16.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage(){
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically

                    ){
                        Text(
                            "Settings",
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.SemiBold
                        )
                    }


                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            showSettings = false
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

        }
    ) { innerpadding ->

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesPage(){
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically

                    ){
                        Text(
                            "Favorites",
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.SemiBold
                        )
                    }


                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            showFavorites = false
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

        }
    ) { innerpadding ->

    }
}








