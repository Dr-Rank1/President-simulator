package com.presidentsimulator.game.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.NewsEntry
import com.presidentsimulator.game.data.VictoryThresholds
import com.presidentsimulator.game.data.effectiveIdeology
import com.presidentsimulator.game.ui.theme.NssAccent
import com.presidentsimulator.game.ui.theme.NssBackground
import com.presidentsimulator.game.ui.theme.NssEmerald
import com.presidentsimulator.game.ui.theme.NssForeground
import com.presidentsimulator.game.ui.theme.NssMutedForeground
import com.presidentsimulator.game.ui.theme.NssPrimary
import com.presidentsimulator.game.ui.theme.NssRed
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

private fun tagColor(tag: String): Color = when (tag) {
    "WAR" -> NssRed
    "ECONOMY" -> Color(0xFFFACC15)
    "DOMINION", "VICTORY" -> NssEmerald
    "MILITARY" -> Color(0xFF818CF8)
    "POLITICS" -> NssAccent
    else -> NssAccent
}

/** Rotating world-news banner. */
@Composable
fun NewsTicker(
    state: GameState,
    modifier: Modifier = Modifier,
) {
    val entries = state.news.entries.takeIf { it.isNotEmpty() } ?: return
    var index by remember(entries) { mutableIntStateOf(0) }

    LaunchedEffect(entries) {
        while (true) {
            delay(4000)
            index = (index + 1) % entries.size
        }
    }

    val entry = entries[index % entries.size]
    AnimatedContent(
        targetState = entry,
        transitionSpec = {
            (slideInVertically { it / 2 } + fadeIn()) togetherWith (slideOutVertically { -it / 2 } + fadeOut())
        },
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xE60B131F)),
        label = "newsTicker",
    ) { item ->
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(tagColor(item.tag)),
            )
            Text(
                "${item.tag} · ${GameState.monthName(item.month)} ${item.year}",
                color = tagColor(item.tag),
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
            )
            Text(
                item.headline,
                color = Color.White,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** Victory-path progress card shown on the dashboard. */
@Composable
fun DominancePanel(
    state: GameState,
    onOpenSettings: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val pathState = state.victoryPath
    val path = pathState.chosenPath ?: return
    val progress = pathState.progressFraction(state.territory.controlledCount)
    val animated by animateFloatAsState(progress, tween(600), label = "dominionProgress")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xE60B131F))
            .padding(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = NssEmerald,
                modifier = Modifier.size(14.dp),
            )
            Spacer(Modifier.size(6.dp))
            Text(
                "PATH TO ${path.displayName.uppercase()}",
                color = NssForeground,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
            )
            Spacer(Modifier.weight(1f))
            Text(
                "${(animated * 100).roundToInt()}%",
                color = NssEmerald,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
            )
        }
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF1E293B)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animated)
                    .height(7.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(NssEmerald),
            )
        }
        Spacer(Modifier.height(5.dp))
        val detail = when (path) {
            com.presidentsimulator.game.data.VictoryPath.MILITARY_DOMINANCE ->
                "${state.territory.controlledCount} of ${VictoryThresholds.NATIONS_TO_CONTROL} nations under control" +
                    " · ${state.territory.annexedCount} annexed · ${state.territory.puppetCount} puppets"
            com.presidentsimulator.game.data.VictoryPath.RELIGIOUS_DOMINANCE ->
                "${pathState.religiousInfluence.roundToInt()}% of the world follows ${state.society.stateReligion.displayName}"
            com.presidentsimulator.game.data.VictoryPath.IDEOLOGICAL_DOMINANCE ->
                "${pathState.ideologicalInfluence.roundToInt()}% global influence for ${state.effectiveIdeology().displayName}"
        }
        Text(detail, color = NssMutedForeground, fontSize = 9.sp)
    }
}
