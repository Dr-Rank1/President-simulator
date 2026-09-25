package com.presidentsimulator.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.presidentsimulator.game.ui.navigation.GameDestination
import com.presidentsimulator.game.ui.theme.NssBackground
import com.presidentsimulator.game.ui.theme.NssForeground
import com.presidentsimulator.game.ui.theme.NssGameCard
import com.presidentsimulator.game.ui.theme.NssMutedForeground
import com.presidentsimulator.game.ui.theme.NssPrimary

private data class DirectoryEntry(val label: String, val keywords: String, val destination: GameDestination)

@Composable
fun ActionDirectoryDialog(
    onDismiss: () -> Unit,
    onNavigate: (GameDestination) -> Unit,
    currentDestination: GameDestination? = null,
) {
    var query by remember { mutableStateOf("") }
    var showGuide by remember { mutableStateOf(false) }
    val entries = listOf(
        DirectoryEntry("Command Center", "dashboard priorities agenda alerts", GameDestination.Dashboard),
        DirectoryEntry("Economy", "budget taxes trade farms power factories debt", GameDestination.Economy),
        DirectoryEntry("Defense", "military recruit weapons defcon training frontline deployment", GameDestination.Military),
        DirectoryEntry("Recruit personnel and equipment", "army tanks jets ships procure", GameDestination.Military),
        DirectoryEntry("Military training", "readiness improve strength doctrine", GameDestination.Military),
        DirectoryEntry("Foreign Affairs", "diplomacy treaty aid relations war peace", GameDestination.Diplomacy),
        DirectoryEntry("Intelligence", "spies covert security coup", GameDestination.SecretService),
        DirectoryEntry("Science", "research technology universities", GameDestination.Science),
        DirectoryEntry("Domestic Policy", "laws society parliament legislation", GameDestination.LawsSociety),
        DirectoryEntry("United Nations", "governance resolutions embargo", GameDestination.Governance),
        DirectoryEntry("Cabinet", "ministers reshuffle advisors", GameDestination.Cabinet),
        DirectoryEntry("Demographics", "people approval election population", GameDestination.Demographics),
        DirectoryEntry("Analytics and Legacy", "history charts achievements save game", GameDestination.Analytics),
        DirectoryEntry("Settings", "audio music sound", GameDestination.AudioSettings),
    )
    val results = entries.filter { it.label.contains(query.trim(), ignoreCase = true) || it.keywords.contains(query.trim(), ignoreCase = true) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Find a ministry", fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth(),
                    singleLine = true, label = { Text("Search ministries or actions") })
                currentDestination?.let { current ->
                    Text(if (showGuide) "${current.title}: ${guideFor(current)}" else "On ${current.title}? Tap for a quick guide.",
                        Modifier.fillMaxWidth().clip(NssCardShape).background(NssGameCard).clickable { showGuide = !showGuide }.padding(9.dp),
                        color = NssMutedForeground, fontSize = 9.sp)
                }
                Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    results.forEach { entry ->
                        Row(Modifier.fillMaxWidth().clip(NssCardShape).background(NssGameCard)
                            .clickable { onNavigate(entry.destination); onDismiss() }.padding(11.dp)) {
                            Column {
                                Text(entry.label, color = NssForeground, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(entry.destination.title.uppercase(), color = NssMutedForeground, fontSize = 8.sp)
                            }
                        }
                    }
                    if (results.isEmpty()) Text("No matching ministries. Try a broader search.", color = NssMutedForeground, fontSize = 11.sp)
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close", color = NssPrimary) } },
        containerColor = NssBackground,
    )
}

private fun guideFor(destination: GameDestination): String = when (destination) {
    GameDestination.Dashboard -> "Check priority files, treasury, approval, and the monthly advisor before advancing time."
    GameDestination.Economy -> "Review the budget first, then adjust policy or invest in production and trade."
    GameDestination.Military -> "Forces changes posture; Recruitment places paid orders; Logistics manages DEFCON and upkeep; Training improves strength."
    GameDestination.Diplomacy -> "Select a country to review relations, then negotiate treaties, aid, trade, alliances, or war."
    GameDestination.SecretService -> "Fund domestic security, recruit agents, and assign operations to foreign targets."
    GameDestination.Science -> "Choose research to unlock lasting improvements; funding can increase progress."
    GameDestination.LawsSociety -> "Move bills through negotiation and enactment; read upkeep and public impacts before voting."
    GameDestination.Governance -> "Review UN resolutions and alliance commitments; votes can change sanctions and military rules."
    GameDestination.Cabinet -> "Appoint ministers to shape policy bonuses, cabinet cohesion, and crisis response."
    GameDestination.Demographics -> "Track public groups and election risk; campaigns and speeches can shift support."
    GameDestination.Analytics -> "Review long-term trends, legacy chapters, campaign milestones, and save slots."
    GameDestination.AudioSettings -> "Adjust music and sound effects, inspect diagnostics, and manage campaign saves."
}
