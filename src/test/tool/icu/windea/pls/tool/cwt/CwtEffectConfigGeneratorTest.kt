package icu.windea.pls.tool.cwt

import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.AssumePredicates
import org.junit.Before
import org.junit.Test

class CwtEffectConfigGeneratorTest {
    @Before
    fun before() = AssumePredicates.includeTool()

    @Test
    fun testForStellaris() {
        val version = "v3.10.0"
        val generator = CwtEffectConfigGenerator(
            ParadoxGameType.Stellaris,
            "cwt/cwtools-stellaris-config/script-docs/$version/effects.log",
            "cwt/cwtools-stellaris-config/config/effects.cwt",
        )
        generator.overrideDocumentation = false
        generator.generate()
    }
}

