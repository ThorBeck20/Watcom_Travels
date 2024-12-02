package com.example.watcomtravels

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// All databases tested and functional as of testing

// Class to store trip stops together
data class TripSet (
    val first : Int,    // first stop of a trip
    val second : Int    // second stop of a trip
)

// Database of favourite/saved routes - no size limit
class dbTrips(context: Context) : SQLiteOpenHelper(context, "MyTripsDb", null, 1) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE IF NOT EXISTS TRIPS(first INT, second INT)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }

    // Add trip to database
    fun insertTrip(id1: Int, id2: Int) {
        writableDatabase.execSQL("INSERT INTO TRIPS(first, second) VALUES(\"$id1\", \"$id2\")")
    }

    // Delete trip from database
    fun deleteTrip(id1: Int, id2: Int) {
        writableDatabase.execSQL("DELETE FROM TRIPS WHERE (first=\"$id1\") AND (second=\"$id2\")")
    }

    // Delete all trips from database
    fun deleteAllTrips() {
        writableDatabase.execSQL("DELETE FROM TRIPS")
    }

    // Return all trips in database
    fun getAllTrips(): List<TripSet> {
        val ret = mutableListOf<TripSet>()

        val cursor = readableDatabase.rawQuery("SELECT * FROM TRIPS", null)
        while (cursor.moveToNext()) {
            val fir = cursor.getInt(0)
            val sec = cursor.getInt(1)
            val trip = TripSet(fir, sec)
            ret.add(trip)
        }

        cursor.close()
        return ret
    }
}

// Database of favourite/saved stops - no size limit
class dbStops(context: Context) : SQLiteOpenHelper(context, "MyStopsDb", null, 1) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE IF NOT EXISTS STOPS(stop INT)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }

    // Add stop to database
    fun insertStop(id: Int) {
        writableDatabase.execSQL("INSERT INTO STOPS(stop) VALUES(\"$id\")")
    }

    // Delete stop from database
    fun deleteStop(id1: Int) {
        writableDatabase.execSQL("DELETE FROM STOPS WHERE stop=\"$id1\"")
    }

    // Delete all stops from database
    fun deleteAllStops() {
        writableDatabase.execSQL("DELETE FROM STOPS")
    }

    // Return all stops in database
    fun getAllStops(): List<Int> {
        val ret = mutableListOf<Int>()

        val cursor = readableDatabase.rawQuery("SELECT * FROM STOPS", null)
        while (cursor.moveToNext()) {
            val stop = cursor.getInt(0)
            ret.add(stop)
        }

        cursor.close()
        return ret
    }
}

// Database of recent stops/trips - max five
// If saving a stop, second id set to -1
// Note: most recent entry will be bottom of the database
class dbRecent(context: Context) : SQLiteOpenHelper(context, "MyRecentsDb", null, 1) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE IF NOT EXISTS RECENTS(first INT, second INT)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }

    // Add trip/stop to database
    fun insertRecent(id1: Int, id2: Int) {
        writableDatabase.execSQL("INSERT INTO RECENTS(first, second) VALUES(\"$id1\", \"$id2\")")
        val cursor = readableDatabase.rawQuery("SELECT COUNT(*) FROM RECENTS", null)
        cursor.moveToFirst()
        val tracker = cursor.getInt(0)

        // Updates database to most recent 5 trips/stops
        if (tracker == 6) {
            val hold = getAllRecents()
            clearRecents()

            var i = 1
            while (i < tracker) {
                val trn = hold[i]
                insertRecent(trn.first, trn.second)
                i++
            }
        }

        cursor.close()
    }

    // Delete trip/stops from database
    fun deleteRecent(id1: Int, id2: Int) {
        writableDatabase.execSQL("DELETE FROM RECENTS WHERE (first=\"$id1\") AND (second=\"$id2\")")
    }

    // Clears recent trips/stops history
    fun clearRecents() {
        writableDatabase.execSQL("DELETE FROM RECENTS")
    }

    // Return all trips/stops in database
    fun getAllRecents(): List<TripSet> {
        val ret = mutableListOf<TripSet>()

        val cursor = readableDatabase.rawQuery("SELECT * FROM RECENTS", null)
        while (cursor.moveToNext()) {
            val fir = cursor.getInt(0)
            val sec = cursor.getInt(1)
            val trip = TripSet(fir, sec)
            ret.add(trip)
        }

        cursor.close()
        return ret
    }
}

//
class dbRoutes(context: Context) : SQLiteOpenHelper(context, "MyRoutesDb", null, 1) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE IF NOT EXISTS ROUTES(route TEXT, pid INT, line INT, direct TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        TODO("Not yet implemented")
    }

    // rt: routeNum for the associated RoutePattern
    // rp: RoutePattern being added to the table
    fun insertRoute(rt: String, rp: RoutePattern) {
        writableDatabase.execSQL("INSERT INTO ROUTES(route, pid, line, direct)" +
                "VALUES(\"$rt\", \"${rp.pid}\", \"${rp.lineNum}\", \"${rp.routeDir}\")")
    }

    // rt: routeNum
    fun deleteRoute(rt: String) {
        writableDatabase.execSQL("DELETE FROM ROUTES WHERE (route=\"$rt\")")
    }

    // rt: routeNum
    fun getRoute(rt: String): RoutePattern {
        val cursor = readableDatabase.rawQuery("SELECT * FROM ROUTES WHERE (route=\"$rt\")", null)
        cursor.moveToFirst()

        val p = cursor.getInt(1)
        val l = cursor.getInt(2)
        val d = cursor.getString(3)
        val pt = mutableListOf<PatternObject>()
        val ret = RoutePattern(p, l, d, pt)

        cursor.close()
        return ret
    }

    fun getAllRoutes(): List<RoutePattern> {
        val ret = mutableListOf<RoutePattern>()

        val cursor = readableDatabase.rawQuery("SELECT * FROM ROUTES", null)
        while (cursor.moveToNext()) {
            val p = cursor.getInt(2)
            val l = cursor.getInt(3)
            val d = cursor.getString(4)
            val pt = mutableListOf<PatternObject>()
            val route = RoutePattern(p, l, d, pt)
            ret.add(route)
        }

        cursor.close()
        return ret
    }

    // fetches routepattern lists for the routes
    private fun fetchRPs(route: String){
        //
    }
}
