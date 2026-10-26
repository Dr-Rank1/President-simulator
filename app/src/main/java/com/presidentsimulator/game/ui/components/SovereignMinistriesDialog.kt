package com.presidentsimulator.game.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
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
import com.presidentsimulator.game.data.SovereignMinistriesState
import com.presidentsimulator.game.ui.theme.NssEmerald
import com.presidentsimulator.game.ui.theme.NssRed

@Composable
fun SovereignMinistriesDialog(
    state: SovereignMinistriesState,
    playerBudget: Long,
    onDismiss: () -> Unit,
    onProcurePoliceEquipment: (String) -> Unit,
    onBuildSportsArena: (String) -> Unit,
    onBuildEcologyFacility: (String) -> Unit,
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Police, 1: Sports, 2: Ecology

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
                // Top Header Image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                ) {
                    val headerRes = when (selectedTab) {
                        0 -> R.drawable.prokuratura
                        1 -> R.drawable.stadium
                        else -> R.drawable.waterpipes
                    }
                    Image(
                        painter = painterResource(id = headerRes),
                        contentDescription = "Sovereign Ministries",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xCC0A101D)),
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
                                text = "SOVEREIGN STATE MINISTRIES",
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                            )
                            Text(
                                text = when (selectedTab) {
                                    0 -> "MINISTRY OF INTERNAL AFFAIRS & LAW ENFORCEMENT"
                                    1 -> "MINISTRY OF YOUTH, ATHLETICS & OLYMPIC SPORTS"
                                    else -> "MINISTRY OF ECOLOGY & ENVIRONMENTAL PROTECTION"
                                },
                                color = Color(0xFFD97706),
                                fontSize = 10.sp,
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

                // 3 Ministry Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E293B)),
                ) {
                    listOf("1. Police & Crime", "2. Olympic Sports", "3. Ecology & Water").forEachIndexed { idx, label ->
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

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    when (selectedTab) {
                        0 -> {
                            // Police & Crime Matrix
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF1E293B))
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceAround,
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("SAFETY SCORE", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        Text("${state.police.overallCrimeSafetyScore}%", color = NssEmerald, fontSize = 14.sp, fontWeight = FontWeight.Black)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("HOOLIGANISM", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        Text("${state.police.hooliganismRatePercent}%", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("GANG/VIOLENT", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        Text("${state.police.violentCrimeRatePercent}%", color = NssRed, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            val policeEquip = listOf(
                                Triple("Patrol Cruiser Fleet", R.drawable.pic_pilicy_auto, 800_000_000L),
                                Triple("SWAT Armored APCs", R.drawable.pic_broneauto, 1_500_000_000L),
                                Triple("Service Handguns & Rifles", R.drawable.pic_policy_pistol, 400_000_000L),
                                Triple("Non-Lethal Riot Stun Batons", R.drawable.pic_policy_palka, 200_000_000L),
                                Triple("High-Security Federal Prison", R.drawable.turma, 3_000_000_000L),
                                Triple("Prosecutor's District Office", R.drawable.prokuratura, 1_800_000_000L),
                            )

                            items(policeEquip.size) { idx ->
                                val (name, icon, cost) = policeEquip[idx]
                                MinistryActionRow(name = name, iconRes = icon, cost = cost, canAfford = playerBudget >= cost) {
                                    onProcurePoliceEquipment(name)
                                }
                            }
                        }

                        1 -> {
                            // Olympic Sports Arenas
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF1E293B))
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceAround,
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("TOTAL ARENAS", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        Text("${state.sports.totalSportsFacilities}", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("PUBLIC HAPPINESS", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        Text("+${state.sports.publicHappinessBonus}%", color = NssEmerald, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("OLYMPIC MEDALS", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        Text("${state.sports.internationalMedalsWon}", color = Color(0xFFFBBF24), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            val sportsArenas = listOf(
                                Triple("National Football Stadium", R.drawable.stadium, 4_000_000_000L),
                                Triple("Olympic Ice Hockey Arena", R.drawable.rink, 3_500_000_000L),
                                Triple("Basketball Colosseum", R.drawable.basketballarena, 2_800_000_000L),
                                Triple("Cycling Velodrome", R.drawable.cycling, 1_900_000_000L),
                                Triple("Olympic Aquatic Center", R.drawable.pool, 2_400_000_000L),
                                Triple("National Equestrian Racecourse", R.drawable.racecourse, 2_100_000_000L),
                            )

                            items(sportsArenas.size) { idx ->
                                val (name, icon, cost) = sportsArenas[idx]
                                MinistryActionRow(name = name, iconRes = icon, cost = cost, canAfford = playerBudget >= cost) {
                                    onBuildSportsArena(name)
                                }
                            }
                        }

                        2 -> {
                            // Ecology & Environmental Protection
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF1E293B))
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceAround,
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("AIR QUALITY (AQI)", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        Text("${state.ecology.airQualityIndex}", color = if (state.ecology.airQualityIndex < 100) NssEmerald else NssRed, fontSize = 14.sp, fontWeight = FontWeight.Black)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("RENEWABLE SHARE", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        Text("${state.ecology.renewableEnergySharePercent}%", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("HEALTH INDEX", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        Text("${state.ecology.publicHealthIndex}%", color = Color(0xFF6EE7B7), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            val ecoProjects = listOf(
                                Triple("Municipal Water Purification Plant", R.drawable.waterpipes, 3_000_000_000L),
                                Triple("Industrial Waste Recycling Center", R.drawable.ic_ecology, 2_200_000_000L),
                                Triple("Protected National Biosphere Reserve", R.drawable.forestindustry, 1_500_000_000L),
                            )

                            items(ecoProjects.size) { idx ->
                                val (name, icon, cost) = ecoProjects[idx]
                                MinistryActionRow(name = name, iconRes = icon, cost = cost, canAfford = playerBudget >= cost) {
                                    onBuildEcologyFacility(name)
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
private fun MinistryActionRow(
    name: String,
    iconRes: Int,
    cost: Long,
    canAfford: Boolean,
    onExecute: () -> Unit,
) {
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
                painter = painterResource(id = iconRes),
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .border(1.dp, Color(0xFF475569), RoundedCornerShape(6.dp)),
            )
            Column {
                Text(name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(formatCompactMoney(cost), color = Color(0xFF34D399), fontSize = 10.sp)
            }
        }

        Button(
            onClick = onExecute,
            enabled = canAfford,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFD97706),
                disabledContainerColor = Color(0xFF334155),
            ),
            shape = RoundedCornerShape(6.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                Text("Build", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
