package com.presidentsimulator.game.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.presidentsimulator.game.data.ActiveCrisis
import com.presidentsimulator.game.data.CrisisOption
import com.presidentsimulator.game.ui.theme.NssRed

@Composable
fun CrisisHotlineDialog(
    crises: List<ActiveCrisis>,
    playerBudget: Long,
    onDismiss: () -> Unit,
    onExecuteOption: (ActiveCrisis, CrisisOption) -> Unit,
) {
    var selectedCrisisIndex by remember { mutableIntStateOf(0) }
    val currentCrisis = crises.getOrNull(selectedCrisisIndex) ?: crises.firstOrNull()

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
            border = BorderStroke(2.dp, Color(0xFFDC2626)),
            shadowElevation = 24.dp,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Red Crisis Hotline Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                ) {
                    if (currentCrisis != null) {
                        Image(
                            painter = painterResource(id = currentCrisis.backdropDrawable),
                            contentDescription = currentCrisis.headline,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xCC0F172A)),
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFDC2626)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                            }
                            Column {
                                Text(
                                    text = "OVAL OFFICE RED HOTLINE",
                                    color = Color(0xFFEF4444),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                )
                                Text(
                                    text = currentCrisis?.type?.displayName?.uppercase() ?: "CRISIS DESK",
                                    color = Color.White,
                                    fontSize = 16.sp,
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
                }

                if (currentCrisis == null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No active national crises at this time.", color = Color.LightGray, fontSize = 14.sp)
                    }
                } else {
                    // Crisis selector strip if multiple
                    if (crises.size > 1) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1E293B))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            crises.forEachIndexed { index, c ->
                                val isSelected = index == selectedCrisisIndex
                                Text(
                                    text = "${index + 1}. ${c.type.displayName}",
                                    color = if (isSelected) Color.White else Color.Gray,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isSelected) Color(0xFFDC2626) else Color.Transparent)
                                        .clickable { selectedCrisisIndex = index }
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(16.dp),
                    ) {
                        Text(
                            text = currentCrisis.headline,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = currentCrisis.summary,
                            color = Color(0xFFCBD5E1),
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                        )

                        Spacer(Modifier.height(12.dp))

                        // Penalties card
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E293B))
                                .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column {
                                Text("AFFECTED DOMAIN", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text(currentCrisis.affectedSectorName, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("MONTHLY REVENUE DRAIN", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text("-\$${currentCrisis.monthlyRevenuePenaltyMillion}M / mo", color = NssRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("APPROVAL IMPACT", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text("-${currentCrisis.monthlyApprovalPenalty}% / mo", color = NssRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = "PRESIDENTIAL EXECUTIVE DIRECTIVES",
                            color = Color(0xFFD97706),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                        )

                        Spacer(Modifier.height(8.dp))

                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(currentCrisis.options) { option ->
                                val canAfford = playerBudget >= option.costBudget
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF1A2333))
                                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(option.label, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text(option.description, color = Color.LightGray, fontSize = 10.sp)
                                    }

                                    Button(
                                        onClick = { onExecuteOption(currentCrisis, option) },
                                        enabled = canAfford,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF059669),
                                            disabledContainerColor = Color(0xFF334155),
                                        ),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Authorize", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            if (option.costBudget > 0) {
                                                Text(formatCompactMoney(option.costBudget), color = Color(0xFFD1FAE5), fontSize = 8.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
