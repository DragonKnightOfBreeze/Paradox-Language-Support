package icu.windea.pls.lang.manipulation

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.core.collections.context
import icu.windea.pls.core.collections.forward
import icu.windea.pls.csv.psi.ParadoxCsvFile
import icu.windea.pls.test.clearIntegrationTest
import icu.windea.pls.test.markIntegrationTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see ParadoxCsvFileManipulationService
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxCsvFileManipulationServiceTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() = markIntegrationTest()

    @After
    fun doTearDown() = clearIntegrationTest()

    @Test
    fun selectedRows_basicAndForward() {
        myFixture.configureByFile("features/walking/walking_test_1.test.csv")
        val file = myFixture.file as ParadoxCsvFile
        val editor = myFixture.editor

        run {
            editor.selectionModel.removeSelection()
            editor.caretModel.moveToOffset(file.text.indexOf("bob"))
            val rows = ParadoxCsvFileManipulationService.selectedRows(editor, file).map { it.text.trim() }.toList()
            Assert.assertEquals(1, rows.size)
            Assert.assertTrue(rows[0].startsWith("bob"))
        }
        run {
            val start = file.text.indexOf("alice")
            val end = file.text.indexOf("charlie") + 1
            editor.caretModel.moveToOffset(start)
            editor.selectionModel.setSelection(start, end)
            val rows = ParadoxCsvFileManipulationService.selectedRows(editor, file).map { it.text.trim() }.toList()
            Assert.assertEquals(3, rows.size)
            Assert.assertTrue(rows[0].startsWith("alice"))
            Assert.assertTrue(rows[1].startsWith("bob"))
            Assert.assertTrue(rows[2].startsWith("charlie"))
        }
        run {
            val start = file.text.indexOf("alice")
            val end = file.text.indexOf("charlie") + 1
            editor.caretModel.moveToOffset(start)
            editor.selectionModel.setSelection(start, end)
            val rows = ParadoxCsvFileManipulationService.selectedRows(editor, file)
                .context { forward(false) }
                .map { it.text.trim() }
                .toList()
            Assert.assertEquals(3, rows.size)
            Assert.assertTrue(rows[0].startsWith("charlie"))
            Assert.assertTrue(rows[1].startsWith("bob"))
            Assert.assertTrue(rows[2].startsWith("alice"))
        }
    }

    @Test
    fun selectedColumns_basic() {
        myFixture.configureByFile("features/walking/walking_test_1.test.csv")
        val file = myFixture.file as ParadoxCsvFile
        val editor = myFixture.editor

        run {
            editor.selectionModel.removeSelection()
            editor.caretModel.moveToOffset(file.text.indexOf("bob"))
            val columns = ParadoxCsvFileManipulationService.selectedColumns(editor, file).map { it.text.trim() }.toList()
            Assert.assertEquals(listOf("bob"), columns)
        }
        run {
            val start = file.text.indexOf("alice") + 1
            val end = file.text.indexOf("student") + 2
            editor.caretModel.moveToOffset(start)
            editor.selectionModel.setSelection(start, end)
            val columns = ParadoxCsvFileManipulationService.selectedColumns(editor, file).map { it.text.trim() }.toList()
            Assert.assertEquals(listOf("alice", "18", "student"), columns)
        }
        run {
            val start = file.text.indexOf("alice") + 1
            val end = file.text.indexOf("qa") + 1
            editor.caretModel.moveToOffset(start)
            editor.selectionModel.setSelection(start, end)
            val columns = ParadoxCsvFileManipulationService.selectedColumns(editor, file).map { it.text.trim() }.toList()
            Assert.assertEquals(listOf("alice", "bob", "20", "qa"), columns)
        }
    }

    @Test
    fun columnsOfIndex_basic() {
        myFixture.configureByText("test.csv", """
           name;value;status
           n1;v1;true
           n2;v2;false;comment
        """.trimIndent())
        val file = myFixture.file as ParadoxCsvFile

        run {
            val columns = ParadoxCsvFileManipulationService.columnsOfIndex(file, 0, includeHeaderColumn = true).map { it.text.trim() }.toList()
            assertEquals(listOf("name", "n1", "n2"), columns)
        }
        run {
            val columns = ParadoxCsvFileManipulationService.columnsOfIndex(file, 0, includeHeaderColumn = false).map { it.text.trim() }.toList()
            assertEquals(listOf("n1", "n2"), columns)
        }
        run {
            val columns = ParadoxCsvFileManipulationService.columnsOfIndex(file, 2, includeHeaderColumn = true).map { it.text.trim() }.toList()
            assertEquals(listOf("status", "true", "false"), columns)
        }
        run {
            val columns = ParadoxCsvFileManipulationService.columnsOfIndex(file, 3, includeHeaderColumn = true).map { it.text.trim() }.toList()
            assertEquals(listOf("comment"), columns)
        }
        run {
            val columns = ParadoxCsvFileManipulationService.columnsOfIndex(file, 4, includeHeaderColumn = true).map { it.text.trim() }.toList()
            assertEquals(emptyList<String>(), columns)
        }
    }
}
