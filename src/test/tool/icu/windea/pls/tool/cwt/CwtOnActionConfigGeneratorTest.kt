package icu.windea.pls.tool.cwt

import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.AssumePredicates
import org.junit.Before
import org.junit.Test

class CwtOnActionConfigGeneratorTest {
    @Before
    fun before() = AssumePredicates.includeTool()

    @Test
    fun testForStellaris() {
        CwtOnActionConfigGenerator(
            ParadoxGameType.Stellaris,
            "common/on_actions",
            "cwt/cwtools-stellaris-config/config/on_actions.cwt",
        ).generate()
    }
}
