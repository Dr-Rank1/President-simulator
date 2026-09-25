package com.presidentsimulator.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.presidentsimulator.game.ui.theme.*

@Composable
fun ScreenHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(NssBackground)
            .border(1.dp, NssBorder)
            .padding(12.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title.uppercase(),
            color = NssOnPhoto,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 8.sp
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = subtitle.uppercase(),
            color = NssSky,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 8.sp
        )
    }
}
