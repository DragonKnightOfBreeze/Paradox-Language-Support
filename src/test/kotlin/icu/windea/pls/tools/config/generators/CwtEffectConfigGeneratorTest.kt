package icu.windea.pls.tools.config.generators

import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.AssumePredicates
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class CwtEffectConfigGeneratorTest : CwtConfigGeneratorTest() {
    @Before
    fun setup() = AssumePredicates.includeConfigGenerator()

    @Test
    fun generate_forStellaris() {
        val version = latestStellarisVersion
        val generator = CwtEffectConfigGenerator(project)
        val gameType = ParadoxGameType.Stellaris
        val inputPath = "cwt/cwtools-stellaris-config/script-docs/$version/effects.log"
        val outputPath = "cwt/cwtools-stellaris-config/config/effects.cwt"
        generate(generator, gameType, inputPath, outputPath, "${gameType.id}_$version")
    }

    @Test
    fun generate_forCk3() {
        val generator = CwtEffectConfigGenerator(project)
        val gameType = ParadoxGameType.Ck3
        val inputPath = "cwt/cwtools-ck3-config/script-docs/effects.log"
        val outputPath = "cwt/cwtools-ck3-config/config/effects.cwt"
        generate(generator, gameType, inputPath, outputPath)
    }

    @Test
    fun generate_forIr() {
        val generator = CwtEffectConfigGenerator(project)
        val gameType = ParadoxGameType.Ir
        val inputPath = "cwt/cwtools-ir-config/effects.log"
        val outputPath = "cwt/cwtools-ir-config/effects.cwt"
        generate(generator, gameType, inputPath, outputPath)
    }

    @Test
    fun generate_forVic3() {
        val generator = CwtEffectConfigGenerator(project)
        val gameType = ParadoxGameType.Vic3
        val inputPath = "cwt/cwtools-vic3-config/script-docs/effects.log"
        val outputPath = "cwt/cwtools-vic3-config/config/effects.cwt"
        generate(generator, gameType, inputPath, outputPath)
    }
}
