package com.presidentsimulator.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import android.view.Gravity
import com.presidentsimulator.game.ui.theme.NssAccent
import com.presidentsimulator.game.ui.theme.NssBackground
import com.presidentsimulator.game.ui.theme.NssBorder
import com.presidentsimulator.game.ui.theme.NssForeground
import com.presidentsimulator.game.ui.theme.NssGameCard
import com.presidentsimulator.game.ui.theme.NssMutedForeground
import com.presidentsimulator.game.ui.theme.NssOnPhoto
import com.presidentsimulator.game.ui.theme.NssPrimary

private data class TutorialPage(
    val title: String,
    val body: String,
    val tip: String,
    val icon: ImageVector,
)

private val tutorialPages = listOf(
    TutorialPage(
        "Welcome, President",
        "This is your command dashboard. From here you monitor your nation's vitals: Approval, Budget, and Stability.",
        "Your first goal is to balance the budget. Check the Economy tab next.",
        Icons.Default.AccountBalance,
    ),
    TutorialPage(
        "Managing the Economy",
        "Use the Economy tab to adjust taxes and allocate funds. A high GDP growth means more revenue.",
        "Warning: High taxes will crash your approval rating. Find the sweet spot.",
        Icons.Default.AccountBalance,
    ),
    TutorialPage(
        "National Defense",
        "The Military tab is where you recruit soldiers and invest in defense technology.",
        "A weak military invites foreign invasions. Keep it strong, but don't bankrupt the country.",
        Icons.Default.Groups,
    ),
    TutorialPage(
        "Global Diplomacy",
        "The Foreign Affairs tab lets you form alliances, sign trade deals, or declare wars.",
        "Build relationships before declaring war, or you will face crippling UN sanctions.",
        Icons.Default.Groups,
    ),
    TutorialPage(
        "Passing Laws",
        "Your parliament is waiting. Pass laws to shape your country's ideology and address crises.",
        "Some laws require high political capital. Check your mandate progress to gain capital.",
        Icons.Default.AccountBalance,
    ),
    TutorialPage(
        "Advancing Time",
        "The simulation is turn-based. Tap the Next Month button at the top to advance time.",
        "Watch out for random events and crises that pop up between turns!",
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

    Dialog(
        onDismissRequest = onSkip,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        val window = (androidx.compose.ui.platform.LocalView.current.parent as? DialogWindowProvider)?.window
        window?.let {
            it.setGravity(Gravity.BOTTOM)
            it.setDimAmount(0.0f) // Removed dimming so it doesn't overshadow // very light dim so we don't overshadow the UI
        }
        
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.BottomCenter) {

        Column(
            modifier = Modifier.fillMaxWidth(0.85f).clip(NssCardShape)
                .background(Color(0xE6050A0F)).border(1.dp, NssBorder, NssCardShape).padding(22.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("FIRST DAY BRIEFING", color = NssAccent, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
                Spacer(Modifier.weight(1f))
                Text("${page + 1} / ${tutorialPages.size}", color = NssMutedForeground, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Row(
                modifier = Modifier.padding(top = 22.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Icon(current.icon, contentDescription = null, tint = NssOnPhoto, modifier = Modifier.size(26.dp))
                Text(current.title, color = NssForeground, fontSize = 22.sp, fontWeight = FontWeight.Black)
            }
            Text(current.body, color = NssMutedForeground, fontSize = 14.sp, lineHeight = 21.sp, modifier = Modifier.padding(top = 12.dp))
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 18.dp).clip(NssCardShape)
                    .background(NssBackground).padding(14.dp),
            ) {
                Text("ADVISOR NOTE", color = NssAccent, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                Text(current.tip, color = NssForeground, fontSize = 12.sp, lineHeight = 18.sp, modifier = Modifier.padding(top = 5.dp))
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 22.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    "Skip tour",
                    modifier = Modifier.clip(NssCardShape).clickable(onClick = onSkip).padding(horizontal = 8.dp, vertical = 12.dp),
                    color = NssMutedForeground,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                if (page > 0) {
                    Text(
                        "Back",
                        modifier = Modifier.clip(NssCardShape).clickable(onClick = onBack).padding(horizontal = 10.dp, vertical = 12.dp),
                        color = NssPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Text(
                    if (page == tutorialPages.lastIndex) "Enter the game" else "Next",
                    modifier = Modifier.clip(NssCardShape).background(NssPrimary).clickable(onClick = onNext)
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    color = NssOnPhoto,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(tutorialPages.size) { index ->
                    Spacer(
                        Modifier.padding(horizontal = 3.dp).size(if (index == page) 8.dp else 6.dp)
                            .clip(CircleShape).background(if (index == page) NssAccent else NssMutedForeground.copy(alpha = 0.28f)),
                    )
                }
            }
        }
    }
}

    }
