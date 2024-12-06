package com.example.watcomtravels

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.robolectric.RuntimeEnvironment

private const val DELTA = 0.0001f

@RunWith(AndroidJUnit4::class)
class DatabaseUnitTest {
    // Robolectric mock context for tests
    @Mock
    private var mockContext = RuntimeEnvironment.getApplication()

    // Test variables
    private val testStopOne = StopObject(1, "James St at Illinois St",
        48.76871f, -122.46502f, 7256)

    private val testStopTwo = StopObject(509, "32nd St at Community Garden",
        48.721516f, -122.47550f, 2064)

    private val testStopThree = StopObject(1538, "Cornwall Ave at Chestnut St",
        48.74856f, -122.48082f, 3471)

    private val testOPOne = mutableListOf(
        PatternObject(1, 48.75850f, -122.50344f, "n/a", 0, null),
        PatternObject(2, 48.75851f, -122.50348f, "n/a", 0, null),
        PatternObject(3, 48.75898f, -122.50299f, "n/a", 0, null),
        PatternObject(4, 48.75952f, -122.50286f, "n/a", 0, null),
        PatternObject(5, 48.75949f, -122.50203f, "n/a", 0, null)
    )

    private val testOPTwo = mutableListOf(
        PatternObject(1, 48.74361f, -122.46244f, "n/a", 0, null),
        PatternObject(2, 48.74364f, -122.46260f, "n/a", 0, null),
        PatternObject(3, 48.74393f, -122.46254f, "n/a", 0, null),
        PatternObject(4, 48.74496f, -122.46252f, "n/a", 0, null),
        PatternObject(5, 48.74496f, -122.46361f, "n/a", 0, null)
    )

    private val testOPThree = mutableListOf(
        PatternObject(1, 48.75038f, -122.47561f, "n/a", 0, null),
        PatternObject(2, 48.75033f, -122.47551f, "n/a", 0, null),
        PatternObject(3, 48.75009f, -122.47585f, "n/a", 0, null),
        PatternObject(4, 48.75000f, -122.47589f, "n/a", 0, null),
        PatternObject(5, 48.74982f, -122.47617f, "n/a", 0, null)
    )

    private val testRPOne = RoutePattern(283, 13039, "DOWNTOWN", testOPOne)
    private val testRPTwo = RoutePattern(177, 27088, "DOWNTOWN", testOPTwo)
    private val testRPThree = RoutePattern(180, 23691, "LINCOLN ST", testOPThree)

    // Testing the stops database
    @Test
    fun test_stopsDB() {
        val tester = dbStops(mockContext)
        tester.deleteAllStops()
        assertEquals(0, tester.getAllStops().size)

        tester.insertStop(testStopOne.stopNum)
        tester.insertStop(testStopTwo.stopNum)
        tester.insertStop(testStopThree.stopNum)

        var allStops = tester.getAllStops()
        assertEquals(3, allStops.size)
        assertEquals(testStopOne.stopNum, allStops[0])
        assertEquals(testStopTwo.stopNum, allStops[1])
        assertEquals(testStopThree.stopNum, allStops[2])

        tester.deleteStop(testStopTwo.stopNum)

        allStops = tester.getAllStops()
        assertEquals(2, allStops.size)
        assertEquals(testStopOne.stopNum, allStops[0])
        assertEquals(testStopThree.stopNum, allStops[1])

        assertEquals(true, tester.findStop(testStopOne.stopNum))
        assertEquals(false, tester.findStop(testStopTwo.stopNum))

        tester.deleteAllStops()
        assertEquals(0, tester.getAllStops().size)
    }

    // Testing the search database
    @Test
    fun test_searchDB() {
        val tester = dbSearch(mockContext)
        tester.clearSearch()
        assertEquals(0, tester.getAllSearches().size)

        tester.insertSearch(testStopOne)
        tester.insertSearch(testStopTwo)
        tester.insertSearch(testStopThree)

        var allSearches = tester.getAllSearches()
        assertEquals(3, allSearches.size)

        cmpObjs(testStopOne, allSearches[0])
        cmpObjs(testStopTwo, allSearches[1])
        cmpObjs(testStopThree, allSearches[2])

        cmpObjs(testStopOne, tester.getSearch(7256))
        cmpObjs(testStopTwo, tester.getSearch(2064))
        cmpObjs(testStopThree, tester.getSearch(3471))

        allSearches = tester.ltlnSearch(48.71, -122.47)
        assertEquals(2, allSearches.size)
        cmpObjs(testStopTwo, allSearches[0])
        cmpObjs(testStopThree, allSearches[1])

        allSearches = tester.ltlnSearch(48.71, -125.00)
        assertEquals(0, allSearches.size)

        allSearches = tester.ltlnSearch(0.0, 0.0)
        assertEquals(0, allSearches.size)

        tester.deleteSearch(testStopTwo.stopNum)

        allSearches = tester.getAllSearches()
        assertEquals(2, allSearches.size)
        cmpObjs(testStopOne, allSearches[0])
        cmpObjs(testStopThree, allSearches[1])

        assertEquals(true, tester.findSearch(testStopOne.stopNum))
        assertEquals(false, tester.findSearch(testStopTwo.stopNum))

        tester.clearSearch()
        assertEquals(0, tester.getAllSearches().size)
    }

    // Testing the routes database
    @Test
    fun test_routesDB() {
        val tester = dbRoutes(mockContext)
        tester.deleteAllRoutes()
        assertEquals(0, tester.getAllRoutes().size)

        tester.insertRoute("harbor", testRPOne)
        tester.insertRoute("ln-dtDT", testRPTwo)
        tester.insertRoute("ln-dtLN", testRPThree)

        var allRoutes = tester.getAllRoutes()
        assertEquals(3, allRoutes.size)

        cmpRPs(testRPOne, allRoutes[0])
        cmpRPs(testRPTwo, allRoutes[1])
        cmpRPs(testRPThree, allRoutes[2])

        cmpRPs(testRPOne, tester.getRoute("harbor"))
        cmpRPs(testRPTwo, tester.getRoute("ln-dtDT"))
        cmpRPs(testRPThree, tester.getRoute("ln-dtLN"))

        tester.deleteRoute("ln-dtDT")

        allRoutes = tester.getAllRoutes()
        assertEquals(2, allRoutes.size)

        cmpRPs(testRPOne, allRoutes[0])
        cmpRPs(testRPThree, allRoutes[1])

        tester.deleteAllRoutes()
        assertEquals(0, tester.getAllRoutes().size)
    }

    // Helper function - compares StopObjects
    private fun cmpObjs(expc: StopObject, actu: StopObject) {
        assertEquals(expc.id, actu.id)
        assertEquals(expc.name, actu.name)
        assertEquals(expc.lat, actu.lat)
        assertEquals(expc.long, actu.long)
        assertEquals(expc.stopNum, actu.stopNum)
    }

    // Helper function - compares RoutePatterns
    private fun cmpRPs(expc: RoutePattern, actu: RoutePattern) {
        assertEquals(expc.pid, actu.pid)
        assertEquals(expc.lineNum, actu.lineNum)
        assertEquals(expc.routeDir, actu.routeDir)
        assertEquals(expc.pt.size, actu.pt.size)

        for (i in 0..<expc.pt.size) {
            assertEquals(expc.pt[i].lat, actu.pt[i].lat, DELTA)
            assertEquals(expc.pt[i].long, actu.pt[i].long, DELTA)
        }
    }
}