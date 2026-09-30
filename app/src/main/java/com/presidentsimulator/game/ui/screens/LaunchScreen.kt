package com.presidentsimulator.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.presidentsimulator.game.ui.theme.NssPrimary
import com.presidentsimulator.game.ui.theme.NssBackground
import com.presidentsimulator.game.ui.theme.NssForeground
import com.presidentsimulator.game.viewmodel.SaveSlotInfo

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
            .background(NssBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.6f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Title
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NssPrimary)
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "PRESIDENT",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 36.sp,
                        letterSpacing = 4.sp
                    )
                    Text(
                        text = "SIMULATOR",
                        color = Color.White.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        letterSpacing = 8.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (hasSave) {
                    LaunchButton(
                        label = "CONTINUE",
                        icon = Icons.Default.PlayArrow,
                        primary = true,
                        onClick = onContinueGame
                    )
                }

                LaunchButton(
                    label = "NEW GAME",
                    icon = Icons.Default.AddCircle,
                    primary = !hasSave,
                    onClick = onNewGame
                )

                val occupiedSlots = slots.filter { it.occupied }
                if (occupiedSlots.isNotEmpty() && onLoadSlot != null) {
                    occupiedSlots.forEach { slot ->
                        LaunchButton(
                            label = slot.label.ifBlank { "LOAD SLOT ${slot.slotIndex}" }.uppercase(),
                            icon = Icons.Default.FolderOpen,
                            primary = false,
                            onClick = { onLoadSlot(slot.slotIndex) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Lead your nation. Shape history.",
                color = NssForeground.copy(alpha = 0.6f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun LaunchButton(
    label: String,
    icon: ImageVector,
    primary: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (primary) NssPrimary else Color(0xFFE0E0E0))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (primary) Color.White else Color(0xFF757575),
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            color = if (primary) Color.White else Color(0xFF424242),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            letterSpacing = 2.sp
        )
    }
}
