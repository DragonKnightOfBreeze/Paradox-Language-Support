package icu.windea.pls.tools.config.generators

import icu.windea.pls.model.ParadoxGameType
import org.junit.Test

/**
 * @see CwtEffectConfigGenerator
 */
class CwtEffectConfigGeneratorTest : CwtConfigGeneratorTest() {
    @Test
    fun generateForStellaris() {
        val version = latestStellarisVersion
        val generator = CwtEffectConfigGenerator(project)
        val gameType = ParadoxGameType.Stellaris
        val inputPath = "cwt/cwtools-stellaris-config/script-docs/$version/effects.log"
        val outputPath = "cwt/cwtools-stellaris-config/config/effects.cwt"
        generate(generator, gameType, inputPath, outputPath, "${gameType.id}_$version")
    }

    @Test
    fun generateForIr() {
        val generator = CwtEffectConfigGenerator(project)
        val gameType = ParadoxGameType.Ir
        val inputPath = "cwt/cwtools-ir-config/effects.log"
        val outputPath = "cwt/cwtools-ir-config/effects.cwt"
        generate(generator, gameType, inputPath, outputPath)
    }

    @Test
    fun generateForCk3() {
        val generator = CwtEffectConfigGenerator(project)
        val gameType = ParadoxGameType.Ck3
        val inputPath = "cwt/cwtools-ck3-config/script-docs/effects.log"
        val outputPath = "cwt/cwtools-ck3-config/config/effects.cwt"
        generate(generator, gameType, inputPath, outputPath)
    }

    @Test
    fun generateForVic3() {
        val generator = CwtEffectConfigGenerator(project)
        val gameType = ParadoxGameType.Vic3
        val inputPath = "cwt/cwtools-vic3-config/script-docs/effects.log"
        val outputPath = "cwt/cwtools-vic3-config/config/effects.cwt"
        generate(generator, gameType, inputPath, outputPath)
    }

    @Test
    fun generateForEu5() {
        val generator = CwtEffectConfigGenerator(project)
        val gameType = ParadoxGameType.Vic3
        val inputPath = "cwt/cwtools-eu5-config/script-docs/effects.log"
        val outputPath = "cwt/cwtools-eu5-config/config/effects.cwt"
        generate(generator, gameType, inputPath, outputPath)
    }
}
