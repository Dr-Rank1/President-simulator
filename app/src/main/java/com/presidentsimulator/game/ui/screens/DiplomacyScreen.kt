package com.presidentsimulator.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Public
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.ui.components.GameTile
import com.presidentsimulator.game.ui.components.GameTileData
import com.presidentsimulator.game.ui.components.NssTabBar
import com.presidentsimulator.game.ui.theme.NssBackground
import com.presidentsimulator.game.viewmodel.GameViewModel

@Composable
fun DiplomacyScreen(
    state: GameState,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize().background(NssBackground).windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))) {
        NssTabBar(tabs = listOf("FOREIGN AFFAIRS"), selectedTab = "FOREIGN AFFAIRS", onTabSelected = {})

        val diplomacyItems = state.diplomacy.rivals.map { rival ->
            val color = when {
                rival.relationshipScore > 60 -> Color(0xFF43A047)
                rival.relationshipScore < 25 -> Color(0xFFD32F2F)
                else -> Color(0xFFFFA000)
            }
            GameTileData(rival.name, "Relation: ${rival.relationshipScore}", Icons.Default.Public, color) { 
                viewModel.conductStateVisit(rival.id)
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 130.dp),
            modifier = Modifier.fillMaxSize().padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(diplomacyItems) { item -> GameTile(item) }
        }
    }
}
