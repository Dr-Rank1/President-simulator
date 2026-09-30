package com.presidentsimulator.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.presidentsimulator.game.audio.GameAudioManager
import com.presidentsimulator.game.audio.playBuildSuccess
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.LawCatalog
import com.presidentsimulator.game.data.LawCategory
import com.presidentsimulator.game.ui.components.GameTile
import com.presidentsimulator.game.ui.components.GameTileData
import com.presidentsimulator.game.ui.components.NssCardImages
import com.presidentsimulator.game.ui.components.NssTabBar
import com.presidentsimulator.game.ui.theme.NssBackground
import com.presidentsimulator.game.viewmodel.GameViewModel

@Composable
fun LawsScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    var selectedTab by remember { mutableStateOf("CONSTITUTION") }
    val tabs = listOf("CONSTITUTION", "ECONOMY", "SOCIAL")

    Column(modifier = modifier.fillMaxSize().background(NssBackground).windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))) {
        NssTabBar(tabs = tabs, selectedTab = selectedTab, onTabSelected = { selectedTab = it })

        val context = LocalContext.current
        val audio = remember(context) { GameAudioManager.getInstance(context) }

        val category = when(selectedTab) {
            "ECONOMY" -> LawCategory.ECONOMIC
            "SOCIAL" -> LawCategory.SOCIAL
            else -> LawCategory.MILITARY
        }

        val laws = LawCatalog.byCategory(category)
        val lawItems = laws.map { law ->
            val isActive = state.legal.isActive(law.id)
            val topColor = if (isActive) Color(0xFF43A047) else Color(0xFFE64A19)
            val statusText = if (isActive) "Active" else "Enact"
            val imageUrl = when(category) {
                LawCategory.MILITARY -> NssCardImages.BANNER_DEFENSE
                LawCategory.ECONOMIC -> NssCardImages.BANNER_ECONOMY
                LawCategory.SOCIAL -> NssCardImages.PARLIAMENT
            }
            
            GameTileData(law.name, statusText, Icons.Default.Gavel, topColor, imageUrl) {
                if (!isActive) {
                    viewModel.enactLaw(law.id)
                    audio.playBuildSuccess()
                } else {
                    viewModel.repealLaw(law.id)
                }
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 130.dp),
            modifier = Modifier.fillMaxSize().padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(lawItems) { item -> GameTile(item) }
        }
    }
}
