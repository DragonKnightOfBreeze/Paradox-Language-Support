package icu.windea.pls.test.issues

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.lang.inspections.script.common.ConflictingResolvedExpressionInspection
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.clearIntegrationTest
import icu.windea.pls.test.initConfigGroups
import icu.windea.pls.test.markFileInfo
import icu.windea.pls.test.markIntegrationTest
import icu.windea.pls.test.markRootDirectory
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * Issue #142: `var:xxx` 在 trigger 上下文中目前会产生冲突的解析结果。
 *
 * @see ConflictingResolvedExpressionInspection
 * @see <a href="https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/142">Issue #142</a>
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class Issue142Test : BasePlatformTestCase() {
    private val gameType = ParadoxGameType.Vic3

    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("issues/issue_142")
        initConfigGroups(project, gameType)
        myFixture.enableInspections(ConflictingResolvedExpressionInspection::class.java)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    @Test
    fun testVarInTrigger() {
        markFileInfo(gameType, "common/scripted_triggers/test_trigger.test.txt")
        myFixture.configureByFile("issues/issue_142/common/scripted_triggers/test_trigger.test.txt")
        myFixture.checkHighlighting(true, false, false)
    }

    @Test
    fun testVarInEffect() {
        markFileInfo(gameType, "common/scripted_effects/test_effect.test.txt")
        myFixture.configureByFile("issues/issue_142/common/scripted_effects/test_effect.test.txt")
        myFixture.checkHighlighting(true, false, false)
    }

    // TODO testVarWithArithmeticBlock - var:xxx = { arithmetic operations }
}
