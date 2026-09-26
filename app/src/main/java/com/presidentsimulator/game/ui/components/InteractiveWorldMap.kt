package com.presidentsimulator.game.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.presidentsimulator.game.R
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.ui.theme.NssAccent
import com.presidentsimulator.game.ui.theme.NssEmerald
import com.presidentsimulator.game.ui.theme.NssRed
import kotlin.math.roundToInt

object WorldCoordinates {
    private val coords = mapOf(
        "KE" to Pair(0.602f, 0.507f), // Kenya
        "UG" to Pair(0.590f, 0.500f), // Uganda
        "TZ" to Pair(0.595f, 0.540f), // Tanzania
        "ET" to Pair(0.608f, 0.450f), // Ethiopia
        "SO" to Pair(0.630f, 0.470f), // Somalia
        "RW" to Pair(0.583f, 0.510f), // Rwanda
        "SS" to Pair(0.585f, 0.460f), // South Sudan
        "SD" to Pair(0.582f, 0.410f), // Sudan
        "EG" to Pair(0.585f, 0.355f), // Egypt
        "ZA" to Pair(0.565f, 0.670f), // South Africa
        "NG" to Pair(0.520f, 0.450f), // Nigeria
        "GH" to Pair(0.495f, 0.460f), // Ghana
        "DZ" to Pair(0.505f, 0.340f), // Algeria
        "MA" to Pair(0.475f, 0.320f), // Morocco
        "CD" to Pair(0.560f, 0.520f), // DR Congo
        "AO" to Pair(0.545f, 0.570f), // Angola
        "MZ" to Pair(0.595f, 0.600f), // Mozambique
        "ZM" to Pair(0.575f, 0.580f), // Zambia
        "ZW" to Pair(0.580f, 0.610f), // Zimbabwe
        // Americas
        "US" to Pair(0.240f, 0.300f), // United States
        "CA" to Pair(0.230f, 0.180f), // Canada
        "MX" to Pair(0.215f, 0.395f), // Mexico
        "BR" to Pair(0.355f, 0.580f), // Brazil
        "AR" to Pair(0.320f, 0.700f), // Argentina
        "CL" to Pair(0.300f, 0.680f), // Chile
        "CO" to Pair(0.295f, 0.480f), // Colombia
        "PE" to Pair(0.290f, 0.560f), // Peru
        "VE" to Pair(0.315f, 0.465f), // Venezuela
        // Europe
        "GB" to Pair(0.495f, 0.230f), // UK
        "FR" to Pair(0.505f, 0.260f), // France
        "DE" to Pair(0.528f, 0.240f), // Germany
        "IT" to Pair(0.535f, 0.280f), // Italy
        "ES" to Pair(0.485f, 0.285f), // Spain
        "PL" to Pair(0.555f, 0.235f), // Poland
        "UA" to Pair(0.585f, 0.245f), // Ukraine
        "SE" to Pair(0.540f, 0.170f), // Sweden
        "NO" to Pair(0.525f, 0.170f), // Norway
        "TR" to Pair(0.595f, 0.295f), // Turkey
        "RU" to Pair(0.720f, 0.190f), // Russia
        // Asia & Middle East
        "CN" to Pair(0.785f, 0.320f), // China
        "IN" to Pair(0.710f, 0.390f), // India
        "JP" to Pair(0.880f, 0.300f), // Japan
        "KR" to Pair(0.845f, 0.305f), // South Korea
        "SA" to Pair(0.625f, 0.370f), // Saudi Arabia
        "IR" to Pair(0.645f, 0.320f), // Iran
        "IQ" to Pair(0.620f, 0.315f), // Iraq
        "IL" to Pair(0.595f, 0.335f), // Israel
        "PK" to Pair(0.685f, 0.350f), // Pakistan
        "ID" to Pair(0.815f, 0.520f), // Indonesia
        "AU" to Pair(0.865f, 0.670f), // Australia
        "NZ" to Pair(0.930f, 0.720f), // New Zealand
        "TH" to Pair(0.775f, 0.420f), // Thailand
        "VN" to Pair(0.790f, 0.420f), // Vietnam
        "PH" to Pair(0.835f, 0.440f), // Philippines
    )

    fun forCode(code: String, countryName: String = ""): Pair<Float, Float> {
        val upper = code.uppercase()
        coords[upper]?.let { return it }
        val nameUpper = countryName.uppercase()
        for ((k, v) in coords) {
            if (nameUpper.contains(k)) return v
        }
        if (nameUpper.contains("KENYA")) return Pair(0.602f, 0.507f)
        if (nameUpper.contains("AMERICA") || nameUpper.contains("UNITED STATES")) return Pair(0.240f, 0.300f)
        if (nameUpper.contains("CHINA")) return Pair(0.785f, 0.320f)
        if (nameUpper.contains("RUSSIA")) return Pair(0.720f, 0.190f)
        if (nameUpper.contains("GERMANY")) return Pair(0.528f, 0.240f)
        if (nameUpper.contains("BRITAIN") || nameUpper.contains("KINGDOM")) return Pair(0.495f, 0.230f)
        if (nameUpper.contains("FRANCE")) return Pair(0.505f, 0.260f)
        if (nameUpper.contains("INDIA")) return Pair(0.710f, 0.390f)
        if (nameUpper.contains("JAPAN")) return Pair(0.880f, 0.300f)
        if (nameUpper.contains("BRAZIL")) return Pair(0.355f, 0.580f)
        if (nameUpper.contains("AUSTRALIA")) return Pair(0.865f, 0.670f)
        return Pair(0.5f, 0.4f)
    }
}

@Composable
fun InteractiveWorldMap(
    state: GameState? = null,
    modifier: Modifier = Modifier,
    onOpenDiplomacy: () -> Unit = {},
) {
    var scale by remember { mutableStateOf(1.0f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    var showCountryDetails by remember { mutableStateOf(false) }

    // Pulsing radar wave animation for HQ beacon
    val infiniteTransition = rememberInfiniteTransition(label = "hqRadar")
    val pulseSize by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 44f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseSize"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    val playerCoord = remember(state?.playerNation) {
        state?.playerNation?.let {
            WorldCoordinates.forCode(it.countryCode, it.name)
        } ?: Pair(0.602f, 0.507f)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF031626))
            .onSizeChanged { containerSize = it }
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoomChange, _ ->
                    scale = (scale * zoomChange).coerceIn(1.0f, 3.8f)
                    val maxPanX = (containerSize.width * (scale - 1f)) / 2f
                    val maxPanY = (containerSize.height * (scale - 1f)) / 2f
                    panOffset = Offset(
                        x = if (scale > 1f) (panOffset.x + pan.x).coerceIn(-maxPanX, maxPanX) else 0f,
                        y = if (scale > 1f) (panOffset.y + pan.y).coerceIn(-maxPanY, maxPanY) else 0f,
                    )
                }
            }
    ) {
        // Zoomable & Pannable Map Layer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = panOffset.x
                    translationY = panOffset.y
                }
        ) {
            // World Map Background Image
            Image(
                painter = painterResource(id = R.drawable.world_map),
                contentDescription = "Tactical World Map",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                alpha = 0.75f,
            )

            // Player Country HQ Beacon (Prominently marks the country being managed)
            if (containerSize.width > 0 && containerSize.height > 0) {
                val hqX = containerSize.width * playerCoord.first
                val hqY = containerSize.height * playerCoord.second

                // Pulsing Radar Rings over managed capital
                Box(
                    modifier = Modifier
                        .offset(
                            x = (hqX - pulseSize / 2f).dp / (containerSize.width / 360f).coerceAtLeast(1f),
                            y = (hqY - pulseSize / 2f).dp / (containerSize.height / 200f).coerceAtLeast(1f)
                        )
                        .size(pulseSize.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, NssEmerald.copy(alpha = pulseAlpha), CircleShape)
                )

                // Glowing Beacon Center Dot & HQ Label
                Column(
                    modifier = Modifier
                        .offset(
                            x = (hqX - 50f).dp / (containerSize.width / 360f).coerceAtLeast(1f),
                            y = (hqY - 32f).dp / (containerSize.height / 200f).coerceAtLeast(1f)
                        )
                        .clickable { showCountryDetails = !showCountryDetails },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Tactical Sovereign HQ Chip
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFA050A0F),
                        border = BorderStroke(1.dp, NssEmerald),
                        shadowElevation = 6.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(NssEmerald)
                            )
                            Text(
                                text = "${state?.playerNation?.flagEmoji ?: "🇰🇪"} ${(state?.playerNation?.name ?: "KENYA").uppercase()} (HQ)",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    // Beacon Pin Needle
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(NssEmerald)
                            .border(1.dp, Color.White, CircleShape)
                    )
                }

                // Minor markers for rival nations if available
                state?.diplomacy?.rivals?.take(4)?.forEach { rival ->
                    val rivalCoord = WorldCoordinates.forCode(rival.id, rival.name)
                    if (rivalCoord != playerCoord) {
                        val rx = containerSize.width * rivalCoord.first
                        val ry = containerSize.height * rivalCoord.second
                        Row(
                            modifier = Modifier
                                .offset(
                                    x = (rx - 20f).dp / (containerSize.width / 360f).coerceAtLeast(1f),
                                    y = (ry - 10f).dp / (containerSize.height / 200f).coerceAtLeast(1f)
                                )
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xCC0B131F))
                                .border(0.5.dp, Color(0x66FFFFFF), RoundedCornerShape(4.dp))
                                .clickable { onOpenDiplomacy() }
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(rival.flagEmoji, fontSize = 9.sp)
                            Text(rival.name.take(6), color = Color.LightGray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // On-Map Floating Interactive Zoom / Focus Controls (Top-Center / Right)
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 12.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xEE0B131F))
                .border(1.dp, Color(0x44FFFFFF), RoundedCornerShape(20.dp))
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .clickable {
                        scale = (scale + 0.4f).coerceAtMost(3.8f)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = "Zoom In", tint = Color.White, modifier = Modifier.size(16.dp))
            }

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .clickable {
                        scale = (scale - 0.4f).coerceAtLeast(1.0f)
                        if (scale == 1.0f) panOffset = Offset.Zero
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = Color.White, modifier = Modifier.size(16.dp))
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        // Focus directly on player HQ
                        scale = 2.0f
                        val targetPanX = -(playerCoord.first - 0.5f) * containerSize.width * 1.5f
                        val targetPanY = -(playerCoord.second - 0.5f) * containerSize.height * 1.5f
                        panOffset = Offset(targetPanX, targetPanY)
                        showCountryDetails = true
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.GpsFixed, contentDescription = null, tint = NssEmerald, modifier = Modifier.size(14.dp))
                    Text("FOCUS HQ", color = NssEmerald, fontSize = 9.sp, fontWeight = FontWeight.Black)
                }
            }
        }

        // Floating Country Dossier (Appears when player taps their HQ beacon or "FOCUS HQ")
        AnimatedVisibility(
            visible = showCountryDetails && state != null,
            enter = fadeIn() + androidx.compose.animation.scaleIn(),
            exit = fadeOut() + androidx.compose.animation.scaleOut(),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(16.dp)
        ) {
            state?.let { s ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xF2070E1A),
                    border = BorderStroke(1.5.dp, NssEmerald),
                    shadowElevation = 16.dp,
                    modifier = Modifier.width(320.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(s.playerNation.flagEmoji, fontSize = 24.sp)
                                Column {
                                    Text(s.playerNation.name.uppercase(), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                                    Text("SOVEREIGN COMMAND • YEAR ${s.year}", color = NssEmerald, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .clickable { showCountryDetails = false },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray, modifier = Modifier.size(16.dp))
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("TREASURY", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text(formatCompactMoney(s.vitals.budget), color = NssAccent, fontSize = 14.sp, fontWeight = FontWeight.Black)
                            }
                            Column {
                                Text("MONTHLY NET", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    formatCompactMoney(s.netIncome) + "/mo",
                                    color = if (s.netIncome >= 0) NssEmerald else NssRed,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Column {
                                Text("APPROVAL", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text("${s.vitals.approval.roundToInt()}%", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF131D2D))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Active Military Personnel", color = Color.LightGray, fontSize = 10.sp)
                            Text("${formatCompactMil(s.military.personnel)} Troops", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(NssEmerald)
                                .clickable {
                                    showCountryDetails = false
                                    onOpenDiplomacy()
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("OPEN STRATEGIC DIPLOMACY", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}
