package icu.windea.pls.tools.config.generators

import icu.windea.pls.model.ParadoxGameType
import org.junit.Test

/**
 * @see CwtGameRuleConfigGenerator
 */
class CwtGameRuleConfigGeneratorTest : CwtConfigGeneratorTest() {
    @Test
    fun generate_forStellaris() {
        val version = latestStellarisVersion
        val generator = CwtGameRuleConfigGenerator(project)
        val gameType = ParadoxGameType.Stellaris
        val inputPath = "common/game_rules"
        val outputPath = "cwt/cwtools-stellaris-config/config/game_rules.cwt"
        generate(generator, gameType, inputPath, outputPath, "${gameType.id}_$version")
    }
}
