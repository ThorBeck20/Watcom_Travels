package com.example.watcomtravels

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchByTextRequest
import com.google.maps.android.ktx.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONStringer
import java.io.FileNotFoundException
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.Arrays

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
    var pattern : MutableList<RoutePattern>?
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
    val stop : StopObject?
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

                if (content.trim().startsWith("{") || content.trim().startsWith("[")){
                    return content
                }else{
                    Log.d("ERROR", errWTA)
                    return null
                }

            } catch(e: JSONException) {
                Log.d("ERROR", errWTA)
                return null
            } catch(e: FileNotFoundException) {
                Log.d("ERROR", errMsg)
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
                    val id = jsonObject.getString("id").toInt()
                    val name: String? = jsonObject.getString("name")
                    val latitude: Float = jsonObject.getString(LATITUDE).toFloat()
                    val longitude: Float = jsonObject.getString("longitude").toFloat()
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
                return null
            } else {
                val jsonArray = JSONArray(json)

                val jsonObject = jsonArray.getJSONObject(0)
                val id = jsonObject.getString("id").toInt()
                val name: String? = jsonObject.getString("name")
                val latitude: Float = jsonObject.getString(LATITUDE).toFloat()
                val longitude: Float = jsonObject.getString("longitude").toFloat()
                val sNum: Int = jsonObject.getInt("stopNum")

                val stop = StopObject(id = id, name = name, lat = latitude, long = longitude,
                    stopNum = sNum)

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

        // Returns next three predictions for a stop
        // id = StopNum
        fun getPredictions(id: Int): List<Prediction>? {
            val predictionList = mutableListOf<Prediction>()

            val response = callAPI("https://api.ridewta.com/stops/$id/predictions") ?: return null

            val jsonObject = JSONObject(response)
            val bustime = jsonObject.getJSONObject("bustime-response")
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
        }

        // Returns bulletins for a stop, if there are any
        // id = StopNum
        fun getBulletins(id: Int): List<ServiceBulletin> {
            val bulls = mutableListOf<ServiceBulletin>()

            val json = callAPI("https://api.ridewta.com/stops/$id/bulletins")


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

        // Compiles/returns a list of PatternObjects for a given route for the database
        // In progress
        fun getPOs(route: String): List<PatternObject>? {
            val arrayStr = callAPI("https://api.ridewta.com/routes/$route/patterns")

            if (arrayStr == null) {
                return null
            } else {
                val jsonArray = JSONArray(arrayStr)
                val patternList = mutableListOf<PatternObject>()

                val jsonObject = jsonArray.getJSONObject(0)
                val patternArray = jsonObject.getJSONArray("pt")

                for (i in (0..<patternArray.length())) {
                    val patternObject = patternArray.getJSONObject(i)
                    val toAdd = getPattern(patternObject)
                    patternList.add(toAdd)
                }

                return patternList
            }
        }

        // Gets a specific pattern from a JSON object that represents that pattern
        private fun getPattern(patternJSON : JSONObject) : PatternObject {
            val sequenceNum : Int = patternJSON.getInt("seq")
            val latitude: Float = patternJSON.getString("lat").toFloat()
            val longitude: Float = patternJSON.getString("lon").toFloat()
            val typeStr : String = patternJSON.getString("typ")
            val pdist : Int = patternJSON.getInt("pdist")
            val stopNum : Int?
            var stop : StopObject? = null

            if (typeStr == "S") {
                try {
                    // If this passes, the pattern is a stop and stopObject has been initialized.
                    stopNum = patternJSON.getString("stpid").toInt()
                    stop = getStop(stopNum)
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
        fun getRoutePatterns(routeNum: String) : MutableList<RoutePattern>? {
            var routePatternList : MutableList<RoutePattern>? = emptyList<RoutePattern>().toMutableList()
            val patternList : MutableList<PatternObject> = emptyList<PatternObject>().toMutableList()

            val responseJson = callAPI("https://api.ridewta.com/routes/$routeNum/patterns")

            if (responseJson == null) {
                return null
            } else {
                val responseJsonArray = JSONArray(responseJson)
                Log.d("@@API@@", "Received response")
                for (i in (0..<responseJsonArray.length())) {
                    val routePattern : RoutePattern
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
                    routePatternList!!.add(routePattern)
                    Log.d("@@API@@", "Finished a pattern")
                }
            }
            Log.d("@@@", "Routes Loaded!")
            return routePatternList
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
        private fun callPlacesAPI(str: String) : String? {
            var retStr : String
            try {
                // Specify what kind of things to return
                val placeFields : List<Place.Field> = Arrays.asList(Place.Field.ID, Place.Field.DISPLAY_NAME)

                // Lat Long bounds for the search
                val swBound = LatLng(48.40004, -122.37137)
                val neBound = LatLng(48.51422, -122.19048)

                val searchByTextRequest = SearchByTextRequest.builder(str, placeFields)
                    .setMaxResultCount(10)
                    .setLocationRestriction(RectangularBounds.newInstance(swBound, neBound)).build()


            } catch (e : Exception) {
                Log.d("@_@", "Error: $e")
                return "??"
            }
            return ""
        }


        /**
         * Test function for places API
         */
        fun getPlacesSearch(str: String) : String? {
            var json : String
            runBlocking {
                json = callPlacesAPI(str).toString()
            }
            return json
        }
    }
}