package com.example.watcomtravels

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    // Tests for WTAApi
    /* can't test without a mock/mockito set-up
    @Test
    fun test_getStopObjects() {
        val stopObjects = mutableListOf<StopObject>()
        stopObjects.addAll(WTAApi.getStopObjets())
        val size = stopObjects.size

        assertEquals(size, 950)

        val stop1 = stopObjects[0]
        assertEquals(stop1.id, 1)
        assertEquals(stop1.name, "James St at Illinois St")
        assertEquals(stop1.lat, 48.768713999886856)
        assertEquals(stop1.long, -122.46502299958198)
        assertEquals(stop1.stopNum, 7256)

        val stop509 = stopObjects[425]
        assertEquals(stop509.id, 509)
        assertEquals(stop509.name, "32nd St at Community Garden")
        assertEquals(stop509.lat, 48.721516)
        assertEquals(stop509.long, -122.475505)
        assertEquals(stop509.stopNum, 2064)

        val stop1538 = stopObjects[949]
        assertEquals(stop1538.id, 1538)
        assertEquals(stop1538.name, "Cornwall Ave at Chestnut St")
        assertEquals(stop1538.lat, 48.74856)
        assertEquals(stop1538.long, -122.480826)
        assertEquals(stop1538.stopNum, 3471)
    } */
}