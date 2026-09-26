package com.presidentsimulator.game.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.presidentsimulator.game.data.PlayableNationCatalog
import com.presidentsimulator.game.data.ScenarioCatalog
import com.presidentsimulator.game.ui.theme.*

@Composable
fun CountrySelectScreen(
    nations: List<PlayableNationCatalog.NationDefinition>,
    onBack: () -> Unit,
    onSelectCountry: (countryId: String, scenarioId: String, challengeId: String) -> Unit,
) {
    val sortedNations = remember(nations) { nations.sortedBy { it.name } }
    var selectedNationId by remember(sortedNations) { mutableStateOf(sortedNations.firstOrNull()?.id.orEmpty()) }
    var selectedChallengeId by remember { mutableStateOf(ScenarioCatalog.CHALLENGES.first().id) }

    val nation = sortedNations.firstOrNull { it.id == selectedNationId } ?: sortedNations.firstOrNull()
    val challenges = remember { ScenarioCatalog.CHALLENGES }

    BackHandler { onBack() }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF070B11))) {
        // Background Image (Faded Map)
        AsyncImage(
            model = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?q=80&w=2072&auto=format&fit=crop",
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            alpha = 0.15f
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0x66000000))
                        .clickable { onBack() }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = NssAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Text("BACK", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.weight(1f))
                Text(
                    "SELECT NATION TO COMMAND",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.weight(1f))
                Text(
                    "${sortedNations.size} NATIONS AVAILABLE",
                    color = NssMutedForeground,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Main Content Area
            if (nation != null) {
                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // LEFT COLUMN: Nation Dossier & COMMENCE COMMAND Button (ALWAYS VISIBLE)
                    Column(
                        modifier = Modifier
                            .weight(1.15f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xCC0A111A))
                            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        // Fixed Card Header
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                        ) {
                            Text(nation.flagEmoji, fontSize = 30.sp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    nation.name.uppercase(),
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    nation.officialName.ifEmpty { nation.governmentLabel },
                                    color = NssMutedForeground,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            // Perk Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(NssAccent.copy(alpha = 0.15f))
                                    .border(1.dp, NssAccent.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    nation.nationalPerk.take(24),
                                    color = NssAccent,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Scrollable Body (Guarantees all content fits any screen or font scale)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Key Vitals Grid
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF131A26))
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                val popM = String.format("%.1fM", nation.vitals.population / 1_000_000.0)
                                StatBlock("POPULATION", popM)
                                StatBlock("TREASURY", com.presidentsimulator.game.ui.components.formatCompactMoney(nation.vitals.budget))
                                val milStr = if (nation.militaryStrength >= 1000) String.format("%.0fK", nation.militaryStrength) else String.format("%.0f", nation.militaryStrength * 1000)
                                StatBlock("MILITARY", milStr)
                            }

                            // Difficulty Selection
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    "DIFFICULTY LEVEL",
                                    color = NssAccent,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(challenges) { challenge ->
                                        val isSelected = selectedChallengeId == challenge.id
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isSelected) NssAccent else Color(0xFF131A26))
                                                .border(
                                                    1.dp,
                                                    if (isSelected) Color.White.copy(alpha = 0.6f) else Color.Transparent,
                                                    RoundedCornerShape(6.dp)
                                                )
                                                .clickable { selectedChallengeId = challenge.id }
                                                .padding(horizontal = 10.dp, vertical = 5.dp)
                                        ) {
                                            Text(
                                                challenge.title,
                                                color = if (isSelected) Color.White else NssMutedForeground,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                                val activeChallenge = challenges.find { it.id == selectedChallengeId } ?: challenges.first()
                                Text(
                                    text = activeChallenge.description,
                                    color = Color(0xFF94A3B8),
                                    fontSize = 9.5.sp,
                                    lineHeight = 13.sp,
                                    modifier = Modifier.padding(horizontal = 2.dp)
                                )
                            }

                            // Ideology & Ruling System
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF131A26))
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Column {
                                        Text("IDEOLOGY", color = NssMutedForeground, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        Text(nation.ideology.name, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF131A26))
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Column {
                                        Text("GOVERNMENT", color = NssMutedForeground, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        Text(nation.governmentSystem.name.replace("_", " "), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        // ALWAYS PINNED COMMENCE COMMAND BUTTON AT BOTTOM OF CARD
                        Button(
                            onClick = { 
                                onSelectCountry(nation.id, ScenarioCatalog.ALL.first().id, selectedChallengeId)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NssEmerald, contentColor = Color.White),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .border(1.dp, Color(0x66FFFFFF), RoundedCornerShape(10.dp)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("★", fontSize = 14.sp, color = Color(0xFFFFD700))
                                Text(
                                    "COMMENCE AS ${nation.name.uppercase()}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                                Text("➔", fontSize = 14.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }

                    // RIGHT COLUMN: Nation Selection List (Sorted A-Z)
                    Column(
                        modifier = Modifier
                            .weight(0.85f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xCC0A111A))
                            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            "CHOOSE A NATION (A-Z)",
                            color = NssAccent,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp, top = 2.dp)
                        )
                        LazyColumn(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(sortedNations, key = { it.id }) { n ->
                                val isSelected = selectedNationId == n.id
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) Color(0xFF1D283A) else Color(0x66131A26))
                                        .border(
                                            if (isSelected) 1.5.dp else 0.5.dp,
                                            if (isSelected) NssAccent else Color(0x22FFFFFF),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { 
                                            if (isSelected) {
                                                // Tapping selected item directly starts command!
                                                onSelectCountry(n.id, ScenarioCatalog.ALL.first().id, selectedChallengeId)
                                            } else {
                                                selectedNationId = n.id 
                                            }
                                        }
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(n.flagEmoji, fontSize = 20.sp)
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            n.name, 
                                            color = if (isSelected) Color.White else NssMutedForeground,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
                                        )
                                        Text(
                                            n.governmentLabel,
                                            color = if (isSelected) NssAccent else Color.Gray,
                                            fontSize = 9.sp
                                        )
                                    }
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(NssEmerald)
                                                .clickable {
                                                    onSelectCountry(n.id, ScenarioCatalog.ALL.first().id, selectedChallengeId)
                                                }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text("START ➔", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatBlock(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = NssMutedForeground, fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Text(value, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Black)
    }
}
