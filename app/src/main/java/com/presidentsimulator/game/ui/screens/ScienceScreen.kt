package com.presidentsimulator.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.ResearchState
import com.presidentsimulator.game.data.TechCatalog
import com.presidentsimulator.game.data.TechCategory
import com.presidentsimulator.game.data.Technology
import com.presidentsimulator.game.ui.components.NssCardShape
import com.presidentsimulator.game.ui.components.NssGameBar
import com.presidentsimulator.game.ui.components.NssPanel
import com.presidentsimulator.game.ui.components.NssTabBar
import com.presidentsimulator.game.ui.components.nssMinistryScrollPadding
import com.presidentsimulator.game.ui.theme.NssAccent
import com.presidentsimulator.game.ui.theme.NssBackground
import com.presidentsimulator.game.ui.theme.NssEmerald
import com.presidentsimulator.game.ui.theme.NssForeground
import com.presidentsimulator.game.ui.theme.NssMutedForeground
import com.presidentsimulator.game.ui.theme.NssOnPhoto
import com.presidentsimulator.game.ui.theme.NssPrimary
import com.presidentsimulator.game.ui.theme.NssRed
import com.presidentsimulator.game.viewmodel.AdvancementEngine
import com.presidentsimulator.game.viewmodel.GameViewModel
import com.presidentsimulator.game.viewmodel.toBudgetString
import kotlin.math.roundToInt

/**
 * Research ministry: reads [TechCatalog] and [GameState.research] directly so every
 * card reflects real science points, prerequisites, progress, and funding state.
 */
@Composable
fun ScienceScreen(
    state: GameState,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier,
) {
    val research = state.research
    var selectedTab by remember { mutableStateOf(TechCategory.ECONOMY.displayName.uppercase()) }
    val tabs = TechCategory.entries.map { it.displayName.uppercase() }
    val category = TechCategory.entries.firstOrNull { it.displayName.uppercase() == selectedTab }
        ?: TechCategory.ECONOMY

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NssBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
    ) {
        NssTabBar(
            tabs = tabs,
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .nssMinistryScrollPadding()
                .padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            ResearchStatusPanel(state = state, viewModel = viewModel)

            Text(
                text = "${category.displayName.uppercase()} TECHNOLOGIES",
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = NssPrimary,
                letterSpacing = 8.sp,
                modifier = Modifier.padding(top = 4.dp),
            )

            TechCatalog.byCategory(category).forEach { tech ->
                TechRow(tech = tech, state = state, viewModel = viewModel)
            }

            ResearchFacilityPanel(state = state, viewModel = viewModel)
        }
    }
}

@Composable
private fun ResearchStatusPanel(state: GameState, viewModel: GameViewModel) {
    val research = state.research
    val active = research.activeTechnology

    NssPanel(modifier = Modifier.fillMaxWidth()) {
        Text(
            "NATIONAL RESEARCH",
            fontWeight = FontWeight.Black,
            fontSize = 9.sp,
            color = NssPrimary,
            letterSpacing = 8.sp,
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Stat("Science", "${research.sciencePoints}")
            Stat("Per month", "+${viewModel.projectedSciencePerTick()}")
            Stat("Universities", "${state.society.universities}")
            Stat("Unlocked", "${research.unlockedTechIds.size}/${TechCatalog.all.size}")
        }

        Spacer(modifier = Modifier.height(7.dp))

        when {
            active == null -> Text(
                text = "No active project. Science accumulates in reserve — start a technology below or unlock one outright.",
                fontSize = 9.sp,
                color = NssMutedForeground,
            )
            else -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = active.name,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = NssForeground,
                    )
                    Text(
                        text = "${research.progressPercent().roundToInt()}%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = NssAccent,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                NssGameBar(percent = research.progressPercent(), color = NssAccent, thick = true)
                val remaining = (active.scienceCost - research.researchProgress).coerceAtLeast(0L)
                val days = research.daysRemaining(viewModel.projectedSciencePerTick())
                Text(
                    text = "$remaining science left · ~$days days · funding tier " +
                        "${research.extraFundingTier}/${ResearchState.MAX_EXTRA_FUNDING_TIER}",
                    fontSize = 8.sp,
                    color = NssMutedForeground,
                    modifier = Modifier.padding(top = 3.dp),
                )
            }
        }

        research.queuedTechnology?.let { queued ->
            Text(
                text = "Queued next: ${queued.name}",
                fontSize = 8.sp,
                fontWeight = FontWeight.SemiBold,
                color = NssAccent,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        Spacer(modifier = Modifier.height(7.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {                ScienceAction(
                label = if (viewModel.canAllocateExtraResearchFunding()) {
                    "FUND PROJECT · ${AdvancementEngine.EXTRA_RESEARCH_FUNDING_COST.toBudgetString()}"
                } else if (active == null) {
                    "Fund project — no active project"
                } else if (research.extraFundingTier >= ResearchState.MAX_EXTRA_FUNDING_TIER) {
                    "Fund project — max tier reached"
                } else {
                    "Fund project — need ${AdvancementEngine.EXTRA_RESEARCH_FUNDING_COST.toBudgetString()}"
                },
                enabled = viewModel.canAllocateExtraResearchFunding(),
                color = NssAccent,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.allocateExtraResearchFunding() },
            )
        }
    }
}

@Composable
private fun ResearchFacilityPanel(state: GameState, viewModel: GameViewModel) {
    val canBuild = state.vitals.budget >= AdvancementEngine.UNIVERSITY_COST
    NssPanel(modifier = Modifier.fillMaxWidth()) {
        Text(
            "RESEARCH INFRASTRUCTURE",
            fontWeight = FontWeight.Black,
            fontSize = 9.sp,
            color = NssPrimary,
            letterSpacing = 8.sp,
        )
        Text(
            text = "Each university adds ${AdvancementEngine.SCIENCE_PER_UNIVERSITY.toLong()} science every month. " +
                "Education funding and unlocked Society technologies multiply the total.",
            fontSize = 8.sp,
            color = NssMutedForeground,
            modifier = Modifier.padding(top = 3.dp, bottom = 6.dp),
        )
        ScienceAction(
            label = if (canBuild) {
                "BUILD UNIVERSITY · ${AdvancementEngine.UNIVERSITY_COST.toBudgetString()}"
            } else {
                "Build university — need ${AdvancementEngine.UNIVERSITY_COST.toBudgetString()}"
            },
            enabled = canBuild,
            color = NssPrimary,
            modifier = Modifier.fillMaxWidth(),
            onClick = { viewModel.buildUniversity() },
        )
    }
}

@Composable
private fun TechRow(tech: Technology, state: GameState, viewModel: GameViewModel) {
    val research = state.research
    val unlocked = research.isUnlocked(tech.id)
    val isActive = research.activeTechId == tech.id
    val isQueued = research.queuedTechId == tech.id
    val prereqsMet = research.prerequisitesMet(tech)
    val missing = tech.prerequisiteIds
        .filterNot { it in research.unlockedTechIds }
        .mapNotNull { TechCatalog.byId(it)?.name }

    val status = when {
        unlocked -> "UNLOCKED"
        isActive -> "RESEARCHING"
        isQueued -> "QUEUED"
        !prereqsMet -> "LOCKED"
        else -> "AVAILABLE"
    }
    val statusColor = when {
        unlocked -> NssEmerald
        isActive -> NssAccent
        isQueued -> NssPrimary
        !prereqsMet -> NssRed
        else -> NssMutedForeground
    }

    NssPanel(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = tech.name,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = NssForeground,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = status,
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                color = statusColor,
                letterSpacing = 1.sp,
            )
        }
        Text(
            text = tech.effect.description,
            fontSize = 9.sp,
            color = NssMutedForeground,
            modifier = Modifier.padding(top = 3.dp),
        )
        Text(
            text = "Science cost ${tech.scienceCost}",
            fontSize = 8.sp,
            color = NssMutedForeground,
            modifier = Modifier.padding(top = 2.dp),
        )

        when {
            unlocked -> Text(
                text = "Part of the national technology base.",
                fontSize = 8.sp,
                fontWeight = FontWeight.SemiBold,
                color = NssEmerald,
                modifier = Modifier.padding(top = 5.dp),
            )
            isActive -> {
                Spacer(modifier = Modifier.height(5.dp))
                NssGameBar(percent = research.progressPercent(), color = NssAccent)
                Text(
                    text = "${research.progressPercent().roundToInt()}% complete",
                    fontSize = 8.sp,
                    color = NssMutedForeground,
                    modifier = Modifier.padding(top = 3.dp),
                )
            }
            !prereqsMet -> Text(
                text = "Requires: ${missing.joinToString(", ")}",
                fontSize = 8.sp,
                fontWeight = FontWeight.SemiBold,
                color = NssRed,
                modifier = Modifier.padding(top = 5.dp),
            )
            else -> {
                val shortfall = (tech.scienceCost - research.sciencePoints).coerceAtLeast(0L)
                val canUnlock = viewModel.canUnlockTechnology(tech.id)
                val label = when {
                    canUnlock -> "UNLOCK NOW · ${tech.scienceCost} science"
                    shortfall > 0L -> "Start research · ${shortfall} science still needed"
                    else -> "START RESEARCH"
                }
                Spacer(modifier = Modifier.height(5.dp))
                ScienceAction(
                    label = label,
                    enabled = viewModel.canStartResearch(tech.id),
                    color = if (canUnlock) NssEmerald else NssPrimary,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        if (canUnlock) viewModel.unlockTechnology(tech.id)
                        else viewModel.startResearch(tech.id)
                    },
                )
            }
        }
    }
}

@Composable
private fun ScienceAction(
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
            .padding(vertical = 7.dp),
        color = NssOnPhoto,
        fontWeight = FontWeight.Bold,
        fontSize = 9.sp,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun Stat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Black, color = NssForeground)
        Text(label, fontSize = 7.sp, fontWeight = FontWeight.Bold, color = NssMutedForeground)
    }
}

