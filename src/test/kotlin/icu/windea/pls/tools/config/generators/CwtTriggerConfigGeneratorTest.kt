package icu.windea.pls.tools.config.generators

import icu.windea.pls.model.ParadoxGameType
import org.junit.Test

/**
 * @see CwtTriggerConfigGenerator
 */
class CwtTriggerConfigGeneratorTest : CwtConfigGeneratorTest() {
    @Test
    fun generateForStellaris() {
        val version = latestStellarisVersion
        val generator = CwtTriggerConfigGenerator(project)
        val gameType = ParadoxGameType.Stellaris
        val inputPath = "cwt/cwtools-stellaris-config/script-docs/$version/triggers.log"
        val outputPath = "cwt/cwtools-stellaris-config/config/triggers.cwt"
        generate(generator, gameType, inputPath, outputPath, "${gameType.id}_$version")
    }

    @Test
    fun generateForCk3() {
        val generator = CwtTriggerConfigGenerator(project)
        val gameType = ParadoxGameType.Ck3
        val inputPath = "cwt/cwtools-ck3-config/script-docs/triggers.log"
        val outputPath = "cwt/cwtools-ck3-config/config/triggers.cwt"
        generate(generator, gameType, inputPath, outputPath)
    }

    @Test
    fun generateForVic3() {
        val generator = CwtTriggerConfigGenerator(project)
        val gameType = ParadoxGameType.Vic3
        val inputPath = "cwt/cwtools-vic3-config/script-docs/triggers.log"
        val outputPath = "cwt/cwtools-vic3-config/config/triggers.cwt"
        generate(generator, gameType, inputPath, outputPath)
    }

    @Test
    fun generateForEu5() {
        val generator = CwtTriggerConfigGenerator(project)
        val gameType = ParadoxGameType.Vic3
        val inputPath = "cwt/cwtools-eu5-config/game-docs/triggers.log"
        val outputPath = "cwt/cwtools-eu5-config/config/triggers.cwt"
        generate(generator, gameType, inputPath, outputPath)
    }
}
