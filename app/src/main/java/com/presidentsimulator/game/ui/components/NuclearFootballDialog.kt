package com.presidentsimulator.game.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
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
import com.presidentsimulator.game.data.NuclearSiloState
import com.presidentsimulator.game.ui.theme.NssEmerald
import com.presidentsimulator.game.ui.theme.NssRed

@Composable
fun NuclearFootballDialog(
    nuclearSilo: NuclearSiloState,
    onDismiss: () -> Unit,
    onSetDefcon: (Int) -> Unit,
    onToggleLaunchKeys: () -> Unit,
    onBuildNewSilo: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF0A0F1D),
            border = BorderStroke(2.dp, Color(0xFFDC2626)),
            shadowElevation = 24.dp,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header with Nuclear Football Artwork
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.yadernoeoruzhie),
                        contentDescription = "Nuclear Football",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xCC000000)),
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
                                text = "PRESIDENTIAL NUCLEAR FOOTBALL",
                                color = Color(0xFFEF4444),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                            )
                            Text(
                                text = "STRATEGIC MISSILE SILO NETWORK (dialogyadernaweapon)",
                                color = Color.LightGray,
                                fontSize = 10.sp,
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

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    // DEFCON Level Selector
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E293B))
                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                    ) {
                        Text("STRATEGIC DEFCON READINESS ALERT", color = Color(0xFFD97706), fontSize = 11.sp, fontWeight = FontWeight.Black)
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            (5 downTo 1).forEach { level ->
                                val isSelected = nuclearSilo.defconLevel == level
                                val levelColor = when (level) {
                                    5 -> Color(0xFF059669) // Green (Normal peacetime)
                                    4 -> Color(0xFF0284C7) // Blue (Increased intelligence)
                                    3 -> Color(0xFFD97706) // Yellow (Air Force ready in 15m)
                                    2 -> Color(0xFFEA580C) // Orange (Armed forces ready in 6h)
                                    1 -> Color(0xFFDC2626) // Red (Maximum readiness / nuclear)
                                    else -> Color.Gray
                                }

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) levelColor else Color(0xFF0F172A))
                                        .border(
                                            1.5.dp,
                                            if (isSelected) Color.White else Color(0xFF334155),
                                            RoundedCornerShape(6.dp),
                                        )
                                        .clickable { onSetDefcon(level) }
                                        .padding(vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Text("DEFCON", color = if (isSelected) Color.White else Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    Text("$level", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }

                    // Silo Status & Deterrence
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E293B))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("WARHEADS", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text("${nuclearSilo.nuclearWarheads}", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("ACTIVE SILOS", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text("${nuclearSilo.activeSilos}", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("DETERRENCE", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text("${nuclearSilo.deterrenceIndex}%", color = NssEmerald, fontSize = 14.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    // Two-Person Launch Keys Authorization Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (nuclearSilo.launchKeysInserted) Color(0xFF450A0A) else Color(0xFF1E293B))
                            .border(
                                1.dp,
                                if (nuclearSilo.launchKeysInserted) Color(0xFFDC2626) else Color(0xFF334155),
                                RoundedCornerShape(8.dp),
                            )
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(
                                if (nuclearSilo.launchKeysInserted) Icons.Default.LockOpen else Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (nuclearSilo.launchKeysInserted) Color(0xFFEF4444) else Color.Gray,
                            )
                            Column {
                                Text("Dual Presidential Authorization Keys", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    if (nuclearSilo.launchKeysInserted) "KEYS INSERTED • LAUNCH CIRCUITS ENERGIZED" else "SAFE STATE • Silos locked against unauthorized launch",
                                    color = if (nuclearSilo.launchKeysInserted) Color(0xFFFCA5A5) else Color.Gray,
                                    fontSize = 10.sp,
                                )
                            }
                        }

                        Button(
                            onClick = onToggleLaunchKeys,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (nuclearSilo.launchKeysInserted) Color(0xFFDC2626) else Color(0xFF475569),
                            ),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        ) {
                            Text(if (nuclearSilo.launchKeysInserted) "Disarm" else "Arm", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Construct New Underground Silo Button
                    Button(
                        onClick = onBuildNewSilo,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857)),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text("Construct Hardened Underground Silo (\$10,000M)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
