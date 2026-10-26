package com.presidentsimulator.game.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.presidentsimulator.game.data.ExecutiveOrderType
import com.presidentsimulator.game.data.OvalOfficeState
import com.presidentsimulator.game.data.VoterDemographic
import com.presidentsimulator.game.ui.theme.NssEmerald
import com.presidentsimulator.game.ui.theme.NssRed

@Composable
fun ExecutiveOrdersDialog(
    ovalOffice: OvalOfficeState,
    onDismiss: () -> Unit,
    onToggleDecree: (ExecutiveOrderType) -> Unit,
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Presidential Decrees, 1: Demographic Polling

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(2.dp, Color(0xFFD97706)),
            shadowElevation = 24.dp,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E293B))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFD97706)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Default.Gavel, contentDescription = null, tint = Color.White)
                        }
                        Column {
                            Text(
                                text = "THE RESOLUTE DESK",
                                color = Color(0xFFFBBF24),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                            )
                            Text(
                                text = "EXECUTIVE ORDERS & DEMOGRAPHIC MANDATE",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0x88000000))
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                // 2 Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF151E2E)),
                ) {
                    listOf("1. Presidential Decrees (${ovalOffice.activeDecreesCount} Active)", "2. Voter Demographics & Polling").forEachIndexed { idx, label ->
                        val isSelected = selectedTab == idx
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedTab = idx }
                                .background(if (isSelected) Color(0xFF0F172A) else Color.Transparent)
                                .border(
                                    BorderStroke(
                                        width = if (isSelected) 2.dp else 0.dp,
                                        color = if (isSelected) Color(0xFFD97706) else Color.Transparent,
                                    ),
                                )
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color(0xFFFBBF24) else Color.LightGray,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            )
                        }
                    }
                }

                if (selectedTab == 0) {
                    // Decrees List
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(ExecutiveOrderType.entries) { orderType ->
                            val isEnacted = ovalOffice.decrees[orderType]?.isEnacted == true

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isEnacted) Color(0xFF064E3B) else Color(0xFF1E293B))
                                    .border(
                                        1.dp,
                                        if (isEnacted) Color(0xFF34D399) else Color(0xFF334155),
                                        RoundedCornerShape(8.dp),
                                    )
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(orderType.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text(orderType.description, color = Color.LightGray, fontSize = 10.sp)
                                    Spacer(Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Text("Cost: \$${orderType.monthlyCostMillion}M / mo", color = Color(0xFFFCA5A5), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Text("Approval: +${orderType.approvalImpact}%", color = Color(0xFF86EFAC), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Button(
                                    onClick = { onToggleDecree(orderType) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isEnacted) Color(0xFF991B1B) else Color(0xFF059669),
                                    ),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                ) {
                                    Text(if (isEnacted) "Revoke" else "Sign Decree", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    // Demographic Polling Breakdown
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1E293B))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column {
                                    Text("AGGREGATE ELECTORAL RATING", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    Text("${ovalOffice.polling.overallWeightedApproval.toInt()}% Weighted Approval", color = NssEmerald, fontSize = 16.sp, fontWeight = FontWeight.Black)
                                }
                                Text("NEXT ELECTION SECURE", color = Color(0xFF34D399), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        items(VoterDemographic.entries) { demo ->
                            val approval = ovalOffice.polling.approvalByDemographic[demo] ?: 50f
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1E293B))
                                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                                    .padding(12.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(demo.title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("${approval.toInt()}% Support (${(demo.weightPercent * 100).toInt()}% of Electorate)", color = Color(0xFFFBBF24), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(demo.description, color = Color.LightGray, fontSize = 10.sp)
                                Spacer(Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { approval / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = if (approval >= 50f) Color(0xFF10B981) else Color(0xFFEF4444),
                                    trackColor = Color(0xFF0F172A),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
