package icu.windea.pls.csv

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.csv.psi.ParadoxCsvFile
import icu.windea.pls.csv.psi.getHeaderColumn
import icu.windea.pls.csv.psi.isHeaderColumn
import org.junit.Assert

@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxCsvSyntaxPsiTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    fun testHeaderAndRowsBasic() {
        myFixture.configureByFile("csv/t_syntax_header_and_rows.csv")
        val file = myFixture.file as ParadoxCsvFile
        val header = file.header
        Assert.assertNotNull(header)
        header!!
        Assert.assertEquals(3, header.columnList.size)

        val rows = file.rows
        Assert.assertEquals(2, rows.size)
        Assert.assertEquals(3, rows[0].columnList.size)
        Assert.assertEquals(3, rows[1].columnList.size)

        val ageHeaderName = header.columnList[1].name
        Assert.assertEquals("age", ageHeaderName)
        val ageCol = rows[0].columnList[1]
        Assert.assertEquals("18", ageCol.value)
        Assert.assertEquals(ageHeaderName, ageCol.getHeaderColumn()?.name)
    }

    fun testHeaderOnly() {
        myFixture.configureByFile("csv/t_syntax_header_only.csv")
        val file = myFixture.file as ParadoxCsvFile
        val header = file.header
        Assert.assertNotNull(header)
        Assert.assertTrue(file.rows.isEmpty())
        Assert.assertEquals(listOf("name", "age", "desc"), header!!.columnList.map { it.name })
    }

    fun testOnlyComments() {
        myFixture.configureByFile("csv/t_syntax_only_comments.csv")
        val file = myFixture.file as ParadoxCsvFile
        Assert.assertNull(file.header)
        Assert.assertTrue(file.rows.isEmpty())
    }

    fun testEmptyColumnsAndQuoted() {
        myFixture.configureByFile("csv/t_syntax_empty_columns.csv")
        val file = myFixture.file as ParadoxCsvFile
        val header = file.header
        Assert.assertNotNull(header)
        header!!
        Assert.assertEquals(3, header.columnList.size)
        Assert.assertTrue(header.columnList.all { it.isHeaderColumn() })

        val rows = file.rows
        Assert.assertEquals(2, rows.size)

        // row 1: first and last columns are empty
        val r1 = rows[0]
        Assert.assertEquals(3, r1.columnList.size)
        Assert.assertEquals("", r1.columnList[0].value)
        Assert.assertEquals("18", r1.columnList[1].value)
        Assert.assertEquals("", r1.columnList[2].value)

        // row 2: quoted column with semicolons inside
        val r2 = rows[1]
        Assert.assertEquals(3, r2.columnList.size)
        Assert.assertEquals("a;b;c", r2.columnList[2].value)
    }
}
