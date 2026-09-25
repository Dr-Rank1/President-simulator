package com.presidentsimulator.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.presidentsimulator.game.ui.components.HeroHeaderScrim
import com.presidentsimulator.game.ui.components.NssCardImages
import com.presidentsimulator.game.ui.components.NssCardShape
import com.presidentsimulator.game.ui.components.NssPhotoHeader
import com.presidentsimulator.game.ui.theme.Dimens
import com.presidentsimulator.game.ui.theme.NssAccent
import com.presidentsimulator.game.ui.theme.NssForeground
import com.presidentsimulator.game.ui.theme.NssMutedForeground
import com.presidentsimulator.game.ui.theme.NssOnPhoto
import com.presidentsimulator.game.ui.theme.NssPrimary
import com.presidentsimulator.game.ui.components.rememberNssLayoutSpec
import com.presidentsimulator.game.viewmodel.SaveSlotInfo

@Composable
fun LaunchScreen(
    hasSave: Boolean,
    onContinueGame: () -> Unit,
    onNewGame: () -> Unit,
    slots: List<SaveSlotInfo> = emptyList(),
    onLoadSlot: ((Int) -> Unit)? = null,
) {
    val layout = rememberNssLayoutSpec()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        NssPhotoHeader(
            imageUrl = NssCardImages.MAP,
            fallbackGradient = listOf(NssPrimary, Color(0xFF0A0A0A)),
            modifier = Modifier.matchParentSize(),
            scrimTopToBottom = HeroHeaderScrim,
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xCC1C1810),
                            Color.Transparent,
                            Color(0xCC1C1810),
                        ),
                    ),
                ),
        )

        if (layout.isLandscape) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 27.dp, vertical = 13.dp),
                horizontalArrangement = Arrangement.spacedBy(30.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                    Text("GLOBAL COMMAND INTERFACE", fontSize = 8.sp, fontWeight = FontWeight.Black, color = NssAccent, letterSpacing = 8.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("NATION STATE\nSIMULATOR", fontFamily = FontFamily.Serif, fontSize = 27.sp, lineHeight = 29.sp,
                        fontWeight = FontWeight.Black, color = NssOnPhoto, letterSpacing = 8.sp)
                    Text("Lead a nation. Shape its future.", color = NssOnPhoto.copy(alpha = .78f), fontSize = 9.sp, modifier = Modifier.padding(top = 6.dp))
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    LaunchActions(hasSave, onContinueGame, onNewGame, slots, onLoadSlot)
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(Dimens.SpacingXLarge),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("GLOBAL COMMAND INTERFACE", fontSize = 8.sp, fontWeight = FontWeight.Black, color = NssAccent, letterSpacing = 8.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text("NATION STATE", fontFamily = FontFamily.Serif, fontSize = 30.sp, fontWeight = FontWeight.Black,
                    color = NssOnPhoto, letterSpacing = 8.sp, textAlign = TextAlign.Center)
                Text("SIMULATOR", fontFamily = FontFamily.Serif, fontSize = 30.sp, fontWeight = FontWeight.Black,
                    color = NssOnPhoto, letterSpacing = 8.sp, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(36.dp))
                Column(modifier = Modifier.fillMaxWidth(if (layout.isNarrowWidth) 0.92f else 0.72f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    LaunchActions(hasSave, onContinueGame, onNewGame, slots, onLoadSlot)
                }
            }
        }

        Text(
            text = "v1.4.0_BETA // SECURE CONNECTION ESTABLISHED",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 18.dp),
            fontSize = 8.sp,
            color = NssMutedForeground,
            letterSpacing = 8.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun LaunchActions(
    hasSave: Boolean,
    onContinueGame: () -> Unit,
    onNewGame: () -> Unit,
    slots: List<SaveSlotInfo>,
    onLoadSlot: ((Int) -> Unit)?,
) {
    if (hasSave) LaunchActionButton("RESUME CAMPAIGN", Icons.Default.PlayArrow, true, onContinueGame)
    LaunchActionButton("NEW CAMPAIGN", Icons.Default.LocalFireDepartment, !hasSave, onNewGame)
    val occupiedSlots = slots.filter { it.occupied }
    if (occupiedSlots.isNotEmpty() && onLoadSlot != null) {
        occupiedSlots.forEach { slot ->
            LaunchActionButton(slot.label.ifBlank { "LOAD SLOT ${slot.slotIndex}" }.uppercase(), Icons.Default.Settings, false) {
                onLoadSlot(slot.slotIndex)
            }
        }
    }
}

@Composable
private fun LaunchActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    primary: Boolean,
    onClick: () -> Unit,
) {
    val shape = NssCardShape
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                if (primary) {
                    Brush.horizontalGradient(listOf(NssAccent, Color(0xFFD97706)))
                } else {
                    Brush.horizontalGradient(
                        listOf(NssPrimary.copy(alpha = 0.6f), NssPrimary.copy(alpha = 0.45f)),
                    )
                },
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = Dimens.SpacingMedium),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = if (primary) NssOnPhoto else Color(0xFFD4C8A8), modifier = Modifier.padding(end = 6.dp))
        Text(
            text = label,
            color = if (primary) NssOnPhoto else Color(0xFFD4C8A8),
            fontWeight = FontWeight.Black,
            fontSize = 9.sp,
            letterSpacing = 8.sp,
        )
    }
}
