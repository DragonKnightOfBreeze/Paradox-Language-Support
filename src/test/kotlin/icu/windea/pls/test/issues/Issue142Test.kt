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
 * Issue #142: `var:xxx` 在 trigger 上下文中目前会产生冲突的解析结果。
 *
 * See: [#142](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/142)
 *
 * @see ConflictingExpressionInspection
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
