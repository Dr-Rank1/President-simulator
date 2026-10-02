package com.presidentsimulator.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.presidentsimulator.game.audio.GameAudioManager
import com.presidentsimulator.game.audio.playBuildSuccess
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.LawCatalog
import com.presidentsimulator.game.data.LawCategory
import com.presidentsimulator.game.ui.components.GameTile
import com.presidentsimulator.game.ui.components.GameTileData
import com.presidentsimulator.game.ui.components.NssCardImages
import com.presidentsimulator.game.ui.components.NssCardShape
import com.presidentsimulator.game.ui.components.NssPanel
import com.presidentsimulator.game.ui.components.NssTabBar
import com.presidentsimulator.game.ui.theme.NssAccent
import com.presidentsimulator.game.ui.theme.NssBackground
import com.presidentsimulator.game.ui.theme.NssEmerald
import com.presidentsimulator.game.ui.theme.NssForeground
import com.presidentsimulator.game.ui.theme.NssMutedForeground
import com.presidentsimulator.game.ui.theme.NssOnPhoto
import com.presidentsimulator.game.ui.theme.NssPrimary
import com.presidentsimulator.game.ui.theme.NssRed
import com.presidentsimulator.game.viewmodel.GameViewModel
import com.presidentsimulator.game.viewmodel.ProductionLawEngine
import com.presidentsimulator.game.viewmodel.toBudgetString
import kotlin.math.roundToInt

@Composable
fun LawsScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf("DEFENCE") }
    // Tab label maps 1:1 onto the real LawCategory it shows. The previous label
    // ("CONSTITUTION") displayed MILITARY laws, which misreported the content.
    val tabs = listOf("DEFENCE", "ECONOMY", "SOCIAL")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NssBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
    ) {
        NssTabBar(tabs = tabs, selectedTab = selectedTab, onTabSelected = { selectedTab = it })

        val context = LocalContext.current
        val audio = remember(context) { GameAudioManager.getInstance(context) }

        val category = when (selectedTab) {
            "ECONOMY" -> LawCategory.ECONOMIC
            "SOCIAL" -> LawCategory.SOCIAL
            else -> LawCategory.MILITARY
        }

        // Parliamentary business already in flight: rush, buy support, or withdraw.
        PendingBillsPanel(state = state, viewModel = viewModel)

        val laws = LawCatalog.byCategory(category)
        val lawItems = laws.map { law ->
            val isActive = state.legal.isActive(law.id)
            val pending = state.legal.pendingLaws.firstOrNull { it.lawId == law.id }
            val topColor = when {
                isActive -> Color(0xFF43A047)
                pending != null -> Color(0xFF1565C0)
                else -> Color(0xFFE64A19)
            }
            val statusText = when {
                isActive -> "Active"
                pending != null -> "In parliament"
                else -> "Enact"
            }
            val imageUrl = when (category) {
                LawCategory.MILITARY -> NssCardImages.BANNER_DEFENSE
                LawCategory.ECONOMIC -> NssCardImages.BANNER_ECONOMY
                LawCategory.SOCIAL -> NssCardImages.PARLIAMENT
            }

            GameTileData(law.name, statusText, Icons.Default.Gavel, topColor, imageUrl) {
                when {
                    isActive -> viewModel.repealLaw(law.id)
                    pending != null -> viewModel.rushPendingLaw(law.id)
                    else -> {
                        viewModel.enactLaw(law.id)
                        audio.playBuildSuccess()
                    }
                }
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 130.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(lawItems) { item -> GameTile(item) }
        }
    }
}

@Composable
private fun PendingBillsPanel(state: GameState, viewModel: GameViewModel) {
    val pending = state.legal.pendingLaws
    if (pending.isEmpty()) return

    NssPanel(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 4.dp),
    ) {
        Text(
            "PARLIAMENT",
            fontWeight = FontWeight.Black,
            fontSize = 9.sp,
            color = NssPrimary,
            letterSpacing = 8.sp,
        )
        pending.forEach { bill ->
            val law = LawCatalog.byId(bill.lawId) ?: return@forEach
            val support = viewModel.parliamentSupportFor(bill.lawId).roundToInt()
            val verb = if (bill.enabling) "enacting" else "repealing"
            Text(
                text = "${law.name} · $verb · ${bill.ticksRemaining}mo left · support $support% · " +
                    "compromises ${bill.compromises}/${ProductionLawEngine.MAX_BILL_COMPROMISES}",
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                color = NssForeground,
                modifier = Modifier.padding(top = 5.dp),
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                BillAction(
                    label = "Rush · ${ProductionLawEngine.RUSH_LAW_COST.toBudgetString()}",
                    enabled = state.vitals.budget >= ProductionLawEngine.RUSH_LAW_COST,
                    color = NssEmerald,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.rushPendingLaw(bill.lawId) },
                )
                if (bill.enabling) {
                    BillAction(
                        label = "Compromise · ${ProductionLawEngine.BILL_COMPROMISE_COST.toBudgetString()}",
                        enabled = state.vitals.budget >= ProductionLawEngine.BILL_COMPROMISE_COST &&
                            bill.compromises < ProductionLawEngine.MAX_BILL_COMPROMISES,
                        color = NssAccent,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.negotiatePendingLaw(bill.lawId) },
                    )
                }
                BillAction(
                    label = "Withdraw",
                    enabled = true,
                    color = NssRed,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.cancelPendingLaw(bill.lawId) },
                )
            }
        }
    }
}

@Composable
private fun BillAction(
    label: String,
    enabled: Boolean,
    color: Color,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Text(
        text = label,
        modifier = modifier
            .clip(NssCardShape)
            .background(if (enabled) color else NssMutedForeground.copy(alpha = 0.3f))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 6.dp),
        color = NssOnPhoto,
        fontWeight = FontWeight.Bold,
        fontSize = 8.sp,
        textAlign = TextAlign.Center,
    )
}
