package icu.windea.pls.lang.intentions.script

import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.lang.intentions.ChronicleIntentionBundle
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.ChronicleTestScope
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
class ReplaceArrayDefineReferenceWithEvaluationResultIntentionTest : BasePlatformTestCase(), ChronicleTestScope {
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
    fun smoke_test() {
        markFileInfo(ParadoxGameType.Stellaris, "common/defines/00_defines.txt")
        myFixture.configureByFile("chronicle/common/defines/00_defines.txt")

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        markFileInfo(ParadoxGameType.Stellaris, "common/entrance.txt")

        val intentionName = ChronicleIntentionBundle.message("intention.replaceArrayDefineReferenceWithEvaluationResult")
        myFixture.configureByText("define_references.test.txt", "tip = <caret>array_define:NEntrance|WELCOMES|0")
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("tip = here_we_send_greetings")
    }

    @Test
    fun unresolved_notAvailable_test() {
        markFileInfo(ParadoxGameType.Stellaris, "common/defines/00_defines.txt")
        myFixture.configureByFile("chronicle/common/defines/00_defines.txt")

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        markFileInfo(ParadoxGameType.Stellaris, "common/entrance.txt")

        val intentionName = ChronicleIntentionBundle.message("intention.replaceArrayDefineReferenceWithEvaluationResult")
        myFixture.configureByText("define_references.test.txt", "tip = <caret>array_define:NEntrance|WELCOMES_UNDEFINED|0")
        assertThrows(AssertionError::class.java) { myFixture.findSingleIntention(intentionName) }
    }

    @Test
    fun indexNotInt_notAvailable_test() {
        markFileInfo(ParadoxGameType.Stellaris, "common/defines/00_defines.txt")
        myFixture.configureByFile("chronicle/common/defines/00_defines.txt")

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        markFileInfo(ParadoxGameType.Stellaris, "common/entrance.txt")

        val intentionName = ChronicleIntentionBundle.message("intention.replaceArrayDefineReferenceWithEvaluationResult")
        myFixture.configureByText("define_references.test.txt", "tip = <caret>array_define:NEntrance|WELCOMES|var")
        assertThrows(AssertionError::class.java) { myFixture.findSingleIntention(intentionName) }
    }

    @Test
    fun indexOutOfBounds_notAvailable_test() {
        markFileInfo(ParadoxGameType.Stellaris, "common/defines/00_defines.txt")
        myFixture.configureByFile("chronicle/common/defines/00_defines.txt")

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        markFileInfo(ParadoxGameType.Stellaris, "common/entrance.txt")

        val intentionName = ChronicleIntentionBundle.message("intention.replaceArrayDefineReferenceWithEvaluationResult")
        myFixture.configureByText("define_references.test.txt", "tip = <caret>array_define:NEntrance|WELCOMES|-1")
        assertThrows(AssertionError::class.java) { myFixture.findSingleIntention(intentionName) }
    }
}
