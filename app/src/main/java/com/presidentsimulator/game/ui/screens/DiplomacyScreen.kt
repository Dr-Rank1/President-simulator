package com.presidentsimulator.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.TreatyType
import com.presidentsimulator.game.data.WarGoal
import com.presidentsimulator.game.viewmodel.GameViewModel

@Composable
fun DiplomacyScreen(
    state: GameState,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize().background(Color(0xFFF5F1E6))) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF03A9F4))
                .padding(vertical = 12.dp, horizontal = 16.dp)
        ) {
            Text(
                text = "FOREIGN AFFAIRS",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
        ) {
            items(state.diplomacy.rivals) { rival ->
                val relationColor = when {
                    rival.relationshipScore > 60 -> Color(0xFF43A047)
                    rival.relationshipScore < 25 -> Color(0xFFD32F2F)
                    else -> Color(0xFFFFA000)
                }
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .border(0.5.dp, Color.LightGray, RoundedCornerShape(8.dp))
                ) {
                    // Header: Flag and Name
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFE3F2FD))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = rival.flagEmoji, fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = rival.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            Spacer(modifier = Modifier.height(4.dp))
                            // Progress bar
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Relation:", fontSize = 10.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.width(4.dp))
                                LinearProgressIndicator(
                                    progress = { (rival.relationshipScore / 100f).coerceIn(0f, 1f) },
                                    modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)),
                                    color = relationColor,
                                    trackColor = Color(0xFFE2E8F0)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${rival.relationshipScore}%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = relationColor)
                            }
                        }
                    }

                    // Treaties
                    if (rival.hasTradeTreaty || rival.hasNonAggressionPact) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (rival.hasTradeTreaty) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.background(Color(0xFFE8F5E9), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                    Icon(Icons.Default.AttachMoney, contentDescription = null, tint = Color(0xFF43A047), modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Trade", fontSize = 10.sp, color = Color(0xFF43A047), fontWeight = FontWeight.Bold)
                                }
                            }
                            if (rival.hasNonAggressionPact) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.background(Color(0xFFE3F2FD), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                    Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF1E88E5), modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Pact", fontSize = 10.sp, color = Color(0xFF1E88E5), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Actions
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        DiplomacyButton(
                            text = "Visit",
                            color = Color(0xFF0288D1),
                            onClick = { viewModel.conductStateVisit(rival.id) }
                        )
                        DiplomacyButton(
                            text = "Send Aid",
                            color = Color(0xFF43A047),
                            onClick = { viewModel.sendForeignAid(rival.id) }
                        )
                        DiplomacyButton(
                            text = "Trade Pact",
                            color = Color(0xFF8E24AA),
                            onClick = { viewModel.negotiateTreaty(rival.id, TreatyType.TRADE) }
                        )
                        DiplomacyButton(
                            text = "War",
                            color = Color(0xFFD32F2F),
                            onClick = { viewModel.declareWar(rival.id, WarGoal.REPARATIONS) }
                        )
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
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text.uppercase(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}
