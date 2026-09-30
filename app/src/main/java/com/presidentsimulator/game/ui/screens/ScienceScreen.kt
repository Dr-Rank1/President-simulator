package com.presidentsimulator.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.presidentsimulator.game.audio.GameAudioManager
import com.presidentsimulator.game.audio.playBuildSuccess
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.Technology
import com.presidentsimulator.game.data.TechCatalog
import com.presidentsimulator.game.data.TechCategory
import com.presidentsimulator.game.ui.components.GameTile
import com.presidentsimulator.game.ui.components.GameTileData
import com.presidentsimulator.game.ui.components.NssCardImages
import com.presidentsimulator.game.ui.components.NssTabBar
import com.presidentsimulator.game.ui.theme.NssBackground
import com.presidentsimulator.game.viewmodel.GameViewModel

@Composable
fun ScienceScreen(
    state: GameState,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf("MILITARY") }
    val tabs = listOf("MILITARY", "ECONOMIC", "SOCIETY")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NssBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
    ) {
        NssTabBar(tabs = tabs, selectedTab = selectedTab, onTabSelected = { selectedTab = it })

        val context = LocalContext.current
        val audio = remember(context) { GameAudioManager.getInstance(context) }
        val domain = when(selectedTab) {
            "ECONOMIC" -> TechCategory.ECONOMY
            "SOCIETY" -> TechCategory.SOCIETY
            else -> TechCategory.MILITARY
        }

        val techs = TechCatalog.all.filter { it.category == domain }
        val techItems = techs.map { tech ->
            val isResearched = state.research.unlockedTechIds.contains(tech.id)
            val isResearching = state.research.activeTechId == tech.id
            val topColor = when {
                isResearched -> Color(0xFF43A047) // Green
                isResearching -> Color(0xFF1E88E5) // Blue
                else -> Color(0xFF757575) // Gray
            }
            val statusText = when {
                isResearched -> "Researched"
                isResearching -> "Researching"
                else -> "Research"
            }
            val icon = when(domain) {
                TechCategory.MILITARY -> Icons.Default.Security
                TechCategory.ECONOMY -> Icons.Default.Build
                TechCategory.SOCIETY -> Icons.Default.People
            }
            GameTileData(tech.name, statusText, icon, topColor) {
                if (!isResearched && !isResearching) {
                    viewModel.startResearch(tech.id)
                    audio.playBuildSuccess()
                }
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 130.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(techItems) { item -> GameTile(item) }
        }
    }
}
