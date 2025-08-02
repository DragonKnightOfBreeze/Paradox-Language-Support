package icu.windea.pls.dev.cwt

import icu.windea.pls.model.*
import org.junit.*

@Ignore
class CwtGameRuleConfigGeneratorTest {
    @Test
    fun testForStellaris() {
        CwtGameRuleConfigGenerator(
            ParadoxGameType.Stellaris,
            "common/game_rules",
            "cwt/cwtools-stellaris-config/config/game_rules.cwt",
        ).generate()
    }
}
