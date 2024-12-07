package com.example.watcomtravels

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.example.watcomtravels.ui.theme.AppTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private lateinit var fusedLocationClient: FusedLocationProviderClient
var showSettings by mutableStateOf(false)
var showFavorites by mutableStateOf(false)
var selectedSearch: Place? = null

var timeOption by mutableStateOf(false) // true = military, false = standard
var darkMode by mutableStateOf(false)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        super.onCreate(savedInstanceState)

        setContent {
            AppTheme (
                darkTheme = darkMode

            ){
                val stops: MutableList<StopObject> = remember { mutableStateListOf<StopObject>()}

                val searchDb = dbSearch(this)
                val stopsDB = dbStops(this)
                val routesDB = dbRoutes(this)
                val apiBool = searchDb.getAllSearches().isEmpty()


                val transitViewModel = TransitViewModel(context = this@MainActivity, searchDb, stopsDB, routesDB)
                val uiState by transitViewModel.uiState.collectAsState()

                LaunchedEffect(Unit) {
                    if (apiBool) {
                        withContext(Dispatchers.IO) {
                            val fetchedStops = WTAApi.getStopObjects()
                            transitViewModel.getRoutes()
                            transitViewModel.displayStop(fetchedStops!![0])
                            withContext(Dispatchers.Main){
                                fetchedStops?.let { stops.addAll(it) }
                                Log.d("@@@", "STOPS LOADED: ${stops.size}")

                                for (i in 0..<stops.size) {
                                    searchDb.insertSearch(stops[i])
                                }
                            }

                        }
                    } else {
                        val fetchedStops = searchDb.getAllSearches()
                        fetchedStops.let { stops.addAll(it) }
                        Log.d("@@@", "STOPS LOADED: ${stops.size}")
                    }
                }


            val mapComposable : @Composable () -> Unit = @Composable { TransitMap(transitViewModel) }
            var location: LatLng? = null

            LaunchedEffect(true) {
                withContext(Dispatchers.IO){
                    location = getLastKnownLocation()
                    val startingCameraPosition = location?.let { CameraPosition(it, 14f, 0f, 0f) }
                    if (startingCameraPosition != null) {
                        transitViewModel.updateCameraPosition(startingCameraPosition)
                    }
                }
            }

                val defaultStops = getDefaultStops()
                if(location == null){
                    location = LatLng(48.769768, -122.485886)
                }

                transitViewModel.displayUser(location!!)

                val nearbyStops: List<StopObject>? = searchDb.ltlnSearch(location!!.latitude, location!!.longitude)
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
                        ModalDrawerSheet(
                            drawerState,
                            drawerContainerColor = MaterialTheme.colorScheme.primaryContainer
                        ){
                            Column{
                                Spacer(
                                    modifier = Modifier
                                        .height(4.dp)
                                )

                                val itemColors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = MaterialTheme.colorScheme.inversePrimary,
                                    unselectedContainerColor = Color.Transparent,
                                    selectedIconColor = MaterialTheme.colorScheme.secondary,
                                    unselectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedTextColor = MaterialTheme.colorScheme.secondary,
                                    unselectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedBadgeColor = MaterialTheme.colorScheme.secondary,
                                    unselectedBadgeColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )

                                NavigationDrawerItem(
                                    icon = { Icon(Icons.Default.Settings, contentDescription = null)},
                                    label = { Text("Settings") },
                                    selected = Icons.Default.Settings == selectedItem.value,
                                    onClick = {
                                        showSettings = true
                                        showFavorites = false
                                        selectedItem.value = items[0]
                                    },
                                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                                    colors = itemColors

                                )

                                NavigationDrawerItem(
                                    icon = { Icon(Icons.Default.Favorite, contentDescription = null)},
                                    label = { Text("Favorites") },
                                    selected = Icons.Default.Favorite == selectedItem.value,
                                    onClick = {
                                        showFavorites = true
                                        showSettings = false
                                        selectedItem.value = items[1]
                                    },
                                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                                    colors = itemColors
                                )

                            }
                        }
                    },
                    content = {

                        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                            if(nearbyStops != null){
                                LandscapeUI(mapComposable, nearbyStops, "Nearby stops", transitViewModel, drawerState)
                            }else{
                                LandscapeUI(mapComposable, defaultStops, "Popular stops", transitViewModel, drawerState)
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
    }
    fun denDix(dp : Int) : Int {
        return (dp * resources.displayMetrics.density).toInt()
    }


    @Composable
    //a short list of default stops to display if nearby are not available
    private fun getDefaultStops(): MutableList<StopObject> {
        val defaultList: MutableList<StopObject> = remember { mutableStateListOf()}

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
private fun getNearbyStops(searchDb: MutableList<StopObject>, location: LatLng?): MutableList<StopObject>? {

    if(location == null) {
        return null
    }

    val maxDistance = 20
    val nearbyStops: MutableList<StopObject> = remember { mutableStateListOf()}

    for(stop in searchDb){
        if(((stop.lat - location.latitude) < maxDistance) && ((stop.long - location.longitude) < maxDistance)){
            nearbyStops.add(stop)
        }
    }

    return nearbyStops
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortraitUI(
    mapComposable: @Composable () -> Unit,
    stops: List<StopObject>,
    stopType: String,
    transitViewModel: TransitViewModel,
    drawerState: DrawerState
) {
    AppTheme(darkMode) {

        val placesClient = getPlacesClient(LocalContext.current)
        val searchResults = rememberSaveable {mutableStateOf(emptyList<Place>())}

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = MaterialTheme.colorScheme.surface
                ),

            ) {
            val scope = rememberCoroutineScope()
            val scaffoldState = rememberBottomSheetScaffoldState()
            val searchText = rememberSaveable { mutableStateOf("") }


            if (showSettings){
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
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    TextField(
                                        value = searchText.value,
                                        onValueChange = {
                                            searchText.value = it
                                        },
                                        placeholder = {
                                            Text(
                                                "Where to?",
                                                style = MaterialTheme.typography.bodyLarge)
                                        },
                                        shape = RoundedCornerShape(50.dp),
                                        singleLine = true,
                                        textStyle = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.testTag("Search Bar")

                                    )

                                    val coroutineScope = rememberCoroutineScope()
                                    val keyboardController = LocalSoftwareKeyboardController.current

                                    IconButton(
                                        onClick = {

                                            if(searchText.value != ""){

                                                coroutineScope.launch {
                                                    withContext(Dispatchers.IO) {
                                                        val fetchedResults = WTAApi.callPlacesAPI(searchText.value, placesClient!!)
                                                        withContext(Dispatchers.Main){
                                                            if (fetchedResults != null) {
                                                                searchResults.value = fetchedResults
                                                            }
                                                            Log.d("@@@", "got the search results!")
                                                        }

                                                    }
                                                }

                                            }else{
                                                searchResults.value = emptyList()
                                            }

                                            keyboardController?.hide()

                                        },
                                        content = {
                                            Icon(
                                                Icons.Default.Search,
                                                contentDescription = "search"
                                            )
                                        }
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
                                        )
                                    }
                                )
                            },
                            colors = TopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                scrolledContainerColor = MaterialTheme.colorScheme.surface,
                                navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                                actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    },
                    sheetContent = {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .verticalScroll(scrollState),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            if(searchResults.value.isNotEmpty()){
                                Log.d("@@@", "displaying search results")
                                SearchResultsList(searchResults.value)
                                for(result in searchResults.value){
                                    result.location?.let { transitViewModel.addMarker(it) }
                                }
                                RoutesMain(transitViewModel)
                                StopRow(stops, stopType)
                                BulletinsMain()
                            }else{
                                RoutesMain(transitViewModel)
                                StopRow(stops, stopType)
                                BulletinsMain()
                            }


                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface

                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        for (stop in stops) {
                            transitViewModel.addMarker(stop)
                        }
                        mapComposable.invoke()
                    }
                }
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandscapeUI(
    mapComposable: @Composable () -> Unit,
    stops: List<StopObject>,
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


        if (showSettings){
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
                sheetPeekHeight = 100.dp,
                sheetShadowElevation = 24.dp,
                modifier = Modifier.fillMaxWidth(),
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Row{

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
                    for (stop in stops) {
                        transitViewModel.addMarker(stop)
                    }
                    mapComposable.invoke()

                }
            }



        }

    }

}

@Composable
fun SearchResultsList(results: List<Place>) {

    val context = LocalContext.current

    Column(
        Modifier.fillMaxWidth()
    ) {

        Text(
            "Search Results",
            style = MaterialTheme.typography.titleLarge

        )

        for (result in results) {
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
                            selectedSearch = result
                            Log.d("@@@", "Selected search result: ${selectedSearch!!.displayName}")
                            val intent = Intent(context, SearchResultPage::class.java)
                            result.location?.let {
                                intent.putExtra("lat", it.latitude)
                                intent.putExtra("long", it.longitude)
                            }
                            intent.putExtra("display name", result.displayName)
                            context.startActivity(intent)
                        }
                    ),
                contentAlignment = Alignment.Center

            ){
                Text(
                    text = result.displayName!!,
                    Modifier
                        .fillMaxWidth()
                )
            }

            Spacer(
                modifier = Modifier
                    .height(4.dp)
            )
        }
    }
}

@Composable
fun StopRow(stopList: List<StopObject>, stopType: String) {

    Column(
        modifier = Modifier
            .padding(16.dp)
    ){
        Text(
            stopType,
            style = MaterialTheme.typography.titleLarge
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

    val context = LocalContext.current

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
                    //showStopInfo = stop
                    val intent = Intent(context, StopInfoPage::class.java)
                    intent.putExtra("stopNum", stop.stopNum)
                    intent.putExtra("time option", timeOption)
                    context.startActivity(intent)
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
            /**
             * TODO(): Get routes from DB instead.
             */
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
            style = MaterialTheme.typography.titleLarge
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
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(8.dp),
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer
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

        val context = LocalContext.current
        val toast = Toast.makeText(LocalContext.current, "Please select a route!", Toast.LENGTH_SHORT)
        Box(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(20)
                )
                .testTag("View Route Info")
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(20)
                )
                .padding(16.dp)
                .clickable(
                    enabled = true,
                    onClick = {
                        val intent = Intent(context, RouteInfoPage::class.java)
                        intent.putExtra("routeNum", transitViewModel.selectedRoute?.routeNum)
                        Log.d("@@@", "Route info clicked!!")
                        if (transitViewModel.selectedRoute != null) {
                            context.startActivity(intent)
                        } else {
                            toast.show()
                        }
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


@Composable
fun BulletinsMain(){

    val bulletins = remember { mutableStateListOf<ServiceBulletin>() }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val fetchedBulletins = WTAApi.getAllBulletins()
            Log.d("@@@", "Fetched Bulletins: ${fetchedBulletins.size}")
            withContext(Dispatchers.Main) {
                fetchedBulletins.let { bulletins.addAll(it) }
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
                    Bulletin(bulletin)
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

                            )
                        }
                    )
                },
                colors = TopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),


                )

        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {


                Text(
                    "Time Preference",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 32.sp,
                    modifier = Modifier.padding(8.dp)
                )

                Text(
                    "Choose how stop arrival times are displayed",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(8.dp)
                )

            Row(
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ){
                RadioButton(
                    selected = !timeOption,
                    onClick = {
                        timeOption = false
                    })
                Text("Standard Time")
            }

            Row(
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ){
                RadioButton(
                    selected = timeOption,
                    onClick = {
                        timeOption = true
                    })
                Text("Military Time")
            }

            Row(
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Text(
                    "Dark Mode",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 32.sp,
                    modifier = Modifier.padding(8.dp)
                )

                Switch(
                    checked = darkMode,
                    onCheckedChange = {
                        darkMode = !darkMode
                    },
                    modifier = Modifier.padding(4.dp)
                )
            }

        }

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
                            )
                        }
                    )
                },
                colors = TopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),


                )

        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val thisContext = LocalContext.current
            var input by remember{mutableStateOf("")}

            val searchDB = dbSearch(thisContext)
            val stopDB = dbStops(thisContext)
            val stops = stopDB.getAllStops()

            if (stops.isNotEmpty()) {
                Text (
                    "Favorite Stops",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 32.sp,
                    modifier = Modifier.padding(8.dp)
                )

                LazyColumn {
                    items(stops.size) { index ->
                        Row{
                            val s1 = searchDB.getSearch(stops[index])
                            if (s1 == null) {
                                // TODO: Not sure what to do in this case

                            } else {
                                Text(
                                    "${s1.name}",
                                    fontSize = 16.sp,
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .clickable {
                                            val intent =
                                                Intent(thisContext, StopInfoPage::class.java)
                                            intent.putExtra("stopNum", s1.stopNum)
                                            intent.putExtra("time option", timeOption)
                                            thisContext.startActivity(intent)
                                        }
                                )
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            Button (
                                onClick = {
                                    stopDB.deleteStop(stops[index])
                                }
                            ) {
                                Text (
                                    "Remove",
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.padding(10.dp)
            ) {
                val toastValid = Toast.makeText(thisContext, "Not a valid stop!", Toast.LENGTH_SHORT)
                val toastFav = Toast.makeText(thisContext, "Already added!", Toast.LENGTH_SHORT)

                TextField (
                    value = input,
                    onValueChange = {
                        input = it
                    },
                    label = {
                        Text ("Enter stop number")
                    }
                )

                Spacer(modifier = Modifier.weight(1f))

                Button (
                    onClick = {
                        try {
                            val inputSN = input.toInt()

                            if (searchDB.findSearch(inputSN)) {
                                if (stopDB.findStop(inputSN)) {
                                    toastFav.show()
                                } else {
                                    stopDB.insertStop(inputSN)
                                }
                            } else {
                                toastValid.show()
                            }
                        } catch (e: NumberFormatException) {
                            toastValid.show()
                        }
                    }
                ) {
                    Text (
                        "Add stop"
                    )
                }
            }
        }
    }
}

@Composable
fun Bulletin(bulletin: ServiceBulletin): Unit {
    Box(
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.tertiaryContainer,
                shape = RoundedCornerShape(20)
            )
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.tertiaryContainer,
                shape = RoundedCornerShape(20)
            )
            .padding(16.dp)
            .clickable(
                enabled = true,
                onClick = {

                }
            )
    ){
        Column{
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

/**
 * Gets places client
 */
fun getPlacesClient(context: Context) : PlacesClient? {
    // Gets API key using secrets
    val apikey = BuildConfig.GOOGLE_MAPS_API_KEY

    // Checks if key is empty
    if (apikey.isEmpty() || apikey == "DEFAULT_API_KEY") {
        Log.e("Places API", "MISSING API KEY")
    }

    Places.initializeWithNewPlacesApiEnabled(context, apikey)
    val placesClient = Places.createClient(context)
    return placesClient
}





