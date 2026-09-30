package com.presidentsimulator.game.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.ui.navigation.GameDestination

@Composable
fun MainDashboardScreen(
    state: GameState,
    onNavigate: (GameDestination) -> Unit,
    activeCategory: String = "General expenses",
    onCategoryChanged: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    // The MainDashboard is just a transparent overlay so the InteractiveWorldMap 
    // (which is rendered behind the NavHost in GameNavigation.kt) can be fully seen.
    // This perfectly matches President Simulator 1's main menu!
    Box(modifier = modifier.fillMaxSize())
}
