package icu.windea.pls.tools.config.generators

import icu.windea.pls.model.ParadoxGameType
import org.junit.Test

/**
 * @see CwtModifierConfigGenerator
 */
class CwtModifierConfigGeneratorTest : CwtConfigGeneratorTest() {
    @Test
    fun generate_forStellaris() {
        val version = latestStellarisVersion
        val generator = CwtModifierConfigGenerator(project)
        val gameType = ParadoxGameType.Stellaris
        val inputPath = "cwt/cwtools-stellaris-config/script-docs/$version/modifiers.log"
        val outputPath = "cwt/cwtools-stellaris-config/config/modifiers.cwt"
        generate(generator, gameType, inputPath, outputPath, "${gameType.id}_$version")
    }

    @Test
    fun generate_forCk3() {
        val generator = CwtModifierConfigGenerator(project)
        val gameType = ParadoxGameType.Ck3
        val inputPath = "cwt/cwtools-ck3-config/script-docs/modifiers.log"
        val outputPath = "cwt/cwtools-ck3-config/config/modifiers.cwt"
        generate(generator, gameType, inputPath, outputPath)
    }

    @Test
    fun generate_forVic3() {
        val generator = CwtModifierConfigGenerator(project)
        val gameType = ParadoxGameType.Vic3
        val inputPath = "cwt/cwtools-vic3-config/script-docs/modifiers.log"
        val outputPath = "cwt/cwtools-vic3-config/config/modifiers.gen.cwt"
        generate(generator, gameType, inputPath, outputPath)
    }
}
