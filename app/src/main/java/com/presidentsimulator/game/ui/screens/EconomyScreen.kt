package com.presidentsimulator.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.viewmodel.GameViewModel
import com.presidentsimulator.game.ui.components.GameTile
import com.presidentsimulator.game.ui.components.GameTileData
import com.presidentsimulator.game.ui.components.NssCardImages

@Composable
fun EconomyScreen(
    state: GameState,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val expenseTiles = listOf(
        GameTileData(
            title = "Police",
            amountString = "-1,299,774/day",
            icon = Icons.Default.LocalPolice, imageUrl = NssCardImages.BANNER_INTELLIGENCE,
            topColor = Color(0xFF0288D1),
            onClick = {}
        ),
        GameTileData(
            title = "Energy industry",
            amountString = "-93,998/day",
            icon = Icons.Default.Bolt, imageUrl = NssCardImages.ENERGY,
            topColor = Color(0xFFD32F2F),
            onClick = {}
        ),
        GameTileData(
            title = "Ecology",
            amountString = "-66,561/day",
            icon = Icons.Default.Eco, imageUrl = NssCardImages.AGRICULTURE,
            topColor = Color(0xFF1565C0),
            onClick = {}
        ),
        GameTileData(
            title = "State Emergency Service",
            amountString = "-313,399/day",
            icon = Icons.Default.LocalFireDepartment, imageUrl = NssCardImages.SERVICES,
            topColor = Color(0xFFE64A19),
            onClick = {}
        ),
        GameTileData(
            title = "Infrastructure",
            amountString = "-20,125/day",
            icon = Icons.Default.Domain, imageUrl = NssCardImages.INDUSTRY,
            topColor = Color(0xFF43A047),
            onClick = {}
        )
    )

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 130.dp),
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F1E6))
            .padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(expenseTiles) { item ->
            GameTile(item)
        }
    }
}
