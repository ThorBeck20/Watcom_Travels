package com.example.watcomtravels

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.junit.Test

import org.junit.Assert.*
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockedConstruction
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import java.net.URL

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

private const val DELTA = 0.0001f

@RunWith(MockitoJUnitRunner::class)
class ExampleUnitTest {
    // Test variables
    private val stopOne = StopObject(1, "James St at Illinois St",
        48.76871f, -122.46502f, 7256)

    private val stopTwo = StopObject(509, "32nd St at Community Garden",
        48.721516f, -122.47550f, 2064)

    private val stopThree = StopObject(949, "Cornwall Ave at Chestnut St",
        48.74856f, -122.48082f, 3471)

    // Tests for WTAApi
    @Test
    fun test_getStopObjects() {
        val stopObjects = mutableListOf<StopObject>()
        val nullTest = WTAApi.getStopObjects()
        if (nullTest != null) {
            stopObjects.addAll(nullTest)
        }

        val stop1 = stopObjects[0]
        assertEquals(1, stop1.id)
        assertEquals("James St at Illinois St", stop1.name)
        assertEquals(48.76871f, stop1.lat, DELTA)
        assertEquals(-122.46502f, stop1.long, DELTA)
        assertEquals(7256, stop1.stopNum)

        val stop509 = stopObjects[425]
        assertEquals(509, stop509.id)
        assertEquals("32nd St at Community Garden", stop509.name)
        assertEquals(48.721516f, stop509.lat, DELTA)
        assertEquals(-122.47550f, stop509.long, DELTA)
        assertEquals(2064, stop509.stopNum)

        val stop1538 = stopObjects[949]
        assertEquals(1538, stop1538.id)
        assertEquals("Cornwall Ave at Chestnut St", stop1538.name)
        assertEquals(48.74856f, stop1538.lat, DELTA)
        assertEquals(-122.48082f, stop1538.long, DELTA)
        assertEquals(3471, stop1538.stopNum)
    }

    // more API tests

    // Tests for WTAdb
    @Mock
    private var mockContext = mock(Context::class.java)  //MockedConstruction.Context

    @Mock
    private var tester = dbStops(mockContext)

    /* @Test
    fun test_stopsDB() {
        // val tester = mock(WTAApi.Companion::class.java)
        // `when`(tester.getStopObjects()).thenReturn(TEST_LIST)

        // val tester = mock(dbStops::class.java)

        tester.deleteAllStops()
        assertEquals(0, tester.getAllStops().size)

        tester.insertStop(stopOne.stopNum)
        tester.insertStop(stopTwo.stopNum)
        tester.insertStop(stopThree.stopNum)

        var allStops = tester.getAllStops()
        assertEquals(3, allStops.size)
        assertEquals(stopOne.stopNum, allStops[0])
        assertEquals(stopTwo.stopNum, allStops[1])
        assertEquals(stopThree.stopNum, allStops[2])

        tester.deleteStop(stopTwo.stopNum)

        allStops = tester.getAllStops()
        assertEquals(2, allStops.size)
        assertEquals(stopOne.stopNum, allStops[0])
        assertEquals(stopThree.stopNum, allStops[1])

        assertEquals(true, tester.findStop(stopOne.stopNum))
        assertEquals(false, tester.findStop(stopTwo.stopNum))

        tester.deleteAllStops()
        assertEquals(0, tester.getAllStops().size)
    } */
}