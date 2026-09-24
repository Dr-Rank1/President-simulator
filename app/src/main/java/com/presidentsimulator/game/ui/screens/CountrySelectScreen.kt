package com.presidentsimulator.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import com.presidentsimulator.game.data.PlayableNationCatalog
import com.presidentsimulator.game.data.ScenarioCatalog
import com.presidentsimulator.game.ui.components.rememberNssLayoutSpec
import com.presidentsimulator.game.ui.components.HeroHeaderScrim
import com.presidentsimulator.game.ui.components.NssCardShape
import com.presidentsimulator.game.ui.components.NssPhotoHeader
import com.presidentsimulator.game.ui.theme.Dimens
import com.presidentsimulator.game.ui.theme.NssAccent
import com.presidentsimulator.game.ui.theme.NssBackground
import com.presidentsimulator.game.ui.theme.NssForeground
import com.presidentsimulator.game.ui.theme.NssMutedForeground
import com.presidentsimulator.game.ui.theme.NssOnPhoto
import com.presidentsimulator.game.ui.theme.NssPrimary

@Composable
fun CountrySelectScreen(
    nations: List<PlayableNationCatalog.NationDefinition>,
    onBack: () -> Unit,
    onSelectCountry: (countryId: String, scenarioId: String, challengeId: String) -> Unit,
) {
    var selectedIndex by remember(nations) { mutableIntStateOf(0) }
    var scenarioIndex by remember { mutableIntStateOf(0) }
    var challengeIndex by remember { mutableIntStateOf(0) }
    var countryQuery by remember { mutableStateOf("") }
    var regionFilter by remember { mutableStateOf("All regions") }
    val nation = nations.getOrElse(selectedIndex) { nations.first() }
    val regionOptions = remember(nations) { listOf("All regions") + nations.map { it.region }.distinct().sorted() }
    val visibleNations = remember(nations, countryQuery, regionFilter) {
        nations.filter { item ->
            val matchesRegion = regionFilter == "All regions" || item.region == regionFilter
            val matchesQuery = countryQuery.isBlank() || listOf(item.name, item.officialName, item.countryCode, item.governmentLabel)
                .any { it.contains(countryQuery.trim(), ignoreCase = true) }
            matchesRegion && matchesQuery
        }
    }
    val scenarios = remember { ScenarioCatalog.ALL }
    val scenario = scenarios.getOrElse(scenarioIndex) { scenarios.first() }
    val challenges = remember { ScenarioCatalog.CHALLENGES }
    val challenge = challenges.getOrElse(challengeIndex) { challenges.first() }
    val layout = rememberNssLayoutSpec()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NssBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.SpacingMedium, vertical = Dimens.SpacingSmall)
                .border(width = 0.dp, color = Color.Transparent),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.clickable(onClick = onBack),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = NssMutedForeground, modifier = Modifier.size(16.dp))
                Text("BACK", color = NssMutedForeground, fontWeight = FontWeight.Black, fontSize = 11.sp)
            }
            Text(
                text = "SELECT NATION",
                color = NssAccent,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
            )
            Spacer(modifier = Modifier.size(48.dp))
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.SpacingMedium),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingMedium),
        ) {
            Text("${nations.size} PLAYABLE NATIONS · SEARCH OR FILTER BY REGION", color = NssAccent, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.2.sp)
            OutlinedTextField(
                value = countryQuery,
                onValueChange = { countryQuery = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Find a country or government system") },
            )
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                regionOptions.forEach { region ->
                    Text(
                        text = region,
                        modifier = Modifier
                            .clip(NssCardShape)
                            .background(if (region == regionFilter) NssAccent.copy(alpha = 0.28f) else NssPrimary.copy(alpha = 0.2f))
                            .clickable { regionFilter = region }
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        color = if (region == regionFilter) NssAccent else NssMutedForeground,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(layout.countrySelectHeroHeight)
                    .clip(NssCardShape)
                    .border(1.dp, Color(0x4DD4C8A8), NssCardShape),
            ) {
                NssPhotoHeader(
                    imageUrl = nation.leaderImageUrl,
                    fallbackGradient = listOf(NssPrimary, NssBackground),
                    modifier = Modifier.matchParentSize(),
                    scrimTopToBottom = HeroHeaderScrim,
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color(0xE61C1810)),
                            ),
                    ),
                )
                Text(nation.flagEmoji, modifier = Modifier.align(Alignment.TopStart).padding(Dimens.SpacingMedium), fontSize = 30.sp)
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(Dimens.SpacingMedium),
                ) {
                    Text(
                        text = nation.governmentLabel.uppercase(),
                        color = Color(0xFFD4C8A8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                    )
                    Text(
                        text = nation.name.uppercase(),
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Black,
                        fontSize = if (layout.isCompactHeight) 26.sp else 32.sp,
                        color = NssOnPhoto,
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth().clip(NssCardShape)
                    .background(NssPrimary.copy(alpha = 0.2f))
                    .border(1.dp, NssPrimary.copy(alpha = 0.55f), NssCardShape)
                    .padding(Dimens.SpacingMedium),
            ) {
                Text("${nation.governmentSystem.displayName.uppercase()} · ${nation.governmentSystem.executiveTitle.uppercase()}", color = NssAccent, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                Text(nation.governmentSystem.description, color = NssMutedForeground, fontSize = 11.sp, modifier = Modifier.padding(top = 5.dp))
                if (nation.countryCode.isNotBlank()) {
                    Text("Population · ${nation.vitals.population.toCompactCount()}${nation.populationYear.takeIf { it > 0 }?.let { " · $it estimate" } ?: ""}", color = NssOnPhoto, fontSize = 10.sp, modifier = Modifier.padding(top = 8.dp))
                    if (nation.gdpUsd > 0L) {
                        Text("Nominal GDP · ${nation.gdpUsd.toCompactUsd()} · World Bank ${nation.gdpYear}", color = NssOnPhoto, fontSize = 10.sp, modifier = Modifier.padding(top = 3.dp))
                    } else {
                        Text("Nominal GDP data unavailable · balanced game baseline used", color = NssMutedForeground, fontSize = 10.sp, modifier = Modifier.padding(top = 3.dp))
                    }
                    if (nation.statusNote.isNotBlank()) Text(nation.statusNote, color = NssMutedForeground, fontSize = 9.sp, modifier = Modifier.padding(top = 4.dp))
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(NssCardShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(NssPrimary.copy(alpha = 0.4f), NssPrimary.copy(alpha = 0.2f)),
                        ),
                    )
                    .border(1.dp, NssPrimary.copy(alpha = 0.8f), NssCardShape)
                    .padding(Dimens.SpacingMedium),
            ) {
                Text(
                    text = "NATIONAL PERK",
                    color = NssAccent,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                )
                Row(
                    modifier = Modifier.padding(top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(18.dp))
                    Text(nation.nationalPerk, color = NssOnPhoto, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            if (visibleNations.isEmpty()) {
                Text("No country matches this search and region filter.", color = NssMutedForeground, fontSize = 11.sp)
            } else {
                Text("COUNTRY RESULTS · ${visibleNations.size}", color = NssAccent, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.2.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(visibleNations, key = { it.id }) { item ->
                        val index = nations.indexOfFirst { it.id == item.id }
                        val selected = index == selectedIndex
                        Box(
                            modifier = Modifier.size(width = 108.dp, height = 86.dp)
                                .clip(NssCardShape)
                                .background(if (selected) NssPrimary.copy(alpha = 0.75f) else NssBackground.copy(alpha = 0.65f))
                                .border(if (selected) 2.dp else 1.dp, if (selected) NssAccent else Color(0x33FFFFFF), NssCardShape)
                                .clickable { if (index >= 0) selectedIndex = index },
                        ) {
                            Column(modifier = Modifier.align(Alignment.Center).padding(5.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(item.flagEmoji, fontSize = 20.sp, textAlign = TextAlign.Center)
                                Text(item.countryCode.ifBlank { item.name }.uppercase(), color = NssOnPhoto, fontSize = 8.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, maxLines = 2)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, NssBackground, NssBackground),
                    ),
                )
                .padding(Dimens.SpacingMedium),
        ) {
            Text(
                "SCENARIO",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = NssPrimary,
                letterSpacing = 2.sp,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(top = 8.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                scenarios.forEachIndexed { index, pack ->
                    val selected = index == scenarioIndex
                    Column(
                        modifier = Modifier
                            .clip(NssCardShape)
                            .background(if (selected) NssPrimary else NssPrimary.copy(alpha = 0.25f))
                            .clickable { scenarioIndex = index }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        Text(pack.title, color = NssOnPhoto, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Text(pack.difficulty.displayName, color = NssOnPhoto.copy(alpha = 0.75f), fontSize = 9.sp)
                    }
                }
            }
            Text(
                scenario.tagline,
                fontSize = 11.sp,
                color = NssMutedForeground,
                modifier = Modifier.padding(bottom = 12.dp),
            )
            Text(
                if (scenario.id == "standard") "FIRST CAMPAIGN RECOMMENDED" else "MISSION OBJECTIVES",
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = NssAccent,
                letterSpacing = 1.5.sp,
            )
            scenario.objectives.forEachIndexed { index, objective ->
                Text(
                    text = "${index + 1}. $objective",
                    fontSize = 11.sp,
                    color = NssOnPhoto.copy(alpha = 0.85f),
                    modifier = Modifier.padding(top = 3.dp),
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("OPTIONAL CHALLENGE RULE", fontSize = 9.sp, fontWeight = FontWeight.Black, color = NssAccent, letterSpacing = 1.5.sp)
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                challenges.forEachIndexed { index, option ->
                    val selected = index == challengeIndex
                    Text(
                        text = option.title,
                        modifier = Modifier.clip(NssCardShape)
                            .background(if (selected) NssAccent.copy(alpha = 0.28f) else NssPrimary.copy(alpha = 0.28f))
                            .border(1.dp, if (selected) NssAccent else Color.Transparent, NssCardShape)
                            .clickable { challengeIndex = index }
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        color = NssOnPhoto,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Text(
                "${challenge.description}  ·  ${challenge.scoreMultiplier}× legacy score",
                fontSize = 10.sp,
                color = NssMutedForeground,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(NssCardShape)
                    .background(Brush.horizontalGradient(listOf(NssAccent, Color(0xFFD97706))))
                    .clickable { onSelectCountry(nation.id, scenario.id, challenge.id) }
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "INITIATE SEQUENCE",
                    color = NssOnPhoto,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    letterSpacing = 1.sp,
                )
                Icon(
                    Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    tint = NssOnPhoto,
                    modifier = Modifier.padding(start = 8.dp).size(18.dp),
                )
            }
        }
    }
}

private fun Long.toCompactCount(): String = when {
    this >= 1_000_000_000L -> "${this / 1_000_000_000L}B"
    this >= 1_000_000L -> "${this / 1_000_000L}M"
    this >= 1_000L -> "${this / 1_000L}K"
    else -> toString()
}

private fun Long.toCompactUsd(): String = when {
    this >= 1_000_000_000_000L -> "$${this / 1_000_000_000_000L}T"
    this >= 1_000_000_000L -> "$${this / 1_000_000_000L}B"
    this >= 1_000_000L -> "$${this / 1_000_000L}M"
    else -> "$${this}"
}
