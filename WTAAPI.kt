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

//      Maybe make a separate request for these when a user clicks on the details?
//      This would save power and space


//    val stopNum : Int,       // The stop number
//    val street : String?,     // The street the stop is on
//    val lighting : String?,   // A string describing the state of lighting of the stop
//    val shelter : String?,    // String describing the state of the shelter
//    val bench : String?,      // A string describing if there is a bench at the stop
)

class WTAApi {
    companion object {


        // Return a list of Stop ids
        fun getStopObjets(): List<StopObject> {
            val stopList = mutableListOf<StopObject>()

            val urlString = "https://api.ridewta.com/stops"
            val url = URL(urlString)
            val connection = url.openConnection()
            connection.setRequestProperty("User-Agent", "Mozilla/5.0")
            val content = connection.getInputStream().bufferedReader().readText()
            val jsonArray: JSONArray = JSONArray(content)

            for (i in (0..(jsonArray.length() - 1))) {
                val jsonObject: JSONObject = jsonArray.getJSONObject(i)
                val id = jsonObject.getString("id").toInt()
                val name: String? = jsonObject.getString("name")
                val latitude: Float = jsonObject.getString(LATITUDE).toFloat()
                val longitude: Float = jsonObject.getString("longitude").toFloat()

                val stop = StopObject(id = id, name = name, lat = latitude, long = longitude)

                stopList.add(stop)

            }

            return stopList
        }


    }

}