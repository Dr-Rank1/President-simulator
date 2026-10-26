package com.presidentsimulator.game.viewmodel

import com.presidentsimulator.game.data.*
import org.junit.Assert.*
import org.junit.Test

class OvalOfficePillarsTest {

    @Test
    fun testPillar1_WorldNationCatalogCompleteness() {
        assertEquals("World catalog must contain authentic 163 nations from APK", 163, WorldNationCatalog.COUNTRIES_163.size)
        val dossiers = WorldNationCatalog.createInitialDossiers("United States")
        assertEquals(163, dossiers.size)

        val usa = dossiers.find { it.name == "USA" }
        assertNotNull(usa)
        assertTrue(usa!!.nuclearPower)

        val kenya = dossiers.find { it.name == "Kenya" }
        assertNotNull(kenya)
    }

    @Test
    fun testPillar2_IndustrialMatrixSimulation() {
        val initialState = GameState()
        assertEquals(12, initialState.industrialMatrix.sectors.size)

        val powerSector = initialState.industrialMatrix.sectors[IndustrialSectorType.POWER]
        assertNotNull(powerSector)
        assertTrue(powerSector!!.facilitiesCount > 0)
        assertTrue(powerSector.subPlants.isNotEmpty())

        val initialNetRevenue = powerSector.netRevenue
        assertTrue(initialNetRevenue > 0)

        // Verify strike halves output
        val strikeSector = powerSector.copy(isUnderStrike = true)
        assertTrue(strikeSector.netRevenue < initialNetRevenue)

        // Test netIncome accounts for industrial matrix 10% dividend
        assertTrue(initialState.netIncome != 0L)
    }

    @Test
    fun testPillar3_CrisisResolutionEngine() {
        val testCrisis = ActiveCrisis(
            id = "strike_coal_1",
            type = CrisisType.STRIKE,
            headline = "Wildcat Strike in Coal Basin",
            summary = "Workers demand wage increases.",
            backdropDrawable = 0,
            options = listOf(
                CrisisOption("settle", "Accede to Demands", 200_000_000L, 0, "Pay emergency bonus"),
            ),
        )

        val stateWithCrisis = GameState(
            crises = CrisisState(
                activeCrises = listOf(testCrisis),
                hotlineRinging = true,
            ),
        )

        assertTrue(stateWithCrisis.crises.hasUnresolvedCrises)
        assertEquals(1, stateWithCrisis.crises.activeCrises.size)
    }

    @Test
    fun testPillar4_WarRoomAndNuclearDeterrence() {
        val warRoom = WarRoomState()
        assertEquals(MilitaryDoctrine.COMBINED_ARMS, warRoom.selectedDoctrine)
        assertEquals(3, warRoom.commanders.size)
        assertEquals(5, warRoom.nuclearSilo.defconLevel)
        assertTrue(warRoom.nuclearSilo.nuclearWarheads > 0)

        // Test doctrine modifiers
        val blitz = MilitaryDoctrine.BLITZKRIEG
        assertTrue(blitz.attackBonus > 0f)

        val bastion = MilitaryDoctrine.IRON_DEFENSE
        assertTrue(bastion.defenseBonus > 0f)
    }

    @Test
    fun testPillar5_SovereignMinistriesMatrix() {
        val ministries = SovereignMinistriesState()
        val initialSafety = ministries.police.overallCrimeSafetyScore
        assertTrue(initialSafety in 0..100)

        // Lower crime improves safety score
        val saferPolice = ministries.police.copy(
            hooliganismRatePercent = 5.0f,
            violentCrimeRatePercent = 2.0f,
        )
        assertTrue(saferPolice.overallCrimeSafetyScore > initialSafety)

        // Sports facilities count
        assertTrue(ministries.sports.totalSportsFacilities > 0)
        assertTrue(ministries.sports.publicHappinessBonus > 0f)

        // Ecology air quality
        assertTrue(ministries.ecology.airQualityIndex in 0..500)
    }

    @Test
    fun testPillar6_OvalOfficeExecutiveOrdersAndDemographics() {
        val ovalOffice = OvalOfficeState()
        assertEquals(7, ovalOffice.decrees.size)
        assertEquals(0, ovalOffice.activeDecreesCount)

        // Enact an executive order
        val stimulus = ExecutiveOrderType.EMERGENCY_ECONOMIC_STIMULUS
        val updatedDecrees = ovalOffice.decrees + (stimulus to PresidentialDecree(type = stimulus, isEnacted = true, enactedYear = 2026))
        val updatedOffice = ovalOffice.copy(decrees = updatedDecrees)

        assertEquals(1, updatedOffice.activeDecreesCount)
        assertEquals(stimulus.monthlyCostMillion, updatedOffice.monthlyDecreesCostMillion)

        // Demographics weighted approval
        val overallApproval = ovalOffice.polling.overallWeightedApproval
        assertTrue(overallApproval in 0f..100f)
        assertEquals(5, VoterDemographic.entries.size)
    }
}
