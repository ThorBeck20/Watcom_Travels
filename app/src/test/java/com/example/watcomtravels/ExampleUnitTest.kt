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

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

@RunWith(MockitoJUnitRunner::class)
class ExampleUnitTest {
    @Mock
    private lateinit var mockContext: Context    // MockedConstruction.Context

    // Tests for WTAApi
    @Test
    fun test_getStopObjects() {
        // val tester = mock(WTAApi.Companion::class.java)
        // `when`(tester.getStopObjects()).thenReturn(TEST_LIST)

        val stopObjects = mutableListOf<StopObject>()
        val nullTest = WTAApi.getStopObjects()
        if (nullTest != null) {
            stopObjects.addAll(nullTest)
        }

        val stop1 = stopObjects[0]
        assertEquals(stop1.id, 1)
        assertEquals(stop1.name, "James St at Illinois St")
        assertEquals(stop1.lat, 48.768713999886856.toFloat())
        assertEquals(stop1.long, (-122.46502299958198).toFloat())
        assertEquals(stop1.stopNum, 7256)

        val stop509 = stopObjects[425]  // 425
        assertEquals(stop509.id, 509)
        assertEquals(stop509.name, "32nd St at Community Garden")
        assertEquals(stop509.lat, 48.721516.toFloat())
        assertEquals(stop509.long, (-122.475505).toFloat())
        assertEquals(stop509.stopNum, 2064)

        val stop1538 = stopObjects[949]  // 949
        assertEquals(stop1538.id, 1538)
        assertEquals(stop1538.name, "Cornwall Ave at Chestnut St")
        assertEquals(stop1538.lat, 48.74856.toFloat())
        assertEquals(stop1538.long, (-122.480826).toFloat())
        assertEquals(stop1538.stopNum, 3471)
    }

    // more API tests

    // Tests for WTAdb
    @Test
    fun test_favTripsDB() {
        val tester = dbTrips(mockContext)
    }
}