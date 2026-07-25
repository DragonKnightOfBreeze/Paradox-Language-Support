package icu.windea.pls.lang.intentions.script

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.test.ChronicleTestScope
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import icu.windea.pls.lang.text.ParadoxScriptTextBuilder.parameter as p

/**
 * @see ReplaceInlineMathWithEvaluationResultIntention
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ReplaceInlineMathWithEvaluationResultIntentionTest : BasePlatformTestCase(), ChronicleTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() = markIntegrationTest()

    @After
    fun doTearDown() = clearIntegrationTest()

    @Test
    fun constant_test() {
        val intentionName = ChronicleBundle.message("intention.replaceInlineMathWithEvaluationResult")
        myFixture.configureByText("inline_maths.test.txt", "key = <caret>@[ 1 + 1 ]")
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("key = 2")
    }

    @Test
    fun parameterWithDefaultValue_test() {
        val intentionName = ChronicleBundle.message("intention.replaceInlineMathWithEvaluationResult")
        myFixture.configureByText("inline_maths.test.txt", "key = <caret>@[ 1 + ${p("NUM", "1")} ]")
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("key = 2")
    }

    @Test
    fun dynamic_notAvailable_test() {
        val intentionName = ChronicleBundle.message("intention.replaceInlineMathWithEvaluationResult")
        myFixture.configureByText("inline_maths.test.txt", "key = <caret>@[ 1 + ${p("NUM")} ]")
        assertThrows(AssertionError::class.java) { myFixture.findSingleIntention(intentionName) }
    }
}
