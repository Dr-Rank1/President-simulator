package com.presidentsimulator.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.presidentsimulator.game.data.TechCatalog
import com.presidentsimulator.game.data.Technology
import androidx.compose.foundation.shape.RoundedCornerShape
import com.presidentsimulator.game.ui.components.NssGameBar
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import com.presidentsimulator.game.ui.theme.NssAccent
import com.presidentsimulator.game.ui.theme.NssBorder
import com.presidentsimulator.game.ui.theme.NssCard
import com.presidentsimulator.game.ui.theme.NssEmerald
import com.presidentsimulator.game.ui.theme.NssForeground
import com.presidentsimulator.game.ui.theme.NssMutedForeground
import com.presidentsimulator.game.ui.theme.NssRed
import com.presidentsimulator.game.viewmodel.AdvancementViewModel
import com.presidentsimulator.game.viewmodel.GameViewModel

private val CardShape = RoundedCornerShape(4.dp)

@Composable
fun ScienceScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val research = state.research
    val sciencePerMonth = viewModel.projectedSciencePerTick()
    val activeTech = research.activeTechnology
    val queuedTech = research.queuedTechnology

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF060D14))
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
    ) {
        // Screen title bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF030A12))
                .border(width = 1.dp, color = NssBorder, shape = RoundedCornerShape(0.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "SCIENCE",
                color = NssAccent,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "${research.unlockedTechIds.size} unlocked · ${research.sciencePoints} pts · +$sciencePerMonth/mo",
                color = NssMutedForeground,
                fontSize = 9.sp,
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                SciSectionTitle("Current Research")
            }
            item {
                CurrentResearchPanel(
                    activeTech = activeTech,
                    queuedTech = queuedTech,
                    progressPercent = research.progressPercent(),
                    daysRemaining = research.daysRemaining(sciencePerMonth),
                    extraFundingTier = research.extraFundingTier,
                    canAllocateFunding = viewModel.canAllocateExtraResearchFunding(),
                    fundingCostLabel = AdvancementViewModel.EXTRA_RESEARCH_FUNDING_COST.formatScienceBudget(),
                    onAllocateFunding = viewModel::allocateExtraResearchFunding,
                )
            }
            item { SciSectionTitle("Tech Tree") }
            items(TechCatalog.all, key = { it.id }) { tech ->
                TechTreeRow(
                    tech = tech,
                    isUnlocked = research.isUnlocked(tech.id),
                    isActive = research.activeTechId == tech.id,
                    isQueued = research.queuedTechId == tech.id,
                    prerequisitesMet = research.prerequisitesMet(tech),
                    canStart = viewModel.canStartResearch(tech.id),
                    canUnlock = viewModel.canUnlockTechnology(tech.id),
                    hasActiveResearch = research.activeTechId != null,
                    onStartResearch = { viewModel.startResearch(tech.id) },
                    onUnlock = { viewModel.unlockTechnology(tech.id) },
                )
            }
        }
    }
}

@Composable
private fun SciSectionTitle(text: String) {
    Text(
        text = text.uppercase(),
        fontSize = 9.sp,
        fontWeight = FontWeight.Black,
        color = NssAccent,
        letterSpacing = 1.5.sp,
        modifier = Modifier.padding(bottom = 4.dp, top = 4.dp),
    )
}

@Composable
private fun CurrentResearchPanel(
    activeTech: Technology?,
    queuedTech: Technology?,
    progressPercent: Float,
    daysRemaining: Int,
    extraFundingTier: Int,
    canAllocateFunding: Boolean,
    fundingCostLabel: String,
    onAllocateFunding: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(NssCard)
            .border(1.dp, NssBorder, CardShape)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text("CURRENT RESEARCH", color = NssAccent, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)

        if (activeTech == null) {
            Text("No active research project", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = NssForeground)
            if (queuedTech != null) {
                Text(
                    "Queued next: ${queuedTech.name}",
                    fontSize = 9.sp,
                    color = NssEmerald,
                )
            } else {
                Text("Select a technology below to begin.", fontSize = 9.sp, color = NssMutedForeground)
            }
        } else {
            Text(activeTech.name, fontWeight = FontWeight.Black, fontSize = 12.sp, color = NssForeground)
            Text(activeTech.effect.description, fontSize = 8.sp, color = NssMutedForeground)
            Text(activeTech.category.displayName, fontSize = 8.sp, color = NssMutedForeground)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Research Progress", fontSize = 9.sp, color = NssMutedForeground)
                Text("${progressPercent.toInt()}%", fontWeight = FontWeight.Black, color = NssAccent, fontSize = 9.sp)
            }
            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(NssBorder),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth((progressPercent / 100f).coerceIn(0f, 1f))
                        .clip(RoundedCornerShape(2.dp))
                        .background(NssAccent),
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Est. remaining", fontSize = 8.sp, color = NssMutedForeground)
                Text(
                    if (daysRemaining == 0) "< 1 day" else "$daysRemaining days",
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    color = Color.White,
                )
            }
            Text(
                text = "Extra funding tier: $extraFundingTier / ${com.presidentsimulator.game.data.ResearchState.MAX_EXTRA_FUNDING_TIER}",
                fontSize = 8.sp,
                color = NssMutedForeground,
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CardShape)
                    .background(if (canAllocateFunding) NssAccent else NssAccent.copy(alpha = 0.35f))
                    .clickable(enabled = canAllocateFunding, onClick = onAllocateFunding)
                    .padding(10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "⬆ ALLOCATE EXTRA FUNDING ($fundingCostLabel)",
                    color = Color(0xFF000000),
                    fontWeight = FontWeight.Black,
                    fontSize = 9.sp,
                    letterSpacing = 1.sp,
                )
            }
            if (queuedTech != null) {
                Text(
                    "Up next: ${queuedTech.name}",
                    fontSize = 8.sp,
                    color = NssEmerald,
                )
            }
        }
    }
}

@Composable
private fun TechTreeRow(
    tech: Technology,
    isUnlocked: Boolean,
    isActive: Boolean,
    isQueued: Boolean,
    prerequisitesMet: Boolean,
    canStart: Boolean,
    canUnlock: Boolean,
    hasActiveResearch: Boolean,
    onStartResearch: () -> Unit,
    onUnlock: () -> Unit,
) {
    val status = when {
        isUnlocked -> "UNLOCKED"
        isActive -> "IN PROGRESS"
        isQueued -> "QUEUED"
        !prerequisitesMet -> "LOCKED"
        else -> "AVAILABLE"
    }
    val borderColor = when {
        isActive -> NssAccent
        isUnlocked -> NssEmerald.copy(alpha = 0.5f)
        else -> NssBorder
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(NssCard)
            .border(1.dp, borderColor, CardShape)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(tech.name, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = NssForeground)
                Text(tech.category.displayName, fontSize = 8.sp, color = NssMutedForeground)
            }
            val statusColor = when (status) {
                "UNLOCKED" -> NssEmerald
                "IN PROGRESS" -> NssAccent
                "QUEUED" -> NssMutedForeground
                "LOCKED" -> NssRed
                else -> NssMutedForeground
            }
            Text(
                text = status,
                color = statusColor,
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(2.dp))
                    .background(statusColor.copy(alpha = 0.12f))
                    .padding(horizontal = 5.dp, vertical = 2.dp),
            )
        }
        Text(tech.effect.description, fontSize = 8.sp, color = NssMutedForeground)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Cost", fontSize = 9.sp, color = NssMutedForeground)
            Text("${tech.scienceCost} pts", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        if (tech.prerequisiteIds.isNotEmpty() && !prerequisitesMet) {
            Text(
                text = "Requires: " + tech.prerequisiteIds.joinToString { id -> TechCatalog.byId(id)?.name ?: id },
                fontSize = 8.sp,
                color = NssAccent,
            )
        }
        if (!isUnlocked && !isActive && !isQueued) {
            Spacer(Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CardShape)
                    .background(if (canStart) NssEmerald else NssMutedForeground.copy(alpha = 0.2f))
                    .clickable(enabled = canStart, onClick = onStartResearch)
                    .padding(vertical = 7.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = when {
                        !canStart -> "Cannot Start"
                        hasActiveResearch -> "⏳ Queue Research"
                        else -> "▶ Start Research"
                    },
                    color = if (canStart) Color(0xFF000000) else NssMutedForeground,
                    fontWeight = FontWeight.Black,
                    fontSize = 9.sp,
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CardShape)
                    .background(if (canUnlock) NssAccent else NssMutedForeground.copy(alpha = 0.2f))
                    .clickable(enabled = canUnlock, onClick = onUnlock)
                    .padding(vertical = 7.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (canUnlock) "⚡ Unlock Instantly (${tech.scienceCost} pts)" else "Instant unlock unavailable",
                    color = if (canUnlock) Color(0xFF000000) else NssMutedForeground,
                    fontWeight = FontWeight.Black,
                    fontSize = 9.sp,
                )
            }
        }
    }
}

private fun Long.formatScienceBudget(): String = when {
    this >= 1_000_000_000L -> "$${"%.1f".format(this / 1_000_000_000.0)}B"
    this >= 1_000_000L -> "$${"%.1f".format(this / 1_000_000.0)}M"
    else -> "$$this"
}
