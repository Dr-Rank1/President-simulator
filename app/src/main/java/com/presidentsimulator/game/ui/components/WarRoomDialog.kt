package com.presidentsimulator.game.ui.components

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.presidentsimulator.game.data.MilitaryDoctrine
import com.presidentsimulator.game.data.WarRoomState
import com.presidentsimulator.game.ui.theme.NssAccent
import com.presidentsimulator.game.ui.theme.NssEmerald
import com.presidentsimulator.game.ui.theme.NssRed

@Composable
fun WarRoomDialog(
    warRoom: WarRoomState,
    onDismiss: () -> Unit,
    onSelectDoctrine: (MilitaryDoctrine) -> Unit,
    onOpenNuclearFootball: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(2.dp, Color(0xFFD97706)),
            shadowElevation = 24.dp,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Header Image with General Staff Backdrop
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.fongenshtab),
                        contentDescription = "General Staff War Room",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xBB0B132B)),
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                text = "GENERAL STAFF & WAR ROOM",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                            )
                            Text(
                                text = "PENTAGON HIGH COMMAND • STRATEGIC DIRECTORY",
                                color = Color(0xFFD97706),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0x88000000))
                                .clickable { onDismiss() },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                }

                // Nuclear Football / DEFCON quick alert strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (warRoom.nuclearSilo.defconLevel <= 2) Color(0xFF7F1D1D) else Color(0xFF1E293B))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.yadernoeoruzhie),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(4.dp)),
                        )
                        Column {
                            Text(
                                "STRATEGIC NUCLEAR DETERRENT: DEFCON ${warRoom.nuclearSilo.defconLevel}",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                            )
                            Text(
                                "${warRoom.nuclearSilo.nuclearWarheads} Warheads  •  ${warRoom.nuclearSilo.activeSilos} Active Silos  •  Deterrence ${warRoom.nuclearSilo.deterrenceIndex}%",
                                color = Color(0xFFCBD5E1),
                                fontSize = 9.sp,
                            )
                        }
                    }

                    Button(
                        onClick = onOpenNuclearFootball,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB91C1C)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(6.dp),
                    ) {
                        Text("Football", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    // Military Doctrine Card
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E293B))
                                .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                        ) {
                            Text("NATIONAL MILITARY DOCTRINE", color = Color(0xFFD97706), fontSize = 11.sp, fontWeight = FontWeight.Black)
                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                MilitaryDoctrine.entries.forEach { doc ->
                                    val isSelected = warRoom.selectedDoctrine == doc
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSelected) Color(0xFF1E3A8A) else Color(0xFF0F172A))
                                            .border(
                                                1.dp,
                                                if (isSelected) Color(0xFF60A5FA) else Color(0xFF334155),
                                                RoundedCornerShape(6.dp),
                                            )
                                            .clickable { onSelectDoctrine(doc) }
                                            .padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                    ) {
                                        Text(doc.title, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            "Atk: ${if (doc.attackBonus >= 0) "+" else ""}${(doc.attackBonus * 100).toInt()}%",
                                            color = if (doc.attackBonus >= 0) NssEmerald else NssRed,
                                            fontSize = 9.sp,
                                        )
                                        Text(
                                            "Def: ${if (doc.defenseBonus >= 0) "+" else ""}${(doc.defenseBonus * 100).toInt()}%",
                                            color = if (doc.defenseBonus >= 0) NssEmerald else NssRed,
                                            fontSize = 9.sp,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Authentic APK ArmyStat: Combat Casualty Ledger
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E293B))
                                .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                        ) {
                            Text("BATTLEFIELD CASUALTY LEDGER (ArmyStat)", color = Color(0xFFD97706), fontSize = 11.sp, fontWeight = FontWeight.Black)
                            Spacer(Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("FRIENDLY FORCES CASUALTIES", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    Text("Troops Fallen: ${formatCompactMil(warRoom.casualtyLedger.troopsFallen)}", color = Color.White, fontSize = 11.sp)
                                    Text("Armor Lost: ${warRoom.casualtyLedger.tanksLost} Tanks", color = Color.White, fontSize = 11.sp)
                                    Text("Airframes Lost: ${warRoom.casualtyLedger.jetsLost} Jets", color = Color.White, fontSize = 11.sp)
                                }
                                Column {
                                    Text("ENEMY COMBAT LOSSES", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    Text("Troops Eliminated: ${formatCompactMil(warRoom.casualtyLedger.enemyTroopsNeutralized)}", color = NssEmerald, fontSize = 11.sp)
                                    Text("Tanks Destroyed: ${warRoom.casualtyLedger.enemyTanksDestroyed}", color = NssEmerald, fontSize = 11.sp)
                                    Text("Jets Downed: ${warRoom.casualtyLedger.enemyJetsDowned}", color = NssEmerald, fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    // High Command Staff Officers List
                    item {
                        Text("JOINT CHIEFS OF STAFF COMMANDERS", color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    }

                    items(warRoom.commanders) { commander ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E293B))
                                .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Image(
                                painter = painterResource(id = commander.portraitDrawable),
                                contentDescription = commander.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .border(1.dp, Color(0xFFD97706), RoundedCornerShape(6.dp)),
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(commander.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    repeat(4) {
                                        Image(
                                            painter = painterResource(id = R.drawable.stararmy),
                                            contentDescription = null,
                                            modifier = Modifier.size(9.dp),
                                        )
                                    }
                                    Spacer(Modifier.width(3.dp))
                                    Text(
                                        text = commander.rank.replace(" (4-Star)", "").replace(" (★★★★)", ""),
                                        color = Color(0xFFFBBF24),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                                Text(commander.roleTitle, color = Color.LightGray, fontSize = 9.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Loyalty: ${commander.loyaltyPercent}%", color = Color(0xFF34D399), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text("Doctrine: ${commander.doctrine.title.take(12)}...", color = Color.Gray, fontSize = 9.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
