package com.presidentsimulator.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.presidentsimulator.game.data.TerritoryStatus
import com.presidentsimulator.game.data.WarOutcome
import com.presidentsimulator.game.ui.theme.NssAccent
import com.presidentsimulator.game.ui.theme.NssBackground
import com.presidentsimulator.game.ui.theme.NssEmerald
import com.presidentsimulator.game.ui.theme.NssForeground
import com.presidentsimulator.game.ui.theme.NssMutedForeground
import com.presidentsimulator.game.ui.theme.NssRed

/**
 * Shown after a victorious war: the defeated nation lies at the player's feet.
 * Choose to annex the land, install a puppet government, or liberate the people —
 * mirroring the Oxiwyle "control conquered lands or grant independence" feature.
 */
@Composable
fun ConquestChoiceDialog(
    outcome: WarOutcome,
    onChoose: (TerritoryStatus) -> Unit,
) {
    Dialog(
        // The fate of the defeated nation MUST be decided: neither tapping the
        // scrim nor pressing system back may dismiss the dialog.
        onDismissRequest = { /* no-op: handled via DialogProperties */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(NssBackground)
                .padding(18.dp),
        ) {
            Text(
                "VICTORY",
                color = NssEmerald,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp,
            )
            Text(
                "The fate of ${outcome.targetName}",
                color = NssForeground,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                "After ${outcome.monthsActive} months of war, ${outcome.targetName} has surrendered. " +
                    "Decide what becomes of its people — the world is watching.",
                color = NssMutedForeground,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                modifier = Modifier.padding(top = 6.dp, bottom = 14.dp),
            )

            ConquestOption(
                title = "ANNEX THE NATION",
                detail = "Claim the land and its industry directly. +industry, but resistance breeds instability.",
                effectColor = NssRed,
                effect = "Instability rises · world relations fall",
                onClick = { onChoose(TerritoryStatus.ANNEXED) },
            )
            ConquestOption(
                title = "INSTALL A PUPPET STATE",
                detail = "Leave local leaders in place under your thumb. Tribute flows home each month.",
                effectColor = NssAccent,
                effect = "+$1.2B monthly tribute · territory counted",
                onClick = { onChoose(TerritoryStatus.PUPPET) },
            )
            ConquestOption(
                title = "LIBERATE THE PEOPLE",
                detail = "Withdraw and grant full independence. Gratitude turns to friendship and applause.",
                effectColor = NssEmerald,
                effect = "+8 approval · allies worldwide · not counted",
                onClick = { onChoose(TerritoryStatus.LIBERATED) },
            )
        }
    }
}

@Composable
private fun ConquestOption(
    title: String,
    detail: String,
    effect: String,
    effectColor: Color,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF111A2B))
            .clickable(onClick = onClick)
            .padding(12.dp),
    ) {
        Text(title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(3.dp))
        Text(detail, color = NssMutedForeground, fontSize = 10.sp, lineHeight = 14.sp)
        Spacer(Modifier.height(4.dp))
        Text(effect, color = effectColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}
