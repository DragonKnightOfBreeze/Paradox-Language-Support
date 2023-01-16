package icu.windea.pls.tool.cwt

import icu.windea.pls.config.core.config.*
import org.junit.*

class CwtEffectConfigGeneratorTest {
    @Test
    fun testForStellaris() {
        CwtEffectConfigGenerator(
            ParadoxGameType.Stellaris,
            "cwt/cwtools-stellaris-config/script-docs/effects.log",
            "cwt/cwtools-stellaris-config/config/effects.cwt",
        ).generate()
    }
}