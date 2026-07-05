package icu.windea.pls.lang.intentions.script

import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.clearIntegrationTest
import icu.windea.pls.test.initConfigGroups
import icu.windea.pls.test.markConfigDirectory
import icu.windea.pls.test.markFileInfo
import icu.windea.pls.test.markIntegrationTest
import icu.windea.pls.test.markRootDirectory
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see ReplaceDefineReferenceWithEvaluationResultIntention
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ReplaceDefineReferenceWithEvaluationResultIntentionTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("features/intentions")
        markConfigDirectory("chronicle/.config")
        initConfigGroups(project, ParadoxGameType.Stellaris)
    }


    @After
    fun doTearDown() = clearIntegrationTest()

    @Test
    fun test_smoke() {
        markFileInfo(ParadoxGameType.Stellaris, "common/defines/00_defines.txt")
        myFixture.configureByFile("chronicle/common/defines/00_defines.txt")

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        markFileInfo(ParadoxGameType.Stellaris, "common/entrance.txt")

        val intentionName = ChronicleBundle.message("intention.replaceDefineReferenceWithEvaluationResult")
        myFixture.configureByText("define_references.test.txt", "description = <caret>define:NEntrance|INTRODUCE")
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("description = here_we_introduce")
    }

    @Test
    fun test_unresolved_notAvailable() {
        markFileInfo(ParadoxGameType.Stellaris, "common/defines/00_defines.txt")
        myFixture.configureByFile("chronicle/common/defines/00_defines.txt")

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        markFileInfo(ParadoxGameType.Stellaris, "common/entrance.txt")

        val intentionName = ChronicleBundle.message("intention.replaceDefineReferenceWithEvaluationResult")
        myFixture.configureByText("define_references.test.txt", "description = <caret>define:NEntrance|INTRODUCE_UNDEFINED")
        assertThrows(AssertionError::class.java) { myFixture.findSingleIntention(intentionName) }
    }
}
