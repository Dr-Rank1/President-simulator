package com.presidentsimulator.game.ui.format

import java.util.Locale

/**
 * Locale-stable numeric formatting for in-game numerals.
 *
 * Uses [Locale.ROOT] so a device set to a comma-decimal locale still renders
 * "5.1B" instead of "5,1B", keeping HUD text consistent across screens.
 */
object Format {

    fun d1(value: Double): String = String.format(Locale.ROOT, "%.1f", value)

    fun d1(value: Float): String = String.format(Locale.ROOT, "%.1f", value)

    fun d2(value: Double): String = String.format(Locale.ROOT, "%.2f", value)

    fun d2(value: Float): String = String.format(Locale.ROOT, "%.2f", value)

    fun d0(value: Double): String = String.format(Locale.ROOT, "%.0f", value)

    fun d0(value: Float): String = String.format(Locale.ROOT, "%.0f", value)

    fun signed1(value: Float): String = String.format(Locale.ROOT, "%+.1f", value)

    fun percent1(value: Float): String = String.format(Locale.ROOT, "%.1f%%", value)

    fun signedBytes(bytes: Int): String = String.format(Locale.ROOT, "%+d", bytes)
}
