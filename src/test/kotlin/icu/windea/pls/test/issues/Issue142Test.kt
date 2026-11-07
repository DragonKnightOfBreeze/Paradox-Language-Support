package icu.windea.pls.test.issues

import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.lang.inspections.script.expression.ConflictingExpressionInspection
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.ChronicleTestScope
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * See: [#142](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/142)
 *
 * In VIC3, the `var` link is declared with `type = both`, so `var:xxx` can match both `scope_field` and
 * `value_field`. In trigger context both `alias[trigger:scope_field]` and `alias[trigger:value_field]`
 * are applicable, so a `var:xxx` block value matches multiple configs, and the conflicting expression
 * inspection reports it if the block content is still ambiguous.
 *
 * Cause: the ambiguity is inherent to the `var` link (e.g., an empty block value), so it cannot always be
 * resolved at match time. If the block content only matches one of the candidate clauses (e.g., only
 * triggers), the conflict is resolved by the clause content, and no conflict is reported. (see #284)
 * In effect context only `scope_field` applies, so no conflict is reported there.
 *
 * @see icu.windea.pls.lang.inspections.script.expression.ConflictingExpressionInspection
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class Issue142Test : BasePlatformTestCase(), ChronicleTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("issues/142")
        initConfigGroups(project, ParadoxGameType.Vic3)
        myFixture.enableInspections(ConflictingExpressionInspection::class.java)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    @Test
    fun testVarInTrigger() {
        markFileInfo(ParadoxGameType.Vic3, "common/scripted_triggers/test_trigger.test.txt")
        myFixture.configureByFile("issues/142/common/scripted_triggers/test_trigger.test.txt")

        IndexingTestUtil.waitUntilIndexesAreReady(project)
        myFixture.checkHighlighting()
    }

    @Test
    fun testVarInEffect() {
        markFileInfo(ParadoxGameType.Vic3, "common/scripted_effects/test_effect.test.txt")
        myFixture.configureByFile("issues/142/common/scripted_effects/test_effect.test.txt")

        IndexingTestUtil.waitUntilIndexesAreReady(project)
        myFixture.checkHighlighting()
    }

    // TODO testVarWithArithmeticBlock - var:xxx = { arithmetic operations }
}
