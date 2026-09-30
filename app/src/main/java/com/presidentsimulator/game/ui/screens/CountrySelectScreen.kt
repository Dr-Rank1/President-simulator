package com.presidentsimulator.game.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
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

    Box(modifier = Modifier.fillMaxSize().background(NssBackground)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Cyan header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NssPrimary)
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(Modifier.width(16.dp))
                Text(
                    text = "SELECT YOUR NATION",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            }

            if (nation != null) {
                Row(modifier = Modifier.fillMaxWidth().weight(1f).padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                    
                    // LEFT PANEL: Nation details and launch
                    Column(
                        modifier = Modifier
                            .weight(1.1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(NssBackground),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(nation.flagEmoji, fontSize = 42.sp)
                            }
                            Column {
                                Text(nation.name.uppercase(), color = Color(0xFF212121), fontSize = 22.sp, fontWeight = FontWeight.Black)
                                Text(nation.officialName, color = NssPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        
                        // Stats Grid
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            StatBlock("POPULATION", "${nation.vitals.population / 1_000_000}M")
                            StatBlock("BUDGET", "$${nation.vitals.budget / 1_000}B")
                            StatBlock("MILITARY", "${nation.militaryStrength / 1000}K")
                        }
                        
                        Spacer(Modifier.height(20.dp))
                        Text("DIFFICULTY", color = Color(0xFF757575), fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                        Spacer(Modifier.height(6.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            challenges.forEach { challenge ->
                                val isSelected = selectedChallengeId == challenge.id
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) NssPrimary else Color(0xFFEEEEEE))
                                        .clickable { selectedChallengeId = challenge.id }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        challenge.title.uppercase(),
                                        color = if (isSelected) Color.White else Color(0xFF757575),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        
                        Spacer(Modifier.weight(1f))
                        
                        Text("IDEOLOGY & GOVERNMENT", color = Color(0xFF757575), fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                        Spacer(Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(6.dp)).background(NssBackground).padding(8.dp)) {
                                Column {
                                    Text("IDEOLOGY", color = NssPrimary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    Text(nation.ideology.name, color = Color(0xFF212121), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(6.dp)).background(NssBackground).padding(8.dp)) {
                                Column {
                                    Text("RULING SYSTEM", color = NssPrimary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    Text(nation.governmentSystem.name.replace("_", " "), color = Color(0xFF212121), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        
                        Spacer(Modifier.weight(1f))
                        
                        Button(
                            onClick = { 
                                onSelectCountry(nation.id, ScenarioCatalog.ALL.first().id, selectedChallengeId)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NssPrimary, contentColor = Color.White),
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("START GAME", fontSize = 13.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                        }
                    }

                    
                    // RIGHT PANEL: Grid of Nations
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier
                            .weight(1.3f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White),
                        contentPadding = PaddingValues(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(nations) { n ->
                            val isSelected = selectedNationId == n.id
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) NssPrimary else NssBackground)
                                    .clickable { selectedNationId = n.id }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                    Text(n.flagEmoji, fontSize = 28.sp)
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        n.name, 
                                        color = if (isSelected) Color.White else Color(0xFF424242),
                                        fontSize = 10.sp,
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
private fun StatBlock(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = NssPrimary, fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Text(value, color = Color(0xFF212121), fontSize = 16.sp, fontWeight = FontWeight.Black)
    }
}


@Composable
fun HexBackground() {
    Canvas(modifier = Modifier.fillMaxSize().background(Color(0xFF050A10))) {
        val hexRadius = 40f
        val hexHeight = hexRadius * 2f
        val hexWidth = (Math.sqrt(3.0) * hexRadius).toFloat()
        
        val vertDist = hexHeight * 0.75f
        val horizDist = hexWidth
        
        val cols = (size.width / horizDist).toInt() + 2
        val rows = (size.height / vertDist).toInt() + 2
        
        val path = Path()
        val paintStroke = Stroke(width = 2f)
        val strokeColor = Color(0x1A456B86) // Very faint NssSky
        
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
                drawPath(path, color = strokeColor, style = paintStroke)
            }
        }
    }
}
