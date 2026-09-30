package com.presidentsimulator.game.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.presidentsimulator.game.audio.GameAudioManager
import com.presidentsimulator.game.audio.playBuildSuccess
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.MilitaryHardware
import com.presidentsimulator.game.ui.navigation.GameDestination
import com.presidentsimulator.game.ui.theme.NssBackground
import com.presidentsimulator.game.viewmodel.GameViewModel

import com.presidentsimulator.game.ui.components.GameTile
import com.presidentsimulator.game.ui.components.GameTileData
import com.presidentsimulator.game.ui.components.NssCardImages

@Composable
fun MainDashboardScreen(
    state: GameState,
    onNavigate: (GameDestination) -> Unit,
    activeCategory: String = "General expenses",
    onCategoryChanged: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val audio = remember(context) { GameAudioManager.getInstance(context) }
    var showDefenceModal by remember { mutableStateOf(false) }

    // Modal when Ministry of Defence is clicked (Exact replica of video 06:50)
    if (showDefenceModal) {
        Dialog(onDismissRequest = { showDefenceModal = false }) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Blue button: MINISTRY OF DEFENCE
                    DefenceModalButton(
                        text = "MINISTRY OF DEFENCE",
                        color = Color(0xFF0288D1),
                        icon = Icons.Default.AccountBalance
                    ) {
                        showDefenceModal = false
                        onNavigate(GameDestination.Military)
                    }

                    // Red button: THE GENERAL STAFF
                    DefenceModalButton(
                        text = "THE GENERAL STAFF",
                        color = Color(0xFFD32F2F),
                        icon = Icons.Default.Casino
                    ) {
                        showDefenceModal = false
                        onNavigate(GameDestination.Military)
                    }

                    // Green button: STATISTICS OF ARMIES
                    DefenceModalButton(
                        text = "STATISTICS OF ARMIES",
                        color = Color(0xFF43A047),
                        icon = Icons.Default.BarChart
                    ) {
                        showDefenceModal = false
                        onNavigate(GameDestination.Analytics)
                    }
                }
            }
        }
    }

    // 20 Ministry Tiles for "General expenses" (Exact list and colors from video frames 01:37 - 02:04, 07:51)
    val expenseTiles = listOf(
        GameTileData(
            title = "Police",
            amountString = "-${formatDaily(state.internalSecurity.monthlyUpkeep)}/day",
            icon = Icons.Default.LocalPolice, imageUrl = NssCardImages.BANNER_DOMESTIC,
            topColor = Color(0xFF1565C0),
            onClick = { onNavigate(GameDestination.SecretService) }
        ),
        GameTileData(
            title = "Energy industry",
            amountString = "-${formatDaily(state.economy.upkeep / 4)}/day",
            icon = Icons.Default.Bolt, imageUrl = NssCardImages.ENERGY,
            topColor = Color(0xFFE64A19),
            onClick = { onNavigate(GameDestination.Economy) }
        ),
        GameTileData(
            title = "Ecology",
            amountString = "-${formatDaily(state.society.totalMinistryUpkeep / 5)}/day",
            icon = Icons.Default.Eco, imageUrl = NssCardImages.AGRICULTURE,
            topColor = Color(0xFF43A047),
            onClick = { onNavigate(GameDestination.LawsSociety) }
        ),
        GameTileData(
            title = "State Emergency Service",
            amountString = "-${formatDaily(state.internalSecurity.monthlyUpkeep / 2)}/day",
            icon = Icons.Default.LocalFireDepartment,
            topColor = Color(0xFF0288D1),
            onClick = { onNavigate(GameDestination.SecretService) }
        ),
        GameTileData(
            title = "Infrastructure",
            amountString = "-${formatDaily(state.economy.upkeep / 3)}/day",
            icon = Icons.Default.Apartment, imageUrl = NssCardImages.AGRICULTURE,
            topColor = Color(0xFF546E7A),
            onClick = { onNavigate(GameDestination.Economy) }
        ),
        GameTileData(
            title = "Housing services and utilities",
            amountString = "-${formatDaily(state.economy.upkeep / 5)}/day",
            icon = Icons.Default.Home,
            topColor = Color(0xFF00838F),
            onClick = { onNavigate(GameDestination.Economy) }
        ),
        GameTileData(
            title = "Culture",
            amountString = "-${formatDaily(state.society.totalMinistryUpkeep / 6)}/day",
            icon = Icons.Default.TheaterComedy,
            topColor = Color(0xFFC2185B),
            onClick = { onNavigate(GameDestination.LawsSociety) }
        ),
        GameTileData(
            title = "Social policy",
            amountString = "-${formatDaily(state.society.totalMinistryUpkeep / 3)}/day",
            icon = Icons.Default.Groups,
            topColor = Color(0xFF7B1FA2),
            onClick = { onNavigate(GameDestination.LawsSociety) }
        ),
        GameTileData(
            title = "Sports",
            amountString = "-${formatDaily(state.society.totalMinistryUpkeep / 7)}/day",
            icon = Icons.Default.EmojiEvents,
            topColor = Color(0xFF1E88E5),
            onClick = { onNavigate(GameDestination.LawsSociety) }
        ),
        GameTileData(
            title = "Science and researches",
            amountString = "-${formatDaily(state.society.totalMinistryUpkeep / 4)}/day",
            icon = Icons.Default.Science,
            topColor = Color(0xFF00ACC1),
            onClick = { onNavigate(GameDestination.Science) }
        ),
        GameTileData(
            title = "Ministry of Defence",
            amountString = "-${formatDaily(state.military.monthlyUpkeep)}/day",
            icon = Icons.Default.Shield,
            topColor = Color(0xFF2E7D32),
            onClick = { showDefenceModal = true }
        ),
        GameTileData(
            title = "Education",
            amountString = "-${formatDaily(state.society.totalMinistryUpkeep / 3)}/day",
            icon = Icons.Default.School, imageUrl = NssCardImages.SERVICES,
            topColor = Color(0xFFEF6C00),
            onClick = { onNavigate(GameDestination.LawsSociety) }
        ),
        GameTileData(
            title = "Healthcare",
            amountString = "-${formatDaily(state.society.totalMinistryUpkeep / 3)}/day",
            icon = Icons.Default.LocalHospital,
            topColor = Color(0xFF0097A7),
            onClick = { onNavigate(GameDestination.LawsSociety) }
        ),
        GameTileData(
            title = "Ministry of Justice",
            amountString = "-${formatDaily(state.legal.totalUpkeep)}/day",
            icon = Icons.Default.Gavel, imageUrl = NssCardImages.PARLIAMENT,
            topColor = Color(0xFFFBC02D),
            onClick = { onNavigate(GameDestination.LawsSociety) }
        ),
        GameTileData(
            title = "Population employment",
            amountString = "-${formatDaily(state.economy.upkeep / 2)}/day",
            icon = Icons.Default.Engineering, imageUrl = NssCardImages.INDUSTRY,
            topColor = Color(0xFF00897B),
            onClick = { onNavigate(GameDestination.Economy) }
        ),
        GameTileData(
            title = "State Security Service",
            amountString = "-${formatDaily(state.internalSecurity.monthlyUpkeep / 2)}/day",
            icon = Icons.Default.Security, imageUrl = NssCardImages.BANNER_INTELLIGENCE,
            topColor = Color(0xFFFFA000),
            onClick = { onNavigate(GameDestination.SecretService) }
        ),
        GameTileData(
            title = "Ministry of Foreign Affairs",
            amountString = "-${formatDaily(0L)}/day",
            icon = Icons.Default.Public, imageUrl = NssCardImages.BANNER_FOREIGN,
            topColor = Color(0xFF03A9F4),
            onClick = { onNavigate(GameDestination.Diplomacy) }
        ),
        GameTileData(
            title = "International organizations",
            amountString = "-${formatDaily(18000L * 30L)}/day",
            icon = Icons.Default.AccountBalance, imageUrl = NssCardImages.BANNER_FOREIGN,
            topColor = Color(0xFF455A64),
            onClick = { onNavigate(GameDestination.Governance) }
        ),
        GameTileData(
            title = "Religions",
            amountString = "-0/day",
            icon = Icons.Default.Language, imageUrl = NssCardImages.PARLIAMENT,
            topColor = Color(0xFFFFB300),
            onClick = { onNavigate(GameDestination.LawsSociety) }
        ),
        GameTileData(
            title = "Ideology",
            amountString = "-1,000",
            icon = Icons.Default.Gavel, imageUrl = NssCardImages.PARLIAMENT,
            topColor = Color(0xFF7CB342),
            onClick = { onNavigate(GameDestination.LawsSociety) }
        )
    )

    // Income Tiles (From video frame 11:58)
    val incomeTiles = listOf(
        GameTileData(
            title = "Taxes",
            amountString = "+${formatDaily(state.economy.taxRevenue(state.vitals.population))}/day",
            icon = Icons.Default.Calculate, imageUrl = NssCardImages.BANNER_ECONOMY,
            topColor = Color(0xFF8E24AA),
            onClick = { onNavigate(GameDestination.Economy) }
        ),
        GameTileData(
            title = "Electrical energy industry",
            amountString = "+75,860/day",
            icon = Icons.Default.Bolt, imageUrl = NssCardImages.ENERGY,
            topColor = Color(0xFF43A047),
            onClick = { onNavigate(GameDestination.Economy) }
        ),
        GameTileData(
            title = "Fuel industry",
            amountString = "+173,088/day",
            icon = Icons.Default.LocalGasStation, imageUrl = NssCardImages.ENERGY,
            topColor = Color(0xFFEF6C00),
            onClick = { onNavigate(GameDestination.Economy) }
        ),
        GameTileData(
            title = "Ferrous industry",
            amountString = "+254,630/day",
            icon = Icons.Default.PrecisionManufacturing,
            topColor = Color(0xFF546E7A),
            onClick = { onNavigate(GameDestination.Economy) }
        ),
        GameTileData(
            title = "Nonferrous industry",
            amountString = "+22,000/day",
            icon = Icons.Default.Landscape,
            topColor = Color(0xFF795548),
            onClick = { onNavigate(GameDestination.Economy) }
        ),
        GameTileData(
            title = "Machine manufacturing",
            amountString = "+118,500/day",
            icon = Icons.Default.Build,
            topColor = Color(0xFF1E88E5),
            onClick = { onNavigate(GameDestination.Economy) }
        ),
        GameTileData(
            title = "Chemical industry",
            amountString = "+64,200/day",
            icon = Icons.Default.Science,
            topColor = Color(0xFF00ACC1),
            onClick = { onNavigate(GameDestination.Economy) }
        ),
        GameTileData(
            title = "Woodworking industry",
            amountString = "+31,100/day",
            icon = Icons.Default.Park,
            topColor = Color(0xFF2E7D32),
            onClick = { onNavigate(GameDestination.Economy) }
        ),
        GameTileData(
            title = "Agriculture & Food",
            amountString = "+89,000/day",
            icon = Icons.Default.Agriculture,
            topColor = Color(0xFFFFB300),
            onClick = { onNavigate(GameDestination.Economy) }
        ),
        GameTileData(
            title = "Tourism & Services",
            amountString = "+42,300/day",
            icon = Icons.Default.FlightTakeoff, imageUrl = NssCardImages.SERVICES,
            topColor = Color(0xFF00897B),
            onClick = { onNavigate(GameDestination.Economy) }
        )
    )

    val currentTiles = when (activeCategory) {
        "Income" -> incomeTiles
        else -> expenseTiles
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 130.dp),
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F1E6))
            .padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
            coil.compose.AsyncImage(
                model = NssCardImages.MAP,
                contentDescription = "World Map",
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
            )
        }
        items(currentTiles) { item ->
            GameTile(item)
        }
    }
}

@Composable
private fun DefenceModalButton(
    text: String,
    color: Color,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(color)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

private fun formatDaily(monthlyAmount: Long): String {
    val daily = kotlin.math.abs(monthlyAmount) / 30L
    return when {
        daily >= 1_000_000_000L -> String.format("%.1fB", daily / 1_000_000_000.0)
        daily >= 1_000_000L -> String.format("%.1fM", daily / 1_000_000.0)
        daily >= 1_000L -> String.format("%,d", daily)
        else -> daily.toString()
    }
}
