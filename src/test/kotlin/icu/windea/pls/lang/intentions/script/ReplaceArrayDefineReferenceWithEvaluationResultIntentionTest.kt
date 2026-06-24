package icu.windea.pls.lang.intentions.script

import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.PlsBundle
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
 * @see ReplaceArrayDefineReferenceWithEvaluationResultIntention
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ReplaceArrayDefineReferenceWithEvaluationResultIntentionTest : BasePlatformTestCase() {
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

        val intentionName = PlsBundle.message("intention.replaceArrayDefineReferenceWithEvaluationResult")
        myFixture.configureByText("define_references.test.txt", "tip = <caret>array_define:NEntrance|WELCOMES|0")
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("tip = here_we_send_greetings")
    }

    @Test
    fun test_unresolved_notAvailable() {
        markFileInfo(ParadoxGameType.Stellaris, "common/defines/00_defines.txt")
        myFixture.configureByFile("chronicle/common/defines/00_defines.txt")

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        markFileInfo(ParadoxGameType.Stellaris, "common/entrance.txt")

        val intentionName = PlsBundle.message("intention.replaceArrayDefineReferenceWithEvaluationResult")
        myFixture.configureByText("define_references.test.txt", "tip = <caret>array_define:NEntrance|WELCOMES_UNDEFINED|0")
        assertThrows(AssertionError::class.java) { myFixture.findSingleIntention(intentionName) }
    }

    @Test
    fun test_indexNotInt_notAvailable() {
        markFileInfo(ParadoxGameType.Stellaris, "common/defines/00_defines.txt")
        myFixture.configureByFile("chronicle/common/defines/00_defines.txt")

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        markFileInfo(ParadoxGameType.Stellaris, "common/entrance.txt")

        val intentionName = PlsBundle.message("intention.replaceArrayDefineReferenceWithEvaluationResult")
        myFixture.configureByText("define_references.test.txt", "tip = <caret>array_define:NEntrance|WELCOMES|var")
        assertThrows(AssertionError::class.java) { myFixture.findSingleIntention(intentionName) }
    }

    @Test
    fun test_indexOutOfBounds_notAvailable() {
        markFileInfo(ParadoxGameType.Stellaris, "common/defines/00_defines.txt")
        myFixture.configureByFile("chronicle/common/defines/00_defines.txt")

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        markFileInfo(ParadoxGameType.Stellaris, "common/entrance.txt")

        val intentionName = PlsBundle.message("intention.replaceArrayDefineReferenceWithEvaluationResult")
        myFixture.configureByText("define_references.test.txt", "tip = <caret>array_define:NEntrance|WELCOMES|-1")
        assertThrows(AssertionError::class.java) { myFixture.findSingleIntention(intentionName) }
    }
}
