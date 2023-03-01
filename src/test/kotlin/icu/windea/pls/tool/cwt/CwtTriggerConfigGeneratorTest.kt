package icu.windea.pls.tool.cwt

import icu.windea.pls.dev.cwt.generator.*
import icu.windea.pls.lang.model.*
import org.junit.*

class CwtTriggerConfigGeneratorTest {
    @Test
    fun testForStellaris() {
        val generator = CwtTriggerConfigGenerator(
            ParadoxGameType.Stellaris,
            "cwt/cwtools-stellaris-config/script-docs/triggers.log",
            "cwt/cwtools-stellaris-config/config/triggers.cwt",
        )
        generator.overrideDocumentation = false
        generator.generate()
    }
}
