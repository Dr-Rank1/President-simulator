package com.presidentsimulator.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import com.presidentsimulator.game.data.TechCatalog
import com.presidentsimulator.game.data.Technology
import com.presidentsimulator.game.ui.components.CardHeaderBottomScrim
import com.presidentsimulator.game.ui.components.NssBadge
import com.presidentsimulator.game.ui.components.NssCardImages
import com.presidentsimulator.game.ui.components.NssCardShape
import com.presidentsimulator.game.ui.components.NssGameBar
import com.presidentsimulator.game.ui.components.NssGradients
import com.presidentsimulator.game.ui.components.NssPanel
import com.presidentsimulator.game.ui.components.NssPhotoHeader
import com.presidentsimulator.game.ui.theme.NssAccent
import com.presidentsimulator.game.ui.theme.NssBackground
import com.presidentsimulator.game.ui.theme.NssBorder
import com.presidentsimulator.game.ui.theme.NssEmerald
import com.presidentsimulator.game.ui.theme.NssForeground
import com.presidentsimulator.game.ui.theme.NssMutedForeground
import com.presidentsimulator.game.ui.theme.NssPrimary
import com.presidentsimulator.game.viewmodel.AdvancementViewModel
import com.presidentsimulator.game.viewmodel.GameViewModel

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
            .background(NssBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
    ) {
        // Cyan tab bar at the top
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(NssPrimary)
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "MANAGEMENT",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                letterSpacing = 2.sp
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 4.dp,
                end = 4.dp,
                top = 4.dp,
                bottom = 4.dp + 12.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column {
                    SectionTitle("Current Research")
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
            }
            item(span = { GridItemSpan(maxLineSpan) }) { 
                Spacer(modifier = Modifier.height(4.dp))
                SectionTitle("Tech Tree") 
            }
            items(TechCatalog.all, key = { it.id }) { tech ->
                TechTreeCard(
                    tech = tech,
                    isUnlocked = research.isUnlocked(tech.id),
                    isActive = research.activeTechId == tech.id,
                    isQueued = research.queuedTechId == tech.id,
                    prerequisitesMet = research.prerequisitesMet(tech),
                    canStart = viewModel.canStartResearch(tech.id),
                    canUnlock = viewModel.canUnlockTechnology(tech.id),
                    hasActiveResearch = research.activeTechId != null,
                    sciencePerMonth = sciencePerMonth,
                    onStartResearch = { viewModel.startResearch(tech.id) },
                    onUnlock = { viewModel.unlockTechnology(tech.id) },
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text.uppercase(),
        fontSize = 9.sp,
        fontWeight = FontWeight.Black,
        color = NssPrimary,
        letterSpacing = 8.sp,
        modifier = Modifier.padding(bottom = 6.dp),
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
    NssPanel(modifier = Modifier.fillMaxWidth()) {
        if (activeTech != null) {
            Box(modifier = Modifier.fillMaxWidth().height(66.dp).padding(bottom = 6.dp)) {
                NssPhotoHeader(
                    imageUrl = NssCardImages.techCategoryImage(activeTech.category),
                    fallbackGradient = NssGradients.Violet,
                    modifier = Modifier.matchParentSize(),
                    scrimTopToBottom = CardHeaderBottomScrim,
                )
            }
        }
        if (activeTech == null) {
            Text("No active research project", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = NssForeground)
            if (queuedTech != null) {
                Text(
                    "Queued next: ${queuedTech.name}",
                    fontSize = 9.sp,
                    color = NssEmerald,
                    modifier = Modifier.padding(top = 3.dp),
                )
            } else {
                Text("Select a technology below to begin.", fontSize = 9.sp, color = NssMutedForeground, modifier = Modifier.padding(top = 3.dp))
            }
        } else {
            Text(activeTech.name, fontWeight = FontWeight.Black, fontSize = 12.sp, color = NssForeground)
            Text(activeTech.effect.description, fontSize = 8.sp, color = NssMutedForeground, modifier = Modifier.padding(top = 3.dp))
            Spacer(modifier = Modifier.padding(top = 6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Research XP", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = NssMutedForeground)
                Text("${progressPercent.toInt()}%", fontWeight = FontWeight.Black, color = NssPrimary)
            }
            NssGameBar(percent = progressPercent, color = NssPrimary, thick = true)
            Row(modifier = Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Est. remaining", fontSize = 8.sp, color = NssMutedForeground)
                Text(if (daysRemaining == 0) "< 1 day" else "$daysRemaining days", fontWeight = FontWeight.Bold, fontSize = 9.sp)
            }
            Text(
                text = "Extra funding tier: $extraFundingTier / ${com.presidentsimulator.game.data.ResearchState.MAX_EXTRA_FUNDING_TIER}",
                fontSize = 8.sp,
                color = NssMutedForeground,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                text = "⬆ Allocate Extra Funding ($fundingCostLabel)",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 7.dp)
                    .clip(NssCardShape)
                    .background(if (canAllocateFunding) NssAccent else NssAccent.copy(alpha = 0.35f))
                    .clickable(enabled = canAllocateFunding, onClick = onAllocateFunding)
                    .padding(vertical = 7.dp),
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 9.sp,
                textAlign = TextAlign.Center,
            )
            if (queuedTech != null) {
                Text(
                    "Up next: ${queuedTech.name}",
                    fontSize = 8.sp,
                    color = NssEmerald,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun TechTreeCard(
    tech: Technology,
    isUnlocked: Boolean,
    isActive: Boolean,
    isQueued: Boolean,
    prerequisitesMet: Boolean,
    canStart: Boolean,
    canUnlock: Boolean,
    hasActiveResearch: Boolean,
    sciencePerMonth: Long,
    onStartResearch: () -> Unit,
    onUnlock: () -> Unit,
) {
    val levelText = if (isUnlocked) "Level 1" else "Level 0"
    val daysEst = if (sciencePerMonth > 0) (tech.scienceCost / (sciencePerMonth / 30f)).toInt() else 999
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .border(1.dp, NssBorder, RoundedCornerShape(8.dp))
    ) {
        // Photo on top
        Box(modifier = Modifier.fillMaxWidth().height(80.dp)) {
            NssPhotoHeader(
                imageUrl = NssCardImages.techCategoryImage(tech.category),
                fallbackGradient = NssGradients.Violet,
                modifier = Modifier.matchParentSize(),
                scrimTopToBottom = CardHeaderBottomScrim,
            )
            
            // Circular "Level X" badge in the top right
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(40.dp)
                    .background(Color(0x99000000), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = levelText,
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
        
        // Blue banner below photo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(NssPrimary)
                .padding(vertical = 6.dp, horizontal = 8.dp)
        ) {
            Text(
                text = tech.name,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        // White area with stats
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 2.dp)) {
                Icon(
                    imageVector = Icons.Default.PieChart,
                    contentDescription = null,
                    tint = Color.DarkGray,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(levelText, color = Color.DarkGray, fontSize = 10.sp)
            }
            Text("-${tech.scienceCost}/day", color = Color.DarkGray, fontSize = 10.sp, modifier = Modifier.padding(bottom = 2.dp, start = 16.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 2.dp)) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = null,
                    tint = Color.DarkGray,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(tech.effect.description, color = Color.DarkGray, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = Color.DarkGray,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (isUnlocked) "Completed" else "~${daysEst} days", color = Color.DarkGray, fontSize = 10.sp)
            }
            
            if (!isUnlocked && !isActive && !isQueued) {
                Spacer(modifier = Modifier.height(8.dp))
                if (tech.prerequisiteIds.isNotEmpty() && !prerequisitesMet) {
                    Text(
                        text = "Requires: " + tech.prerequisiteIds.joinToString { id -> TechCatalog.byId(id)?.name ?: id },
                        fontSize = 8.sp,
                        color = NssAccent,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }
                Text(
                    text = when {
                        !canStart -> "Cannot start"
                        hasActiveResearch -> "⏳ Queue"
                        else -> "▶ Start"
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (canStart) NssEmerald else Color.LightGray)
                        .clickable(enabled = canStart, onClick = onStartResearch)
                        .padding(vertical = 6.dp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (canUnlock) "⚡ Unlock" else "Unavailable",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (canUnlock) NssAccent else Color.LightGray)
                        .clickable(enabled = canUnlock, onClick = onUnlock)
                        .padding(vertical = 6.dp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                )
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                val statusText = when {
                    isUnlocked -> "UNLOCKED"
                    isActive -> "IN PROGRESS"
                    isQueued -> "QUEUED"
                    else -> ""
                }
                if (statusText.isNotEmpty()) {
                    Text(
                        text = statusText,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isUnlocked) NssEmerald else NssAccent)
                            .padding(vertical = 6.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

private fun Long.formatScienceBudget(): String = when {
    this >= 1_000_000_000L -> "$${"%.1f".format(this / 1_000_000_000.0)}B"
    this >= 1_000_000L -> "$${"%.1f".format(this / 1_000_000.0)}M"
    else -> "$$this"
}
