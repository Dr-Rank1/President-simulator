package com.presidentsimulator.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import com.presidentsimulator.game.data.PlayableNationCatalog
import com.presidentsimulator.game.data.ScenarioCatalog
import com.presidentsimulator.game.ui.components.NssCardShape
import com.presidentsimulator.game.ui.theme.NssAccent
import com.presidentsimulator.game.ui.theme.NssBackground
import com.presidentsimulator.game.ui.theme.NssBorder
import com.presidentsimulator.game.ui.theme.NssEmerald
import com.presidentsimulator.game.ui.theme.NssForeground
import com.presidentsimulator.game.ui.theme.NssGameCard
import com.presidentsimulator.game.ui.theme.NssMutedForeground
import com.presidentsimulator.game.ui.theme.NssOnPhoto
import com.presidentsimulator.game.ui.theme.NssPrimary

private const val NATION_STEP = 0
private const val SCENARIO_STEP = 1
private const val CHALLENGE_STEP = 2

@Composable
fun CountrySelectScreen(
    nations: List<PlayableNationCatalog.NationDefinition>,
    onBack: () -> Unit,
    onSelectCountry: (countryId: String, scenarioId: String, challengeId: String) -> Unit,
) {
    var step by remember { mutableIntStateOf(NATION_STEP) }
    var selectedNationId by remember(nations) { mutableStateOf(nations.firstOrNull()?.id.orEmpty()) }
    var scenarioIndex by remember { mutableIntStateOf(0) }
    var challengeIndex by remember { mutableIntStateOf(0) }
    var countryQuery by remember { mutableStateOf("") }

    val context = LocalContext.current
    val favoritePreferences = remember(context) { context.getSharedPreferences("country_picker", 0) }
    var favoriteIds by remember(context) {
        mutableStateOf(favoritePreferences.getStringSet("favorites", emptySet()).orEmpty().toSet())
    }
    var favoritesOnly by remember { mutableStateOf(false) }

    val nation = nations.firstOrNull { it.id == selectedNationId } ?: nations.firstOrNull()
    val visibleNations = remember(nations, countryQuery, favoritesOnly, favoriteIds) {
        nations.filter { candidate ->
            val matchesQuery = countryQuery.isBlank() || listOf(
                candidate.name,
                candidate.officialName,
                candidate.countryCode,
            ).any { it.contains(countryQuery.trim(), ignoreCase = true) }
            matchesQuery && (!favoritesOnly || candidate.id in favoriteIds)
        }
    }
    val scenarios = remember { ScenarioCatalog.ALL }
    val challenges = remember { ScenarioCatalog.CHALLENGES }
    val scenario = scenarios.getOrElse(scenarioIndex) { scenarios.first() }
    val challenge = challenges.getOrElse(challengeIndex) { challenges.first() }

    BackHandler {
        if (step == NATION_STEP) onBack() else step -= 1
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NssBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.clip(NssCardShape).clickable {
                    if (step == NATION_STEP) onBack() else step -= 1
                }.padding(vertical = 8.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = NssMutedForeground, modifier = Modifier.size(17.dp))
                Text(if (step == NATION_STEP) "BACK" else "PREVIOUS", color = NssMutedForeground, fontWeight = FontWeight.Bold, fontSize = 10.sp)
            }
            Spacer(Modifier.weight(1f))
            Text("NEW CAMPAIGN", color = NssAccent, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
            Spacer(Modifier.weight(1f))
            Text("${step + 1} / 3", color = NssMutedForeground, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            repeat(3) { index ->
                Spacer(
                    modifier = Modifier.weight(1f).height(3.dp).clip(CircleShape)
                        .background(if (index <= step) NssAccent else NssBorder),
                )
            }
        }

        when (step) {
            NATION_STEP -> {
                StepHeading("Choose your nation", "Pick the country you want to lead.")
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = countryQuery,
                        onValueChange = { countryQuery = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        label = { Text("Search countries") },
                    )
                    Text(
                        if (favoritesOnly) "ALL" else "★ ${favoriteIds.size}",
                        modifier = Modifier.clip(NssCardShape)
                            .background(if (favoritesOnly) NssAccent.copy(alpha = 0.22f) else NssGameCard)
                            .clickable { favoritesOnly = !favoritesOnly }
                            .padding(horizontal = 12.dp, vertical = 14.dp),
                        color = if (favoritesOnly) NssAccent else NssMutedForeground,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                    )
                }
                Text("${visibleNations.size} countries", color = NssMutedForeground, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp, bottom = 8.dp))
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(visibleNations, key = { it.id }) { item ->
                        NationChoiceRow(
                            nation = item,
                            selected = item.id == selectedNationId,
                            favorite = item.id in favoriteIds,
                            onSelect = { selectedNationId = item.id },
                            onToggleFavorite = {
                                val updated = if (item.id in favoriteIds) favoriteIds - item.id else favoriteIds + item.id
                                favoriteIds = updated
                                favoritePreferences.edit().putStringSet("favorites", updated).apply()
                            },
                        )
                    }
                    if (visibleNations.isEmpty()) {
                        item { Text("No countries match that search.", color = NssMutedForeground, fontSize = 12.sp, modifier = Modifier.padding(16.dp)) }
                    }
                }
                nation?.let {
                    SelectedNationSummary(it)
                    PrimaryAction("Continue to scenario") { step = SCENARIO_STEP }
                }
            }

            SCENARIO_STEP -> {
                StepHeading("Choose a scenario", "Pick the kind of campaign you want to play.")
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(scenarios, key = { it.id }) { option ->
                        val index = scenarios.indexOf(option)
                        val selected = index == scenarioIndex
                        SelectionCard(selected = selected, onClick = { scenarioIndex = index }) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(option.title, color = NssForeground, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("${option.difficulty.displayName} · ${option.tagline}", color = NssMutedForeground, fontSize = 11.sp, modifier = Modifier.padding(top = 3.dp))
                                }
                                if (selected) SelectionCheck()
                            }
                        }
                    }
                    item {
                        SelectionCard(selected = true, onClick = {}) {
                            Text("MISSION OBJECTIVES", color = NssAccent, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                            scenario.objectives.take(3).forEachIndexed { index, objective ->
                                Text("${index + 1}. $objective", color = NssMutedForeground, fontSize = 10.sp, modifier = Modifier.padding(top = 5.dp))
                            }
                        }
                    }
                }
                PrimaryAction("Continue to challenge") { step = CHALLENGE_STEP }
            }

            else -> {
                StepHeading("Set your challenge", "Optional rules make the campaign harder and raise your legacy score.")
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(challenges, key = { it.id }) { option ->
                        val index = challenges.indexOf(option)
                        val selected = index == challengeIndex
                        SelectionCard(selected = selected, onClick = { challengeIndex = index }) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(option.title, color = NssForeground, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(option.description, color = NssMutedForeground, fontSize = 11.sp, modifier = Modifier.padding(top = 3.dp))
                                }
                                Text("${option.scoreMultiplier}×", color = NssAccent, fontWeight = FontWeight.Black, fontSize = 14.sp)
                                if (selected) SelectionCheck()
                            }
                        }
                    }
                    item { nation?.let { SelectedCampaignSummary(it.name, scenario.title, challenge.title) } }
                }
                PrimaryAction("Start campaign") {
                    nation?.let { onSelectCountry(it.id, scenario.id, challenge.id) }
                }
            }
        }
    }
}

@Composable
private fun StepHeading(title: String, subtitle: String) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(title, color = NssForeground, fontSize = 25.sp, fontWeight = FontWeight.Black)
        Text(subtitle, color = NssMutedForeground, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun NationChoiceRow(
    nation: PlayableNationCatalog.NationDefinition,
    selected: Boolean,
    favorite: Boolean,
    onSelect: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(NssCardShape)
            .background(if (selected) NssPrimary.copy(alpha = 0.35f) else NssGameCard)
            .border(1.dp, if (selected) NssAccent else NssBorder, NssCardShape)
            .clickable(onClick = onSelect).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(nation.flagEmoji, fontSize = 26.sp)
        Column(modifier = Modifier.weight(1f)) {
            Text(nation.name, color = NssForeground, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${nation.governmentLabel} · ${nation.region}", color = NssMutedForeground, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(
            if (favorite) "★" else "☆",
            modifier = Modifier.clip(CircleShape).clickable(onClick = onToggleFavorite).padding(6.dp),
            color = NssAccent,
            fontSize = 17.sp,
        )
        if (selected) SelectionCheck()
    }
}

@Composable
private fun SelectedNationSummary(nation: PlayableNationCatalog.NationDefinition) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 8.dp)
            .clip(NssCardShape).background(NssGameCard).border(1.dp, NssBorder, NssCardShape).padding(12.dp),
    ) {
        Text("YOUR SELECTION", color = NssAccent, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        Text("${nation.flagEmoji} ${nation.name} · ${nation.governmentSystem.displayName}", color = NssForeground, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
        Text("National advantage: ${nation.nationalPerk}", color = NssMutedForeground, fontSize = 10.sp, modifier = Modifier.padding(top = 3.dp))
    }
}

@Composable
private fun SelectedCampaignSummary(nation: String, scenario: String, challenge: String) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(NssCardShape).background(NssGameCard)
            .border(1.dp, NssBorder, NssCardShape).padding(14.dp),
    ) {
        Text("CAMPAIGN SUMMARY", color = NssAccent, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        Text("$nation · $scenario", color = NssForeground, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(top = 5.dp))
        Text("Challenge: $challenge", color = NssMutedForeground, fontSize = 11.sp, modifier = Modifier.padding(top = 3.dp))
    }
}

@Composable
private fun SelectionCard(selected: Boolean, onClick: () -> Unit, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(NssCardShape)
            .background(if (selected) NssPrimary.copy(alpha = 0.25f) else NssGameCard)
            .border(1.dp, if (selected) NssAccent else NssBorder, NssCardShape)
            .clickable(onClick = onClick).padding(14.dp),
    ) { content() }
}

@Composable
private fun SelectionCheck() {
    Icon(Icons.Default.Check, contentDescription = "Selected", tint = NssEmerald, modifier = Modifier.padding(start = 8.dp).size(18.dp))
}

@Composable
private fun PrimaryAction(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
        shape = NssCardShape,
        colors = ButtonDefaults.buttonColors(containerColor = NssAccent, contentColor = Color.White),
    ) {
        Text(label, fontWeight = FontWeight.Black, fontSize = 14.sp, modifier = Modifier.padding(vertical = 5.dp))
    }
}
