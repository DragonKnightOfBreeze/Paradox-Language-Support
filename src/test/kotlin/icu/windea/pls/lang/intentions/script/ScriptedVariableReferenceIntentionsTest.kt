package icu.windea.pls.lang.intentions.script

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.PlsBundle
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.clearIntegrationTest
import icu.windea.pls.test.markFileInfo
import icu.windea.pls.test.markIntegrationTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see ReplaceScriptedVariableReferenceWithResolvedValueIntention
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ScriptedVariableReferenceIntentionsTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() = markIntegrationTest()

    @After
    fun doTearDown() = clearIntegrationTest()

    @Test
    fun testReplaceScriptedVariableReferenceWithResolvedValue_smoke() {
        val intentionName = PlsBundle.message("intention.replaceScriptedVariableReferenceWithResolvedValue")

        markFileInfo(ParadoxGameType.Stellaris, "common/test/scripted_variable_references.test.txt")
        myFixture.configureByText("scripted_variable_references.test.txt", "@var = 1\nkey = <caret>@var")
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("@var = 1\nkey = 1")
    }

    @Test
    fun testReplaceScriptedVariableReferenceWithResolvedValue_unresolved_notAvailable() {
        val intentionName = PlsBundle.message("intention.replaceScriptedVariableReferenceWithResolvedValue")

        markFileInfo(ParadoxGameType.Stellaris, "common/test/scripted_variable_references.test.txt")
        myFixture.configureByText("scripted_variable_references.test.txt", "@var = 1\nkey = <caret>@v")
        assertThrows(AssertionError::class.java) { myFixture.findSingleIntention(intentionName) }
    }
}
