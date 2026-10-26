package com.presidentsimulator.game.ui.components

import android.content.Context
import androidx.annotation.DrawableRes
import com.presidentsimulator.game.R

object CountryFlagHelper {
    private val explicitFlags = mapOf(
        "us" to R.drawable.united_states_america,
        "usa" to R.drawable.united_states_america,
        "united states" to R.drawable.united_states_america,
        "united states of america" to R.drawable.united_states_america,
        "uk" to R.drawable.united_kingdom,
        "gbr" to R.drawable.united_kingdom,
        "great britain" to R.drawable.united_kingdom,
        "united kingdom" to R.drawable.united_kingdom,
        "the united kingdom" to R.drawable.united_kingdom,
        "russia" to R.drawable.russia,
        "the russian federation" to R.drawable.russia,
        "china" to R.drawable.china,
        "south africa" to R.drawable.south_africa,
        "rsa" to R.drawable.south_africa,
        "south korea" to R.drawable.south_korea,
        "south sudan" to R.drawable.south_sudan,
        "uae" to R.drawable.united_arab_emirates,
        "united arab emirates" to R.drawable.united_arab_emirates,
        "democratic republic of the congo" to R.drawable.democratic_republic_congo,
        "dr congo" to R.drawable.democratic_republic_congo,
        "congo" to R.drawable.republic_congo,
        "bosnia" to R.drawable.bosnia_and_herzegovina,
        "bosnia and herzegovina" to R.drawable.bosnia_and_herzegovina,
        "ivory coast" to R.drawable.ivory_coast,
        "côte d'ivoire" to R.drawable.ivory_coast,
        "cote d'ivoire" to R.drawable.ivory_coast,
        "czech republic" to R.drawable.czech_republic,
        "czechia" to R.drawable.czech_republic,
    )

    @DrawableRes
    fun getFlagResId(context: Context, nationId: String, nationName: String): Int {
        val lowerId = nationId.lowercase().trim()
        val lowerName = nationName.lowercase().trim()

        explicitFlags[lowerId]?.let { return it }
        explicitFlags[lowerName]?.let { return it }

        val cleanName = lowerName.replace(" ", "_").replace("-", "_").replace("'", "")
        val res = context.resources.getIdentifier(cleanName, "drawable", context.packageName)
        if (res != 0) return res

        val resId = context.resources.getIdentifier(lowerId, "drawable", context.packageName)
        if (resId != 0) return resId

        return 0
    }

    @DrawableRes
    fun getFlagResource(countryName: String): Int {
        val lowerName = countryName.lowercase().trim()
        explicitFlags[lowerName]?.let { return it }
        return when (lowerName) {
            "australia" -> R.drawable.australia
            "austria" -> R.drawable.austria
            "algeria" -> R.drawable.algeria
            "angola" -> R.drawable.angola
            "argentina" -> R.drawable.argentina
            "brazil" -> R.drawable.brazil
            "canada" -> R.drawable.canada
            "france" -> R.drawable.france
            "germany" -> R.drawable.germany
            "india" -> R.drawable.india
            "japan" -> R.drawable.japan
            "kenya" -> R.drawable.kenya
            "mexico" -> R.drawable.mexico
            "nigeria" -> R.drawable.nigeria
            "poland" -> R.drawable.poland
            "saudi arabia" -> R.drawable.saudi_arabia
            "turkey" -> R.drawable.turkey
            "ukraine" -> R.drawable.ukraine
            else -> 0
        }
    }
}
