package icu.windea.pls.dev.cwt

import icu.windea.pls.model.*
import org.junit.*

class CwtEffectConfigGeneratorTest {
    @Test
    fun testForStellaris() {
        val version = "v3.9.1"
        val generator = CwtEffectConfigGenerator(
            ParadoxGameType.Stellaris,
            "cwt/cwtools-stellaris-config/script-docs/$version/effects.log",
            "cwt/cwtools-stellaris-config/config/effects.cwt",
        )
        generator.overrideDocumentation = false
        generator.generate()
    }
}

