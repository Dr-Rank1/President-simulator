package com.presidentsimulator.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.presidentsimulator.game.audio.GameAudioManager
import com.presidentsimulator.game.audio.playBuildSuccess
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.MilitaryHardware
import com.presidentsimulator.game.ui.components.GameTile
import com.presidentsimulator.game.ui.components.GameTileData
import com.presidentsimulator.game.ui.components.NssTabBar
import com.presidentsimulator.game.ui.components.NssCardImages
import com.presidentsimulator.game.ui.theme.NssBackground
import com.presidentsimulator.game.viewmodel.GameViewModel
import kotlin.math.roundToInt

@Composable
fun MilitaryScreen(
    state: GameState,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableStateOf("MANAGEMENT") }
    val tabs = listOf("MANAGEMENT", "MILITARY MACHINERY")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NssBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
    ) {
        NssTabBar(tabs = tabs, selectedTab = selectedTab, onTabSelected = { selectedTab = it })

        if (selectedTab == "MANAGEMENT") {
            MilitaryManagementTab(state)
        } else {
            MilitaryMachineryTab(state, viewModel)
        }
    }
}

@Composable
private fun MilitaryManagementTab(state: GameState) {
    val military = state.military
    val items = listOf(
        GameTileData("Infantry", "${military.personnel.toInt()} units", Icons.Default.Security, Color(0xFF43A047), NssCardImages.INFANTRY) {},
        GameTileData("Tanks", "${military.tanks}", Icons.Default.Security, Color(0xFFD32F2F), NssCardImages.ARMORED) {},
        GameTileData("Artillery", "${military.tanks / 2}", Icons.Default.FilterCenterFocus, Color(0xFF1565C0), NssCardImages.ARTILLERY) {},
        GameTileData("Destroyers", "${military.ships}", Icons.Default.DirectionsBoat, Color(0xFF0288D1), NssCardImages.DESTROYER) {},
        GameTileData("Submarines", "${military.ships / 2}", Icons.Default.DirectionsBoat, Color(0xFF546E7A), NssCardImages.SUBMARINE) {},
        GameTileData("Fighters", "${military.jets}", Icons.Default.Flight, Color(0xFF1E88E5), NssCardImages.FIGHTER) {},
        GameTileData("Bombers", "${military.jets / 3}", Icons.Default.Flight, Color(0xFF00ACC1), NssCardImages.BOMBER) {},
    )

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 130.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(items) { item -> GameTile(item) }
    }
}

@Composable
private fun MilitaryMachineryTab(state: GameState, viewModel: GameViewModel) {
    val context = LocalContext.current
    val audio = remember(context) { GameAudioManager.getInstance(context) }
    
    val items = listOf(
        GameTileData("Recruit Infantry", "Build", Icons.Default.PersonAdd, Color(0xFF43A047), NssCardImages.INFANTRY) { 
            viewModel.recruitPersonnel(100L)
            audio.playBuildSuccess()
        },
        GameTileData("Buy Tanks", "Build", Icons.Default.Security, Color(0xFFD32F2F), NssCardImages.ARMORED) { 
            viewModel.purchaseMilitaryHardware(MilitaryHardware.TANKS, 10)
            audio.playBuildSuccess()
        },
        GameTileData("Buy Artillery", "Build", Icons.Default.FilterCenterFocus, Color(0xFF1565C0), NssCardImages.ARTILLERY) { 
            viewModel.purchaseMilitaryHardware(MilitaryHardware.TANKS, 5) // Simplified mapping
            audio.playBuildSuccess()
        },
        GameTileData("Build Ships", "Build", Icons.Default.DirectionsBoat, Color(0xFF0288D1), NssCardImages.DESTROYER) { 
            viewModel.purchaseMilitaryHardware(MilitaryHardware.NAVAL_SHIPS, 1)
            audio.playBuildSuccess()
        },
        GameTileData("Build Jets", "Build", Icons.Default.Flight, Color(0xFF1E88E5), NssCardImages.FIGHTER) { 
            viewModel.purchaseMilitaryHardware(MilitaryHardware.FIGHTER_JETS, 5)
            audio.playBuildSuccess()
        }
    )

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 130.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(items) { item -> GameTile(item) }
    }
}
