package icu.windea.pls.tool.cwt

import icu.windea.pls.dev.cwt.generator.*
import icu.windea.pls.lang.model.*
import org.junit.*

class CwtEffectConfigGeneratorTest {
    @Test
    fun testForStellaris() {
        val generator = CwtEffectConfigGenerator(
            ParadoxGameType.Stellaris,
            "cwt/cwtools-stellaris-config/script-docs/effects.log",
            "cwt/cwtools-stellaris-config/config/effects.cwt",
        )
        generator.overrideDocumentation = false
        generator.generate()
    }
}
