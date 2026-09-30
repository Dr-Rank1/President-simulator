package com.presidentsimulator.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.viewmodel.GameViewModel
import com.presidentsimulator.game.ui.components.PresidentCard
import com.presidentsimulator.game.ui.components.PresidentCardData
import com.presidentsimulator.game.ui.components.BadgeMode
import com.presidentsimulator.game.ui.components.NssCardImages

@Composable
fun ScienceScreen(
    state: GameState,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        PresidentCardData(
            title = "Electrical energy industry",
            imageUrl = NssCardImages.ENERGY,
            badgeMode = BadgeMode.LevelCircle,
            badgeText = "0",
            stats = listOf(
                "Level" to "0",
                "Cost" to "-100/day",
                "Bonus" to "+0%",
                "Time" to "60 days"
            ),
            onClick = { }
        ),
        PresidentCardData(
            title = "Fuel industry",
            imageUrl = NssCardImages.ENERGY,
            badgeMode = BadgeMode.LevelCircle,
            badgeText = "0",
            stats = listOf(
                "Level" to "0",
                "Cost" to "-100/day",
                "Bonus" to "+0%",
                "Time" to "60 days"
            ),
            onClick = { }
        ),
        PresidentCardData(
            title = "Ferrous industry",
            imageUrl = NssCardImages.INDUSTRY,
            badgeMode = BadgeMode.LevelCircle,
            badgeText = "0",
            stats = listOf(
                "Level" to "0",
                "Cost" to "-100/day",
                "Bonus" to "+0%",
                "Time" to "60 days"
            ),
            onClick = { }
        ),
        PresidentCardData(
            title = "Chemical industry",
            imageUrl = NssCardImages.SERVICES,
            badgeMode = BadgeMode.LevelCircle,
            badgeText = "0",
            stats = listOf(
                "Level" to "0",
                "Cost" to "-100/day",
                "Bonus" to "+0%",
                "Time" to "60 days"
            ),
            onClick = { }
        ),
        PresidentCardData(
            title = "Aerospace technology",
            imageUrl = NssCardImages.BOMBER,
            badgeMode = BadgeMode.LevelCircle,
            badgeText = "0",
            stats = listOf(
                "Level" to "0",
                "Cost" to "-100/day",
                "Bonus" to "+0%",
                "Time" to "60 days"
            ),
            onClick = { }
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F1E6))
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items) { data ->
                PresidentCard(data)
            }
        }
    }
}
