package com.presidentsimulator.game.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
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
import com.presidentsimulator.game.ui.theme.NssAccent
import com.presidentsimulator.game.ui.theme.NssRed
import com.presidentsimulator.game.ui.components.formatCompactMoney
import com.presidentsimulator.game.ui.components.formatCompactMil
import kotlin.math.roundToInt

@Composable
fun GlobalHud(
    state: GameState,
    timeSpeedMode: TimeSpeedMode,
    timeSpeedEnabled: Boolean,
    alertCount: Int,
    onTimeSpeedModeSelected: (TimeSpeedMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hudShape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(hudShape)
            .background(Color(0xEE050A0F)) // Dark sleek background
            .windowInsetsPadding(
                WindowInsets.statusBars.only(WindowInsetsSides.Top)
            )
            .padding(horizontal = 12.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Date and Flag (Modern Age 2 style)
        Column {
            Text(
                text = "${state.monthName()} ${state.year}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = state.playerNation.name,
                color = Color.LightGray,
                fontSize = 11.sp
            )
        }

        // Vitals
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // Money
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AttachMoney, contentDescription = null, tint = NssAccent, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = formatCompactMoney(state.vitals.budget),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
            
            // Approval/Population
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Pop: ${formatCompactMil(state.vitals.population)}",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                )
            }
        }

        // Time Controls
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.background(Color(0xFF1E293B), RoundedCornerShape(4.dp)).padding(2.dp)
        ) {
            val playIconColor = if (!timeSpeedEnabled) Color.DarkGray else if (timeSpeedMode == TimeSpeedMode.NORMAL) NssAccent else Color.LightGray
            val ffIconColor = if (!timeSpeedEnabled) Color.DarkGray else if (timeSpeedMode == TimeSpeedMode.FAST) NssAccent else Color.LightGray

            Box(modifier = Modifier.size(28.dp).clip(RoundedCornerShape(4.dp)).clickable(enabled = timeSpeedEnabled) {
                onTimeSpeedModeSelected(if (timeSpeedMode == TimeSpeedMode.PAUSED) TimeSpeedMode.NORMAL else TimeSpeedMode.PAUSED)
            }, contentAlignment = Alignment.Center) {
                Icon(if (timeSpeedMode == TimeSpeedMode.PAUSED) Icons.Default.PlayArrow else Icons.Default.Pause, contentDescription = null, tint = playIconColor, modifier = Modifier.size(20.dp))
            }
            Box(modifier = Modifier.size(28.dp).clip(RoundedCornerShape(4.dp)).clickable(enabled = timeSpeedEnabled) {
                onTimeSpeedModeSelected(if (timeSpeedMode == TimeSpeedMode.FAST) TimeSpeedMode.NORMAL else TimeSpeedMode.FAST)
            }, contentAlignment = Alignment.Center) {
                Icon(Icons.Default.FastForward, contentDescription = null, tint = ffIconColor, modifier = Modifier.size(20.dp))
            }
        }
    }
}

private fun GameState.monthName(): String {
    val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    return months.getOrElse(month - 1) { "Unknown" }
}
