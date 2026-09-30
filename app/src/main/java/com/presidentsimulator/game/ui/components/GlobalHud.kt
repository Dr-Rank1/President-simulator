package com.presidentsimulator.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.viewmodel.TimeSpeedMode
import com.presidentsimulator.game.ui.theme.NssPrimary
import com.presidentsimulator.game.ui.theme.NssRed
import com.presidentsimulator.game.ui.components.formatCompactMoney
import com.presidentsimulator.game.ui.components.formatCompactMil
import kotlin.math.roundToInt

/** The cyan top HUD bar — matches President Simulator 1 exactly */
@Composable
fun GlobalHud(
    state: GameState,
    timeSpeedMode: TimeSpeedMode,
    timeSpeedEnabled: Boolean,
    alertCount: Int,
    onTimeSpeedModeSelected: (TimeSpeedMode) -> Unit,
    onOpenShop: () -> Unit,
    onOpenMenu: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // === TOP ROW: Cyan bar with icons ===
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(NssPrimary)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Hamburger + Cart
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box {
                    Icon(
                        Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = Color.White,
                        modifier = Modifier
                            .size(26.dp)
                            .clickable(onClick = onOpenMenu)
                    )
                    if (alertCount > 0) {
                        // Crisis/war/shortage count — mirrors the original's alert bubble.
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 4.dp, y = (-2).dp)
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(NssRed),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                "$alertCount",
                                color = Color.White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                            )
                        }
                    }
                }
                Icon(
                    Icons.Default.ShoppingCart,
                    contentDescription = "Shop",
                    tint = Color.White,
                    modifier = Modifier
                        .size(26.dp)
                        .clickable(onClick = onOpenShop)
                )
            }

            // Center: Money | Stars | Population
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Money
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    Icon(
                        Icons.Default.AttachMoney,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    val dailyIncome = (state.netIncome / 30).let { if (it >= 0) "+${formatCompactMoney(it)}" else formatCompactMoney(it) }
                    Text(
                        text = "${formatCompactMoney(state.vitals.budget)} ($dailyIncome)",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                // Divider
                Text("|", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)

                // Approval (star)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${state.vitals.approval.roundToInt()}%",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                // Divider
                Text("|", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)

                // Population
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    Icon(
                        Icons.Default.People,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = formatCompactMil(state.vitals.population),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            // Right: Time controls + date
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Pause button
                val isPaused = timeSpeedMode == TimeSpeedMode.PAUSED
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = if (isPaused) 0.3f else 0.1f))
                        .clickable(enabled = timeSpeedEnabled) {
                            onTimeSpeedModeSelected(
                                if (isPaused) TimeSpeedMode.NORMAL else TimeSpeedMode.PAUSED
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Play normal
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = if (timeSpeedMode == TimeSpeedMode.NORMAL) 0.3f else 0.1f))
                        .clickable(enabled = timeSpeedEnabled) {
                            onTimeSpeedModeSelected(TimeSpeedMode.NORMAL)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Fast forward
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = if (timeSpeedMode == TimeSpeedMode.FAST) 0.3f else 0.1f))
                        .clickable(enabled = timeSpeedEnabled) {
                            onTimeSpeedModeSelected(
                                if (timeSpeedMode == TimeSpeedMode.FAST) TimeSpeedMode.NORMAL else TimeSpeedMode.FAST
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.FastForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // === TICKER ROW: White bar with news + date ===
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = state.press.headlines.firstOrNull()?.title ?: "All is quiet in the nation.",
                color = Color(0xFF333333),
                fontSize = 12.sp,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = state.dateLabel,
                color = Color(0xFF757575),
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

private fun GameState.monthName(): String {
    val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    return months.getOrElse(month - 1) { "Unknown" }
}
