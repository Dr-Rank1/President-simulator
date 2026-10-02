package com.presidentsimulator.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.presidentsimulator.game.R
import com.presidentsimulator.game.data.DailyChallengeInfo
import com.presidentsimulator.game.data.DailyScore
import com.presidentsimulator.game.data.PlayableNationCatalog
import com.presidentsimulator.game.ui.theme.NssPrimary
import com.presidentsimulator.game.ui.theme.NssBackground
import com.presidentsimulator.game.ui.theme.NssForeground
import com.presidentsimulator.game.viewmodel.SaveSlotInfo
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun LaunchScreen(
    hasSave: Boolean,
    onContinueGame: () -> Unit,
    onNewGame: () -> Unit,
    dailyInfo: DailyChallengeInfo? = null,
    dailyBoard: List<DailyScore> = emptyList(),
    onDailyChallenge: (() -> Unit)? = null,
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
                        text = stringResource(R.string.launch_title_top),
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 36.sp,
                        letterSpacing = 4.sp
                    )
                    Text(
                        text = stringResource(R.string.launch_title_bottom),
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
                        label = stringResource(R.string.launch_continue),
                        icon = Icons.Default.PlayArrow,
                        primary = true,
                        onClick = onContinueGame
                    )
                }

                LaunchButton(
                    label = stringResource(R.string.launch_new_game),
                    icon = Icons.Default.AddCircle,
                    primary = !hasSave,
                    onClick = onNewGame
                )

                if (dailyInfo != null && onDailyChallenge != null) {
                    LaunchButton(
                        label = stringResource(R.string.launch_daily_challenge),
                        icon = Icons.Default.CalendarToday,
                        primary = false,
                        onClick = onDailyChallenge,
                    )
                    DailyChallengeSummary(info = dailyInfo, board = dailyBoard)
                }

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
                text = stringResource(R.string.launch_tagline),
                color = NssForeground.copy(alpha = 0.6f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun DailyChallengeSummary(info: DailyChallengeInfo, board: List<DailyScore>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        val nationLabel = "${info.flagEmoji} ${info.nationName}"
        Text(
            text = if (info.bestScore > 0) {
                stringResource(R.string.launch_daily_nation_today_best, nationLabel, info.bestScore)
            } else {
                stringResource(R.string.launch_daily_nation_today, nationLabel)
            },
            color = NssForeground.copy(alpha = 0.7f),
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
        )
        val topScores = board.sortedByDescending { it.score }.take(3)
        if (topScores.isNotEmpty()) {
            val formatter = remember { DateTimeFormatter.ofPattern("MMM d") }
            topScores.forEach { entry ->
                val date = LocalDate.ofEpochDay(entry.epochDay).format(formatter)
                val nation = PlayableNationCatalog.byId(entry.nationId)?.name ?: entry.nationId
                Text(
                    text = "★ ${entry.score} · $nation · $date${if (entry.victory) " · victory" else ""}",
                    color = NssForeground.copy(alpha = 0.5f),
                    fontSize = 9.sp,
                    textAlign = TextAlign.Center,
                )
            }
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
