package icu.windea.pls.csv

import com.intellij.psi.PsiErrorElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.csv.psi.ParadoxCsvFile
import org.junit.Assert

@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxCsvMoreSyntaxPsiTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    fun testAdvancedQuoted() {
        myFixture.configureByFile("csv/t_syntax_advanced_quoted.csv")
        val file = myFixture.file as ParadoxCsvFile
        val header = file.header
        Assert.assertNotNull(header)
        header!!
        Assert.assertEquals(3, header.columnList.size)
        val rows = file.rows
        Assert.assertEquals(1, rows.size)
        val r1 = rows[0]
        Assert.assertEquals(3, r1.columnList.size)
        Assert.assertEquals("1", r1.columnList[0].value)
        Assert.assertEquals("Alice;Dev", r1.columnList[1].value)
        Assert.assertEquals("line1;line2", r1.columnList[2].value)
    }

    fun testBoundaryOnlyWhitespace() {
        myFixture.configureByFile("csv/t_syntax_only_whitespace.csv")
        val file = myFixture.file as ParadoxCsvFile
        Assert.assertNull(file.header)
        Assert.assertTrue(file.rows.isEmpty())
    }

    fun testErrorUnclosedQuote() {
        myFixture.configureByFile("csv/t_syntax_error_unclosed_quote.csv")
        val file = myFixture.file as ParadoxCsvFile
        val header = file.header
        Assert.assertNotNull(header)
        header!!
        Assert.assertEquals(3, header.columnList.size)
        val rows = file.rows
        Assert.assertEquals(1, rows.size)
        val r1 = rows[0]
        // unclosed quoted column is tokenized as COLUMN_TOKEN and unquoted via getValue()
        Assert.assertEquals(2, r1.columnList.size)
        Assert.assertEquals("alice", r1.columnList[0].value)
        Assert.assertEquals("18;student", r1.columnList[1].value)
    }
}
