package com.presidentsimulator.game.data

import com.presidentsimulator.game.ui.components.CountryFlagHelper

/**
 * Authentic 163-nation catalog reverse-engineered directly from R.array.country in the original APK.
 * Matches 1:1 with mappath.txt vector polygon lines (index 0 to 162).
 */
data class WorldCountryDossier(
    val id: Int, // 0 to 162 matching mappath.txt line
    val name: String,
    val flagEmoji: String,
    val flagRes: Int,
    val capital: String,
    val population: Long,
    val gdpBillion: Long,
    val activeTroops: Long,
    val tanks: Int,
    val jets: Int,
    val nuclearPower: Boolean,
    val ideology: Ideology,
    val religion: StateReligion,
    val relationshipScore: Int = 50, // 0 to 100
    val hasEmbassy: Boolean = false,
    val nonAggressionPact: Boolean = false,
    val mutualDefensePact: Boolean = false,
    val tradeDealActive: Boolean = false,
    val atWarWithPlayer: Boolean = false,
    val isPuppetOrAnnexed: Boolean = false,
)

object WorldNationCatalog {

    val COUNTRIES_163: List<String> = listOf(
        "Australia", "Austria", "Azerbaijan", "Albania", "Algeria", "Angola", "Argentina", "Armenia",
        "Afghanistan", "Bangladesh", "Belize", "Belarus", "Belgium", "Benin", "Bulgaria", "Bolivia",
        "Bosnia and Herzegovina", "Botswana", "Brazil", "Burkina Faso", "Burundi", "Bhutan",
        "United Kingdom", "Hungary", "Venezuela", "East Timor", "Vietnam", "Gabon", "Haiti",
        "Guyana", "Gambia", "Ghana", "Guatemala", "Guiana", "Guinea", "Guinea-Bissau", "Germany",
        "Honduras", "Greenland", "Greece", "Georgia", "Denmark", "Djibouti", "The Dominican Republic",
        "Egypt", "Zambia", "Zimbabwe", "Israel", "India", "Indonesia", "Jordan", "Iraq", "Iran",
        "Ireland", "Iceland", "Spain", "Italy", "Yemen", "Kazakhstan", "Cambodia", "Cameroon",
        "Canada", "Qatar", "Kenya", "Cyprus", "Kyrgyzstan", "China", "Taiwan", "Colombia", "DRC",
        "Congo", "North Korea", "South Korea", "Costa Rica", "Ivory Coast", "Cuba", "Kuwait", "Laos",
        "Latvia", "Lesotho", "Liberia", "Lebanon", "Libya", "Lithuania", "Luxembourg", "Mauritania",
        "Madagascar", "Macedonia", "Malawi", "Malaysia", "Mali", "Morocco", "Mexico", "Mozambique",
        "Moldova", "Mongolia", "Myanmar", "Namibia", "Nepal", "Niger", "Nigeria", "Netherlands",
        "Nicaragua", "New Zealand", "Norway", "United Arab Emirates", "Oman", "Pakistan", "Panama",
        "Papua - New Guinea", "Paraguay", "Peru", "Poland", "Portugal", "Russia", "Rwanda", "Romania",
        "El Salvador", "Saudi Arabia", "Western Sahara", "Swaziland", "Senegal", "Serbia", "Syria",
        "Slovakia", "Slovenia", "USA", "Somalia", "Sudan", "Suriname", "Sierra Leone", "Tajikistan",
        "Thailand", "Tanzania", "Togo", "Tunisia", "Turkmenistan", "Turkey", "Uganda", "Uzbekistan",
        "Ukraine", "Uruguay", "The Philippines", "Finland", "France", "Croatia", "CAR", "Chad",
        "Montenegro", "The Czech Republic", "Chile", "Switzerland", "Sweden", "Sri Lanka", "Ecuador",
        "Equatorial Guinea", "Eritrea", "Estonia", "Ethiopia", "South Africa", "Southern Sudan",
        "Jamaica", "Japan"
    )

    fun createInitialDossiers(playerCountryName: String = "Kenya"): List<WorldCountryDossier> {
        return COUNTRIES_163.mapIndexed { index, countryName ->
            val flagRes = CountryFlagHelper.getFlagResource(countryName)
            val isPlayer = countryName.equals(playerCountryName, ignoreCase = true)
            val isMajorPower = countryName in listOf("USA", "Russia", "China", "United Kingdom", "France", "Germany", "Japan", "India")
            val isNuclear = countryName in listOf("USA", "Russia", "China", "United Kingdom", "France", "India", "Pakistan", "North Korea", "Israel")

            val basePop = when (countryName) {
                "China", "India" -> 1_400_000_000L
                "USA" -> 335_000_000L
                "Indonesia" -> 275_000_000L
                "Pakistan" -> 240_000_000L
                "Nigeria" -> 220_000_000L
                "Brazil" -> 215_000_000L
                "Russia" -> 145_000_000L
                "Mexico" -> 128_000_000L
                "Japan" -> 125_000_000L
                "Germany" -> 84_000_000L
                "United Kingdom", "France" -> 67_000_000L
                "Kenya" -> 54_000_000L
                else -> (5_000_000L + (index * 732_100L) % 70_000_000L)
            }

            val baseGdp = when (countryName) {
                "USA" -> 27_000L
                "China" -> 18_000L
                "Germany" -> 4_500L
                "Japan" -> 4_200L
                "India" -> 3_750L
                "United Kingdom" -> 3_300L
                "France" -> 3_000L
                "Russia" -> 2_000L
                "Canada", "Brazil", "Italy" -> 2_100L
                "Kenya" -> 115L
                else -> (20L + (index * 37L) % 600L)
            }

            val troops = when {
                countryName == "China" -> 2_000_000L
                countryName == "India" -> 1_450_000L
                countryName == "USA" -> 1_390_000L
                countryName == "North Korea" -> 1_280_000L
                countryName == "Russia" -> 1_150_000L
                isMajorPower -> 300_000L
                else -> (30_000L + (index * 1_400L) % 250_000L)
            }

            val tanksCount = when {
                countryName == "Russia" -> 12_000
                countryName == "USA" -> 6_000
                countryName == "China" -> 5_200
                isMajorPower -> 800
                else -> (50 + (index * 13) % 400)
            }

            val jetsCount = when {
                countryName == "USA" -> 3_200
                countryName == "Russia" -> 1_800
                countryName == "China" -> 1_900
                isMajorPower -> 450
                else -> (20 + (index * 7) % 150)
            }

            val ideology = when (countryName) {
                "China", "Cuba", "Vietnam", "Laos", "North Korea" -> Ideology.COMMUNISM
                "Saudi Arabia", "Oman", "Qatar", "Kuwait", "Russia", "Iran", "Syria", "Belarus" -> Ideology.AUTOCRACY
                else -> Ideology.DEMOCRACY
            }

            val religion = when (countryName) {
                "Saudi Arabia", "Iran", "Egypt", "Pakistan", "Indonesia", "Turkey", "Iraq", "Algeria", "Morocco" -> StateReligion.TRADITIONAL
                "India", "Nepal", "Thailand", "Cambodia", "Myanmar", "Bhutan", "Sri Lanka", "Japan" -> StateReligion.PLURALIST
                "North Korea" -> StateReligion.STATE_CULT
                else -> StateReligion.SECULAR
            }

            val flagEmoji = when (countryName) {
                "USA" -> "🇺🇸"
                "United Kingdom" -> "🇬🇧"
                "China" -> "🇨🇳"
                "Russia" -> "🇷🇺"
                "Germany" -> "🇩🇪"
                "France" -> "🇫🇷"
                "Japan" -> "🇯🇵"
                "India" -> "🇮🇳"
                "Kenya" -> "🇰🇪"
                "Brazil" -> "🇧🇷"
                "Canada" -> "🇨🇦"
                "Australia" -> "🇦🇺"
                else -> "🌐"
            }

            WorldCountryDossier(
                id = index,
                name = countryName,
                flagEmoji = flagEmoji,
                flagRes = flagRes,
                capital = getCapital(countryName),
                population = basePop,
                gdpBillion = baseGdp,
                activeTroops = troops,
                tanks = tanksCount,
                jets = jetsCount,
                nuclearPower = isNuclear,
                ideology = ideology,
                religion = religion,
                relationshipScore = if (isPlayer) 100 else (40 + (index * 17) % 55),
                hasEmbassy = isMajorPower && !isPlayer,
                nonAggressionPact = false,
                mutualDefensePact = false,
                tradeDealActive = isMajorPower && !isPlayer,
                atWarWithPlayer = false,
                isPuppetOrAnnexed = false,
            )
        }
    }

    private fun getCapital(name: String): String = when (name) {
        "USA" -> "Washington, D.C."
        "United Kingdom" -> "London"
        "China" -> "Beijing"
        "Russia" -> "Moscow"
        "Germany" -> "Berlin"
        "France" -> "Paris"
        "Japan" -> "Tokyo"
        "India" -> "New Delhi"
        "Kenya" -> "Nairobi"
        "Brazil" -> "Brasília"
        "Canada" -> "Ottawa"
        "Australia" -> "Canberra"
        "Egypt" -> "Cairo"
        "South Africa" -> "Pretoria"
        "Nigeria" -> "Abuja"
        "Italy" -> "Rome"
        "Spain" -> "Madrid"
        "Turkey" -> "Ankara"
        "Saudi Arabia" -> "Riyadh"
        "South Korea" -> "Seoul"
        "North Korea" -> "Pyongyang"
        "Ukraine" -> "Kyiv"
        "Israel" -> "Jerusalem"
        else -> "$name Capital"
    }
}
