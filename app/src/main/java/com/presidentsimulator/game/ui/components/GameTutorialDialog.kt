package com.presidentsimulator.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
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
import com.presidentsimulator.game.ui.theme.NssAccent
import com.presidentsimulator.game.ui.theme.NssBackground
import com.presidentsimulator.game.ui.theme.NssBorder
import com.presidentsimulator.game.ui.theme.NssForeground
import com.presidentsimulator.game.ui.theme.NssMutedForeground
import com.presidentsimulator.game.ui.theme.NssOnPhoto
import com.presidentsimulator.game.ui.theme.NssPrimary

import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider

private data class TutorialPage(
    val title: String,
    val body: String,
    val tip: String,
    val icon: ImageVector,
)

private val tutorialPages = listOf(
    TutorialPage(
        "Welcome, President",
        "Your first term begins now. This interface represents your presidential desk. At the top, you can track your Approval, Budget, and Global Influence.",
        "Your first goal is to explore the dashboard. Try tapping the tabs below.",
        Icons.Default.AccountBalance,
    ),
    TutorialPage(
        "Economic Strategy",
        "Open the 'ECONOMY' tab to view your national budget. You can set tax rates, enact economic policies, and invest in infrastructure.",
        "Avoid high taxes early on! They will cripple your approval rating.",
        Icons.Default.AccountBalance,
    ),
    TutorialPage(
        "Defense & Security",
        "Open the 'MILITARY' tab. You need a strong defense to deter invasions. Construct units and invest in the military-industrial complex.",
        "Watch your deficit! A massive army costs a lot of maintenance.",
        Icons.Default.Groups,
    ),
    TutorialPage(
        "Foreign Policy",
        "The 'FOREIGN' tab lets you manage global relations. Propose trade deals, send aid, or form alliances.",
        "Before declaring war, make sure you have enough military power and political capital.",
        Icons.Default.Groups,
    ),
    TutorialPage(
        "Laws & Society",
        "Your parliament shapes the ideology of the nation. Pass laws to tackle crises or shift your government towards Democracy or Autocracy.",
        "Laws require 'Political Capital' and parliamentary support to pass.",
        Icons.Default.AccountBalance,
    ),
    TutorialPage(
        "Advancing Time",
        "This game is turn-based. When you are ready, tap 'ADVANCE' on the top right to process the month.",
        "You can leave this tutorial open while you explore. Good luck, Mr. President!",
        Icons.Default.PlayArrow,
    )
)

@Composable
fun GameTutorialDialog(
    page: Int,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onSkip: () -> Unit,
) {
    val current = tutorialPages[page.coerceIn(tutorialPages.indices)]

    // Floating overlay, NOT a Dialog, allowing background clicks
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 80.dp, end = 16.dp, start = 16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 350.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xF5050A0F)) // Opaque enough to read, but not modal
                .border(1.dp, NssBorder, RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("TUTORIAL BRIEFING", color = NssAccent, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.8.sp)
                Spacer(Modifier.weight(1f))
                Text("${page + 1} / ${tutorialPages.size}", color = NssMutedForeground, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
            Row(
                modifier = Modifier.padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(current.icon, contentDescription = null, tint = NssOnPhoto, modifier = Modifier.size(16.dp))
                Text(current.title, color = NssForeground, fontSize = 14.sp, fontWeight = FontWeight.Black)
            }
            Text(current.body, color = NssMutedForeground, fontSize = 11.sp, lineHeight = 16.sp, modifier = Modifier.padding(top = 6.dp))
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(NssBackground)
                    .padding(8.dp),
            ) {
                Text("ADVISOR TIP", color = NssAccent, fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                Text(current.tip, color = NssForeground, fontSize = 10.sp, lineHeight = 14.sp, modifier = Modifier.padding(top = 3.dp))
            }
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    "Dismiss",
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable(onClick = onSkip).padding(horizontal = 6.dp, vertical = 6.dp),
                    color = NssMutedForeground,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                if (page > 0) {
                    Text(
                        "Back",
                        modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable(onClick = onBack).padding(horizontal = 8.dp, vertical = 6.dp),
                        color = NssPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Text(
                    if (page == tutorialPages.lastIndex) "Finish Tour" else "Next",
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(NssPrimary).clickable(onClick = onNext)
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    color = NssOnPhoto,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(tutorialPages.size) { index ->
                    Spacer(
                        Modifier.padding(horizontal = 2.dp).size(if (index == page) 6.dp else 4.dp)
                            .clip(CircleShape).background(if (index == page) NssAccent else NssMutedForeground.copy(alpha = 0.28f)),
                    )
                }
            }
        }
    }
}
