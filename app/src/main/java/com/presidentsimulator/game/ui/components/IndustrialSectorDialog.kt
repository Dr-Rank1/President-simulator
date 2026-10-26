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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
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
import com.presidentsimulator.game.data.IndustrialSectorData
import com.presidentsimulator.game.data.SubPlant
import com.presidentsimulator.game.ui.theme.NssAccent
import com.presidentsimulator.game.ui.theme.NssEmerald
import com.presidentsimulator.game.ui.theme.NssRed

@Composable
fun IndustrialSectorDialog(
    sector: IndustrialSectorData,
    playerBudget: Long,
    onDismiss: () -> Unit,
    onBuildSubPlant: (SubPlant) -> Unit,
    onModernizeSector: () -> Unit,
    onResolveStrike: () -> Unit,
) {
    val modernizeCost = sector.level * 4_500_000_000L

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
                // Top Header Image with overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                ) {
                    Image(
                        painter = painterResource(id = sector.type.iconDrawable),
                        contentDescription = sector.type.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xBB0A101D)),
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
                                text = sector.type.title.uppercase(),
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                            )
                            Text(
                                text = "SOVEREIGN INDUSTRIAL MATRIX • TIER ${sector.level}",
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

                // Strike Alert Banner if active
                if (sector.isUnderStrike) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF991B1B))
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White)
                            Column {
                                Text("GENERAL INDUSTRIAL STRIKE IN PROGRESS", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                                Text("Production slashed to 40%. Labor demands wage settlement.", color = Color(0xFFFECACA), fontSize = 10.sp)
                            }
                        }
                        Button(
                            onClick = onResolveStrike,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(6.dp),
                        ) {
                            Text("Settle (\$500M)", color = Color(0xFF991B1B), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Sector Vital Statistics Strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E293B))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("MONTHLY REVENUE", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(
                            formatCompactMoney(sector.netRevenue),
                            color = if (sector.netRevenue >= 0) NssEmerald else NssRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("WORKFORCE", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "${formatCompactMil(sector.workers)} Workers",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("EFFICIENCY", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "${(sector.efficiencyMultiplier * 100).toInt()}%",
                            color = NssAccent,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("FACILITIES", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "${sector.facilitiesCount} Operating",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                // Modernization Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF182234))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(16.dp))
                            Text("Modernize & Automate Sector", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("Boosts industrial efficiency by +15% and increases gross GDP output.", color = Color.LightGray, fontSize = 10.sp)
                    }
                    Button(
                        onClick = onModernizeSector,
                        enabled = playerBudget >= modernizeCost,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF047857),
                            disabledContainerColor = Color(0xFF1F2937),
                        ),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Upgrade", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(formatCompactMoney(modernizeCost), color = Color(0xFF6EE7B7), fontSize = 9.sp)
                        }
                    }
                }

                Text(
                    text = "PRODUCTION PLANTS & OPERATIONAL UNITS",
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    letterSpacing = 1.sp,
                )

                // Sub-Plants List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(sector.subPlants) { plant ->
                        val cost = plant.buildCostMillion * 1_000_000L
                        val canAfford = playerBudget >= cost

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E293B))
                                .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Image(
                                    painter = painterResource(id = plant.imageDrawable),
                                    contentDescription = plant.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .border(1.dp, Color(0xFF475569), RoundedCornerShape(6.dp)),
                                )
                                Column {
                                    Text(plant.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        "Units: ${plant.count}  •  +${plant.outputUnitBonus}M / mo",
                                        color = Color(0xFF34D399),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                            }

                            Button(
                                onClick = { onBuildSubPlant(plant) },
                                enabled = canAfford,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFD97706),
                                    disabledContainerColor = Color(0xFF334155),
                                ),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Build", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Text(formatCompactMoney(cost), color = Color(0xFFFEF3C7), fontSize = 8.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
            }
        }
    }
}
