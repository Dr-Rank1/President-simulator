package com.presidentsimulator.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.TradeCommodity
import com.presidentsimulator.game.ui.theme.NssBackground
import com.presidentsimulator.game.ui.theme.NssEmerald
import com.presidentsimulator.game.ui.theme.NssForeground
import com.presidentsimulator.game.ui.theme.NssMutedForeground
import com.presidentsimulator.game.ui.theme.NssRed
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * The "shop" behind the HUD cart icon: the global commodity exchange.
 * Buy bundles of oil/steel/grain/goods at the live price (+import tariff),
 * or sell from national stockpiles at the live price.
 */
@Composable
fun MarketShopDialog(
    state: GameState,
    priceOf: (TradeCommodity) -> Long,
    stockOf: (TradeCommodity) -> Long,
    tariffRate: Float,
    onBuy: (TradeCommodity) -> Unit,
    onSell: (TradeCommodity) -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(NssBackground)
                .padding(16.dp),
        ) {
            Text(
                "GLOBAL EXCHANGE",
                color = NssEmerald,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp,
            )
            Text(
                "Buy what the nation lacks. Sell what it has in surplus.",
                color = NssMutedForeground,
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 2.dp, bottom = 10.dp),
            )

            TradeCommodity.entries.forEach { commodity ->
                val quote = state.market.quote(commodity)
                val price = priceOf(commodity)
                val stock = stockOf(commodity)
                val up = quote.isTrendingUp
                val deltaPct = if (quote.previousPrice > 0) {
                    ((quote.currentPrice - quote.previousPrice) * 100f / quote.previousPrice).roundToInt()
                } else {
                    0
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF111A2B))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "${commodity.iconEmoji} ${commodity.displayName}",
                            color = NssForeground,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                        )
                        Text(
                            "${formatCompactMoney(price)} / unit${if (tariffRate > 0f) " · +${(tariffRate * 100).roundToInt()}% tariff" else ""}",
                            color = NssMutedForeground,
                            fontSize = 9.sp,
                        )
                        Text(
                            "Stockpile: ${formatCompactNumber(stock)}",
                            color = Color(0xFF8ECBFF),
                            fontSize = 9.sp,
                        )
                    }
                    Text(
                        if (up) "▲ $deltaPct%" else "▼ ${abs(deltaPct)}%",
                        color = if (up) NssEmerald else NssRed,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    ShopButton(
                        label = "BUY",
                        color = NssEmerald,
                        onClick = { onBuy(commodity) },
                    )
                    Spacer(Modifier.width(6.dp))
                    ShopButton(
                        label = "SELL",
                        color = NssRed,
                        enabled = stock > 0L,
                        onClick = { onSell(commodity) },
                    )
                }
            }

            Text(
                "Trades ${TradeShopBundle.SIZE} units · price moves with your trades and world events",
                color = NssMutedForeground,
                fontSize = 8.sp,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun ShopButton(
    label: String,
    color: Color,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (enabled) color else color.copy(alpha = 0.25f))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            color = Color.White,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Every shop trade moves [TradeShopBundle.SIZE] units, matching the spot-market bundle. */
object TradeShopBundle {
    const val SIZE = 50L
}

/** Compact stock formatting: 12,345 -> "12.3K". */
private fun formatCompactNumber(value: Long): String =
    when {
        value >= 1_000_000L -> String.format(java.util.Locale.ROOT, "%.1fM", value / 1_000_000.0)
        value >= 1_000L -> String.format(java.util.Locale.ROOT, "%.1fK", value / 1_000.0)
        else -> value.toString()
    }
