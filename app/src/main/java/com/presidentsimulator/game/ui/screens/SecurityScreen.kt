package com.presidentsimulator.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.presidentsimulator.game.audio.GameAudioManager
import com.presidentsimulator.game.audio.playBuildSuccess
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.SecurityProtocol
import com.presidentsimulator.game.ui.components.GameTile
import com.presidentsimulator.game.ui.components.GameTileData
import com.presidentsimulator.game.ui.components.NssCardImages
import com.presidentsimulator.game.ui.components.NssTabBar
import com.presidentsimulator.game.ui.theme.NssBackground
import com.presidentsimulator.game.viewmodel.GameViewModel

@Composable
fun SecurityScreen(
    state: GameState,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize().background(NssBackground).windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))) {
        NssTabBar(tabs = listOf("STATE SECURITY"), selectedTab = "STATE SECURITY", onTabSelected = {})

        val context = LocalContext.current
        val audio = remember(context) { GameAudioManager.getInstance(context) }
        
        val protocolItems = SecurityProtocol.entries.map { protocol ->
            val isActive = state.internalSecurity.isProtocolActive(protocol)
            val topColor = if (isActive) Color(0xFF43A047) else Color(0xFF1565C0)
            val statusText = if (isActive) "Active" else "Activate"
            val icon = if (isActive) Icons.Default.Warning else Icons.Default.Security
            
            GameTileData(protocol.displayName, statusText, icon, topColor, NssCardImages.BANNER_DOMESTIC) {
                viewModel.toggleSecurityProtocol(protocol)
                if (!isActive) audio.playBuildSuccess()
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 130.dp),
            modifier = Modifier.fillMaxSize().padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(protocolItems) { item -> GameTile(item) }
        }
    }
}
