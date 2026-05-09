package icu.windea.pls.test.issues

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.lang.inspections.script.common.UnresolvedExpressionInspection
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
 * See: [#288](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/288)
 *
 * @see UnresolvedExpressionInspection
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class Issue288Test : BasePlatformTestCase() {
    private val gameType = ParadoxGameType.Eu5

    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("issues/288")
        initConfigGroups(project, gameType)
        myFixture.enableInspections(UnresolvedExpressionInspection::class.java)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    @Test
    fun test() {
        markFileInfo(gameType, "common/location_ranks/issue_288_eu5.test.txt")
        myFixture.configureByFile("issues/288/common/location_ranks/issue_288_eu5.test.txt")
        myFixture.checkHighlighting(true, false, false)
    }
}
