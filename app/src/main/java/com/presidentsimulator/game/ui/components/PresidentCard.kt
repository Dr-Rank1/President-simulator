package com.presidentsimulator.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

data class PresidentCardData(
    val title: String,
    val imageUrl: String,
    val badgeMode: BadgeMode = BadgeMode.UpgradeArrow,
    val badgeText: String = "",
    val stats: List<Pair<String, String>>, // e.g. "Cost" to "-2,000"
    val onClick: () -> Unit
)

enum class BadgeMode {
    UpgradeArrow,
    LevelCircle,
    None
}

@Composable
fun PresidentCard(data: PresidentCardData) {
    Column(
        modifier = Modifier
            .width(150.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color.White)
            .border(0.5.dp, Color.LightGray, RoundedCornerShape(4.dp))
            .clickable(onClick = data.onClick)
    ) {
        // Top Photo Area (approx 45%)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        ) {
            AsyncImage(
                model = data.imageUrl,
                contentDescription = data.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            
            // Badge
            when (data.badgeMode) {
                BadgeMode.UpgradeArrow -> {
                    Box(
                        modifier = Modifier
                            .padding(6.dp)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E88E5))
                            .align(Alignment.TopEnd),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = "Upgrade", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
                BadgeMode.LevelCircle -> {
                    Box(
                        modifier = Modifier
                            .padding(6.dp)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.dp, Color.Black, CircleShape)
                            .align(Alignment.TopEnd),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(data.badgeText, color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                BadgeMode.None -> {}
            }
        }

        // Blue Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E88E5))
                .padding(vertical = 6.dp, horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = data.title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
        }

        // White Stats Area
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            data.stats.forEach { (label, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(label, color = Color.Gray, fontSize = 11.sp)
                    Text(value, color = if (value.startsWith("+")) Color(0xFF43A047) else if (value.startsWith("-")) Color(0xFFD32F2F) else Color.DarkGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
