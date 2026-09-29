package com.presidentsimulator.game.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.presidentsimulator.game.ui.theme.*
import com.presidentsimulator.game.viewmodel.SaveSlotInfo
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LaunchScreen(
    hasSave: Boolean,
    onContinueGame: () -> Unit,
    onNewGame: () -> Unit,
    slots: List<SaveSlotInfo> = emptyList(),
    onLoadSlot: ((Int) -> Unit)? = null,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF040B11)),
    ) {
        // Animated hex-grid background — MA2 signature look
        HexBackgroundLaunch()

        // Dark gradient vignette over hex grid
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color.Transparent, Color(0xCC040B11)),
                        radius = 1200f
                    )
                )
        )

        // Horizontal center divider glow
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, NssAccent.copy(alpha = 0.3f), NssAccent.copy(alpha = 0.6f), NssAccent.copy(alpha = 0.3f), Color.Transparent)
                    )
                )
        )

        // Main layout: split landscape
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 40.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(60.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // LEFT: Game title / branding
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Version badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(NssAccent.copy(alpha = 0.15f))
                        .border(1.dp, NssAccent.copy(alpha = 0.4f), RoundedCornerShape(3.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "GLOBAL STRATEGY SIMULATION · v1.4.0",
                        color = NssAccent,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }

                Spacer(Modifier.height(6.dp))

                Text(
                    text = "NATION",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 54.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 6.sp,
                    lineHeight = 56.sp
                )
                Text(
                    text = "STATE",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 54.sp,
                    fontWeight = FontWeight.Black,
                    color = NssAccent,
                    letterSpacing = 6.sp,
                    lineHeight = 56.sp
                )
                Text(
                    text = "SIMULATOR",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = NssMutedForeground,
                    letterSpacing = 10.sp,
                )

                Spacer(Modifier.height(12.dp))

                // Decorative line
                Box(Modifier.width(120.dp).height(2.dp).background(
                    Brush.horizontalGradient(listOf(NssAccent, Color.Transparent))
                ))

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Lead a nation. Shape its destiny.\nManage economy, military, diplomacy and governance.",
                    color = NssMutedForeground,
                    fontSize = 10.sp,
                    lineHeight = 15.sp
                )
            }

            // RIGHT: Action buttons — MA2 menu style
            Column(
                modifier = Modifier.width(260.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "MAIN MENU",
                    color = NssMutedForeground,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Box(Modifier.fillMaxWidth().height(1.dp).background(NssBorder))
                Spacer(Modifier.height(4.dp))

                if (hasSave) {
                    LaunchMenuButton(
                        label = "RESUME CAMPAIGN",
                        icon = Icons.Default.PlayArrow,
                        primary = true,
                        onClick = onContinueGame
                    )
                }
                LaunchMenuButton(
                    label = "NEW CAMPAIGN",
                    icon = Icons.Default.LocalFireDepartment,
                    primary = !hasSave,
                    onClick = onNewGame
                )

                val occupiedSlots = slots.filter { it.occupied }
                if (occupiedSlots.isNotEmpty() && onLoadSlot != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "SAVED CAMPAIGNS",
                        color = NssMutedForeground,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    occupiedSlots.forEach { slot ->
                        LaunchMenuButton(
                            label = slot.label.ifBlank { "SLOT ${slot.slotIndex}" }.uppercase(),
                            icon = Icons.Default.Settings,
                            primary = false,
                            onClick = { onLoadSlot(slot.slotIndex) }
                        )
                    }
                }
            }
        }

        // Bottom watermark
        Text(
            text = "NATION STATE SIMULATOR · SECURE SESSION",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp),
            fontSize = 8.sp,
            color = NssMutedForeground.copy(alpha = 0.5f),
            letterSpacing = 4.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun LaunchMenuButton(
    label: String,
    icon: ImageVector,
    primary: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(
                if (primary)
                    Brush.horizontalGradient(listOf(NssAccent, Color(0xFF0096D6)))
                else
                    Brush.horizontalGradient(listOf(NssPrimary, NssSecondary))
            )
            .border(
                width = 1.dp,
                color = if (primary) NssAccent.copy(alpha = 0.7f) else NssBorder,
                shape = RoundedCornerShape(4.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 13.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (primary) Color(0xFF000000) else NssAccent,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = label,
            color = if (primary) Color(0xFF000000) else NssForeground,
            fontWeight = FontWeight.Black,
            fontSize = 10.sp,
            letterSpacing = 2.sp,
        )
    }
}

@Composable
private fun HexBackgroundLaunch() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val hexRadius = 42f
        val hexWidth = (kotlin.math.sqrt(3.0) * hexRadius).toFloat()
        val vertDist = hexRadius * 1.5f
        val cols = (size.width / hexWidth).toInt() + 2
        val rows = (size.height / vertDist).toInt() + 2
        val path = Path()
        // Gradient: brighter near center
        val cx = size.width / 2
        val cy = size.height / 2
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val x = c * hexWidth + if (r % 2 == 1) hexWidth / 2f else 0f
                val y = r * vertDist
                val dist = kotlin.math.sqrt((x - cx) * (x - cx) + (y - cy) * (y - cy).toDouble()).toFloat()
                val maxDist = kotlin.math.sqrt((cx * cx + cy * cy).toDouble()).toFloat()
                val alpha = (1f - dist / maxDist).coerceIn(0f, 1f) * 0.25f
                path.reset()
                for (i in 0..5) {
                    val angle = Math.PI / 3 * i - Math.PI / 2
                    val px = x + hexRadius * cos(angle).toFloat()
                    val py = y + hexRadius * sin(angle).toFloat()
                    if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                }
                path.close()
                drawPath(path, color = Color(0xFF29B6F6).copy(alpha = alpha * 0.4f), style = Stroke(width = 1f))
            }
        }
    }
}
