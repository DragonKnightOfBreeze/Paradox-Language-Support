package icu.windea.pls.lang.inspections.localisation.common

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.ep.ChronicleEpBundle
import icu.windea.pls.test.HighlightingTestScope
import icu.windea.pls.test.clearIntegrationTest
import icu.windea.pls.test.markIntegrationTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see IncorrectSyntaxInspection
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class IncorrectSyntaxInspectionTest : BasePlatformTestCase(), HighlightingTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        myFixture.enableInspections(IncorrectSyntaxInspection::class.java)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    @Test
    fun leftBracketEscape() {
        val tag = ChronicleEpBundle.message("incorrectSyntax.leftBracketEscape.desc").toWarningTag()

        myFixture.configureByText("test.yml", """
            l_english:
             key: "text [GetName] text"
             key: "text [[GetName] text"
             key: "text ${tag.start}\[${tag.end}GetName] text"
             key: "text ${tag.start}\[${tag.end} ${tag.start}\[${tag.end} text"
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    @Test
    fun danglingEndMarker() {
        val tag1 = ChronicleEpBundle.message("incorrectSyntax.danglingEndMarker.desc.1").toWarningTag()
        val tag2 = ChronicleEpBundle.message("incorrectSyntax.danglingEndMarker.desc.2").toWarningTag()

        myFixture.configureByText("test.yml", """
            l_english:
             key: "§G colored text §!"
             key: "text ${tag1.start}§!${tag1.end} text"
             key: "#v formatted text #!"
             key: "text ${tag2.start}#!${tag2.end} text"
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    @Test
    fun plainText() {
        myFixture.configureByText("test.yml", """
            l_english:
             key: "plain text"
        """.trimIndent())
        myFixture.checkHighlighting()
    }
}
