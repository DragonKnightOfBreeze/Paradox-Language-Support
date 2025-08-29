package icu.windea.pls.tool.cwt

import icu.windea.pls.model.ParadoxGameType
import org.junit.Ignore
import org.junit.Test

@Ignore
class CwtTriggerConfigGeneratorTest {
    @Test
    fun testForStellaris() {
        val version = "v3.10.0"
        val generator = CwtTriggerConfigGenerator(
            ParadoxGameType.Stellaris,
            "cwt/cwtools-stellaris-config/script-docs/$version/triggers.log",
            "cwt/cwtools-stellaris-config/config/triggers.cwt",
        )
        generator.overrideDocumentation = false
        generator.ignoredNames += setOf("and", "or", "not", "nand", "nor", "hidden_trigger")
        generator.generate()
    }
}
