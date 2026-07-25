package icu.windea.pls.tools.config.generators

import icu.windea.pls.model.ParadoxGameType
import org.junit.Test

/**
 * @see CwtOnActionConfigGenerator
 */
class CwtOnActionConfigGeneratorTest : CwtConfigGeneratorTest() {
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
