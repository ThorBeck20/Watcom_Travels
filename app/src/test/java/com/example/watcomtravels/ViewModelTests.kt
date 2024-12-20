package com.example.watcomtravels

import android.content.Context
import android.content.res.Resources
import android.graphics.BitmapFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.compose.MarkerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner


@RunWith(MockitoJUnitRunner::class)
class ViewModelTests {

    @Mock
    private lateinit var bitmapFactory: BitmapFactory

    @Mock
    private lateinit var uiState: MutableStateFlow<TransitUiState>

    @Mock
    private var res : Resources = mock(Resources::class.java)

    @Captor
    private lateinit var uiStateCaptor: ArgumentCaptor<(TransitUiState) -> TransitUiState>

    @Mock
    private lateinit var viewModel : TransitViewModel

    @Mock
    private lateinit var dbSearch: dbSearch
    @Mock
    private lateinit var dbStops: dbStops
    @Mock
    private lateinit var dbRoutes: dbRoutes

    private var context : Context = mock(Context::class.java)

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        viewModel = TransitViewModel(context, dbSearch, dbStops, dbRoutes)
        viewModel._uiState = uiState
    }

    @Test
    fun `test AddMarker`() {
        val latLng = LatLng(48.0, -122.0)

        val initialUiState =
            TransitUiState(
                displayedMarkers = mutableMapOf<MarkerState, MarkerOptions>(),
                context = context,
                dbSearch = dbSearch,
                dbStops = dbStops,
                dbRoutes = dbRoutes
            )
        `when`(uiState.value).thenReturn(initialUiState)

        val markerState = viewModel.addMarker(latLng)
        verify(uiState).update(uiStateCaptor.capture())
        val updateFun = uiStateCaptor.value
        val updatedUiState = updateFun(initialUiState)

        assertEquals(1, updatedUiState.displayedMarkers.size)
        assert(updatedUiState.displayedMarkers.containsKey(markerState))
        assertEquals(latLng, markerState.position)
    }
}