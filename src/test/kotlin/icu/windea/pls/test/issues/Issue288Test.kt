package icu.windea.pls.test.issues

import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.lang.inspections.script.expression.UnresolvedExpressionInspection
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.ChronicleTestScope
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * See: [#288](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/288)
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class Issue288Test : BasePlatformTestCase(), ChronicleTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("issues/288")
        initConfigGroups(project, ParadoxGameType.Eu5)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    @Test
    fun testInspection() {
        myFixture.enableInspections(UnresolvedExpressionInspection::class.java)

        markFileInfo(ParadoxGameType.Eu5, "common/location_ranks/issue_288_eu5.test.txt")
        myFixture.configureByFile("issues/288/common/location_ranks/issue_288_eu5.test.txt")

        IndexingTestUtil.waitUntilIndexesAreReady(project)
        myFixture.checkHighlighting()
    }
}
