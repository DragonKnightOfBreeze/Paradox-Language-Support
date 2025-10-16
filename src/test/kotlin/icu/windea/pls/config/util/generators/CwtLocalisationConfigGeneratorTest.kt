package icu.windea.pls.config.util.generators

import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.AssumePredicates
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class CwtLocalisationConfigGeneratorTest : CwtConfigGeneratorTest() {
    @Before
    fun setup() = AssumePredicates.includeConfigGenerator()

    @Test
    fun generate_forStellaris() {
        val version = latestStellarisVersion
        val generator = CwtLocalisationConfigGenerator(project)
        val gameType = ParadoxGameType.Stellaris
        val inputPath = "cwt/cwtools-stellaris-config/script-docs/$version/localizations.log"
        val outputPath = "cwt/cwtools-stellaris-config/config/localisation.cwt"
        generate(generator, gameType, inputPath, outputPath, "${gameType.id}_$version")
    }
}
