package com.example.watcomtravels

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

const val DISTANCE = 0.4

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
    fun insertTrip(sn1: Int, sn2: Int) {
        writableDatabase.execSQL("INSERT INTO TRIPS(first, second) VALUES(\"$sn1\", \"$sn2\")")
    }

    // Delete trip from database
    fun deleteTrip(sn1: Int, sn2: Int) {
        writableDatabase.execSQL("DELETE FROM TRIPS WHERE (first=\"$sn1\") AND (second=\"$sn2\")")
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
    fun insertStop(sn: Int) {
        writableDatabase.execSQL("INSERT INTO STOPS(stop) VALUES(\"$sn\")")
    }

    // Delete stop from database
    fun deleteStop(sn: Int) {
        writableDatabase.execSQL("DELETE FROM STOPS WHERE stop=\"$sn\"")
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
    fun insertRecent(sn1: Int, sn2: Int) {
        writableDatabase.execSQL("INSERT INTO RECENTS(first, second) VALUES(\"$sn1\", \"$sn2\")")
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
    fun deleteRecent(sn1: Int, sn2: Int) {
        writableDatabase.execSQL("DELETE FROM RECENTS WHERE (first=\"$sn1\") AND (second=\"$sn2\")")
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

// Database of all stops - no size limit
class dbSearch(context: Context) : SQLiteOpenHelper(context, "MySearchDB", null, 1) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE IF NOT EXISTS SEARCH(id INT, name TEXT, lat DOUBLE, lon DOUBLE, sn INT)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }

    // Add a StopObject to database
    fun insertSearch(stop: StopObject) {
        val lt = (stop.lat).toDouble()
        val ln = (stop.long).toDouble()
        writableDatabase.execSQL("INSERT INTO SEARCH(id, name, lat, lon, sn)" +
                "VALUES(\"${stop.id}\", \"${stop.name}\", \"$lt\", \"$ln\", \"${stop.stopNum}\")")
    }

    // Delete a StopObject from database based on its StopNum
    fun deleteSearch(sn: Int) {
        writableDatabase.execSQL("DELETE FROM SEARCH WHERE (sn=\"$sn\")")
    }

    // Clear all StopObjects in database
    fun clearSearch() {
        writableDatabase.execSQL("DELETE FROM SEARCH")
    }

    // Get a specific StopObject based on its StopNum
    fun getSearch(sn: Int): StopObject {
        val cursor = readableDatabase.rawQuery("SELECT * FROM SEARCH WHERE + (sn=\"$sn\")",
            null)
        cursor.moveToFirst()

        val i = cursor.getInt(0)
        val n = cursor.getString(1)
        val lt = cursor.getDouble(2).toFloat()
        val ln = cursor.getDouble(3).toFloat()
        val s = cursor.getInt(4)
        val ret = StopObject(i, n, lt, ln, s)

        cursor.close()
        return ret
    }

    // Returns a list of StopObjects within a ~2mi radius of the passed location
    fun ltlnSearch(lati: Double, long: Double): List<StopObject> {
        val ret = mutableListOf<StopObject>()

        val srLat = lati - DISTANCE
        val erLat = lati + DISTANCE
        val srLon = long - DISTANCE
        val erLon = long + DISTANCE

        val cursor = readableDatabase.rawQuery("SELECT * FROM SEARCH WHERE " +
                "(lat BETWEEN $srLat AND $erLat) AND (lon BETWEEN $srLon AND $erLon)", null)

        while (cursor.moveToNext()) {
            val i = cursor.getInt(0)
            val n = cursor.getString(1)
            val lt = cursor.getDouble(2).toFloat()
            val ln = cursor.getDouble(3).toFloat()
            val s = cursor.getInt(4)
            val stop = StopObject(i, n, lt, ln, s)
            ret.add(stop)
        }

        cursor.close()
        return ret
    }

    // Returns a list of all StopObjects in database
    fun getAllSearches(): List<StopObject> {
        val ret = mutableListOf<StopObject>()

        val cursor = readableDatabase.rawQuery("SELECT * FROM SEARCH", null)
        while (cursor.moveToNext()) {
            val id = cursor.getInt(0)
            val name = cursor.getString(1)
            val lat = cursor.getDouble(2).toFloat()
            val lon = cursor.getDouble(3).toFloat()
            val sn = cursor.getInt(4)

            val stop = StopObject(id, name, lat, lon, sn)
            ret.add(stop)
        }

        cursor.close()
        return ret
    }
}

// Database of RoutePattern objects - no size limit
// rt: routeNum for the associated RoutePattern
// rp: RoutePattern being added to the table
class dbRoutes(context: Context) : SQLiteOpenHelper(context, "MyRoutesDb", null, 1) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE IF NOT EXISTS ROUTES(route TEXT, pid INT, line INT, direct TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        TODO("Not yet implemented")
    }

    // Add a route to database
    fun insertRoute(rt: String, rp: RoutePattern) {
        writableDatabase.execSQL("INSERT INTO ROUTES(route, pid, line, direct)" +
                "VALUES(\"$rt\", \"${rp.pid}\", \"${rp.lineNum}\", \"${rp.routeDir}\")")
    }

    // Delete a route from database
    fun deleteRoute(rt: String) {
        writableDatabase.execSQL("DELETE FROM ROUTES WHERE (route=\"$rt\")")
    }

    // Delete all routes from database
    fun deleteAllRoutes() {
        writableDatabase.execSQL("DELETE FROM ROUTES")
    }

    // Get a route with the given routeNum
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

    // Returns a list of all routes in database
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

    private fun stringRoute(rp: RoutePattern): String {
        return "test"
    }

    private fun unstringRoute(rps: String): List<PatternObject> {
        val rp = mutableListOf<PatternObject>()
        return rp
    }

    // fetches RoutePattern lists for the routes
    private suspend fun fetchRPs(route: String): List<PatternObject>? {
        val rp : List<PatternObject>?
        withContext(Dispatchers.IO) {
            rp = WTAApi.getPOs(route)
        }
        return rp
    }
}
