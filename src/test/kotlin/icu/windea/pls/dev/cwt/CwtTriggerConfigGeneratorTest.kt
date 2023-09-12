package icu.windea.pls.dev.cwt

import icu.windea.pls.model.*
import org.junit.*

class CwtTriggerConfigGeneratorTest {
    @Test
    fun testForStellaris() {
        val version = "v3.9.1"
        val generator = CwtTriggerConfigGenerator(
            ParadoxGameType.Stellaris,
            "cwt/cwtools-stellaris-config/script-docs/$version/triggers.log",
            "cwt/cwtools-stellaris-config/config/triggers.cwt",
        )
        generator.overrideDocumentation = false
        generator.generate()
    }
}
