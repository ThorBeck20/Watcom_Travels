package com.example.watcomtravels

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchByTextRequest
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.FileNotFoundException
import java.net.URL
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


const val LATITUDE = "latitutde" // The api spelled latitude wrong ...

// Class to store the stops
data class StopObject (
    val id : Int,            // The id of the stop (index in stops array)
    val name : String?,      // The name of the stop
    val lat : Float,         // The latitude of the stop
    val long : Float,        // The longitude
    val stopNum : Int        // The stop number  <-- helpful for other data calls
)

// Class to store prediction data
data class Prediction (
    val hour: String,
    val min: String,
    val sec: String,
    val bus: String
)

// Data class to store routes
data class Route (
    val routeNum: String,
    val name : String,
    val color : String,
    var pattern : RoutePattern?
)

// Data class to store the patterns of the route
data class RoutePattern (
    val pid : Int,
    val lineNum : Int,
    val routeDir : String,
    val pt : MutableList<PatternObject>
)

// Data Class for the pattern objects
data class PatternObject (
    val seq : Int,          // Stop number in sequence
    val lat : Float,
    val long : Float,
    val type : String,      // Seems to be either "S" for stop or "W" for ...
    val pdist : Int,        // Not really sure what the units are for this ...
    val stop : Int?
)

data class ServiceBulletin(
    val subject: String,
    val brief: String,
    val priority: String,
    val effect: String,
    val service: JSONArray
)

// All functions in WTAApi must be called in an IO thread
class WTAApi {

    companion object {
        // Handles getting the JSONArray for api work
        private fun callAPI(urlString: String): String? {
            val errWTA = "No JSONObject, String instead - WTA API error likely"
            val errMsg = "FileNotFoundException - Attempt refresh"
            val url = URL(urlString)
            val connection = url.openConnection()
            connection.setRequestProperty("User-Agent", "Mozilla/5.0")
            val content = connection.getInputStream().bufferedReader().readText()

            try {
                if (content.trim().equals("\"No service bulletins found.\"")) {
                    return content
                }
                if (content.trim().startsWith("{") || content.trim().startsWith("[") || content.count() == 58){
                    return content
                }else{
                    Log.d("CALLAPI - ERROR1", errWTA)
                    return null
                }

            } catch(e: JSONException) {
                Log.d("CALLAPI - ERROR2", errWTA)
                return null
            } catch(e: FileNotFoundException) {
                Log.d("CALLAPI - ERROR3", errMsg)
                return null
            }

        }

        // Return a list of StopObjects
        // List returns null in event of WTA API errors
        fun getStopObjects(): List<StopObject>? {
            val stopList = mutableListOf<StopObject>()
            val response = callAPI("https://api.ridewta.com/stops")

            if (response == null) {
                return null
            } else {
                val jsonArray = JSONArray(response)

                for (i in (0..<jsonArray.length())) {
                    val jsonObject: JSONObject = jsonArray.getJSONObject(i)
                    val id = jsonObject.getInt("id")
                    val name: String? = jsonObject.getString("name")
                    val latitude: Float = jsonObject.getDouble(LATITUDE).toFloat()
                    val longitude: Float = jsonObject.getDouble("longitude").toFloat()
                    val sNum: Int = jsonObject.getInt("stopNum")

                    val stop = StopObject(id = id, name = name, lat = latitude, long = longitude,
                        stopNum = sNum)

                    stopList.add(stop)

                }
            }

            return stopList
        }

        // Gets the stop information through the stop Number
        fun getStop(stopNum: Int) : StopObject? {
            val json = callAPI("https://api.ridewta.com/stops/$stopNum")


            if (json == null) {
                Log.d("WTAAPI - getStop()","Recieved nothing from api")
                return null
            } else {
                val jsonArray = JSONArray(json)

                val jsonObject = jsonArray.getJSONObject(0)
                val id = jsonObject.getInt("id")
                val name: String? = jsonObject.getString("name")
                val latitude: Float = jsonObject.getDouble(LATITUDE).toFloat()
                val longitude: Float = jsonObject.getDouble("longitude").toFloat()
                val sNum: Int = jsonObject.getInt("stopNum")

                val stop = StopObject(id = id, name = name, lat = latitude, long = longitude,
                    stopNum = sNum)

                Log.d("WTAAPI", "Loaded stop info")
                return stop
            }
        }

        // Return the street the stop is on
        // id = StopNum
        fun getStreet(id: Int): String {
            val json = callAPI("https://api.ridewta.com/stops/$id")


            if (json == null) {
                return "N/A"
            } else {
                val jsonArray = JSONArray(json)

                val jsonObject: JSONObject = jsonArray.getJSONObject(0)
                val street = jsonObject.getString("street")
                Log.d("@@@@", street)
                return street
            }
        }

        // Return the lighting status of the street
        // id = StopNum
        fun getLighting(id: Int): String {
            val json = callAPI("https://api.ridewta.com/stops/$id")

            if (json == null) {
                return "N/A"
            } else {
                val jsonArray = JSONArray(json)

                val jsonObject: JSONObject = jsonArray.getJSONObject(0)
                val light = jsonObject.getString("lighting")
                Log.d("@@@@", light)
                return light
            }
        }

        // Return the shelter status of the street
        // id = StopNum
        fun getShelter(id: Int): String {
            val json = callAPI("https://api.ridewta.com/stops/$id")


            if (json == null) {
                return "N/A"
            } else {
                val jsonArray = JSONArray(json)

                val jsonObject: JSONObject = jsonArray.getJSONObject(0)
                val shelter = jsonObject.getString("shelter")
                Log.d("@@@@", shelter)
                return shelter
            }
        }

        // Return the lighting status of the street
        // id = StopNum
        fun getBench(id: Int): String {
            val json = callAPI("https://api.ridewta.com/stops/$id")

            if (json == null) {
                return "N/A"
            } else {
                val jsonArray = JSONArray(json)

                val jsonObject: JSONObject = jsonArray.getJSONObject(0)
                val bench = jsonObject.getString("bench")
                Log.d("@@@@", bench)
                return bench
            }
        }


        fun getPredictions(id: Int): List<Prediction>? {
            val predictionList = mutableListOf<Prediction>()

            val response = callAPI("https://api.ridewta.com/stops/$id/predictions") ?: return null

            val jsonObject = JSONObject(response)
            val bustime = jsonObject.getJSONObject("bustime-response")

            try {
                val prd = bustime.getJSONArray("prd")

                var i = prd.length()
                if (i < 1) {
                    return null
                } else {
                    if (i > 3) {
                        i = 3
                    }

                    var j = 0
                    while (j < i) {
                        val pred = prd.getJSONObject(j)
                        val route = pred.getString("des")
                        val time = pred.getString("prdtm")

                        val hr = time.substring(9,11)
                        val mn = time.substring(12,14)
                        val sc = time.substring(15)

                        val enter = Prediction(hr, mn, sc, route)
                        predictionList.add(enter)
                        j++
                    }

                    return predictionList
                }
            } catch (e: JSONException) {
                return null
            }
        }

        // Returns bulletins for a stop, if there are any
        // id = StopNum
        fun getBulletins(id: Int): List<ServiceBulletin> {
            val bulls = mutableListOf<ServiceBulletin>()

            val json = callAPI("https://api.ridewta.com/stops/$id/bulletins")


            if (json == null) {
                return emptyList()
            } else if (json.trim().equals("\"No service bulletins found.\"")) {
                return bulls
            } else {
                val jsonArray = JSONArray(json)
                for (b in 0..<jsonArray.length()) {
                    val jsonObject: JSONObject = jsonArray.getJSONObject(b)
                    val bulletin = ServiceBulletin(
                        subject = jsonObject.getString("sbj"),
                        brief = jsonObject.getString("brf"),
                        priority = jsonObject.getString("prty"),
                        effect = jsonObject.getString("efct"),
                        service = jsonObject.getJSONArray("srvc")
                    )
                    bulls.add(bulletin)
                }
            }

            return bulls
        }

        // Gets a specific pattern from a JSON object that represents that pattern
        private fun getPattern(patternJSON : JSONObject) : PatternObject {
            val sequenceNum : Int = patternJSON.getInt("seq")
            val latitude: Float = patternJSON.getString("lat").toFloat()
            val longitude: Float = patternJSON.getString("lon").toFloat()
            val typeStr : String = patternJSON.getString("typ")
            val pdist : Int = patternJSON.getInt("pdist")
            val stopNum : Int?
            var stop : Int? = null

            if (typeStr == "S") {
                try {
                    // If this passes, the pattern is a stop and stopObject has been initialized.
                    // TODO: Change name of stop to stopNum
                    stopNum = patternJSON.getString("stpid").toInt()
                    stop = stopNum
                } catch (e: JSONException) {
                    Log.d("@_@", "Pattern JSON Exception : $e")
                } catch (e: FileNotFoundException) {
                    Log.d("@_@", "Stop not found : $e")
                }
            }

            val patternObject = PatternObject(
                seq = sequenceNum,
                lat = latitude,
                long = longitude,
                type = typeStr,
                pdist = pdist,
                stop = stop
            )

            return patternObject
        }


        // This is un-used for now
        // Gets a specific pattern from a JSON Object that represents the route pattern
        private fun getRoutePattern(jsonObject: JSONObject) : RoutePattern {
            val routePattern : RoutePattern
            val patternList : MutableList<PatternObject> = emptyList<PatternObject>().toMutableList()
            /**
             *      Potentially useful for debug
             *
            val id = jsonObject.getInt("pid")
            val lineNum = jsonObject.getInt("ln")
            val lineDirection : String = jsonObject.getString("rtdir")
             */
            val pattern : JSONArray = jsonObject.getJSONArray("pt")
            for (j in (0..<pattern.length())) {
                val patternJSON = pattern.getJSONObject(j)
                val patternObject = getPattern(patternJSON)
                patternList.add(patternObject)
            }

            routePattern = RoutePattern(
                pid = jsonObject.getInt("pid"),
                lineNum = jsonObject.getInt("ln"),
                routeDir = jsonObject.getString("rtdir"),
                pt = patternList
            )

            return routePattern
        }

        // Gets the List of Route Patterns
        fun getRoutePatterns(routeNum: String) : RoutePattern? {
            val patternList : MutableList<PatternObject> = emptyList<PatternObject>().toMutableList()
            var routePattern : RoutePattern? = null

            val responseJson = callAPI("https://api.ridewta.com/routes/$routeNum/patterns")

            if (responseJson == null) {
                return null
            } else {
                val responseJsonArray = JSONArray(responseJson)
                Log.d("@@API@@", "Received response")
                for (i in (0..<responseJsonArray.length())) {

                    val jsonObject: JSONObject = responseJsonArray.getJSONObject(i)
                    val patternJSONArray : JSONArray = jsonObject.getJSONArray("pt")

                    for (j in (0..<patternJSONArray.length())) {
                        val patternJSON = patternJSONArray.getJSONObject(j)
                        val patternObject = getPattern(patternJSON)
                        patternList.add(patternObject)
                    }

                    routePattern = RoutePattern(
                        pid = jsonObject.getInt("pid"),
                        lineNum = jsonObject.getInt("ln"),
                        routeDir = jsonObject.getString("rtdir"),
                        pt = patternList
                    )
                    Log.d("@@API@@", "Finished a pattern")
                }
            }

            Log.d("@@@", "Routes Loaded!")
            return routePattern
        }

        // Gets a list of routes
        fun getRoutes(): List<Route>? {
            val routeList = mutableListOf<Route>()
            val response = callAPI("https://api.ridewta.com/routes")

            if (response == null) {
                return null
            } else {
                val json = JSONObject(response)
                val jsonArray = json.getJSONArray("routes")
                for (i in (0..<jsonArray.length())) {

                    val jsonObject: JSONObject = jsonArray.getJSONObject(i)
                    val routeNum = jsonObject.getString("routeNum")
                    val name: String = jsonObject.getString("routeName")
                    val color: String = jsonObject.getString("routeColor")


                    // val patternList = getRoutePatterns(routeNum)
                    val route = Route(routeNum = routeNum, name = name, color = color, pattern = null)

                    routeList.add(route)

                }
            }

            return routeList

        }

        // Gets the stop information through the route Number
        fun getRoute(routeNum: String) : Route? {
            val json = callAPI("https://api.ridewta.com/routes/$routeNum")

            if (json == null) {
                return null
            } else {
                val jsonObject = JSONObject(json)

                val name = jsonObject.getString("routeName")
                val color = jsonObject.getString("routeColor")

                val route = Route(
                    routeNum = routeNum,
                    name = name,
                    color = color,
                    pattern = null
                )

                return route
            }
        }

        fun getAllBulletins(): List<ServiceBulletin>{
            val bulletinList = mutableListOf<ServiceBulletin>()

            val response = callAPI("https://api.ridewta.com/servicebulletins")

            if(response == null){
                return emptyList()
            }else{
                val json = JSONObject(response)
                val jsonArray = json.getJSONArray("bulletins")
                for(i in (0..<jsonArray.length())){
                    val jsonObject = jsonArray.getJSONObject(i)
                    val subject = jsonObject.getString("sbj")
                    val brief = jsonObject.getString("brf")
                    val priority = jsonObject.getString("prty")
                    val effect = jsonObject.getString("efct")
                    val service = jsonObject.getJSONArray("srvc")

                    val bulletin = ServiceBulletin(subject, brief, priority, effect, service)

                    bulletinList.add(bulletin)
                }

                return bulletinList
            }

        }

        fun getRouteBulletins(id: String): List<ServiceBulletin> {
            val bulls = mutableListOf<ServiceBulletin>()

            val json = callAPI("https://api.ridewta.com/routes/$id/bulletins")


            if (json == null) {
                return emptyList()
            } else {
                val jsonArray = JSONArray(json)
                for (b in 0..<jsonArray.length()) {
                    val jsonObject: JSONObject = jsonArray.getJSONObject(b)
                    val bulletin = ServiceBulletin(
                        subject = jsonObject.getString("sbj"),
                        brief = jsonObject.getString("brf"),
                        priority = jsonObject.getString("prty"),
                        effect = jsonObject.getString("efct"),
                        service = jsonObject.getJSONArray("srvc")
                    )
                    bulls.add(bulletin)
                }
            }

            return bulls
        }



        /**
         * Calls the placesAPI
         */
        suspend fun callPlacesAPI(str: String, placesClient: PlacesClient): List<Place> {
            // Specify what kind of things to return
            val placeFields: List<Place.Field> = listOf(Place.Field.ID, Place.Field.DISPLAY_NAME)

            // Lat Long bounds for the search
            val swBound = LatLng(48.40004, -122.37137)
            val neBound = LatLng(48.51422, -122.19048)

            val searchByTextRequest = SearchByTextRequest.builder(str, placeFields)
                .setMaxResultCount(10)
                .setLocationRestriction(RectangularBounds.newInstance(swBound, neBound))
                .build()

            return suspendCancellableCoroutine { continuation ->
                try {
                    placesClient.searchByText(searchByTextRequest)
                        .addOnSuccessListener { response ->
                            if (response.places.isNotEmpty()) {
                                val places = response.places
                                Log.i("Place - API", "Fetched ${places.size} places")
                                continuation.resume(places) // Resume with results
                            } else {
                                Log.i("Place - API", "No places found")
                                continuation.resume(emptyList()) // Resume with empty list
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.e("Place - API", "Error fetching places: $exception")
                            continuation.resumeWithException(exception) // Resume with exception
                        }
                } catch (e: Exception) {
                    continuation.resumeWithException(e) // Handle any other exceptions
                }
            }
        }
    }
}