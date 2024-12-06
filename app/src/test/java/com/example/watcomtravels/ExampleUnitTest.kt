package com.example.watcomtravels

import android.content.Context
import org.junit.Test

import org.junit.Assert.*
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

private const val DELTA = 0.0001f

@RunWith(MockitoJUnitRunner::class)
class ExampleUnitTest {
    // Test variables
    private val testStopOne = StopObject(1, "James St at Illinois St",
        48.76871f, -122.46502f, 7256)

    private val testStopTwo = StopObject(509, "32nd St at Community Garden",
        48.721516f, -122.47550f, 2064)

    private val testStopThree = StopObject(1538, "Cornwall Ave at Chestnut St",
        48.74856f, -122.48082f, 3471)

    // val tester = mock(WTAApi.Companion::class.java)
    // `when`(tester.getStopObjects()).thenReturn(TEST_LIST)

    // Tests for WTAApi
    @Test
    fun test_getStopObjects() {
        val so1 = WTAApi.getStop(7256)
        val so2 = WTAApi.getStop(2064)
        val so3 = WTAApi.getStop(3471)

        val stopObjects = mutableListOf<StopObject>()
        val nullTest = WTAApi.getStopObjects()

        if ((so1 != null) && (so2 != null) && (so3 != null)) {
            assertEquals(testStopOne.id, so1.id)
            assertEquals(testStopOne.name, so1.name)
            assertEquals(testStopOne.lat, so1.lat, DELTA)
            assertEquals(testStopOne.long, so1.long, DELTA)
            assertEquals(testStopOne.stopNum, so1.stopNum)

            assertEquals(testStopTwo.id, so2.id)
            assertEquals(testStopTwo.name, so2.name)
            assertEquals(testStopTwo.lat, so2.lat, DELTA)
            assertEquals(testStopTwo.long, so2.long, DELTA)
            assertEquals(testStopTwo.stopNum, so2.stopNum)

            assertEquals(testStopThree.id, so3.id)
            assertEquals(testStopThree.name, so3.name)
            assertEquals(testStopThree.lat, so3.lat, DELTA)
            assertEquals(testStopThree.long, so3.long, DELTA)
            assertEquals(testStopThree.stopNum, so3.stopNum)
        }

        if (nullTest != null) {
            stopObjects.addAll(nullTest)

            val stop1 = stopObjects[0]
            assertEquals(testStopOne.id, stop1.id)
            assertEquals(testStopOne.name, stop1.name)
            assertEquals(testStopOne.lat, stop1.lat, DELTA)
            assertEquals(testStopOne.long, stop1.long, DELTA)
            assertEquals(testStopOne.stopNum, stop1.stopNum)

            val stop509 = stopObjects[425]
            assertEquals(testStopTwo.id, stop509.id)
            assertEquals(testStopTwo.name, stop509.name)
            assertEquals(testStopTwo.lat, stop509.lat, DELTA)
            assertEquals(testStopTwo.long, stop509.long, DELTA)
            assertEquals(testStopTwo.stopNum, stop509.stopNum)

            val stop1538 = stopObjects[949]
            assertEquals(testStopThree.id, stop1538.id)
            assertEquals(testStopThree.name, stop1538.name)
            assertEquals(testStopThree.lat, stop1538.lat, DELTA)
            assertEquals(testStopThree.long, stop1538.long, DELTA)
            assertEquals(testStopThree.stopNum, stop1538.stopNum)
        }
    }

    // more API tests
}