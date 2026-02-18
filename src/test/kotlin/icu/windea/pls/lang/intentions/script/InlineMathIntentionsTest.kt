package icu.windea.pls.lang.intentions.script

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.PlsBundle
import icu.windea.pls.test.clearIntegrationTest
import icu.windea.pls.test.markIntegrationTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import icu.windea.pls.lang.util.builders.ParadoxScriptTextBuilder.parameter as p

/**
 * @see ReplaceInlineMathWithEvaluatedValueIntention
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class InlineMathIntentionsTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun setup() = markIntegrationTest()

    @After
    fun clear() = clearIntegrationTest()

    @Test
    fun testReplaceInlineMathWithEvaluatedValue_constant() {
        val intentionName = PlsBundle.message("intention.replaceInlineMathWithEvaluatedValue")
        myFixture.configureByText("inline_maths.test.txt", "key = <caret>@[ 1 + 1 ]")
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("key = 2")
    }

    @Test
    fun testReplaceInlineMathWithEvaluatedValue_parameterWithDefaultValue() {
        val intentionName = PlsBundle.message("intention.replaceInlineMathWithEvaluatedValue")
        myFixture.configureByText("inline_maths.test.txt", "key = <caret>@[ 1 + ${p("NUM", "1")} ]")
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("key = 2")
    }

    @Test
    fun testReplaceInlineMathWithEvaluatedValue_dynamic_notAvailable() {
        val intentionName = PlsBundle.message("intention.replaceInlineMathWithEvaluatedValue")
        myFixture.configureByText("inline_maths.test.txt", "key = <caret>@[ 1 + ${p("NUM")} ]")
        assertThrows(AssertionError::class.java) { myFixture.findSingleIntention(intentionName) }
    }
}
