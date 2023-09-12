package icu.windea.pls.dev.cwt

import icu.windea.pls.model.*
import org.junit.*

class CwtLocalisationConfigGeneratorTest {
    @Test
    fun testForStellaris() {
        val version = "v3.9.1"
        val generator = CwtLocalisationConfigGenerator(
            ParadoxGameType.Stellaris,
            "cwt/cwtools-stellaris-config/script-docs/$version/localizations.log",
            "cwt/cwtools-stellaris-config/config/localisation.cwt",
        )
        generator.generate()
    }
}