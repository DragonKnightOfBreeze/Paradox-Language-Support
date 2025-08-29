package icu.windea.pls.tool.cwt

import icu.windea.pls.model.ParadoxGameType
import org.junit.Ignore
import org.junit.Test

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
