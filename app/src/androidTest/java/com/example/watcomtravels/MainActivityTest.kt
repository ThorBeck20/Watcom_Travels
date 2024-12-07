package com.example.watcomtravels

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.watcomtravels.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    val appContext = InstrumentationRegistry.getInstrumentation().targetContext

    val allStops = dbSearch(appContext)
    val routesDB = dbRoutes(appContext)
    val stopsDB = dbStops(appContext)

    val transitViewModel = TransitViewModel(context = appContext, allStops, stopsDB, routesDB)
    val mapComposable = @Composable { TransitMap(transitViewModel) }

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testSearchBarAcceptsInput() {
        composeTestRule.setContent {
            AppTheme(
                darkTheme = darkMode
            ) {
                val stops = emptyList<StopObject>()
                val stopType = "Nearby stops"
                val drawerState = rememberDrawerState(DrawerValue.Closed)
                PortraitUI(mapComposable, stops, stopType, transitViewModel, drawerState) // Assuming this function sets up the main activity UI
            }
        }

        // Find the search bar by content description or tag
        val searchBar = composeTestRule.onNodeWithContentDescription("Search Bar")

        // Verify the search bar exists
        searchBar.assertExists()

        // Type into the search bar
        searchBar.performTextInput("Route 123")

        // Verify the text appears in the search bar
        searchBar.assertTextContains("Route 123")
    }

    @Test
    fun testSearchResultsAppear() {
        composeTestRule.setContent {
            AppTheme(
                darkTheme = darkMode
            ) {
                val stops = emptyList<StopObject>()
                val stopType = "Nearby stops"
                val drawerState = rememberDrawerState(DrawerValue.Closed)
                PortraitUI(mapComposable, stops, stopType, transitViewModel, drawerState) // Assuming this function sets up the main activity UI
            }
        }

        val mockResults = listOf("Route 1", "Route 2", "Route 3")

        // Verify each search result appears
        mockResults.forEach { result ->
            composeTestRule.onNodeWithText(result).assertExists()
        }
    }

    @Test
    fun testViewRouteScheduleButton() {
        composeTestRule.setContent {
            AppTheme(
                darkTheme = darkMode
            ) {
                val stops = emptyList<StopObject>()
                val stopType = "Nearby stops"
                val drawerState = rememberDrawerState(DrawerValue.Closed)
                PortraitUI(mapComposable, stops, stopType, transitViewModel, drawerState) // Assuming this function sets up the main activity UI
            }
        }

        // Find the button to view the route schedule
        val button = composeTestRule.onNodeWithText("View Route Info")

        // Assert the button exists
        button.assertExists()

        // Click the button
        button.performClick()

        // Verify navigation (e.g., ensure a new activity or composable opens)
        // This depends on how you're handling navigation in your app
        composeTestRule.onNodeWithText("WebView").assertExists()
    }

}
