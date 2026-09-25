package com.presidentsimulator.game.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.presidentsimulator.game.data.PlayableNationCatalog
import com.presidentsimulator.game.data.ScenarioCatalog
import com.presidentsimulator.game.ui.components.NssCardShape
import com.presidentsimulator.game.ui.components.rememberNssLayoutSpec
import com.presidentsimulator.game.ui.theme.*

@Composable
fun CountrySelectScreen(
    nations: List<PlayableNationCatalog.NationDefinition>,
    onBack: () -> Unit,
    onSelectCountry: (countryId: String, scenarioId: String, challengeId: String) -> Unit,
) {
    val layout = rememberNssLayoutSpec()
    var selectedNationId by remember(nations) { mutableStateOf(nations.firstOrNull()?.id.orEmpty()) }
    var selectedChallengeId by remember { mutableStateOf(ScenarioCatalog.CHALLENGES.first().id) }
    
    val nation = nations.firstOrNull { it.id == selectedNationId } ?: nations.firstOrNull()
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
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.clip(CircleShape).background(Color.Black.copy(alpha=0.3f)).clickable { onBack() }.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = NssAccent, modifier = Modifier.size(16.dp))
                }
                Spacer(Modifier.weight(1f))
                Text("SELECT NATION", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                Spacer(Modifier.weight(1f))
                Box(Modifier.size(32.dp)) // balance
            }

            // Top Area: Nation Stats & Start Game
            if (nation != null) {
                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Left: Selected Nation Details
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0x990A111A))
                            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(nation.flagEmoji, fontSize = 28.sp)
                            Column {
                                Text(nation.name.uppercase(), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                                Text(nation.officialName, color = NssMutedForeground, fontSize = 10.sp)
                            }
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        
                        // Stats Grid
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            StatBlock("POPULATION", "${nation.vitals.population / 1_000_000}M")
                            StatBlock("BUDGET", "$${nation.vitals.budget / 1_000}B")
                            StatBlock("MILITARY", "${nation.militaryStrength / 1000}K")
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        Text("DIFFICULTY", color = NssAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Spacer(Modifier.height(8.dp))
                        
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(challenges) { challenge ->
                                val isSelected = selectedChallengeId == challenge.id
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) NssAccent else Color(0xFF131A26))
                                        .clickable { selectedChallengeId = challenge.id }
                                        .padding(horizontal = 16.dp, vertical = 10.dp)
                                ) {
                                    Text(
                                        challenge.title,
                                        color = if (isSelected) Color.White else NssMutedForeground,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        
                        Spacer(Modifier.weight(1f))
                        
                        Text("IDEOLOGY & GOVERNMENT", color = NssAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Spacer(Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(Color(0xFF131A26)).padding(10.dp)) {
                                Column {
                                    Text("IDEOLOGY", color = NssMutedForeground, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    Text(nation.ideology.name, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(Color(0xFF131A26)).padding(10.dp)) {
                                Column {
                                    Text("RULING SYSTEM", color = NssMutedForeground, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    Text(nation.governmentSystem.name.replace("_", " "), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                        
                        Spacer(Modifier.weight(1f))
                        
                        Button(
                            onClick = { 
                                // Default scenario is Modern Age
                                onSelectCountry(nation.id, ScenarioCatalog.ALL.first().id, selectedChallengeId)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NssEmerald, contentColor = Color.White),
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("COMMENCE COMMAND", fontSize = 13.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                        }
                    }
                    
                    // Right: Scrollable Nation List
                    LazyColumn(
                        modifier = Modifier
                            .weight(0.8f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0x990A111A))
                            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(16.dp)),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(nations) { n ->
                            val isSelected = selectedNationId == n.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0xFF1D283A) else Color.Transparent)
                                    .border(1.dp, if (isSelected) NssAccent else Color.Transparent, RoundedCornerShape(8.dp))
                                    .clickable { selectedNationId = n.id }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(n.flagEmoji, fontSize = 18.sp)
                                Text(
                                    n.name, 
                                    color = if (isSelected) Color.White else NssMutedForeground,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
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
        Text(label, color = NssMutedForeground, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Text(value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
    }
}
