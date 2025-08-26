package icu.windea.pls.tool.cwt

import icu.windea.pls.model.*
import org.junit.*

@Ignore
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
