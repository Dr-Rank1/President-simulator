package com.presidentsimulator.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.viewmodel.TimeSpeedMode
import com.presidentsimulator.game.ui.theme.*
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
    val approval = state.vitals.approval
    val approvalColor = when {
        approval >= 65f -> NssEmerald
        approval >= 45f -> NssAmber
        else -> NssRed
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF030A12), Color(0xFF060F1A)))
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(listOf(Color.Transparent, NssAccent.copy(alpha = 0.3f), Color.Transparent)),
                shape = RoundedCornerShape(0.dp)
            )
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // LEFT: Flag + Country + Date
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(state.playerNation.flagEmoji, fontSize = 18.sp)
            Column {
                Text(
                    state.playerNation.name.uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    "${state.monthName()} ${state.year}",
                    color = NssAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }

        // CENTER: Key resource stats — MA2 style compact strips
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            HudStat(icon = Icons.Default.AttachMoney,   label = "BUDGET",   value = formatCompactMoney(state.vitals.budget), color = if (state.netIncome >= 0) NssEmerald else NssRed)
            HudDivider()
            HudStat(icon = Icons.Default.TrendingUp,    label = "INCOME",   value = (if (state.netIncome >= 0) "+" else "") + formatCompactMoney(state.netIncome), color = if (state.netIncome >= 0) NssEmerald else NssRed)
            HudDivider()
            HudStat(icon = Icons.Default.People,        label = "POP",      value = formatCompactMil(state.vitals.population), color = Color.White)
            HudDivider()
            HudStat(icon = Icons.Default.FavoriteBorder, label = "APPROVAL", value = "${approval.roundToInt()}%", color = approvalColor)
            HudDivider()
            HudStat(icon = Icons.Default.Shield,        label = "DEFCON",   value = "${state.military.defcon}", color = when (state.military.defcon) { 1, 2 -> NssRed; 3 -> NssAmber; else -> NssEmerald })
        }

        // RIGHT: Time controls — MA2 has pause / play / fast-forward
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (alertCount > 0) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(NssRed.copy(alpha = 0.2f))
                        .border(1.dp, NssRed.copy(alpha = 0.5f), RoundedCornerShape(3.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = NssRed, modifier = Modifier.size(10.dp))
                        Text("$alertCount", color = NssRed, fontSize = 9.sp, fontWeight = FontWeight.Black)
                    }
                }
                Spacer(Modifier.width(4.dp))
            }
            TimeBtn(Icons.Default.Pause,       active = timeSpeedMode == TimeSpeedMode.PAUSED, enabled = timeSpeedEnabled) {
                onTimeSpeedModeSelected(if (timeSpeedMode == TimeSpeedMode.PAUSED) TimeSpeedMode.NORMAL else TimeSpeedMode.PAUSED)
            }
            TimeBtn(Icons.Default.PlayArrow,   active = timeSpeedMode == TimeSpeedMode.NORMAL,  enabled = timeSpeedEnabled) {
                onTimeSpeedModeSelected(TimeSpeedMode.NORMAL)
            }
            TimeBtn(Icons.Default.FastForward, active = timeSpeedMode == TimeSpeedMode.FAST,    enabled = timeSpeedEnabled) {
                onTimeSpeedModeSelected(TimeSpeedMode.FAST)
            }
        }
    }
}

@Composable
private fun HudStat(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            Icon(icon, contentDescription = null, tint = NssMutedForeground, modifier = Modifier.size(9.dp))
            Text(label, color = NssMutedForeground, fontSize = 7.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
        }
        Text(value, color = color, fontWeight = FontWeight.Black, fontSize = 11.sp, letterSpacing = 0.3.sp)
    }
}

@Composable
private fun HudDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(28.dp)
            .background(NssBorder)
    )
}

@Composable
private fun TimeBtn(
    icon: ImageVector,
    active: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(if (active) NssAccent.copy(alpha = 0.18f) else Color.Transparent)
            .border(1.dp, if (active) NssAccent.copy(alpha = 0.6f) else NssBorder, RoundedCornerShape(3.dp))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null,
            tint = if (!enabled) Color.DarkGray else if (active) NssAccent else Color.Gray,
            modifier = Modifier.size(16.dp))
    }
}

private fun GameState.monthName(): String {
    val months = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
    return months.getOrElse(month - 1) { "?" }
}
