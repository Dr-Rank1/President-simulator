package com.presidentsimulator.game.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.presidentsimulator.game.data.Ideology
import com.presidentsimulator.game.data.PlayableNationCatalog
import com.presidentsimulator.game.data.ScenarioCatalog
import com.presidentsimulator.game.data.StateReligion
import com.presidentsimulator.game.data.VictoryPath
import com.presidentsimulator.game.ui.theme.NssAccent
import com.presidentsimulator.game.ui.theme.NssBackground
import com.presidentsimulator.game.ui.theme.NssPrimary
import com.presidentsimulator.game.ui.theme.NssRed

/**
 * Oxiwyle-style new game flow:
 * 1. Pick your nation  2. Leader title  3. State ideology
 * 4. State religion  5. Victory path  6. Difficulty
 */
@Composable
fun NewGameSetupScreen(
    nations: List<PlayableNationCatalog.NationDefinition>,
    onBack: () -> Unit,
    onStartGame: (
        countryId: String,
        scenarioId: String,
        challengeId: String,
        leaderTitle: String,
        ideologyId: String,
        religionId: String,
        victoryPath: VictoryPath,
    ) -> Unit,
) {
    var step by remember { mutableIntStateOf(0) }
    var selectedNationId by remember { mutableStateOf(nations.firstOrNull()?.id.orEmpty()) }
    var leaderTitle by remember { mutableStateOf("President") }
    var ideologyId by remember { mutableStateOf(Ideology.DEMOCRACY.name) }
    var religionId by remember { mutableStateOf(StateReligion.SECULAR.name) }
    var victoryPath by remember { mutableStateOf(VictoryPath.MILITARY_DOMINANCE) }
    var challengeId by remember { mutableStateOf(ScenarioCatalog.CHALLENGES.first().id) }

    val nation = nations.find { it.id == selectedNationId }
    val steps = listOf("NATION", "LEADER", "IDEOLOGY", "RELIGION", "GOAL", "DIFFICULTY")

    BackHandler(enabled = step > 0) { step -= 1 }

    Column(modifier = Modifier.fillMaxSize().background(NssBackground)) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(NssPrimary)
                .windowInsetsPaddingSafe()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { if (step == 0) onBack() else step -= 1 }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("NEW GAME", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                Text(
                    "Step ${step + 1} of ${steps.size} — ${steps[step]}",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            nation?.let {
                Text(it.flagEmoji, fontSize = 26.sp)
                Spacer(Modifier.width(8.dp))
            }
        }

        // Step dots
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            steps.forEachIndexed { index, _ ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (index <= step) NssAccent else NssAccent.copy(alpha = 0.25f)),
                )
            }
        }

        when (step) {
            0 -> NationStep(nations, selectedNationId) { selectedNationId = it }
            1 -> LeaderStep(leaderTitle, nation) { leaderTitle = it }
            2 -> IdeologyStep(ideologyId) { ideologyId = it }
            3 -> ReligionStep(religionId) { religionId = it }
            4 -> VictoryPathStep(victoryPath, nation) { victoryPath = it }
            else -> DifficultyStep(challengeId) { challengeId = it }
        }

        // Footer navigation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0C1322))
                .windowInsetsPaddingSafe()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                if (step == 0) "" else "< ${steps[step - 1]}",
                color = NssAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(enabled = step > 0) { step -= 1 }
                    .padding(horizontal = 8.dp, vertical = 6.dp),
            )
            val canProceed = when (step) {
                0 -> nation != null
                1 -> leaderTitle.isNotBlank()
                else -> true
            }
            if (step < steps.size - 1) {
                Text(
                    "${steps[step + 1]} >",
                    color = if (canProceed) NssAccent else NssAccent.copy(alpha = 0.35f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(enabled = canProceed) { step += 1 }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                )
            } else {
                Text(
                    "TAKE OFFICE ▸",
                    color = if (canProceed) Color(0xFF4ADE80) else Color(0xFF4ADE80).copy(alpha = 0.35f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(enabled = canProceed) {
                            onStartGame(
                                selectedNationId,
                                ScenarioCatalog.ALL.first().id,
                                challengeId,
                                leaderTitle.trim(),
                                ideologyId,
                                religionId,
                                victoryPath,
                            )
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun Modifier.windowInsetsPaddingSafe(): Modifier =
    this.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))

@Composable
private fun StepContainer(
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        content = content,
    )
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        color = NssAccent,
        fontSize = 10.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 1.5.sp,
        modifier = Modifier.padding(top = 10.dp, bottom = 6.dp),
    )
}

@Composable
private fun ChoiceCard(
    selected: Boolean,
    onClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) NssPrimary.copy(alpha = 0.22f) else Color(0xFF111A2B))
            .border(
                1.dp,
                if (selected) NssPrimary else Color(0xFF1E293B),
                RoundedCornerShape(8.dp),
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        content = content,
    )
}

// ── Step 1: Nation ───────────────────────────────────────────────────────────

@Composable
private fun androidx.compose.foundation.layout.ColumnScope.NationStep(
    nations: List<PlayableNationCatalog.NationDefinition>,
    selectedId: String,
    onSelect: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().weight(1f)) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(nations) { n ->
                val selected = n.id == selectedId
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selected) NssPrimary.copy(alpha = 0.25f) else Color(0xFF111A2B))
                        .border(1.dp, if (selected) NssPrimary else Color(0xFF1E293B), RoundedCornerShape(8.dp))
                        .clickable { onSelect(n.id) }
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(n.flagEmoji, fontSize = 22.sp)
                    Spacer(Modifier.height(3.dp))
                    Text(
                        n.name,
                        color = if (selected) Color.White else Color(0xFFB7C2D0),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

// ── Step 2: Leader title ─────────────────────────────────────────────────────

@Composable
private fun LeaderStep(
    title: String,
    nation: PlayableNationCatalog.NationDefinition?,
    onTitleChange: (String) -> Unit,
) {
    StepContainer {
        SectionLabel("HOW SHALL THE WORLD ADDRESS YOU?")
        val suggestions = listOf(
            "President", "Prime Minister", "Chancellor",
            "Supreme Leader", "Commander-in-Chief", "Dear Leader",
        )
        suggestions.forEach { suggestion ->
            ChoiceCard(selected = title == suggestion, onClick = { onTitleChange(suggestion) }) {
                Text(suggestion, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text("Custom title") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        if (nation != null) {
            Spacer(Modifier.height(14.dp))
            Text(
                "${nation.flagEmoji} ${nation.name} — ${nation.officialName}\n\n" +
                    "You will rule ${nation.name} as its ${title.ifBlank { nation.governmentSystem.executiveTitle }}. " +
                    "Your first acts will define the era to come.",
                color = Color(0xFFB7C2D0),
                fontSize = 11.sp,
                lineHeight = 16.sp,
            )
        }
    }
}

// ── Step 3: Ideology ─────────────────────────────────────────────────────────

@Composable
private fun IdeologyStep(selected: String, onSelect: (String) -> Unit) {
    StepContainer {
        SectionLabel("STATE IDEOLOGY")
        val options = listOf(
            Triple(Ideology.DEMOCRACY, "Free elections, strong civil society, global goodwill.", "Approval grows faster; strong ideological appeal"),
            Triple(Ideology.AUTOCRACY, "Order above all. The security service answers to you.", "Lower instability; military keeps order"),
            Triple(Ideology.COMMUNISM, "The state directs industry for the workers.", "Industrial muscle; industry fuels your message"),
        )
        options.forEach { (ideology, blurb, effect) ->
            ChoiceCard(selected = selected == ideology.name, onClick = { onSelect(ideology.name) }) {
                Text(ideology.displayName, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(3.dp))
                Text(blurb, color = Color(0xFFB7C2D0), fontSize = 10.sp, lineHeight = 14.sp)
                Spacer(Modifier.height(3.dp))
                Text(effect, color = NssAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ── Step 4: Religion ─────────────────────────────────────────────────────────

@Composable
private fun ReligionStep(selected: String, onSelect: (String) -> Unit) {
    StepContainer {
        SectionLabel("STATE RELIGION")
        val options = listOf(
            Triple(StateReligion.SECULAR, "Faith stays private. Reason governs.", "+5% science, mild stability"),
            Triple(StateReligion.TRADITIONAL, "The old faith anchors the nation.", "+3 approval, steadier streets"),
            Triple(StateReligion.PLURALIST, "Many faiths, one people.", "+2 approval, gentle stability"),
            Triple(StateReligion.STATE_CULT, "The state itself is sacred.", "Fastest ideological spread; unrest grows"),
        )
        options.forEach { (religion, blurb, effect) ->
            ChoiceCard(selected = selected == religion.name, onClick = { onSelect(religion.name) }) {
                Text(religion.displayName, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(3.dp))
                Text(blurb, color = Color(0xFFB7C2D0), fontSize = 10.sp, lineHeight = 14.sp)
                Spacer(Modifier.height(3.dp))
                Text(effect, color = NssAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            "Your religion shapes how far your faith spreads on the Religious Dominance path.",
            color = Color(0xFF8FA0B5),
            fontSize = 9.sp,
        )
    }
}

// ── Step 5: Victory path ─────────────────────────────────────────────────────

@Composable
private fun VictoryPathStep(
    selected: VictoryPath,
    nation: PlayableNationCatalog.NationDefinition?,
    onSelect: (VictoryPath) -> Unit,
) {
    StepContainer {
        SectionLabel("CHOOSE YOUR DESTINY")
        Text(
            "The path you choose defines your ultimate victory. You cannot change it later.",
            color = Color(0xFF8FA0B5),
            fontSize = 10.sp,
        )
        Spacer(Modifier.height(4.dp))
        VictoryPath.entries.forEach { path ->
            ChoiceCard(selected = selected == path, onClick = { onSelect(path) }) {
                Text(path.displayName, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(3.dp))
                Text(path.goalLabel, color = Color(0xFFB7C2D0), fontSize = 10.sp, lineHeight = 14.sp)
                if (selected == path) {
                    Spacer(Modifier.height(3.dp))
                    Text("✓ SELECTED", color = Color(0xFF4ADE80), fontSize = 9.sp, fontWeight = FontWeight.Black)
                }
            }
        }
        nation?.let {
            Spacer(Modifier.height(8.dp))
            Text(
                "${it.flagEmoji} The story of ${it.name} awaits its greatest era — or its final one.",
                color = NssAccent,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

// ── Step 6: Difficulty ───────────────────────────────────────────────────────

@Composable
private fun DifficultyStep(selected: String, onSelect: (String) -> Unit) {
    StepContainer {
        SectionLabel("DIFFICULTY")
        ScenarioCatalog.CHALLENGES.forEach { challenge ->
            ChoiceCard(selected = selected == challenge.id, onClick = { onSelect(challenge.id) }) {
                Text(challenge.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(3.dp))
                Text(challenge.description, color = Color(0xFFB7C2D0), fontSize = 10.sp, lineHeight = 14.sp)
                if (challenge.scoreMultiplier != 1f) {
                    Spacer(Modifier.height(3.dp))
                    Text(
                        "Score ×${challenge.scoreMultiplier}",
                        color = if (challenge.scoreMultiplier > 1f) NssRed else NssAccent,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}
