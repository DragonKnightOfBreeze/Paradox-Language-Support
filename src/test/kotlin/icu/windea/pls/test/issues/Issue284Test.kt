package icu.windea.pls.test.issues

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.lang.inspections.script.expression.ConflictingResolvedExpressionInspection
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
 * Issue #284: scope link 在 trigger 上下文中不应产生冲突的解析结果。
 *
 * See: [#284](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/284)
 *
 * @see ConflictingResolvedExpressionInspection
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class Issue284Test : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("issues/284")
        initConfigGroups(project, ParadoxGameType.Vic3)
        myFixture.enableInspections(ConflictingResolvedExpressionInspection::class.java)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    @Test
    fun testScopeLinkInTrigger() {
        markFileInfo(ParadoxGameType.Vic3, "common/scripted_triggers/test_trigger.test.txt")
        myFixture.configureByFile("issues/284/common/scripted_triggers/test_trigger.test.txt")
        myFixture.checkHighlighting()
    }

    @Test
    fun testScopeLinkInEffect() {
        markFileInfo(ParadoxGameType.Vic3, "common/scripted_effects/test_effect.test.txt")
        myFixture.configureByFile("issues/284/common/scripted_effects/test_effect.test.txt")
        myFixture.checkHighlighting()
    }
}
