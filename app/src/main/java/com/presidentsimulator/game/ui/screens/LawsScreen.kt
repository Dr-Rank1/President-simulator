package com.presidentsimulator.game.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.HourglassTop
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.presidentsimulator.game.R
import com.presidentsimulator.game.audio.GameAudioManager
import com.presidentsimulator.game.audio.playBuildSuccess
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.LawCatalog
import com.presidentsimulator.game.data.LawCategory
import com.presidentsimulator.game.ui.components.formatCompactMoney
import com.presidentsimulator.game.ui.theme.NssAccent
import com.presidentsimulator.game.ui.theme.NssEmerald
import com.presidentsimulator.game.ui.theme.NssRed
import com.presidentsimulator.game.viewmodel.GameViewModel
import com.presidentsimulator.game.viewmodel.ProductionLawEngine
import com.presidentsimulator.game.viewmodel.toBudgetString
import kotlin.math.roundToInt

@Composable
fun LawsScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var selectedCategoryIndex by remember { mutableIntStateOf(0) } // 0: Economic, 1: Social, 2: Military
    val categories = listOf(LawCategory.ECONOMIC, LawCategory.SOCIAL, LawCategory.MILITARY)
    val activeCategory = categories[selectedCategoryIndex]

    val context = LocalContext.current
    val audio = remember(context) { GameAudioManager.getInstance(context) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        // Parliament Hemicycle Chamber Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(115.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.sud),
                contentDescription = "Parliament Hemicycle Chamber",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xCC080E1A)),
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
                        Icon(Icons.Default.Gavel, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(20.dp))
                        Text(
                            text = "NATIONAL PARLIAMENT & SENATE",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                        )
                    }
                    Text(
                        text = "CONSTITUTIONAL CODEX • 400 SEATS TOTAL",
                        color = Color(0xFFD97706),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }

                // Hemicycle Seat Gauge
                Column(horizontalAlignment = Alignment.End) {
                    Text("COALITION MAJORITY", color = Color.Gray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    Text("235 / 400 Seats", color = Color(0xFF34D399), fontSize = 13.sp, fontWeight = FontWeight.Black)
                    Text("Threshold: 201 Ayes", color = Color(0xFFCBD5E1), fontSize = 9.sp)
                }
            }
        }

        // Parliamentary Seating Hemicycle Visual Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E293B))
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("SEATS:", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Black)
            // Coalition
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFD97706)))
                Text("Coalition 235", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
            // Opposition
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFDC2626)))
                Text("Opposition 135", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
            // Independents
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF64748B)))
                Text("Independents 30", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Category Tab Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF151E2E)),
        ) {
            categories.forEachIndexed { idx, cat ->
                val isSelected = selectedCategoryIndex == idx
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedCategoryIndex = idx }
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
                        text = cat.displayName.uppercase(),
                        color = if (isSelected) Color(0xFFFBBF24) else Color.LightGray,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    )
                }
            }
        }

        // Pending Bills in Floor Debate
        PendingBillsSection(state = state, viewModel = viewModel)

        // Laws Grid
        val laws = LawCatalog.byCategory(activeCategory)
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(laws) { law ->
                val isActive = state.legal.isActive(law.id)
                val pending = state.legal.pendingLaws.firstOrNull { it.lawId == law.id }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isActive) Color(0xFF064E3B) else Color(0xFF1E293B))
                        .border(
                            1.dp,
                            if (isActive) Color(0xFF34D399) else if (pending != null) Color(0xFF3B82F6) else Color(0xFF334155),
                            RoundedCornerShape(8.dp),
                        )
                        .padding(10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = law.name,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            modifier = Modifier.weight(1f),
                        )
                        if (isActive) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(16.dp))
                        } else if (pending != null) {
                            Icon(Icons.Default.HourglassTop, contentDescription = null, tint = Color(0xFF60A5FA), modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = law.description,
                        color = Color(0xFFCBD5E1),
                        fontSize = 9.sp,
                        lineHeight = 12.sp,
                        maxLines = 2,
                    )

                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "Upkeep: ${formatCompactMoney(law.upkeepCost)}/mo",
                            color = if (law.upkeepCost > 0) Color(0xFFFCA5A5) else Color.LightGray,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Cost: ${formatCompactMoney(law.activationCost)}",
                            color = Color(0xFFFBBF24),
                            fontSize = 8.sp,
                        )
                    }

                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            when {
                                isActive -> viewModel.repealLaw(law.id)
                                pending != null -> viewModel.rushPendingLaw(law.id)
                                else -> {
                                    viewModel.enactLaw(law.id)
                                    audio.playBuildSuccess()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when {
                                isActive -> Color(0xFF991B1B)
                                pending != null -> Color(0xFF1D4ED8)
                                else -> Color(0xFFD97706)
                            },
                        ),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(vertical = 4.dp),
                    ) {
                        Text(
                            text = when {
                                isActive -> "Repeal Law"
                                pending != null -> "Rush Floor Vote"
                                else -> "Introduce Bill"
                            },
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PendingBillsSection(state: GameState, viewModel: GameViewModel) {
    val pending = state.legal.pendingLaws
    if (pending.isEmpty()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1E293B))
            .border(1.dp, Color(0xFF3B82F6), RoundedCornerShape(8.dp))
            .padding(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(Icons.Default.HourglassTop, contentDescription = null, tint = Color(0xFF60A5FA), modifier = Modifier.size(14.dp))
            Text("BILLS UNDER PARLIAMENTARY DEBATE", color = Color(0xFF93C5FD), fontSize = 10.sp, fontWeight = FontWeight.Black)
        }

        Spacer(Modifier.height(6.dp))

        pending.forEach { bill ->
            val law = LawCatalog.byId(bill.lawId) ?: return@forEach
            val support = viewModel.parliamentSupportFor(bill.lawId).roundToInt()
            val verb = if (bill.enabling) "Enactment" else "Repeal"

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("${law.name} ($verb)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("Support: $support% • Debate: ${bill.ticksRemaining} mo remaining", color = Color(0xFFCBD5E1), fontSize = 9.sp)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = { viewModel.rushPendingLaw(bill.lawId) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    ) {
                        Text("Rush (${formatCompactMoney(ProductionLawEngine.RUSH_LAW_COST)})", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
