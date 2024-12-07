package com.example.watcomtravels

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.watcomtravels.ui.theme.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StopInfoPage : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val stopNum = intent.getIntExtra("stopNum", 0)
        val timeOpt = intent.getBooleanExtra("time option", false)
        if(stopNum == 0){
            Log.d("@@Stop Info Page@@", "Error getting stopNum information")
            finish()
        }
        val timeAdj: Int = if(timeOpt){
            0 //military time
        }else{
            12
        }

        // Gets Databases
        val stopDB = dbStops(this@StopInfoPage)
        val searchDB = dbSearch(this@StopInfoPage)
        val routeDB = dbRoutes(this@StopInfoPage)


        setContent {
            AppTheme (darkMode){
                val sheetPeekHeight = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    100.dp
                }else{
                    256.dp
                }

                // Create a new ViewModel for the new Activity
                val transitViewModel = TransitViewModel(context = this@StopInfoPage, searchDB, stopDB, routeDB)
                val uiState by transitViewModel.uiState.collectAsState()

                val mapComposable = @Composable { TransitMap(this@StopInfoPage, transitViewModel) }


            var stop by remember { mutableStateOf<StopObject?>(null) }
                var stop by remember { mutableStateOf<StopObject?>(null) }
                stop = searchDB.getSearch(stopNum)

                LaunchedEffect(Unit){
                    withContext(Dispatchers.IO) {
                        if (stop == null) {
                            val fetchedStop = WTAApi.getStop(stopNum)!!
                            withContext(Dispatchers.Main){
                                Log.d("@@Stop Info Page@@", "$fetchedStop")
                                stop = fetchedStop
//                                transitViewModel.loaded()
                                if(transitViewModel.isLoaded()) {
                                    transitViewModel.displayStop(stop!!)
                                } else {

                                }
                            }
                        } else {
                            withContext(Dispatchers.Main){
                                Log.d("@@Stop Info Page@@", "$stop")
//                                transitViewModel.loaded()
                                if(transitViewModel.isLoaded()) {
                                    transitViewModel.displayStop(stop!!)
                                } else {

                                }
                            }
                        }
                    }
                }
                if(stop == null){
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

                                    ){
                                        Text(
                                            stop!!.name!!,
                                            textAlign = TextAlign.Center,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 20.sp
                                        )

                                        var favorited by rememberSaveable { mutableStateOf(false) }
                                        favorited = stopDB.findStop(stopNum)

                                        IconButton(
                                            onClick = {

                                                if(!favorited){
                                                    favorited = true
                                                    Log.d("@@Stop Info Page@@", "Page added to favorites")
                                                    stopDB.insertStop(stopNum)
                                                }else{
                                                    favorited = false
                                                    Log.d("@@Stop Info Page@@", "Page removed from favorites")
                                                    stopDB.deleteStop(stopNum)
                                                }
                                            },
                                            content = {
                                                Icon(
                                                    imageVector = if (favorited) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                    "Favorite",
                                                    tint = MaterialTheme.colorScheme.onPrimary
                                                )
                                            }
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
                                    "Stop Information",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 32.sp,
                                    modifier = Modifier.padding(4.dp)
                                )
                                Text(
                                    stop!!.name!!,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(4.dp),
                                    fontSize = 24.sp,
                                )

                                Text(
                                    "Stop number: ${stop!!.stopNum}",
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(4.dp),
                                    fontSize = 24.sp,
                                )


                                val predictions = remember { mutableStateListOf<Prediction>() }
                                LaunchedEffect(Unit) {
                                    withContext(Dispatchers.IO) {
                                        val fetchedPredictions = WTAApi.getPredictions(stop!!.stopNum)
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
                                    Prediction(prediction, timeAdj)

                                }

                                val bulletins = remember { mutableStateListOf<ServiceBulletin>() }
                                LaunchedEffect(Unit) {
                                    withContext(Dispatchers.IO) {
                                        val fetchedBulletins = WTAApi.getBulletins(stop!!.stopNum)
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
                                            Bulletin(bulletin)
                                        }
                                    }

                                }
                            }

                        }
                    ) {

                        //TODO - have map display the stop
                        if (uiState.isLoaded) {
                            mapComposable.invoke()
                        }
                    }
                }
            }



        }

    }
}

@Composable
fun Prediction(prediction: Prediction, timeAdj: Int){
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
                "${(prediction.hour.toInt() - timeAdj)}:${prediction.min}",
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

