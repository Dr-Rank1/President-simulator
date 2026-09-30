package com.presidentsimulator.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
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
import com.presidentsimulator.game.data.EventChoice
import com.presidentsimulator.game.data.EventConsequence
import com.presidentsimulator.game.data.GameEvent
import com.presidentsimulator.game.ui.components.graphics.EventIllustration
import com.presidentsimulator.game.viewmodel.toBudgetString
import kotlin.math.roundToInt

@Composable
fun EventCrisisDialog(
    event: GameEvent,
    onChoiceSelected: (EventChoice) -> Unit,
) {
    Dialog(
        onDismissRequest = { },
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
                    .background(Color(0xFFD32F2F))
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "NATIONAL CRISIS",
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
                    text = event.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFF1E293B),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                EventIllustration(
                    eventType = event.id,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Text(
                    text = event.description,
                    fontSize = 14.sp,
                    color = Color(0xFF475569),
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF1F5F9), RoundedCornerShape(4.dp))
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "CHOOSE A RESPONSE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0288D1),
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp),
                ) {
                    items(event.choices, key = { it.text }) { choice ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFE3F2FD))
                                .clickable { onChoiceSelected(choice) }
                                .padding(12.dp)
                        ) {
                            Text(
                                text = choice.text,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF0D47A1)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = choice.consequence.toEffectSummary(),
                                fontSize = 12.sp,
                                color = Color(0xFF1565C0),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "⏸ Time paused until resolved",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }
        }
    }
}

fun EventConsequence.toEffectSummary(): String {
    val parts = buildList {
        if (budgetChange != 0L) {
            val prefix = if (budgetChange > 0) "+" else ""
            add("Treasury $prefix${budgetChange.toBudgetString()}")
        }
        if (approvalChange != 0f) {
            val prefix = if (approvalChange > 0) "+" else ""
            add("Approval $prefix${approvalChange.roundToInt()}%")
        }
        if (populationChange != 0L) add("Population ${populationChange.toSignedCount()}")
        if (factoriesChange != 0) add("Infrastructure ${factoriesChange.toSignedInt()}")
        if (farmsChange != 0) add("Farms ${farmsChange.toSignedInt()}")
        if (housingChange != 0) add("Housing ${housingChange.toSignedInt()}")
        if (armySizeChange != 0L) add("Army ${armySizeChange.toSignedCount()}")
        if (defconChange != 0) add("DEFCON ${defconChange.toSignedInt()}")
    }
    return parts.joinToString(", ").ifEmpty { "No direct effect" }
}

private fun Int.toSignedInt(): String = if (this > 0) "+$this" else "$this"

private fun Long.toSignedCount(): String {
    val sign = when {
        this > 0L -> "+"
        this < 0L -> "-"
        else -> ""
    }
    val abs = kotlin.math.abs(this)
    val body = when {
        abs >= 1_000_000_000L -> String.format(java.util.Locale.ROOT, "%.2fB", abs / 1_000_000_000.0)
        abs >= 1_000_000L -> String.format(java.util.Locale.ROOT, "%.1fM", abs / 1_000_000.0)
        abs >= 1_000L -> String.format(java.util.Locale.ROOT, "%.1fK", abs / 1_000.0)
        else -> abs.toString()
    }
    return "$sign$body"
}
