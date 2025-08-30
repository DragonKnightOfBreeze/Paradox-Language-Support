package icu.windea.pls.csv.editor

import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase

@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxCsvBasicAnnotatorTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    fun testValidCsv_noErrors() {
        myFixture.configureByText(
            "annotator_csv_valid.test.csv",
            """
            name;age;desc
            alice;18;student
            """.trimIndent()
        )
        val highlights = myFixture.doHighlighting()
        assertTrue(highlights.none { it.severity == HighlightSeverity.ERROR })
    }
}
