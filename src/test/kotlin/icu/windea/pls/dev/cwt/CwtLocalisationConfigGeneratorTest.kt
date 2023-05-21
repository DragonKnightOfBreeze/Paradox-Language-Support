package icu.windea.pls.dev.cwt

import icu.windea.pls.lang.model.*
import org.junit.*

class CwtLocalisationConfigGeneratorTest {
    @Test
    fun testForStellaris() {
        val generator = CwtLocalisationConfigGenerator(
            ParadoxGameType.Stellaris,
            "cwt/cwtools-stellaris-config/script-docs/localizations.log",
            "cwt/cwtools-stellaris-config/config/localisation.cwt",
        )
        generator.generate()
    }
}