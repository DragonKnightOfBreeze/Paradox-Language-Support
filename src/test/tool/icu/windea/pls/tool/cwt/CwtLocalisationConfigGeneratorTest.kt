package icu.windea.pls.tool.cwt

import icu.windea.pls.model.ParadoxGameType
import org.junit.Ignore
import org.junit.Test

@Ignore
class CwtLocalisationConfigGeneratorTest {
    @Test
    fun testForStellaris() {
        val version = "v3.10.0"
        val generator = CwtLocalisationConfigGenerator(
            ParadoxGameType.Stellaris,
            "cwt/cwtools-stellaris-config/script-docs/$version/localizations.log",
            "cwt/cwtools-stellaris-config/config/localisation.cwt",
        )
        generator.generate()
    }
}
