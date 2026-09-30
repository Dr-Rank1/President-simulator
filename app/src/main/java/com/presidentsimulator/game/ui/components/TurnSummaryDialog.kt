package com.presidentsimulator.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.TurnSummary
import com.presidentsimulator.game.viewmodel.toBudgetString
import com.presidentsimulator.game.viewmodel.toPopulationString

@Composable
fun TurnSummaryDialog(
    summary: TurnSummary,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
        ) {
            // Blue Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF03A9F4))
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "MONTHLY BULLETIN",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    letterSpacing = 1.sp
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Date
                val dateLabel = "${GameState.monthName(summary.month)} ${summary.year}"
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
                    Icon(Icons.Default.EventNote, contentDescription = null, tint = Color(0xFF03A9F4), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = dateLabel,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF1E293B)
                    )
                }

                // Stats Panel
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFF1F5F9))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SummaryRow(
                        icon = Icons.Default.AttachMoney,
                        label = "Net Income",
                        value = summary.netIncome.toBudgetString(),
                        isPositive = summary.netIncome >= 0
                    )
                    SummaryRow(
                        icon = Icons.Default.AttachMoney,
                        label = "Budget Delta",
                        value = (if (summary.budgetDelta > 0) "+" else "") + summary.budgetDelta.toBudgetString(),
                        isPositive = summary.budgetDelta >= 0
                    )
                    SummaryRow(
                        icon = Icons.Default.ThumbUp,
                        label = "Approval Delta",
                        value = (if (summary.approvalDelta > 0) "+" else "") + String.format("%.1f%%", summary.approvalDelta),
                        isPositive = summary.approvalDelta >= 0f
                    )
                    SummaryRow(
                        icon = Icons.Default.People,
                        label = "Population Growth",
                        value = (if (summary.populationDelta > 0) "+" else "") + summary.populationDelta.toPopulationString(),
                        isPositive = summary.populationDelta >= 0
                    )
                    SummaryRow(
                        icon = Icons.Default.BarChart,
                        label = "GDP Delta",
                        value = (if (summary.gdpDelta > 0) "+" else "") + summary.gdpDelta.toBudgetString(),
                        isPositive = summary.gdpDelta >= 0
                    )
                }

                if (summary.bulletin.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFFFF3E0))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "WHAT HAPPENED",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE65100),
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        summary.bulletin.forEach { line ->
                            Text(
                                text = "• $line",
                                fontSize = 12.sp,
                                color = Color(0xFF424242),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Continue Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF43A047))
                        .clickable { onDismiss() }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "CONTINUE",
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
private fun SummaryRow(
    icon: ImageVector,
    label: String,
    value: String,
    isPositive: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color(0xFF334155),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (isPositive) Color(0xFF43A047) else Color(0xFFD32F2F),
        )
    }
}
