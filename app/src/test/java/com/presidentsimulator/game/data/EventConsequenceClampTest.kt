package com.presidentsimulator.game.data

import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Property-style coverage (§4.3 of the improvement roadmap): every choice of every
 * event — pool and story-arc alike — must keep a boundary state inside its clamps,
 * no matter how extreme the incoming stats are.
 */
class EventConsequenceClampTest {

    private val storyEvents: List<GameEvent> =
        listOf("cabinet_scandal", "confidence_vote", "policy_backlash").flatMap { arc ->
            (1..3).mapNotNull { chapter -> StoryArcEngine.eventById("story_${arc}_$chapter") }
        }

    private val allEvents: List<GameEvent> = EventRepository.eventPool + storyEvents

    /** State pushed against the floor of every clamp the consequence touches. */
    private val floorState = GameState(
        vitals = VitalsState(budget = 0L, approval = 0f, population = 1_000_000L),
        economy = EconomyState(factories = 0, farms = 0, housing = 0),
        military = MilitaryState(personnel = 0L, defcon = 1),
        internalSecurity = InternalSecurityState(instabilityScore = 0f, coupRisk = 0f),
        research = ResearchState(sciencePoints = 0L),
        press = PressState(mediaSentiment = 0f, credibility = 0f),
        cabinet = CabinetState(cohesion = 0f),
        opposition = OppositionState(noConfidenceHeat = 0f),
    )

    /** State pushed against the ceiling of every clamp. */
    private val ceilingState = GameState(
        vitals = VitalsState(budget = 1_000_000_000_000L, approval = 100f, population = 5_000_000_000L),
        economy = EconomyState(factories = 500, farms = 500, housing = 500),
        military = MilitaryState(personnel = 10_000_000L, defcon = 5),
        internalSecurity = InternalSecurityState(instabilityScore = 100f, coupRisk = 100f),
        research = ResearchState(sciencePoints = 1_000_000_000_000L),
        press = PressState(mediaSentiment = 100f, credibility = 100f),
        cabinet = CabinetState(cohesion = 100f),
        opposition = OppositionState(noConfidenceHeat = 100f),
    )

    @Test
    fun everyEventChoiceKeepsBoundaryStatesClamped() {
        assertTrue("expected a healthy event pool", allEvents.size >= 30)

        listOf(floorState, ceilingState).forEach { origin ->
            allEvents.forEach { event ->
                event.choices.forEachIndexed { index, choice ->
                    val label = "${event.id} choice #$index"
                    val after = choice.consequence.applyTo(origin)

                    assertTrue("$label approval ${after.vitals.approval}", after.vitals.approval in 0f..100f)
                    assertTrue("$label population", after.vitals.population >= 1_000_000L)
                    assertTrue("$label factories", after.economy.factories >= 0)
                    assertTrue("$label farms", after.economy.farms >= 0)
                    assertTrue("$label housing", after.economy.housing >= 0)
                    assertTrue("$label personnel", after.military.personnel >= 0L)
                    assertTrue("$label defcon", after.military.defcon in 1..5)
                    assertTrue(
                        "$label instability",
                        after.internalSecurity.instabilityScore in 0f..100f,
                    )
                    assertTrue("$label science", after.research.sciencePoints >= 0L)
                    assertTrue("$label sentiment", after.press.mediaSentiment in 0f..100f)
                    assertTrue("$label credibility", after.press.credibility in 0f..100f)
                    assertTrue("$label cohesion", after.cabinet.cohesion in 0f..100f)
                    assertTrue(
                        "$label opposition heat",
                        after.opposition.noConfidenceHeat in 0f..100f,
                    )
                    after.diplomacy.rivals.forEach { rival ->
                        assertTrue(
                            "$label relationship ${rival.relationshipScore}",
                            rival.relationshipScore in -100..100,
                        )
                    }
                }
            }
        }
    }

    @Test
    fun eventPoolIsDiscoverableById() {
        EventRepository.eventPool.forEach { event ->
            assertTrue(
                "byId must resolve pool event ${event.id}",
                EventRepository.byId(event.id) != null,
            )
        }
        storyEvents.forEach { event ->
            assertTrue(
                "byId must resolve story event ${event.id}",
                EventRepository.byId(event.id) != null,
            )
        }
    }
}
