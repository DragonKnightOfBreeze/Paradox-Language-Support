package icu.windea.pls.test.issues

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.lang.inspections.script.common.ConflictingResolvedExpressionInspection
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
import icu.windea.pls.model.ParadoxGameType

/**
 * Issue #142: `var:xxx` 在 trigger 上下文中目前会产生冲突的解析结果。
 *
 * See: [#142](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/142)
 *
 * @see ConflictingResolvedExpressionInspection
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class Issue142Test : BasePlatformTestCase() {
    private val gameType = ParadoxGameType.Vic3

    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("issues/142")
        initConfigGroups(project, gameType)
        myFixture.enableInspections(ConflictingResolvedExpressionInspection::class.java)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    @Test
    fun testVarInTrigger() {
        markFileInfo(gameType, "common/scripted_triggers/test_trigger.test.txt")
        myFixture.configureByFile("issues/142/common/scripted_triggers/test_trigger.test.txt")
        myFixture.checkHighlighting(true, false, false)
    }

    @Test
    fun testVarInEffect() {
        markFileInfo(gameType, "common/scripted_effects/test_effect.test.txt")
        myFixture.configureByFile("issues/142/common/scripted_effects/test_effect.test.txt")
        myFixture.checkHighlighting(true, false, false)
    }

    // TODO testVarWithArithmeticBlock - var:xxx = { arithmetic operations }
}
