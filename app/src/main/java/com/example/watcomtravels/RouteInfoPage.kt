package com.example.watcomtravels

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.watcomtravels.ui.theme.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RouteInfoPage : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val routeNum = intent.getStringExtra("routeNum")
        if(routeNum == null){
            Log.d("@@Route Info Page@@", "Error getting routeNum information")
            finish()
        }

        val searchDb = dbSearch(this)
        val stopsDb = dbStops(this)
        val routesDb = dbRoutes(this)

        setContent {
            AppTheme (darkMode){
                val sheetPeekHeight: Dp = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    100.dp
                }else{
                    256.dp
                }
                var route by remember { mutableStateOf<Route?>(null) }

                LaunchedEffect(Unit) {
                    withContext(Dispatchers.IO) {
                        val fetchedRoute = WTAApi.getRoute(routeNum!!)!!
                        withContext(Dispatchers.Main) {
                            Log.d("@@Stop Info Page@@", fetchedRoute.routeNum)
                            route = fetchedRoute
                        }
                    }
                }

                if(route == null){
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ){
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                }else{
                    val scaffoldState = rememberBottomSheetScaffoldState()

                    val scrollState = rememberScrollState()
                    BottomSheetScaffold(
                        scaffoldState = scaffoldState,
                        sheetPeekHeight = sheetPeekHeight,
                        sheetShadowElevation = 24.dp,
                        topBar = {
                            CenterAlignedTopAppBar(
                                title = {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically

                                    ) {
                                        Text(
                                            "${route!!.routeNum} ${route!!.name}",
                                            textAlign = TextAlign.Center,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                    }


                                },
                                navigationIcon = {
                                    IconButton(
                                        onClick = {
                                            finish()
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
                                    "${route!!.routeNum} ${route!!.name}",
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(4.dp),
                                    fontSize = 24.sp

                                )

                                Spacer(
                                    Modifier.height(4.dp)
                                )

                                val context = LocalContext.current
                                val url = "https://schedules.ridewta.com/#route-details?"
                                val hostInApp = false //Set to true if you want to host the routes website within the app

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
                                                if(hostInApp){
                                                    //attempted to implement hosting the webpage in the app, but it was not functioning correctly
                                                    val intent = Intent(context, RouteSchedule::class.java).apply {
                                                        putExtra("url", url)
                                                    }
                                                    context.startActivity(intent)
                                                }else{
                                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                                    context.startActivity(intent)
                                                }

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
                                        val fetchedBulletins = WTAApi.getRouteBulletins(route!!.routeNum)
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
                                            Bulletin(bulletin)
                                        }
                                    }
                                }
                            }

                        }
                    ) {

                        val patternState = remember { mutableStateOf(false) }

                        val routePattern = routesDb.getRoute(route!!.routeNum)

                        if(routePattern != null){
                            route!!.pattern = routePattern
                        }else{
                            LaunchedEffect(Unit) {

                                withContext(Dispatchers.IO) {
                                    val fetchedPattern = WTAApi.getRoutePatterns(route!!.routeNum)!!
                                    withContext(Dispatchers.Main) {
                                        Log.d("@@Stop Info Page@@", "Fetched the route pattern")
                                        patternState.value = true
                                        route!!.pattern = fetchedPattern
                                    }
                                }
                            }
                        }

                        if (!patternState.value) {
                            Box(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }else{
                            val transitViewModel = TransitViewModel(this@RouteInfoPage, searchDb, stopsDb, routesDb)
                            transitViewModel.updateSelectedRoute(route!!)
                            val mapComposable = @Composable { TransitMap(this@RouteInfoPage, transitViewModel) }

                            mapComposable.invoke()
                        }


                    }
                }
            }

        }
    }
}



