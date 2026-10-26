package com.presidentsimulator.game.ui.components

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.presidentsimulator.game.R

object CountryFlagHelper {
    val explicitFlags: Map<String, Int> = mapOf(
        "afghanistan" to R.drawable.afghanistan,
        "albania" to R.drawable.albania,
        "algeria" to R.drawable.algeria,
        "angola" to R.drawable.angola,
        "argentina" to R.drawable.argentina,
        "armenia" to R.drawable.armenia,
        "aurum coast" to R.drawable.australia,
        "aurumcoast" to R.drawable.australia,
        "australia" to R.drawable.australia,
        "austria" to R.drawable.austria,
        "azerbaijan" to R.drawable.azerbaijan,
        "bangladesh" to R.drawable.bangladesh,
        "belarus" to R.drawable.belarus,
        "belgium" to R.drawable.belgium,
        "belize" to R.drawable.belize,
        "benin" to R.drawable.benin,
        "bhutan" to R.drawable.bhutan,
        "bolivia" to R.drawable.bolivia,
        "bosnia" to R.drawable.bosnia_and_herzegovina,
        "bosnia and herzegovina" to R.drawable.bosnia_and_herzegovina,
        "bosniaand herzegovina" to R.drawable.bosniaandherzegovina,
        "bosniaandherzegovina" to R.drawable.bosniaandherzegovina,
        "botswana" to R.drawable.botswana,
        "brazil" to R.drawable.brazil,
        "bulgaria" to R.drawable.bulgaria,
        "burkina faso" to R.drawable.burkinafaso,
        "burkinafaso" to R.drawable.burkinafaso,
        "burundi" to R.drawable.burundi,
        "c a r" to R.drawable.carcountry,
        "cambodia" to R.drawable.cambodia,
        "cameroon" to R.drawable.cameroon,
        "canada" to R.drawable.canada,
        "car" to R.drawable.carcountry,
        "central african republic" to R.drawable.carcountry,
        "chad" to R.drawable.chad,
        "chile" to R.drawable.chile,
        "china" to R.drawable.china,
        "colombia" to R.drawable.colombia,
        "congo" to R.drawable.republic_congo,
        "costa rica" to R.drawable.costa_rica,
        "costarica" to R.drawable.costa_rica,
        "cote d'ivoire" to R.drawable.ivory_coast,
        "croatia" to R.drawable.croatia,
        "cuba" to R.drawable.cuba,
        "cyprus" to R.drawable.cyprus,
        "czech republic" to R.drawable.czech_republic,
        "d r c" to R.drawable.democratic_republic_congo,
        "democratic republic of the congo" to R.drawable.democratic_republic_congo,
        "denmark" to R.drawable.denmark,
        "djibouti" to R.drawable.djibouti,
        "dominican republic" to R.drawable.dominican_republic,
        "drc" to R.drawable.democratic_republic_congo,
        "east timor" to R.drawable.east_timor,
        "eastmark" to R.drawable.poland,
        "eastmark republic" to R.drawable.poland,
        "easttimor" to R.drawable.east_timor,
        "ecuador" to R.drawable.ecuador,
        "egypt" to R.drawable.egypt,
        "el salvador" to R.drawable.salvador,
        "equatorial guinea" to R.drawable.equatorial_guinea,
        "equatorialguinea" to R.drawable.equatorial_guinea,
        "eritrea" to R.drawable.eritrea,
        "estonia" to R.drawable.estonia,
        "ethiopia" to R.drawable.ethiopia,
        "finland" to R.drawable.finland,
        "france" to R.drawable.france,
        "gabon" to R.drawable.gabon,
        "gambia" to R.drawable.gambia,
        "georgia" to R.drawable.georgia,
        "germany" to R.drawable.germany,
        "ghana" to R.drawable.ghana,
        "greece" to R.drawable.greece,
        "greenland" to R.drawable.greenland,
        "guatemala" to R.drawable.guatemala,
        "guiana" to R.drawable.guiana,
        "guinea" to R.drawable.guinea,
        "guinea bissau" to R.drawable.guinea_bissau,
        "guinea- bissau" to R.drawable.guinea_bissau,
        "guinea-bissau" to R.drawable.guinea_bissau,
        "guyana" to R.drawable.guyana,
        "haiti" to R.drawable.haiti,
        "honduras" to R.drawable.honduras,
        "hungary" to R.drawable.hungary,
        "iceland" to R.drawable.iceland,
        "india" to R.drawable.india,
        "indonesia" to R.drawable.indonesia,
        "iran" to R.drawable.iran,
        "iraq" to R.drawable.iraq,
        "ireland" to R.drawable.ireland,
        "israel" to R.drawable.israel,
        "italy" to R.drawable.italy,
        "ivory coast" to R.drawable.ivory_coast,
        "ivorycoast" to R.drawable.ivory_coast,
        "jamaica" to R.drawable.jamaica,
        "japan" to R.drawable.japan,
        "jordan" to R.drawable.jordan,
        "kazakhstan" to R.drawable.kazakhstan,
        "kenya" to R.drawable.kenya,
        "kryos" to R.drawable.iceland,
        "kryos directorate" to R.drawable.iceland,
        "kuwait" to R.drawable.kuwait,
        "kyrgyzstan" to R.drawable.kyrgyzstan,
        "laos" to R.drawable.laos,
        "latvia" to R.drawable.latvia,
        "lebanon" to R.drawable.lebanon,
        "lesotho" to R.drawable.lesotho,
        "liberia" to R.drawable.liberia,
        "libya" to R.drawable.libya,
        "lithuania" to R.drawable.lithuania,
        "luxembourg" to R.drawable.luxembourg,
        "macedonia" to R.drawable.macedonia,
        "madagascar" to R.drawable.madagascar,
        "malawi" to R.drawable.malawi,
        "malaysia" to R.drawable.malaysia,
        "mali" to R.drawable.mali,
        "mauritania" to R.drawable.mauritania,
        "mexico" to R.drawable.mexico,
        "moldova" to R.drawable.moldova,
        "mongolia" to R.drawable.mongolia,
        "montenegro" to R.drawable.montenegro,
        "morocco" to R.drawable.morocco,
        "mozambique" to R.drawable.mozambique,
        "myanmar" to R.drawable.myanmar,
        "namibia" to R.drawable.namibia,
        "nepal" to R.drawable.nepal,
        "netherlands" to R.drawable.netherlands,
        "new zealand" to R.drawable.new_zealand,
        "newzealand" to R.drawable.new_zealand,
        "nicaragua" to R.drawable.nicaragua,
        "niger" to R.drawable.niger,
        "nigeria" to R.drawable.nigeria,
        "north korea" to R.drawable.north_korea,
        "northkorea" to R.drawable.north_korea,
        "northland" to R.drawable.sweden,
        "northland federation" to R.drawable.sweden,
        "norway" to R.drawable.norway,
        "oman" to R.drawable.oman,
        "pakistan" to R.drawable.pakistan,
        "panama" to R.drawable.panama,
        "papua - new guinea" to R.drawable.papua_new_guinea,
        "papua new guinea" to R.drawable.papua_new_guinea,
        "paraguay" to R.drawable.paraguay,
        "peru" to R.drawable.peru,
        "philippines" to R.drawable.philippines,
        "poland" to R.drawable.poland,
        "portugal" to R.drawable.portugal,
        "qatar" to R.drawable.qatar,
        "republic of the congo" to R.drawable.republic_congo,
        "romania" to R.drawable.romania,
        "rsa" to R.drawable.south_africa,
        "russia" to R.drawable.russia,
        "rwanda" to R.drawable.rwanda,
        "saudi arabia" to R.drawable.saudi_arabia,
        "saudiarabia" to R.drawable.saudi_arabia,
        "senegal" to R.drawable.senegal,
        "serbia" to R.drawable.serbia,
        "sierra leone" to R.drawable.sierra_leone,
        "sierraleone" to R.drawable.sierra_leone,
        "slovakia" to R.drawable.slovakia,
        "slovenia" to R.drawable.slovenia,
        "somalia" to R.drawable.somalia,
        "south africa" to R.drawable.south_africa,
        "south korea" to R.drawable.south_korea,
        "south sudan" to R.drawable.south_sudan,
        "southafrica" to R.drawable.south_africa,
        "southern sudan" to R.drawable.south_sudan,
        "southkorea" to R.drawable.south_korea,
        "southreach" to R.drawable.south_africa,
        "southreach union" to R.drawable.south_africa,
        "spain" to R.drawable.spain,
        "sri lanka" to R.drawable.sri_lanka,
        "srilanka" to R.drawable.sri_lanka,
        "sudan" to R.drawable.sudan,
        "suriname" to R.drawable.suriname,
        "swaziland" to R.drawable.swaziland,
        "sweden" to R.drawable.sweden,
        "switzerland" to R.drawable.switzerland,
        "syria" to R.drawable.syria,
        "taiwan" to R.drawable.taiwan,
        "tajikistan" to R.drawable.tajikistan,
        "tanzania" to R.drawable.tanzania,
        "thailand" to R.drawable.thailand,
        "the czech republic" to R.drawable.czech_republic,
        "the dominican republic" to R.drawable.dominican_republic,
        "the netherlands" to R.drawable.netherlands,
        "the philippines" to R.drawable.philippines,
        "the uae" to R.drawable.united_arab_emirates,
        "the united kingdom" to R.drawable.united_kingdom,
        "togo" to R.drawable.togo,
        "tunisia" to R.drawable.tunisia,
        "turkey" to R.drawable.turkey,
        "turkmenistan" to R.drawable.turkmenistan,
        "u s a" to R.drawable.united_states_america,
        "uae" to R.drawable.united_arab_emirates,
        "uganda" to R.drawable.uganda,
        "ukraine" to R.drawable.ukraine,
        "united arab emirates" to R.drawable.united_arab_emirates,
        "united kingdom" to R.drawable.united_kingdom,
        "united states" to R.drawable.united_states_america,
        "unitedarabemirates" to R.drawable.united_arab_emirates,
        "unitedkingdom" to R.drawable.united_kingdom,
        "uruguay" to R.drawable.uruguay,
        "us" to R.drawable.united_states_america,
        "usa" to R.drawable.united_states_america,
        "uzbekistan" to R.drawable.uzbekistan,
        "veltra" to R.drawable.united_states_america,
        "venezuela" to R.drawable.venezuela,
        "verdehaan" to R.drawable.ireland,
        "verdehaan compact" to R.drawable.ireland,
        "vietnam" to R.drawable.vietnam,
        "western sahara" to R.drawable.westsahara,
        "westoria" to R.drawable.united_kingdom,
        "westoria empire" to R.drawable.united_kingdom,
        "yemen" to R.drawable.yemen,
        "zambia" to R.drawable.zambia,
        "zimbabwe" to R.drawable.zimbabwe,
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

        return R.drawable.all_country
    }

    @DrawableRes
    fun getFlagResource(countryName: String): Int {
        val lowerName = countryName.lowercase().trim()
        explicitFlags[lowerName]?.let { return it }
        val cleanName = lowerName.replace("the ", "").replace(" ", "_").replace("-", "_").replace("'", "")
        explicitFlags[cleanName]?.let { return it }
        return R.drawable.all_country
    }
}

@Composable
fun CountryFlag(
    countryName: String,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(2.dp),
    border: BorderStroke? = BorderStroke(0.5.dp, Color(0xFF64748B)),
) {
    val flagRes = CountryFlagHelper.getFlagResource(countryName)
    Image(
        painter = painterResource(id = flagRes),
        contentDescription = countryName,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .clip(shape)
            .then(if (border != null) Modifier.border(border, shape) else Modifier),
    )
}
