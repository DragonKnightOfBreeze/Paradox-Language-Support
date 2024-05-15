package icu.windea.pls.dev.cwt

import icu.windea.pls.model.*
import org.junit.*

class CwtOnActionConfigGeneratorTest {
    @Test
    fun testForStellaris() {
        CwtOnActionConfigGenerator(
            ParadoxGameType.Stellaris,
            "common/on_actions",
            "cwt/cwtools-stellaris-config/config/on_actions.cwt",
        ).generate()
    }
}