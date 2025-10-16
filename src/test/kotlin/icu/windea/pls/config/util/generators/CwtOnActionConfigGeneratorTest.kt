package icu.windea.pls.config.util.generators

import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.AssumePredicates
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class CwtOnActionConfigGeneratorTest : CwtConfigGeneratorTest() {
    @Before
    fun setup() = AssumePredicates.includeLocalEnv()

    @Test
    fun generate_forStellaris() {
        val version = latestStellarisVersion
        val generator = CwtOnActionConfigGenerator(project)
        val gameType = ParadoxGameType.Stellaris
        val inputPath = "common/on_actions"
        val outputPath = "cwt/cwtools-stellaris-config/config/on_actions.cwt"
        generate(generator, gameType, inputPath, outputPath, "${gameType.id}_$version")
    }
}
