package com.example.watcomtravels

import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner


@RunWith(MockitoJUnitRunner::class)
class APITest {

    @Test
    fun test_getRoutePatterns() {
        var routePattern : MutableList<RoutePattern>? = mutableListOf<RoutePattern>()
        routePattern = WTAApi.getRoutePatterns(1)?.toMutableList()

        assert(routePattern != null)

        val size = routePattern?.size
        assert(size == 10)

        val pt1 = routePattern?.get(0)
        assertEquals(pt1?.pid, 80)
        assertEquals(pt1?.lineNum, 18853)
        assertEquals(pt1?.routeDir, "DOWNTOWN")

        val seq1 = pt1?.pt?.get(0)
        assertEquals(seq1?.seq, 1)
        assertEquals(seq1?.lat, 48.720355999987)
        assertEquals(seq1?.long, -122.510690000002)
        assertEquals(seq1?.type, "S")

        val stop1 = seq1?.stop
        assertEquals(stop1?.stopNum, 9393)



    }
}