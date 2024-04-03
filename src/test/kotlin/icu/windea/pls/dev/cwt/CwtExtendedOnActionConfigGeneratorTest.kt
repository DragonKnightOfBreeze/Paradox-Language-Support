package icu.windea.pls.dev.cwt

import icu.windea.pls.model.*
import org.junit.*

class CwtExtendedOnActionConfigGeneratorTest {
    @Test
    fun testForStellaris() {
        CwtOnActionConfigGenerator(
            ParadoxGameType.Stellaris,
            "common/on_actions/00_on_actions.txt",
            "cwt/cwtools-stellaris-config/config/on_actions.cwt",
        ).generate()
    }
}