package com.example.watcomtravels

import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.net.URL

const val LATITUDE = "latitutde" // The api spelled latitude wrong ...

// Class to store the stops
data class StopObject (
    val id : Int,            // The id of the stop
    val name : String?,       // The name of the stop
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

// All functions in WTAApi must be called in an IO thread
class WTAApi {
    companion object {
        // Return a list of StopObjects
        // List contains only errObj in event of WTA API errors
        fun getStopObjets(): List<StopObject> {
            val stopList = mutableListOf<StopObject>()
            val jsonArray = callAPI("https://api.ridewta.com/stops")

            if (jsonArray == null) {
                val errObj = StopObject(-1, "System error", 48.73.toFloat(),
                    -122.49.toFloat(), -1)
                stopList.add(errObj)
            } else {
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

        // Return the street the stop is on
        // id = StopNum
        fun getStreet(id: Int): String {
            val jsonArray = callAPI("https://api.ridewta.com/stops/$id")
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
            val jsonArray = callAPI("https://api.ridewta.com/stops/$id")
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
            val jsonArray = callAPI("https://api.ridewta.com/stops/$id")
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
            val jsonArray = callAPI("https://api.ridewta.com/stops/$id")
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

            val jsonArray = callAPI("https://api.ridewta.com/stops/$id/bulletins")
            if (jsonArray == null) {
                bulls.add(noBulletins)
            } else {
                // work here when a bulletin populates and I can see the standard formatting
                bulls.add("placeholder")
            }

            return bulls
        }

        // Handles getting the JSONArray for api work
        private fun callAPI(urlString: String): JSONArray? {
            val errMsg = "No JSONArray, String instead - WTA API error likely"

            try {
                val url = URL(urlString)
                val connection = url.openConnection()
                connection.setRequestProperty("User-Agent", "Mozilla/5.0")
                val content = connection.getInputStream().bufferedReader().readText()
                val jsonArray = JSONArray(content)
                return jsonArray
            } catch(e: JSONException) {
                Log.d("ERROR", errMsg)
                return null
            }
        }

    }

}