package com.example.watcomtravels

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.watcomtravels.ui.theme.AppTheme
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SearchResultPage : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme(darkMode) {
                val sheetPeekHeight =
                    if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        100.dp
                    } else {
                        256.dp
                    }

                val lat = intent.getDoubleExtra("lat", 0.0)
                val long = intent.getDoubleExtra("long", 0.0)
                val displayName = intent.getStringExtra("display name")

                val latLng = LatLng(lat, long)

                // Gets Databases
                val stopDB = dbStops(this@SearchResultPage)
                val searchDB = dbSearch(this@SearchResultPage)
                val routeDB = dbRoutes(this@SearchResultPage)

                // Create a new ViewModel for the new Activity
                val transitViewModel =
                    TransitViewModel(context = this@SearchResultPage, searchDB, stopDB, routeDB)
                val uiState by transitViewModel.uiState.collectAsState()

                transitViewModel.addMarker(latLng)

                val mapComposable = @Composable { TransitMap(transitViewModel) }
                transitViewModel.loaded()

                var nearbyStops by remember { mutableStateOf<List<StopObject>?>(emptyList()) }


                nearbyStops = searchDB.ltlnSearch(lat, long)

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
                                            displayName!!,
                                            textAlign = TextAlign.Center,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 20.sp
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

                                if (nearbyStops != null) {
                                    StopRow(nearbyStops!!, "Nearby stops")
                                } else {
                                    Text("No stops nearby", style = MaterialTheme.typography.titleLarge)
                                }


                            }

                        }
                    ) {

                        if (uiState.isLoaded) {
                            mapComposable.invoke()
                        }
                    }
                }
            }
        }
    }


