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
 * See: [#284](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/284)
 *
 * A scope link that is defined only once (e.g., `owner`) was reported as a conflicting resolved expression
 * in both trigger and effect context. The regression was introduced together with the script value
 * implementation.
 *
 * Cause: the match logic conflated the scope link with an unrelated value link, so a single link matched
 * multiple configs and produced conflicting resolved configs. The regression test ensures that a scope link
 * no longer produces a conflict; no special-casing by context is needed.
 *
 * @see icu.windea.pls.lang.inspections.script.expression.ConflictingExpressionInspection
 * @see icu.windea.pls.lang.match.ParadoxConfigMatchService
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class Issue284Test : BasePlatformTestCase(), ChronicleTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("issues/284")
        initConfigGroups(project, ParadoxGameType.Vic3)
        myFixture.enableInspections(ConflictingExpressionInspection::class.java)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    @Test
    fun testScopeLinkInTrigger() {
        markFileInfo(ParadoxGameType.Vic3, "common/scripted_triggers/test_trigger.test.txt")
        myFixture.configureByFile("issues/284/common/scripted_triggers/test_trigger.test.txt")

        IndexingTestUtil.waitUntilIndexesAreReady(project)
        myFixture.checkHighlighting()
    }

    @Test
    fun testScopeLinkInEffect() {
        markFileInfo(ParadoxGameType.Vic3, "common/scripted_effects/test_effect.test.txt")
        myFixture.configureByFile("issues/284/common/scripted_effects/test_effect.test.txt")

        IndexingTestUtil.waitUntilIndexesAreReady(project)
        myFixture.checkHighlighting()
    }
}
