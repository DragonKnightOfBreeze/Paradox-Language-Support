package icu.windea.pls.dev.cwt

import icu.windea.pls.lang.model.*
import org.junit.*

class CwtGameRuleConfigGeneratorTest {
    @Test
    fun testForStellaris() {
        CwtGameRuleConfigGenerator(
            ParadoxGameType.Stellaris,
            "common/game_rules/00_rules.txt",
            "cwt/cwtools-stellaris-config/config/game_rules.cwt",
        ).generate()
    }
}