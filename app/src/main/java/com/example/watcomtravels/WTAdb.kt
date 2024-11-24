package com.example.watcomtravels

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


// Class to store trip stops together
data class TripSet (
    val first : Int,    // first stop of a trip
    val second : Int    // second stop of a trip
)

// Database of favourite/saved routes - no size limit
class dbTrips(context: Context) : SQLiteOpenHelper(context, "MyDb", null, 1) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE IF NOT EXISTS TRIPS(first INT, second INT)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }

    // Add trip to database
    fun insertTrip(id1: Int, id2: Int) {
        writableDatabase.execSQL("INSERT INTO TRIPS VALUES(\"$id1\", \"$id2\")")
    }

    // Delete trip from database
    // NEEDS TO BE TESTED - may not function as expected
    fun deleteTrip(id1: Int, id2: Int) {
        writableDatabase.execSQL("DELETE FROM TRIPS WHERE first=\"$id1\" && second=\"$id2\"")
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
class dbStops(context: Context) : SQLiteOpenHelper(context, "MyDb", null, 1) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE IF NOT EXISTS STOPS(stop INT)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }

    // Add stop to database
    fun insertStop(id: Int) {
        writableDatabase.execSQL("INSERT INTO STOPS VALUES(\"$id\")")
    }

    // Delete stop from database
    // NEEDS TO BE TESTED - may not function as expected
    fun deleteStop(id1: Int) {
        writableDatabase.execSQL("DELETE FROM STOPS WHERE first=\"$id1\"")
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
class dbRecent(context: Context) : SQLiteOpenHelper(context, "MyDb", null, 1) {
    private var tracker = 0

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE IF NOT EXISTS RECENTS(first INT, second INT)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }

    // Add trip/stop to database
    // also needs to be tested
    fun insertRecent(id1: Int, id2: Int) {
        writableDatabase.execSQL("INSERT INTO RECENTS VALUES(\"$id1\", \"$id2\")")
        tracker++

        // Updates database to most recent 5 trips/stops
        if (tracker == 5) {
            val hold = getAllRecents()
            clearRecents()

            var i = 1
            while (i < 6) {
                val trn = hold[i]
                insertRecent(trn.first, trn.second)
                i++
            }

            tracker = 0
        }
    }

    // Delete trip/stops from database
    // NEEDS TO BE TESTED - may not function as expected
    fun deleteTrip(id1: Int, id2: Int) {
        writableDatabase.execSQL("DELETE FROM RECENTS WHERE first=\"$id1\" && second=\"$id2\"")
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
