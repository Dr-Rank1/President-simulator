package com.presidentsimulator.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.VictoryThresholds
import com.presidentsimulator.game.ui.components.DominancePanel
import com.presidentsimulator.game.ui.components.NewsTicker
import com.presidentsimulator.game.ui.components.formatCompactMoney
import com.presidentsimulator.game.ui.components.formatCompactMil
import com.presidentsimulator.game.ui.navigation.GameDestination
import com.presidentsimulator.game.ui.theme.NssAccent
import com.presidentsimulator.game.ui.theme.NssBackground
import com.presidentsimulator.game.ui.theme.NssEmerald
import com.presidentsimulator.game.ui.theme.NssForeground
import com.presidentsimulator.game.ui.theme.NssMutedForeground
import com.presidentsimulator.game.ui.theme.NssPrimary
import com.presidentsimulator.game.ui.theme.NssRed
import kotlin.math.roundToInt

/**
 * Oxiwyle-style command center: the world map is the hero (rendered behind the
 * NavHost), with a news ticker, dominance tracker, war banner, and ministry
 * tiles floating over it.
 */
@Composable
fun MainDashboardScreen(
    state: GameState,
    onNavigate: (GameDestination) -> Unit,
    activeCategory: String = "General expenses",
    onCategoryChanged: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Rotating world news feed
        NewsTicker(state = state)

        // Victory path progress
        DominancePanel(state = state)

        // Active war banner
        state.diplomacy.activeWar?.let { war ->
            val target = state.diplomacy.rivalById(war.targetCountryId)?.name ?: war.targetCountryId
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xE64A0D0D))
                    .clickable { onNavigate(GameDestination.Military) }
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
                Text("OPEN ›", color = NssRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
            DashboardTile(Icons.Default.AccountBalance, "TREASURY", "${formatCompactMoney(state.vitals.budget)}", NssPrimary) {
                onNavigate(GameDestination.Economy)
            }
            DashboardTile(Icons.Default.Shield, "MILITARY", "${formatCompactMil(state.military.personnel)}", NssRed) {
                onNavigate(GameDestination.Military)
            }
            DashboardTile(Icons.Default.Gavel, "DIPLOMACY", "${state.diplomacy.diplomaticInfluence} inf", NssAccent) {
                onNavigate(GameDestination.Diplomacy)
            }
            DashboardTile(Icons.Default.Insights, "STATS", "${state.vitals.approval.roundToInt()}% app", NssEmerald) {
                onNavigate(GameDestination.Analytics)
            }
            DashboardTile(Icons.Default.EmojiEvents, "VICTORY", "${(state.victoryPath.progressFraction(state.territory.controlledCount) * 100).toInt()}%", NssEmerald) {
                onNavigate(GameDestination.Dashboard)
            }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.DashboardTile(
    icon: ImageVector,
    label: String,
    value: String,
    accent: Color,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xE60B131F))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = label, tint = accent, modifier = Modifier.size(18.dp))
        Spacer(Modifier.height(3.dp))
        Text(value, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black, maxLines = 1)
        Text(label, color = NssMutedForeground, fontSize = 7.sp, fontWeight = FontWeight.Bold)
    }
}
