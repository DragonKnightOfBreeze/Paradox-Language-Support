package icu.windea.pls.tool.cwt

import icu.windea.pls.config.core.config.*
import org.junit.*

class CwtTriggerConfigGeneratorTest {
    @Test
    fun testForStellaris() {
        val generator = CwtTriggerConfigGenerator(
            ParadoxGameType.Stellaris,
            "cwt/cwtools-stellaris-config/script-docs/triggers.log",
            "cwt/cwtools-stellaris-config/config/triggers.cwt",
        )
        generator.generate()
    }
}
