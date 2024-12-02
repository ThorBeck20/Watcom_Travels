package com.example.watcomtravels

import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONStringer
import java.io.FileNotFoundException
import java.net.URL

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

// All functions in WTAApi must be called in an IO thread
class WTAApi {
    companion object {
        // Handles getting the JSONArray for api work
        private fun callAPI(urlString: String): String? {
            val errWTA = "No JSONArray, String instead - WTA API error likely"
            val errMsg = "FileNotFoundException - Attempt refresh"

            try {
                val url = URL(urlString)
                val connection = url.openConnection()
                connection.setRequestProperty("User-Agent", "Mozilla/5.0")
                val content = connection.getInputStream().bufferedReader().readText()
                return content

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
            val jsonArray = JSONArray(response)

            if (jsonArray == null) {
                return null
            } else {
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
            val jsonArray = JSONArray(json)

            if (jsonArray == null) {
                return null
            } else {
                val jsonObject = jsonArray.getJSONObject(0)
                val id = jsonObject.getInt("id")
                val name: String? = jsonObject.getString("name")
                val latitude: Float = jsonObject.getDouble(LATITUDE).toFloat()
                val longitude: Float = jsonObject.getDouble("longitude").toFloat()
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
            val jsonArray = JSONArray(json)

            if (jsonArray == null) {
                return "N/A"
            } else {
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
            val jsonArray = JSONArray(json)

            if (jsonArray == null) {
                return "N/A"
            } else {
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
            val jsonArray = JSONArray(json)

            if (jsonArray == null) {
                return "N/A"
            } else {
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
            val jsonArray = JSONArray(json)

            if (jsonArray == null) {
                return "N/A"
            } else {
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

            val url = URL("https://api.ridewta.com/stops/$id/predictions")
            val connection = url.openConnection()
            connection.setRequestProperty("User-Agent", "Mozilla/5.0")
            val content = connection.getInputStream().bufferedReader().readText()
            val jsonObject = JSONObject(content)
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
                    val route = pred.getString("rt")
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
        fun getBulletins(id: Int): List<String> {
            val bulls = mutableListOf<String>()
            val noBulletins = "No current bulletins for this stop"

            val json = callAPI("https://api.ridewta.com/stops/$id/bulletins")
            val jsonArray = JSONArray(json)

            if (jsonArray == null) {
                bulls.add(noBulletins)
            } else {
                for (b in 0..<jsonArray.length()) {
                    val jsonObject: JSONObject = jsonArray.getJSONObject(b)
                    val bulletin = jsonObject.getString("dtl")
                    bulls.add(bulletin)
                }
            }

            return bulls
        }

        // Compiles/returns a list of PatternObjects for a given route for the database
        // In progress; not currently usable
        /* fun getPOs(route: String): List<PatternObject>? {
            val poList = mutableListOf<PatternObject>()
            val firstArray = callAPI("https://api.ridewta.com/routes/$route/patterns")
            val secondArray = JSONArray(firstArray)

            if (secondArray == null) {
                return null
            } else {
                val routePattern : RoutePattern
                val jsonObject: JSONObject = secondArray.getJSONObject(i)
                val patternJSONArray : JSONArray = jsonObject.getJSONArray("pt")

                for (j in (0..<patternJSONArray.length())) {
                    val patternJSON = patternJSONArray.getJSONObject(j)
                    val patternObject = getPattern(patternJSON)
                    patternList.add(patternObject)
                }
            }
        } */

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
            val responseJsonArray = JSONArray(responseJson)

            if (responseJsonArray == null) {
                return null
            } else {
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
            val json = JSONObject(response)
            val jsonArray = json.getJSONArray("routes")

            if (jsonArray == null) {
                return null
            } else {
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
    }
}