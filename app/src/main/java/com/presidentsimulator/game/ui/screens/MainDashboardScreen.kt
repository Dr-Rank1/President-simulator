package com.presidentsimulator.game.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.presidentsimulator.game.R
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.VictoryThresholds
import com.presidentsimulator.game.ui.components.DominancePanel
import com.presidentsimulator.game.ui.components.NewsTicker
import com.presidentsimulator.game.ui.components.formatCompactMoney
import com.presidentsimulator.game.ui.components.formatCompactMil
import com.presidentsimulator.game.ui.navigation.GameDestination
import com.presidentsimulator.game.ui.theme.NssAccent
import com.presidentsimulator.game.ui.theme.NssEmerald
import com.presidentsimulator.game.ui.theme.NssMutedForeground
import com.presidentsimulator.game.ui.theme.NssPrimary
import com.presidentsimulator.game.ui.theme.NssRed
import kotlin.math.roundToInt

/**
 * "The Oval Office" Resolute Desk Command Center:
 * Real-time Geopolitical Map in the background, News Ticker,
 * Red Crisis Hotline Phone, Executive Orders, War Room & Deterrent Silos,
 * and 12 Industrial Sectors quick-access.
 */
@Composable
fun MainDashboardScreen(
    state: GameState,
    onNavigate: (GameDestination) -> Unit,
    activeCategory: String = "General expenses",
    onCategoryChanged: (String) -> Unit = {},
    onOpenWarRoom: () -> Unit = {},
    onOpenExecutiveOrders: () -> Unit = {},
    onOpenMinistries: () -> Unit = {},
    onOpenCrisisHotline: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Rotating world news feed
        NewsTicker(state = state)

        // Red Crisis Hotline Card (Appears whenever strikes, mutinies, epidemics, or disasters strike)
        if (state.crises.activeCrises.isNotEmpty()) {
            val primaryCrisis = state.crises.activeCrises.first()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xE67F1D1D))
                    .border(1.5.dp, Color(0xFFEF4444), RoundedCornerShape(10.dp))
                    .clickable { onOpenCrisisHotline() }
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDC2626)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                "OVAL OFFICE RED HOTLINE ALERT",
                                color = Color(0xFFFCA5A5),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                            )
                            Text(
                                "(${state.crises.activeCrises.size} ACTIVE)",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Text(
                            text = primaryCrisis.headline,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                Button(
                    onClick = onOpenCrisisHotline,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text("Answer", color = Color(0xFF991B1B), fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
            }
        }

        // Victory path progress & Dominance
        DominancePanel(state = state)

        // Resolute Desk Command Hub
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xDD0F172A))
                .border(1.dp, Color(0xFFD97706), RoundedCornerShape(10.dp))
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            // Executive Orders & Decrees Button
            DeskActionCard(
                modifier = Modifier.weight(1f),
                title = "EXECUTIVE ORDERS",
                subtitle = "${state.ovalOffice.activeDecreesCount} Active Decrees",
                icon = Icons.Default.Gavel,
                accentColor = Color(0xFFD97706),
                onClick = onOpenExecutiveOrders,
            )

            // War Room & Nuclear Silos Button
            DeskActionCard(
                modifier = Modifier.weight(1f),
                title = "WAR ROOM",
                subtitle = "DEFCON ${state.warRoom.nuclearSilo.defconLevel} · Silos ${state.warRoom.nuclearSilo.activeSilos}",
                icon = Icons.Default.Shield,
                accentColor = if (state.warRoom.nuclearSilo.defconLevel <= 2) Color(0xFFDC2626) else Color(0xFF3B82F6),
                onClick = onOpenWarRoom,
            )

            // Sovereign Ministries Button
            DeskActionCard(
                modifier = Modifier.weight(1f),
                title = "MINISTRIES",
                subtitle = "Safety ${state.sovereignMinistries.police.overallCrimeSafetyScore}%",
                icon = Icons.Default.AccountBalance,
                accentColor = Color(0xFF10B981),
                onClick = onOpenMinistries,
            )
        }

        // Active war banner
        state.diplomacy.activeWar?.let { war ->
            val target = state.diplomacy.rivalById(war.targetCountryId)?.name ?: war.targetCountryId
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xE64A0D0D))
                    .clickable { onOpenWarRoom() }
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = NssRed, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        "WAR WITH ${target.uppercase()} — MONTH ${war.monthsActive}",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                    )
                    Text(
                        war.lastBattleSummary.ifBlank { "The front awaits orders." },
                        color = Color(0xFFFFC9C9),
                        fontSize = 9.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.weight(1f))
                Text("WAR ROOM ›", color = NssRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Conquered territories strip
        if (state.territory.conquered.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xE60B131F))
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(Icons.Default.Public, contentDescription = null, tint = NssEmerald, modifier = Modifier.size(13.dp))
                Text(
                    "REALM:",
                    color = NssMutedForeground,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                )
                state.territory.conquered.take(6).forEach { territory ->
                    Text(
                        "${territory.flagEmoji} ${territory.name}",
                        color = when (territory.status) {
                            com.presidentsimulator.game.data.TerritoryStatus.ANNEXED -> NssRed
                            com.presidentsimulator.game.data.TerritoryStatus.PUPPET -> NssAccent
                            com.presidentsimulator.game.data.TerritoryStatus.LIBERATED -> NssEmerald
                        },
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    "${state.territory.controlledCount}/${VictoryThresholds.NATIONS_TO_CONTROL}",
                    color = NssEmerald,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                )
            }
        }

        Spacer(Modifier.weight(1f))

        // Ministry quick tiles (bottom overlay)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DashboardTile(Icons.Default.AccountBalance, "INDUSTRY", "+${formatCompactMoney(state.industrialMatrix.totalMonthlyRevenue / 10L)}/mo", Color(0xFFD97706)) {
                onNavigate(GameDestination.Economy)
            }
            DashboardTile(Icons.Default.Shield, "MILITARY", "${formatCompactMil(state.military.personnel)}", NssRed) {
                onNavigate(GameDestination.Military)
            }
            DashboardTile(Icons.Default.Gavel, "DIPLOMACY", "${state.diplomacy.diplomaticInfluence} inf", NssAccent) {
                onNavigate(GameDestination.Diplomacy)
            }
            DashboardTile(Icons.Default.Insights, "APPROVAL", "${state.vitals.approval.roundToInt()}% app", NssEmerald) {
                onNavigate(GameDestination.Analytics)
            }
            DashboardTile(Icons.Default.EmojiEvents, "VICTORY", "${(state.victoryPath.progressFraction(state.territory.controlledCount) * 100).toInt()}%", NssEmerald) {
                onNavigate(GameDestination.Dashboard)
            }
        }
    }
}

@Composable
private fun DeskActionCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1E293B))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
        Spacer(Modifier.height(4.dp))
        Text(title, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black, maxLines = 1)
        Text(subtitle, color = Color.LightGray, fontSize = 8.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun RowScope.DashboardTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accentColor: Color,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .height(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xE60D1B2A))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 2.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(16.dp),
            )
            Text(
                title,
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
            )
            Text(
                subtitle,
                color = accentColor,
                fontSize = 8.sp,
                maxLines = 1,
            )
        }
    }
}
