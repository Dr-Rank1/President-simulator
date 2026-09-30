package com.presidentsimulator.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.presidentsimulator.game.data.ElectionNightResult
import kotlin.math.roundToInt

@Composable
fun ElectionNightDialog(
    result: ElectionNightResult,
    onConfirm: () -> Unit,
) {
    Dialog(
        onDismissRequest = onConfirm,
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
        ) {
            val headerColor = if (result.victory) Color(0xFF43A047) else Color(0xFFD32F2F)
            val headerTitle = if (result.victory) "VICTORY" else "DEFEAT"
            
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerColor)
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.HowToVote, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ELECTION NIGHT: $headerTitle",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        letterSpacing = 1.sp
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = result.narrative,
                    fontSize = 14.sp,
                    color = Color(0xFF475569),
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // National Split
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFF1F5F9))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("NATIONAL SPLIT", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0288D1))
                    VoteBar(label = "You", percent = result.playerShare, color = Color(0xFF43A047))
                    VoteBar(label = result.challengerName, percent = result.challengerShare, color = Color(0xFFD32F2F))
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                // Cohorts
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFF1F5F9))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("COHORT READOUT", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0288D1))
                    CohortLine("Working class", result.workingShare)
                    CohortLine("Business elite", result.businessShare)
                    CohortLine("Military", result.militaryShare)
                    CohortLine("Academics", result.academicsShare)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Continue Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(headerColor)
                        .clickable(onClick = onConfirm)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (result.victory) "ACCEPT MANDATE" else "CONCEDE",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun VoteBar(label: String, percent: Float, color: Color) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
            Text("${percent.roundToInt()}%", fontSize = 14.sp, fontWeight = FontWeight.Black, color = color)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { (percent / 100f).coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = Color(0xFFCBD5E1)
        )
    }
}

@Composable
private fun CohortLine(label: String, value: Float) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, fontSize = 12.sp, color = Color(0xFF64748B))
        Text("${value.roundToInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
    }
}
