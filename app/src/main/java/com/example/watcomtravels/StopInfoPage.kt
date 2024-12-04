package com.example.watcomtravels

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StopInfoPage : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val stopNum = intent.getIntExtra("stopNum", 0)
        if(stopNum == 0){
            Log.d("@@Stop Info Page@@", "Error getting stopNum information")
            finish()
        }



        setContent {
            var stop by remember { mutableStateOf<StopObject?>(null) }

            LaunchedEffect(Unit){
                withContext(Dispatchers.IO){
                    val fetchedStop = WTAApi.getStop(stopNum)!!
                    withContext(Dispatchers.Main){
                        Log.d("@@Stop Info Page@@", "$fetchedStop")
                        stop = fetchedStop
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
                                        stop!!.name!!,
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.SemiBold
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


        }

    }
}

