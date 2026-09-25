package com.presidentsimulator.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.ui.navigation.GameDestination
import com.presidentsimulator.game.ui.theme.NssAccent
import com.presidentsimulator.game.ui.theme.NssRed

data class BottomNavItem(
    val destination: GameDestination,
    val label: String,
    val icon: ImageVector,
)

val bottomNavItems = listOf(
    BottomNavItem(GameDestination.Dashboard, "Map", Icons.Default.Public),
    BottomNavItem(GameDestination.Economy, "Economy", Icons.Default.AttachMoney),
    BottomNavItem(GameDestination.Military, "Military", Icons.Default.Shield),
    BottomNavItem(GameDestination.Diplomacy, "Foreign", Icons.Default.Gavel),
    BottomNavItem(GameDestination.LawsSociety, "Laws", Icons.Default.AccountBalance),
    BottomNavItem(GameDestination.Science, "Research", Icons.Default.Science),
)

private fun bottomNavAlertCount(state: GameState, dest: GameDestination): Int = when (dest) {
    GameDestination.Military -> if (state.diplomacy.activeWar != null) 1 else 0
    GameDestination.Diplomacy -> state.diplomacy.rivals.count { it.relationshipScore < 25 }
    GameDestination.Science -> if ((state.research.activeTechnology != null && state.research.progressPercent() >= 80f) || (state.research.activeTechnology == null && state.research.sciencePoints >= 150L)) 1 else 0
    GameDestination.LawsSociety -> state.legal.pendingLaws.size
    GameDestination.Governance -> if (state.governance.activeResolution != null) 1 else 0
    else -> 0
}

@Composable
fun MinistryBottomNav(
    state: GameState,
    currentRoute: String?,
    onNavigate: (GameDestination) -> Unit,
    sideRail: Boolean = false,
    modifier: Modifier = Modifier,
) {
    // In Modern Age 2, the bottom navigation is made of chunky metallic/dark tiles.
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF0F172A)) // Dark slate background
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal
                )
            )
            .padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.destination.route
            val alerts = bottomNavAlertCount(state, item.destination)
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (selected) Color(0xFF1E293B) else Color.Transparent)
                    .border(
                        1.dp,
                        if (selected) NssAccent.copy(alpha = 0.5f) else Color.Transparent,
                        RoundedCornerShape(6.dp)
                    )
                    .clickable { onNavigate(item.destination) }
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (selected) NssAccent else Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                    if (alerts > 0) {
                        Box(
                            Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 8.dp, y = (-6).dp)
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(NssRed),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = alerts.coerceAtMost(9).toString(),
                                color = Color.White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.label.uppercase(),
                    color = if (selected) NssAccent else Color.Gray,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
