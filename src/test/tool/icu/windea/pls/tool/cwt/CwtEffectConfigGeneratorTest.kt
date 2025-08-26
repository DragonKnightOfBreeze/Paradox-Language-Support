package icu.windea.pls.tool.cwt

import icu.windea.pls.model.*
import org.junit.*

@Ignore
class CwtEffectConfigGeneratorTest {
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

