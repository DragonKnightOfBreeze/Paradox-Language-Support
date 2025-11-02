package icu.windea.pls.core.match.similarity

import org.junit.Assert
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.*

@RunWith(Enclosed::class)
class SimilarityMatchServiceExtraTest {
    @RunWith(Parameterized::class)
    class PlanetModifier(val input: String) {
        companion object {
            @Suppress("SpellCheckingInspection")
            @Parameters(name = "{0}")
            @JvmStatic
            fun data() = arrayOf(
                "planet_modifier",
                "planet_modifie",
                "planet_modifer",
                "planet_modifir",
            )
        }

        @Test
        fun test_1() {
            val candidates = listOf("planet_limit", "planet_modifier")
            val results = SimilarityMatchService.findBestMatches(input, candidates)
            Assert.assertEquals("planet_modifier", results.firstOrNull()?.value)
        }
    }

    @RunWith(Parameterized::class)
    class TriggeredPlanetModifier(val input: String) {
        companion object {
            @Suppress("SpellCheckingInspection")
            @Parameters(name = "{0}")
            @JvmStatic
            fun data() = arrayOf(
                "triggerred_planet_modifier",
                "triggerred_planet_modifie",
                "triggerred_planet_modifer",
                "triggerred_planet_modifir",
            )
        }

        @Test
        fun test_1() {
            val candidates = listOf("triggerred_army_modifier", "triggerred_planet_modifier", "triggerred_country_modifier")
            val results = SimilarityMatchService.findBestMatches(input, candidates)
            Assert.assertEquals("triggerred_planet_modifier", results.firstOrNull()?.value)
        }

        @Test
        fun test_2() {
            val candidates = listOf("triggerred_army_modifier", "triggerred_planet_modifier", "triggerred_country_modifier")
            val results = SimilarityMatchService.findBestMatches(input, candidates)
            Assert.assertEquals(listOf("triggerred_planet_modifier", "triggerred_army_modifier", "triggerred_country_modifier"), results.map { it.value })
        }
    }
}
