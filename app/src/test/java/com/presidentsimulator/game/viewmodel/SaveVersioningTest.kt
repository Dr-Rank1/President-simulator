package com.presidentsimulator.game.viewmodel

import com.presidentsimulator.game.data.GameState
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class SaveVersioningTest {

    private val engine = AnalyticsSaveEngine()
    private val editing = Json { ignoreUnknownKeys = true }

    private fun payloadOf(state: GameState): String = engine.exportGameStateToJson(state)

    private fun rewrite(json: String, transform: (MutableMap<String, kotlinx.serialization.json.JsonElement>) -> Unit): String {
        val obj = editing.parseToJsonElement(json).jsonObject.toMutableMap()
        transform(obj)
        return JsonObject(obj).toString()
    }

    @Test
    fun roundTripPreservesVersionAndSeed() {
        val original = GameState.initial("us").copy(campaignSeed = 4242L)
        val restored = engine.importGameStateFromJson(payloadOf(original))

        assertEquals(GameState.SAVE_VERSION, restored.saveVersion)
        assertEquals(4242L, restored.campaignSeed)
        assertEquals(original.year, restored.year)
        assertEquals(original.month, restored.month)
        assertEquals(original.playerNation.id, restored.playerNation.id)
    }

    @Test
    fun payloadWrittenBeforeVersioningStillLoads() {
        val json = rewrite(payloadOf(GameState.initial("de").copy(campaignSeed = 77L))) { fields ->
            fields.remove(AnalyticsSaveEngine.SAVE_VERSION_KEY)
        }

        val restored = engine.importGameStateFromJson(json)

        // Migrated forward so the next save stamps the current schema.
        assertEquals(GameState.SAVE_VERSION, restored.saveVersion)
        // An explicit seed survives migration untouched.
        assertEquals(77L, restored.campaignSeed)
    }

    @Test
    fun legacyPayloadWithoutAnySeedGetsAFixedFallback() {
        val json = rewrite(payloadOf(GameState.initial("fr"))) { fields ->
            fields.remove(AnalyticsSaveEngine.SAVE_VERSION_KEY)
            fields.remove("campaignSeed")
        }

        val restored = engine.importGameStateFromJson(json)

        assertEquals(GameState.SAVE_VERSION, restored.saveVersion)
        assertEquals(AnalyticsSaveEngine.LEGACY_SEED_FALLBACK, restored.campaignSeed)
        // Deterministic: the same legacy payload always maps to the same seed.
        assertEquals(restored.campaignSeed, engine.importGameStateFromJson(json).campaignSeed)
    }

    @Test
    fun savesFromANewerBuildAreRejectedWithAReadableReason() {
        val json = rewrite(payloadOf(GameState.initial("us"))) { fields ->
            fields["saveVersion"] = JsonPrimitive(GameState.SAVE_VERSION + 5)
        }

        try {
            engine.importGameStateFromJson(json)
            fail("a newer schema must not be silently accepted")
        } catch (error: IllegalArgumentException) {
            assertTrue("unexpected message: ${error.message}", error.message!!.contains("newer"))
        }
    }

    @Test
    fun malformedPayloadExplainsItselfInsteadOfThrowingSerializationNoise() {
        val payloads = listOf("not json at all", "[1,2,3]", "{\"something\": 1}")
        payloads.forEach { payload ->
            try {
                engine.importGameStateFromJson(payload)
                fail("expected $payload to be rejected")
            } catch (error: IllegalArgumentException) {
                assertTrue("unexpected message: ${error.message}", error.message!!.isNotBlank())
            }
        }
    }

    @Test
    fun unknownFieldsFromFutureBuildsAreIgnored() {
        val json = rewrite(payloadOf(GameState.initial("us"))) { fields ->
            fields["holographicPolicyEngine"] = JsonPrimitive(true)
        }

        val restored = engine.importGameStateFromJson(json)

        assertEquals(GameState.SAVE_VERSION, restored.saveVersion)
        assertEquals("us", restored.playerNation.id)
    }
}
