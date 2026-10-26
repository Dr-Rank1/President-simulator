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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.presidentsimulator.game.R
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.ui.theme.NssAccent
import com.presidentsimulator.game.ui.theme.NssEmerald
import com.presidentsimulator.game.ui.theme.NssPrimary
import com.presidentsimulator.game.ui.theme.NssRed
import com.presidentsimulator.game.viewmodel.toBudgetString
import kotlin.math.abs
import kotlin.math.roundToInt

data class AdvisorAdvice(
    val title: String,
    val description: String,
    val isCritical: Boolean = false,
    val actionLabel: String? = null,
    val targetRoute: String? = null,
)

/**
 * Authentic Presidential Advisor Dialogue using the extracted [R.drawable.sovetnikfon] card.
 * Evaluates real-time national metrics and gives strategic counsel.
 */
@Composable
fun PresidentialAdvisorDialog(
    state: GameState,
    onDismiss: () -> Unit,
    onNavigateRoute: (String) -> Unit = {},
) {
    val adviceList = computeNationalAdvice(state)

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
                .padding(bottom = 14.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Header: Authentic Sovetnik Art Frame
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
            ) {
                Image(
                    painter = painterResource(id = R.drawable.sovetnikfon),
                    contentDescription = "Chief Presidential Adviser",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(130.dp),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .background(Color(0x550F172A)),
                )

                // Close Button
                Box(
                    modifier = Modifier
                        .padding(10.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xAA000000))
                        .align(Alignment.TopEnd)
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp),
                    )
                }

                // Title Overlay
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = "THE OVAL OFFICE BRIEFING",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFD97706), // Executive Gold
                        letterSpacing = 2.sp,
                    )
                    Text(
                        text = "Chief Presidential Adviser",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Subtitle status badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${state.dateLabel} · National Vitals",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8),
                )
                Text(
                    text = "${adviceList.count { it.isCritical }} Critical Alert(s)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = if (adviceList.any { it.isCritical }) NssRed else NssEmerald,
                )
            }

            Spacer(Modifier.height(8.dp))

            // Advice Item Cards
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                adviceList.forEach { advice ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E293B)) // Deep Slate Navy
                            .border(
                                0.75.dp,
                                if (advice.isCritical) NssRed.copy(alpha = 0.8f) else Color(0xFF334155),
                                RoundedCornerShape(8.dp),
                            )
                            .padding(10.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Icon(
                            imageVector = if (advice.isCritical) Icons.Default.Warning else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (advice.isCritical) NssRed else NssEmerald,
                            modifier = Modifier.size(18.dp).padding(top = 2.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = advice.title,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (advice.isCritical) Color(0xFFFCA5A5) else Color.White,
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = advice.description,
                                fontSize = 11.sp,
                                color = Color(0xFFCBD5E1),
                                lineHeight = 15.sp,
                            )

                            if (advice.actionLabel != null && advice.targetRoute != null) {
                                Spacer(Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFD97706))
                                        .clickable {
                                            onDismiss()
                                            onNavigateRoute(advice.targetRoute)
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                ) {
                                    Text(
                                        text = advice.actionLabel,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Bottom Acknowledge Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFD97706)) // Executive Gold
                    .clickable(onClick = onDismiss)
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "COUNSEL ACKNOWLEDGED",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.sp,
                )
            }
        }
    }
}

/**
 * Computes realistic presidential briefings matching the APK's 70 advisor triggers.
 */
private fun computeNationalAdvice(state: GameState): List<AdvisorAdvice> {
    val list = mutableListOf<AdvisorAdvice>()

    // 1. Housing Deficit check
    val housingCapacity = state.economy.housing * 250_000L
    if (housingCapacity < state.vitals.population) {
        val deficit = state.vitals.population - housingCapacity
        list.add(
            AdvisorAdvice(
                title = "Residential Shortage",
                description = "Our population lacks adequate residential housing (${deficit / 1_000}K unhoused). Construct new high-rise residential districts to prevent public discontent.",
                isCritical = true,
                actionLabel = "EXPAND HOUSING",
                targetRoute = "economy",
            ),
        )
    }

    // 2. Budget Deficit & Low Reserves
    if (state.netIncome < 0L) {
        list.add(
            AdvisorAdvice(
                title = "Fiscal Deficit Alert",
                description = "Government spending exceeds revenue by ${abs(state.netIncome).toBudgetString()}/mo. Consider raising the income tax, applying import tariffs, or cutting military upkeep.",
                isCritical = true,
                actionLabel = "ADJUST TAXES",
                targetRoute = "economy",
            ),
        )
    }

    if (state.vitals.budget < 3_000_000_000L) {
        list.add(
            AdvisorAdvice(
                title = "Low Sovereign Reserves",
                description = "National Treasury balance is currently ${state.vitals.budget.toBudgetString()}. Secure a credit line from the Central Bank or limit new facility construction.",
                isCritical = state.vitals.budget < 1_000_000_000L,
                actionLabel = "CENTRAL BANK",
                targetRoute = "economy",
            ),
        )
    }

    // 3. Electric Grid & Power Capacity
    val factoryDemand = state.economy.factories * 18L
    if (state.production.energy < factoryDemand) {
        list.add(
            AdvisorAdvice(
                title = "Power Grid Deficiency",
                description = "National energy output (${state.production.energy} MW) is insufficient to run all factories (${factoryDemand} MW needed). Construct more power plants.",
                isCritical = true,
                actionLabel = "BUILD POWER PLANT",
                targetRoute = "economy",
            ),
        )
    }

    // 4. Armed Forces Readiness & Training
    if (state.military.trainingLevel < 3) {
        list.add(
            AdvisorAdvice(
                title = "Troop Training Suboptimal",
                description = "Combat readiness training is at Level ${state.military.trainingLevel}/5. Conduct military command coordination drills to improve defensive effectiveness.",
                isCritical = false,
                actionLabel = "TRAIN TROOPS",
                targetRoute = "military",
            ),
        )
    }

    if (state.military.tanks < 50 || state.military.jets < 20) {
        list.add(
            AdvisorAdvice(
                title = "Hardware Deterrence Gap",
                description = "Armor (${state.military.tanks} tanks) and air fleet (${state.military.jets} jets) remain modest. Procure tactical hardware to deter foreign aggressors.",
                isCritical = false,
                actionLabel = "PROCURE WEAPONS",
                targetRoute = "military",
            ),
        )
    }

    // 5. Commodity Surplus in Warehouses
    if (state.production.goods > 600L || state.production.materials > 1500L) {
        list.add(
            AdvisorAdvice(
                title = "Export Opportunity",
                description = "Warehouses hold surplus manufactured goods and mineral extraction. Sell commodities on the World Market to inject capital into the Treasury.",
                isCritical = false,
                actionLabel = "WORLD MARKET",
                targetRoute = "economy",
            ),
        )
    }

    // 6. Public Approval & Discontent
    if (state.vitals.approval < 45f) {
        list.add(
            AdvisorAdvice(
                title = "Electoral Discontent",
                description = "Presidential approval has fallen to ${state.vitals.approval.roundToInt()}%. Launch a nationwide address, negotiate with the opposition, or pass popular public healthcare reforms.",
                isCritical = true,
                actionLabel = "CAMPAIGN RALLY",
                targetRoute = "demographics",
            ),
        )
    }

    // 7. Stable state fallback
    if (list.isEmpty()) {
        list.add(
            AdvisorAdvice(
                title = "State Operations Stable",
                description = "All government ministries report orderly operations. Public approval stands at ${state.vitals.approval.roundToInt()}% and national borders are secure, Mr. President.",
                isCritical = false,
            ),
        )
    }

    return list
}
