package icu.windea.pls.config.util.generators

import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.AssumePredicates
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class CwtTriggerConfigGeneratorTest : CwtConfigGeneratorTest() {
    @Before
    fun setup() = AssumePredicates.includeLocalEnv()

    @Test
    fun generate_forStellaris_v_latest() {
        generate_forStellaris(latestStellarisVersion)
    }

    @Suppress("SameParameterValue")
    private fun generate_forStellaris(version: String) {
        val generator = CwtTriggerConfigGenerator(project)
        val inputPath = "cwt/cwtools-stellaris-config/script-docs/$version/triggers.log"
        val outputPath = "cwt/cwtools-stellaris-config/config/triggers.cwt"
        execute(generator, ParadoxGameType.Stellaris, inputPath, outputPath, version)
    }
}
