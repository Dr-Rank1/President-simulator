package com.presidentsimulator.game.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.presidentsimulator.game.R
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.TreatyType
import com.presidentsimulator.game.data.WarGoal
import com.presidentsimulator.game.ui.components.CountryFlag
import com.presidentsimulator.game.ui.components.CountryFlagHelper
import com.presidentsimulator.game.ui.components.formatCompactMoney
import com.presidentsimulator.game.ui.theme.NssAccent
import com.presidentsimulator.game.ui.theme.NssEmerald
import com.presidentsimulator.game.ui.theme.NssRed
import com.presidentsimulator.game.viewmodel.GameViewModel

@Composable
fun DiplomacyScreen(
    state: GameState,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Embassies & Bilateral, 1: UN Security Council

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        // UN & Foreign Ministry Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(115.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.unitednations),
                contentDescription = "United Nations & Foreign Affairs",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xCC08101E)),
            )
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Image(
                            painter = painterResource(id = R.drawable.un_white),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                        )
                        Text(
                            text = "DEPARTMENT OF STATE & UN MISSIONS",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                        )
                    }
                    Text(
                        text = "GLOBAL DIPLOMACY & MULTILATERAL TREATIES",
                        color = Color(0xFFD97706),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("DIPLOMATIC INFLUENCE", color = Color.Gray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    Text("${state.diplomacy.diplomaticInfluence} Points", color = Color(0xFF60A5FA), fontSize = 13.sp, fontWeight = FontWeight.Black)
                    Text("Trade Treaties: ${state.diplomacy.rivals.count { it.hasTradeTreaty }}", color = Color(0xFF34D399), fontSize = 9.sp)
                }
            }
        }

        // Navigation Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E293B)),
        ) {
            listOf("1. Embassies & Sovereign Rivals", "2. UN Security Council").forEachIndexed { idx, label ->
                val isSelected = selectedTab == idx
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedTab = idx }
                        .background(if (isSelected) Color(0xFF0F172A) else Color.Transparent)
                        .border(
                            BorderStroke(
                                width = if (isSelected) 2.dp else 0.dp,
                                color = if (isSelected) Color(0xFFD97706) else Color.Transparent,
                            ),
                        )
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) Color(0xFFFBBF24) else Color.LightGray,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    )
                }
            }
        }

        if (selectedTab == 0) {
            // Embassies & Sovereign Rivals List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(state.diplomacy.rivals) { rival ->
                    val relationColor = when {
                        rival.relationshipScore > 65 -> Color(0xFF10B981)
                        rival.relationshipScore < 30 -> Color(0xFFEF4444)
                        else -> Color(0xFFFBBF24)
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E293B))
                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                    ) {
                        // Header: Flag, Name, Relation Progress
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            CountryFlag(
                                countryName = rival.name,
                                modifier = Modifier
                                    .size(width = 38.dp, height = 26.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .border(0.5.dp, Color(0xFF64748B), RoundedCornerShape(3.dp)),
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = rival.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Spacer(modifier = Modifier.height(3.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Bilateral Relations:", fontSize = 9.sp, color = Color.Gray)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    LinearProgressIndicator(
                                        progress = { (rival.relationshipScore / 100f).coerceIn(0f, 1f) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(5.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = relationColor,
                                        trackColor = Color(0xFF0F172A),
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("${rival.relationshipScore}%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = relationColor)
                                }
                            }
                        }

                        // Treaties Badges
                        if (rival.hasTradeTreaty || rival.hasNonAggressionPact) {
                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                if (rival.hasTradeTreaty) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFF065F46))
                                            .padding(horizontal = 6.dp, vertical = 2.dp),
                                    ) {
                                        Icon(Icons.Default.AttachMoney, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(11.dp))
                                        Spacer(Modifier.width(3.dp))
                                        Text("Trade Deal", fontSize = 9.sp, color = Color(0xFF6EE7B7), fontWeight = FontWeight.Bold)
                                    }
                                }
                                if (rival.hasNonAggressionPact) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFF1E3A8A))
                                            .padding(horizontal = 6.dp, vertical = 2.dp),
                                    ) {
                                        Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF60A5FA), modifier = Modifier.size(11.dp))
                                        Spacer(Modifier.width(3.dp))
                                        Text("Non-Aggression", fontSize = 9.sp, color = Color(0xFF93C5FD), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        // Action Buttons Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            DiplomacyButton(
                                text = "State Visit",
                                color = Color(0xFF0284C7),
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.conductStateVisit(rival.id) },
                            )
                            DiplomacyButton(
                                text = "Foreign Aid",
                                color = Color(0xFF059669),
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.sendForeignAid(rival.id) },
                            )
                            DiplomacyButton(
                                text = "Trade Deal",
                                color = Color(0xFFD97706),
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.negotiateTreaty(rival.id, TreatyType.TRADE) },
                            )
                            DiplomacyButton(
                                text = "Declare War",
                                color = Color(0xFFDC2626),
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.declareWar(rival.id, WarGoal.REPARATIONS) },
                            )
                        }
                    }
                }
            }
        } else {
            // UN Security Council & Sanctions Chamber
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E293B))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("WORLD PEACE INDEX", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text("74% Stable", color = NssEmerald, fontSize = 15.sp, fontWeight = FontWeight.Black)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("GLOBAL TENSION", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(if (state.diplomacy.activeWar != null) "WAR THEATER" else "LOW CONFLICT", color = if (state.diplomacy.activeWar != null) NssRed else Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("P5 VETO PRIVILEGE", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text("RECOGNIZED", color = Color(0xFFFBBF24), fontSize = 13.sp, fontWeight = FontWeight.Black)
                    }
                }

                Text(
                    "UNITED NATIONS GENERAL ASSEMBLY RESOLUTIONS",
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                )

                val resolutions = listOf(
                    Triple("Global Nuclear Non-Proliferation Resolution", "Affirms inspection regime and prohibits unauthorized warhead proliferation.", Color(0xFF059669)),
                    Triple("Multilateral Humanitarian Aid Corridor Mandate", "Guarantees free passage for medical convoys in disputed zones.", Color(0xFF0284C7)),
                    Triple("International Environmental Carbon Cap Protocol", "Establishes emission quotas for heavy industrial complexes.", Color(0xFFD97706)),
                )

                resolutions.forEach { (title, desc, tagColor) ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E293B))
                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("RATIFIED", color = tagColor, fontSize = 9.sp, fontWeight = FontWeight.Black)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(desc, color = Color.LightGray, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun DiplomacyButton(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text.uppercase(), color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
    }
}
