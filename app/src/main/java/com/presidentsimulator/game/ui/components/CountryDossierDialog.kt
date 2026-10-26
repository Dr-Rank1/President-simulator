package com.presidentsimulator.game.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.presidentsimulator.game.R
import com.presidentsimulator.game.data.WorldCountryDossier
import com.presidentsimulator.game.ui.theme.NssAccent
import com.presidentsimulator.game.ui.theme.NssEmerald
import com.presidentsimulator.game.ui.theme.NssRed

@Composable
fun CountryDossierDialog(
    dossier: WorldCountryDossier,
    onDismiss: () -> Unit,
    onOpenEmbassy: () -> Unit = {},
    onSignNonAggression: () -> Unit = {},
    onSignTradeDeal: () -> Unit = {},
    onSendAid: () -> Unit = {},
    onDeploySpy: () -> Unit = {},
    onDeclareWar: () -> Unit = {},
    onDemandVassalage: () -> Unit = {},
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF0F172A)) // Midnight Presidential Navy
                .border(1.5.dp, Color(0xFFD97706), RoundedCornerShape(14.dp)) // Executive Gold border
                .padding(14.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            // Header Row: Flag + Name + Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (dossier.flagRes != 0) {
                        Image(
                            painter = painterResource(id = dossier.flagRes),
                            contentDescription = dossier.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(width = 38.dp, height = 26.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .border(0.5.dp, Color.White, RoundedCornerShape(4.dp)),
                        )
                    } else {
                        Text(dossier.flagEmoji, fontSize = 26.sp)
                    }

                    Column {
                        Text(
                            text = dossier.name.uppercase(),
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                        )
                        Text(
                            text = "Capital: ${dossier.capital} · ${dossier.ideology.name}",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0x33FFFFFF))
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(Modifier.height(12.dp))

            // Geopolitical Statistics Grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E293B))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text("POPULATION", color = Color(0xFF64748B), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(formatCompactMil(dossier.population), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black)
                }
                Column {
                    Text("ANNUAL GDP", color = Color(0xFF64748B), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text("\$${dossier.gdpBillion}B", color = NssAccent, fontSize = 13.sp, fontWeight = FontWeight.Black)
                }
                Column {
                    Text("MILITARY", color = Color(0xFF64748B), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(formatCompactMil(dossier.activeTroops), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black)
                }
                Column {
                    Text("RELATIONS", color = Color(0xFF64748B), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "${dossier.relationshipScore}/100",
                        color = when {
                            dossier.relationshipScore >= 70 -> NssEmerald
                            dossier.relationshipScore <= 35 -> NssRed
                            else -> NssAccent
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Hardware Stockpile Status Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF131D2D))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Hardware: ${dossier.tanks} Tanks · ${dossier.jets} Jets",
                    color = Color(0xFFCBD5E1),
                    fontSize = 11.sp,
                )
                if (dossier.nuclearPower) {
                    Text(
                        text = "☢ NUCLEAR CAPABLE",
                        color = Color(0xFFF59E0B),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // SECTION 1: BILATERAL DIPLOMATIC COMMANDS
            Text(
                text = "BILATERAL DIPLOMATIC PROTOCOLS",
                color = Color(0xFFD97706),
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
            )
            Spacer(Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionChip(
                    label = if (dossier.hasEmbassy) "✓ Embassy Open" else "Open Embassy",
                    enabled = !dossier.hasEmbassy,
                    onClick = onOpenEmbassy,
                    modifier = Modifier.weight(1f),
                )
                ActionChip(
                    label = if (dossier.nonAggressionPact) "✓ Non-Aggression" else "Sign NAP Pact",
                    enabled = !dossier.nonAggressionPact,
                    onClick = onSignNonAggression,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionChip(
                    label = if (dossier.tradeDealActive) "✓ Trade Contract" else "Trade Deal",
                    enabled = !dossier.tradeDealActive,
                    onClick = onSignTradeDeal,
                    modifier = Modifier.weight(1f),
                )
                ActionChip(
                    label = "Send Foreign Aid (\$500M)",
                    enabled = true,
                    onClick = onSendAid,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(14.dp))

            // SECTION 2: INTELLIGENCE & COVERT ACTIONS
            Text(
                text = "COVERT INTELLIGENCE & ESPIONAGE",
                color = Color(0xFF38BDF8),
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
            )
            Spacer(Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionChip(
                    label = "Infiltrate Intelligence (Spy)",
                    enabled = true,
                    onClick = onDeploySpy,
                    modifier = Modifier.weight(1f),
                )
                ActionChip(
                    label = "Demand Vassalage / Puppet",
                    enabled = dossier.relationshipScore >= 80,
                    onClick = onDemandVassalage,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(14.dp))

            // SECTION 3: WAR POWERS & MILITARY COMMAND
            Text(
                text = "WAR POWERS & STRATEGIC THEATER",
                color = NssRed,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
            )
            Spacer(Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (dossier.atWarWithPlayer) Color(0xFF7F1D1D) else Color(0xFF991B1B))
                    .clickable(onClick = onDeclareWar)
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (dossier.atWarWithPlayer) "ACTIVE WAR THEATER ›" else "DECLARE WAR ON ${dossier.name.uppercase()}",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                )
            }
        }
    }
}

@Composable
private fun ActionChip(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (enabled) Color(0xFF1E293B) else Color(0xFF0F172A))
            .border(0.75.dp, if (enabled) Color(0xFF475569) else Color(0xFF1E293B), RoundedCornerShape(6.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (enabled) Color.White else Color(0xFF64748B),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
    }
}
