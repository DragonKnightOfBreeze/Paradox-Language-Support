package icu.windea.pls.tool.cwt

import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.AssumePredicates
import org.junit.Before
import org.junit.Test

class CwtGameRuleConfigGeneratorTest {
    @Before
    fun before() = AssumePredicates.includeTool()

    @Test
    fun testForStellaris() {
        CwtGameRuleConfigGenerator(
            ParadoxGameType.Stellaris,
            "common/game_rules",
            "cwt/cwtools-stellaris-config/config/game_rules.cwt",
        ).generate()
    }
}
