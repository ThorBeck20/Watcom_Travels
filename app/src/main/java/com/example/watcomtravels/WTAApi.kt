package com.example.watcomtravels

import org.json.JSONArray
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

//      Maybe make a separate request for these when a user clicks on the details?
//      This would save power and space

//    val street : String?,     // The street the stop is on
//    val lighting : String?,   // A string describing the state of lighting of the stop
//    val shelter : String?,    // String describing the state of the shelter
//    val bench : String?,      // A string describing if there is a bench at the stop
)

class WTAApi {
    companion object {


        // Return a list of StopObjects
        fun getStopObjets(): List<StopObject> {
            val stopList = mutableListOf<StopObject>()
            val jsonArray = callAPI("https://api.ridewta.com/stops")

            for (i in (0..(jsonArray.length() - 1))) {
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

            return stopList
        }

        // Return the street the stop is on
        // id = StopNum
        fun getStreet(id: Int): String {
            val jsonArray = callAPI("https://api.ridewta.com/stops/$id")
            val jsonObject: JSONObject = jsonArray.getJSONObject(0)
            var street = jsonObject.getString("street")

            if (street == null) {
                street = "N/A"
            }

            return street
        }

        // Return the lighting status of the street
        // id = StopNum
        fun getLighting(id: Int): String {
            val jsonArray = callAPI("https://api.ridewta.com/stops/$id")
            val jsonObject: JSONObject = jsonArray.getJSONObject(0)
            var light = jsonObject.getString("lighting")

            if (light == null) {
                light = "N/A"
            }

            return light
        }

        // Return the shelter status of the street
        // id = StopNum
        fun getShelter(id: Int): String {
            val jsonArray = callAPI("https://api.ridewta.com/stops/$id")
            val jsonObject: JSONObject = jsonArray.getJSONObject(0)
            var shelter = jsonObject.getString("shelter")

            if (shelter == null) {
                shelter = "N/A"
            }

            return shelter
        }

        // Return the lighting status of the street
        // id = StopNum
        fun getBench(id: Int): String {
            val jsonArray = callAPI("https://api.ridewta.com/stops/$id")
            val jsonObject: JSONObject = jsonArray.getJSONObject(0)
            var bench = jsonObject.getString("bench")

            if (bench == null) {
                bench = "N/A"
            }

            return bench
        }

        // Returns next three predictions for a stop
        // id = StopNum
        /* fun getPredictions(id: Int): */

        // Returns bulletins for a stop, if there are any
        // id = StopNum
        /* fun getBulletins(id: Int): List<String> */

        // Handles getting the JSONArray for api work
        private fun callAPI(urlString: String): JSONArray {
            val url = URL(urlString)
            val connection = url.openConnection()
            connection.setRequestProperty("User-Agent", "Mozilla/5.0")
            val content = connection.getInputStream().bufferedReader().readText()
            val jsonArray = JSONArray(content)
            return jsonArray
        }

    }

}