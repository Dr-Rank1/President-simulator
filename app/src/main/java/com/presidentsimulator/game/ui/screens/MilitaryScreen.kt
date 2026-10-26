package com.presidentsimulator.game.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.presidentsimulator.game.R
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.MilitaryDoctrine
import com.presidentsimulator.game.data.MilitaryFacilityType
import com.presidentsimulator.game.data.MilitaryHardware
import com.presidentsimulator.game.data.WarTactic
import com.presidentsimulator.game.ui.components.formatCompactMoney
import com.presidentsimulator.game.ui.components.formatCompactMil
import com.presidentsimulator.game.ui.theme.NssAccent
import com.presidentsimulator.game.ui.theme.NssEmerald
import com.presidentsimulator.game.ui.theme.NssRed
import com.presidentsimulator.game.viewmodel.DiplomacyEngine
import com.presidentsimulator.game.viewmodel.GameViewModel
import com.presidentsimulator.game.viewmodel.toArmyString
import com.presidentsimulator.game.viewmodel.toBudgetString

@Composable
fun MilitaryScreen(
    state: GameState,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    var selectedServiceTab by remember { mutableIntStateOf(0) } // 0: Army, 1: Air Force, 2: Navy, 3: War Theater

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        // Pentagon Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
        ) {
            val backdropRes = when (selectedServiceTab) {
                0 -> R.drawable.groundtroops
                1 -> R.drawable.aviation
                2 -> R.drawable.vms
                else -> R.drawable.fongenshtab
            }
            Image(
                painter = painterResource(id = backdropRes),
                contentDescription = "Pentagon Military Command",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xCC090E17)),
            )
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PENTAGON HIGH COMMAND",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                    )
                    Text(
                        text = "ARMED FORCES COMBAT READINESS: ${(state.effectiveCombatStrength / 1000).toInt()}k POWER",
                        color = Color(0xFFD97706),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("ACTIVE TROOPS", color = Color.Gray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    Text(
                        formatCompactMil(state.military.personnel),
                        color = Color(0xFF34D399),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                    )
                }
            }
        }

        // Tri-Service Command Navigation Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E293B)),
        ) {
            listOf("1. Army", "2. Air Force", "3. Navy", "4. War Theater").forEachIndexed { idx, label ->
                val isSelected = selectedServiceTab == idx
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedServiceTab = idx }
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
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            when (selectedServiceTab) {
                0 -> {
                    // 1. ARMY GROUND FORCES
                    item {
                        MilitaryServiceOverview(
                            serviceName = "Ground Army Division",
                            headcount = "${formatCompactMil(state.military.personnel)} Troops",
                            primaryAsset = "${state.military.tanks} Main Battle Tanks",
                            upkeep = formatCompactMoney(state.military.monthlyUpkeep),
                        )
                    }

                    item {
                        MilitaryActionRow(
                            title = "Mobilize Infantry Personnel (Batch 10,000)",
                            subtitle = "Salary Funding: ${(state.military.salaryFunding * 100).toInt()}% • Lead Time 3 Months",
                            cost = (DiplomacyEngine.RECRUIT_COST_PER_SOLDIER * DiplomacyEngine.RECRUIT_BATCH_SIZE),
                            imageRes = R.drawable.soldierstraine,
                            canAfford = state.vitals.budget >= (DiplomacyEngine.RECRUIT_COST_PER_SOLDIER * DiplomacyEngine.RECRUIT_BATCH_SIZE),
                            buttonText = "Recruit",
                            onExecute = { viewModel.recruitPersonnel(DiplomacyEngine.RECRUIT_BATCH_SIZE) },
                        )
                    }

                    item {
                        MilitaryActionRow(
                            title = "Armored Main Battle Tank (T-90/Abrams)",
                            subtitle = "Owned: ${state.military.tanks} • Power +180/unit • Heavy Breakthrough Armor",
                            cost = MilitaryHardware.TANKS.unitCost,
                            imageRes = R.drawable.pic_tank,
                            canAfford = state.vitals.budget >= MilitaryHardware.TANKS.unitCost,
                            buttonText = "Procure",
                            onExecute = { viewModel.purchaseMilitaryHardware(MilitaryHardware.TANKS, 1) },
                        )
                    }

                    item {
                        MilitaryActionRow(
                            title = "Heavy Ordinance Arsenal Facility",
                            subtitle = "Owned: ${state.militaryIndustry.arsenals} • Produces +${state.militaryIndustry.lastTanksProduced} armored units/mo",
                            cost = MilitaryFacilityType.ARSENAL.unitCost,
                            imageRes = R.drawable.pic_broneauto,
                            canAfford = state.vitals.budget >= MilitaryFacilityType.ARSENAL.unitCost,
                            buttonText = "Construct",
                            onExecute = { viewModel.buildMilitaryFacility(MilitaryFacilityType.ARSENAL, 1) },
                        )
                    }
                }

                1 -> {
                    // 2. AIR FORCE
                    item {
                        MilitaryServiceOverview(
                            serviceName = "Tactical & Strategic Air Command",
                            headcount = "${state.military.jets} Air Superiority Fighters",
                            primaryAsset = "Mach 2.2 Stealth Interceptors",
                            upkeep = formatCompactMoney(state.military.monthlyUpkeep / 3L),
                        )
                    }

                    item {
                        MilitaryActionRow(
                            title = "Air Superiority Jet Fighter (Su-35/F-22)",
                            subtitle = "Owned: ${state.military.jets} • Combat Strength +450 • Mach 2.2 Interceptor",
                            cost = MilitaryHardware.FIGHTER_JETS.unitCost,
                            imageRes = R.drawable.armyfighter,
                            canAfford = state.vitals.budget >= MilitaryHardware.FIGHTER_JETS.unitCost,
                            buttonText = "Procure",
                            onExecute = { viewModel.purchaseMilitaryHardware(MilitaryHardware.FIGHTER_JETS, 1) },
                        )
                    }

                    item {
                        MilitaryActionRow(
                            title = "Combat Reconnaissance Drone Array (UAV)",
                            subtitle = "Autonomous surveillance radar & precision strike payload",
                            cost = 1_800_000_000L,
                            imageRes = R.drawable.bpla,
                            canAfford = state.vitals.budget >= 1_800_000_000L,
                            buttonText = "Deploy",
                            onExecute = { viewModel.upgradeMilitaryTraining() },
                        )
                    }

                    item {
                        MilitaryActionRow(
                            title = "Hardened Military Airfield & Radar Base",
                            subtitle = "Owned: ${state.militaryIndustry.airfields} • Produces +${state.militaryIndustry.lastJetsProduced} aircraft/mo",
                            cost = MilitaryFacilityType.AIRFIELD.unitCost,
                            imageRes = R.drawable.aircraftfactory,
                            canAfford = state.vitals.budget >= MilitaryFacilityType.AIRFIELD.unitCost,
                            buttonText = "Construct",
                            onExecute = { viewModel.buildMilitaryFacility(MilitaryFacilityType.AIRFIELD, 1) },
                        )
                    }
                }

                2 -> {
                    // 3. NAVY
                    item {
                        MilitaryServiceOverview(
                            serviceName = "Blue-Water Sovereign Navy",
                            headcount = "${state.military.ships} Surface & Subsurface Vessels",
                            primaryAsset = "Destroyers, Cruisers, Nuclear Subs",
                            upkeep = formatCompactMoney(state.military.monthlyUpkeep / 4L),
                        )
                    }

                    item {
                        MilitaryActionRow(
                            title = "Guided Missile Destroyer / Cruiser",
                            subtitle = "Owned: ${state.military.ships} • Aegis air defense & cruise missile battery",
                            cost = MilitaryHardware.NAVAL_SHIPS.unitCost,
                            imageRes = R.drawable.armydestroyer,
                            canAfford = state.vitals.budget >= MilitaryHardware.NAVAL_SHIPS.unitCost,
                            buttonText = "Commission",
                            onExecute = { viewModel.purchaseMilitaryHardware(MilitaryHardware.NAVAL_SHIPS, 1) },
                        )
                    }

                    item {
                        MilitaryActionRow(
                            title = "Nuclear Attack Submarine (SSN)",
                            subtitle = "Stealth sonar, torpedo tubes, long-range deterrent patrol",
                            cost = 4_500_000_000L,
                            imageRes = R.drawable.armyapl,
                            canAfford = state.vitals.budget >= 4_500_000_000L,
                            buttonText = "Commission",
                            onExecute = { viewModel.upgradeMilitaryTraining() },
                        )
                    }

                    item {
                        MilitaryActionRow(
                            title = "Naval Slipway & Deep-Water Drydock",
                            subtitle = "Owned: ${state.militaryIndustry.shipyards} • Produces capital ships/mo",
                            cost = MilitaryFacilityType.SHIPYARD.unitCost,
                            imageRes = R.drawable.armycruiser,
                            canAfford = state.vitals.budget >= MilitaryFacilityType.SHIPYARD.unitCost,
                            buttonText = "Construct",
                            onExecute = { viewModel.buildMilitaryFacility(MilitaryFacilityType.SHIPYARD, 1) },
                        )
                    }
                }

                3 -> {
                    // 4. WAR THEATER & GENERAL STAFF DOCTRINE
                    item {
                        state.diplomacy.activeWar?.let { war ->
                            val target = state.diplomacy.rivalById(war.targetCountryId)?.name ?: war.targetCountryId
                            val armisticeCost = viewModel.armisticeCost()

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF450A0A))
                                    .border(1.dp, Color(0xFFDC2626), RoundedCornerShape(8.dp))
                                    .padding(12.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFFEF4444))
                                        Text("ACTIVE WAR: $target (MONTH ${war.monthsActive})", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black)
                                    }
                                    Text("${war.warProgress.toInt()}% Control", color = Color(0xFFFCA5A5), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { (war.warProgress / 100f).coerceIn(0f, 1f) },
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                    color = Color(0xFF10B981),
                                    trackColor = Color(0xFF7F1D1D),
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(war.lastBattleSummary.ifBlank { "Forces positioned along the front." }, color = Color(0xFFCBD5E1), fontSize = 10.sp)
                                Spacer(Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { viewModel.signArmistice() },
                                        enabled = state.vitals.budget >= armisticeCost,
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                        shape = RoundedCornerShape(6.dp),
                                    ) {
                                        Text("Armistice (${formatCompactMoney(armisticeCost)})", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        } ?: run {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1E293B))
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("No active wars. The armed forces are on peacetime standby.", color = Color.LightGray, fontSize = 12.sp)
                            }
                        }
                    }

                    // Strategic Military Doctrine Selector
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E293B))
                                .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                        ) {
                            Text("HIGH COMMAND DOCTRINE", color = Color(0xFFD97706), fontSize = 11.sp, fontWeight = FontWeight.Black)
                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                MilitaryDoctrine.entries.forEach { doc ->
                                    val isSelected = state.warRoom.selectedDoctrine == doc
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSelected) Color(0xFF1E3A8A) else Color(0xFF0F172A))
                                            .border(1.dp, if (isSelected) Color(0xFF60A5FA) else Color(0xFF334155), RoundedCornerShape(6.dp))
                                            .clickable { viewModel.assignMilitaryDoctrine(doc) }
                                            .padding(6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                    ) {
                                        Text(doc.title.take(10), color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                        Text("Atk ${(doc.attackBonus * 100).toInt()}%", color = if (doc.attackBonus >= 0) NssEmerald else NssRed, fontSize = 8.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Authentic APK ArmyStat Casualty Ledger
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
                                    Text("FRIENDLY COMBAT LOSSES", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    Text("Troops: ${formatCompactMil(state.warRoom.casualtyLedger.troopsFallen)}", color = Color.White, fontSize = 11.sp)
                                    Text("Tanks: ${state.warRoom.casualtyLedger.tanksLost}", color = Color.White, fontSize = 11.sp)
                                    Text("Jets: ${state.warRoom.casualtyLedger.jetsLost}", color = Color.White, fontSize = 11.sp)
                                }
                                Column {
                                    Text("ENEMY NEUTRALIZED", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    Text("Troops: ${formatCompactMil(state.warRoom.casualtyLedger.enemyTroopsNeutralized)}", color = NssEmerald, fontSize = 11.sp)
                                    Text("Tanks: ${state.warRoom.casualtyLedger.enemyTanksDestroyed}", color = NssEmerald, fontSize = 11.sp)
                                    Text("Jets: ${state.warRoom.casualtyLedger.enemyJetsDowned}", color = NssEmerald, fontSize = 11.sp)
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
private fun MilitaryServiceOverview(
    serviceName: String,
    headcount: String,
    primaryAsset: String,
    upkeep: String,
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
        Column {
            Text(serviceName.uppercase(), color = Color(0xFFD97706), fontSize = 10.sp, fontWeight = FontWeight.Black)
            Text(headcount, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(primaryAsset, color = Color(0xFF94A3B8), fontSize = 10.sp)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("UPKEEP", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Text("$upkeep/mo", color = Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun MilitaryActionRow(
    title: String,
    subtitle: String,
    cost: Long,
    imageRes: Int,
    canAfford: Boolean,
    buttonText: String,
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
                painter = painterResource(id = imageRes),
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .border(1.dp, Color(0xFF475569), RoundedCornerShape(6.dp)),
            )
            Column {
                Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = Color.LightGray, fontSize = 9.sp)
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
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(buttonText, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(formatCompactMoney(cost), color = Color(0xFFFEF3C7), fontSize = 8.sp)
            }
        }
    }
}
