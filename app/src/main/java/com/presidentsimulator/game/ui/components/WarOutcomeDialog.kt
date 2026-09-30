package com.presidentsimulator.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.SentimentVeryDissatisfied
import androidx.compose.material3.Icon
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
import com.presidentsimulator.game.data.WarOutcome
import com.presidentsimulator.game.viewmodel.toBudgetString
import com.presidentsimulator.game.viewmodel.toCasualtyString
import kotlin.math.roundToInt

@Composable
fun WarOutcomeDialog(
    outcome: WarOutcome,
    onDismiss: () -> Unit,
) {
    val headerColor = if (outcome.victory) Color(0xFF43A047) else Color(0xFFD32F2F)
    val icon = if (outcome.victory) Icons.Default.MilitaryTech else Icons.Default.SentimentVeryDissatisfied
    val title = if (outcome.victory) "VICTORY" else "DEFEAT"

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerColor)
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "WAR OUTCOME: $title",
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
                    text = "War with ${outcome.targetName} ended after ${outcome.monthsActive} months.",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF1E293B),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFF1F5F9))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (outcome.warGoalLabel.isNotBlank()) {
                        Text("Objective: ${outcome.warGoalLabel}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0288D1))
                    }
                    if (outcome.settlementNote.isNotBlank()) {
                        Text(outcome.settlementNote, fontSize = 14.sp, color = Color(0xFF475569), textAlign = TextAlign.Center)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFFFF3E0))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Our Casualties", fontSize = 12.sp, color = Color(0xFF64748B))
                        Text(outcome.playerCasualties.toCasualtyString(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Enemy Casualties", fontSize = 12.sp, color = Color(0xFF64748B))
                        Text(outcome.enemyCasualties.toCasualtyString(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Budget Change", fontSize = 12.sp, color = Color(0xFF64748B))
                        val isPos = outcome.budgetDelta >= 0
                        Text("${if (isPos) "+" else ""}${outcome.budgetDelta.toBudgetString()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if(isPos) Color(0xFF43A047) else Color(0xFFD32F2F))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Approval Change", fontSize = 12.sp, color = Color(0xFF64748B))
                        val isPos = outcome.approvalDelta >= 0
                        Text("${if (isPos) "+" else ""}${outcome.approvalDelta.roundToInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if(isPos) Color(0xFF43A047) else Color(0xFFD32F2F))
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(headerColor)
                        .clickable(onClick = onDismiss)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ACKNOWLEDGE",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
