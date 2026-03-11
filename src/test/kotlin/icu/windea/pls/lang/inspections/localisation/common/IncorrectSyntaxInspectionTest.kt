package icu.windea.pls.lang.inspections.localisation.common

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
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
class IncorrectSyntaxInspectionTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        myFixture.enableInspections(IncorrectSyntaxInspection::class.java)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    @Test
    fun testIncorrectSyntax() {
        myFixture.configureByFile("features/inspections/localisation/incorrectSyntax/incorrect_syntax.test.yml")
        myFixture.checkHighlighting(true, false, false)
    }
}
