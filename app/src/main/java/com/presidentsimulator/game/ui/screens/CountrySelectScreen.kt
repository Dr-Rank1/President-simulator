package com.presidentsimulator.game.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.presidentsimulator.game.data.PlayableNationCatalog
import com.presidentsimulator.game.data.ScenarioCatalog
import com.presidentsimulator.game.ui.components.rememberNssLayoutSpec
import com.presidentsimulator.game.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CountrySelectScreen(
    nations: List<PlayableNationCatalog.NationDefinition>,
    onBack: () -> Unit,
    onSelectCountry: (countryId: String, scenarioId: String, challengeId: String) -> Unit,
) {
    val layout = rememberNssLayoutSpec()
    var selectedNationId by remember(nations) { mutableStateOf(nations.firstOrNull()?.id.orEmpty()) }
    var selectedChallengeId by remember { mutableStateOf(ScenarioCatalog.CHALLENGES.first().id) }

    val nation = nations.find { it.id == selectedNationId }
    val challenges = ScenarioCatalog.CHALLENGES

    BackHandler(onBack = onBack)

    Box(modifier = Modifier.fillMaxSize()) {
        // MA2-style hex background
        HexBackground()

        // Dark vignette overlay
        Box(modifier = Modifier.fillMaxSize().background(
            Brush.radialGradient(
                colors = listOf(Color(0x55060D14), Color(0xCC060D14))
            )
        ))

        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            // Header bar — MA2 style
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xCC030A12))
                    .border(1.dp, NssBorder, RoundedCornerShape(4.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(NssPrimary)
                        .border(1.dp, NssBorder, RoundedCornerShape(4.dp))
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = NssAccent, modifier = Modifier.size(16.dp))
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "SELECT YOUR NATION",
                    color = NssAccent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "${nations.size} NATIONS AVAILABLE",
                    color = NssMutedForeground,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(Modifier.height(10.dp))

            if (nation != null) {
                Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(10.dp)) {

                    // LEFT PANEL: Nation details — MA2 dossier style
                    Column(
                        modifier = Modifier
                            .weight(1.1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xEE030A12))
                            .border(1.dp, NssBorder, RoundedCornerShape(4.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Nation identity
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(NssSecondary)
                                    .border(1.dp, NssBorder, RoundedCornerShape(4.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(nation.flagEmoji, fontSize = 36.sp)
                            }
                            Column {
                                Text(nation.name.uppercase(), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
                                Text(nation.officialName, color = NssAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text(nation.governmentSystem.name.replace("_", " "), color = NssMutedForeground, fontSize = 9.sp)
                            }
                        }

                        // Divider
                        Box(Modifier.fillMaxWidth().height(1.dp).background(NssBorder))

                        // Stats — MA2 data row style
                        Text("NATIONAL OVERVIEW", color = NssAccent, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
                        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            DossierRow("Population", "${nation.vitals.population / 1_000_000}M")
                            DossierRow("State Budget", "\$${nation.vitals.budget / 1_000}B")
                            DossierRow("Military Strength", "${nation.militaryStrength.toLong() / 1000}K")
                            DossierRow("Ideology", nation.ideology.name)
                            DossierRow("Government", nation.governmentSystem.name.replace("_", " "))
                        }

                        // Divider
                        Box(Modifier.fillMaxWidth().height(1.dp).background(NssBorder))

                        // Difficulty selector — MA2 style chips
                        Text("CAMPAIGN DIFFICULTY", color = NssAccent, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            challenges.forEach { challenge ->
                                val isSelected = selectedChallengeId == challenge.id
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isSelected) NssAccent else NssSecondary)
                                        .border(1.dp, if (isSelected) NssAccent else NssBorder, RoundedCornerShape(4.dp))
                                        .clickable { selectedChallengeId = challenge.id }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        challenge.title.uppercase(),
                                        color = if (isSelected) Color(0xFF000000) else NssMutedForeground,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.weight(1f))

                        // Launch button — MA2 accent CTA
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(Brush.horizontalGradient(listOf(NssAccent, Color(0xFF0096D6))))
                                .clickable {
                                    onSelectCountry(nation.id, ScenarioCatalog.ALL.first().id, selectedChallengeId)
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "▶  COMMENCE COMMAND",
                                color = Color(0xFF000000),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp
                            )
                        }
                    }

                    // RIGHT PANEL: Nation grid — MA2 compact tile grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        modifier = Modifier
                            .weight(1.6f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xCC030A12))
                            .border(1.dp, NssBorder, RoundedCornerShape(4.dp)),
                        contentPadding = PaddingValues(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(nations) { n ->
                            val isSelected = selectedNationId == n.id
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1.2f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSelected) NssAccent.copy(alpha = 0.15f) else NssSecondary.copy(alpha = 0.6f))
                                    .border(if (isSelected) 2.dp else 1.dp, if (isSelected) NssAccent else NssBorder, RoundedCornerShape(4.dp))
                                    .clickable { selectedNationId = n.id }
                                    .padding(6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(n.flagEmoji, fontSize = 22.sp)
                                    Spacer(Modifier.height(3.dp))
                                    Text(
                                        n.name,
                                        color = if (isSelected) NssAccent else NssMutedForeground,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DossierRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = NssMutedForeground, fontSize = 10.sp)
        Text(value, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun HexBackground() {
    Canvas(modifier = Modifier.fillMaxSize().background(Color(0xFF040B11))) {
        val hexRadius = 36f
        val hexHeight = hexRadius * 2f
        val hexWidth = (kotlin.math.sqrt(3.0) * hexRadius).toFloat()
        val vertDist = hexHeight * 0.75f
        val horizDist = hexWidth
        val cols = (size.width / horizDist).toInt() + 2
        val rows = (size.height / vertDist).toInt() + 2
        val path = Path()
        val strokeColor = Color(0x1829B6F6) // Very faint cyan
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val x = c * horizDist + if (r % 2 == 1) horizDist / 2f else 0f
                val y = r * vertDist
                path.reset()
                for (i in 0..5) {
                    val angle = Math.PI / 3 * i - Math.PI / 2
                    val px = x + hexRadius * cos(angle).toFloat()
                    val py = y + hexRadius * sin(angle).toFloat()
                    if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                }
                path.close()
                drawPath(path, color = strokeColor, style = Stroke(width = 1.5f))
            }
        }
    }
}
