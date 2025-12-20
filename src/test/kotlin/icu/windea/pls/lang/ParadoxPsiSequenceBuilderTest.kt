package icu.windea.pls.lang

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.core.collections.forward
import icu.windea.pls.core.collections.options
import icu.windea.pls.csv.psi.ParadoxCsvFile
import icu.windea.pls.lang.psi.ParadoxPsiSequenceBuilder
import icu.windea.pls.lang.psi.conditional
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.script.psi.ParadoxScriptFile
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxPsiSequenceBuilderTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Test
    fun localisations() {
        myFixture.configureByFile("features/walking/walking_test_1.test.yml")
        val file = myFixture.file as ParadoxLocalisationFile
        run {
            val sequence = ParadoxPsiSequenceBuilder.localisations(file)
            Assert.assertEquals(sequence.toList().size, 5)
        }
        run {
            val sequence = ParadoxPsiSequenceBuilder.localisations(file.propertyLists[0])
            Assert.assertEquals(sequence.toList().size, 2)
        }
        run {
            val sequence = ParadoxPsiSequenceBuilder.localisations(file.propertyLists[1])
            Assert.assertEquals(sequence.toList().size, 3)
        }
    }

    @Test
    fun localisations_forwardFalse() {
        myFixture.configureByFile("features/walking/walking_test_1.test.yml")
        val file = myFixture.file as ParadoxLocalisationFile
        val names = ParadoxPsiSequenceBuilder.localisations(file)
            .options { forward(false) }
            .map { it.name }
            .toList()
        Assert.assertEquals(listOf("K5", "K4", "K3", "K2", "K1"), names)
    }

    @Test
    fun selectedLocalisations_caretInLocale() {
        myFixture.configureByFile("features/walking/walking_test_1.test.yml")
        val file = myFixture.file as ParadoxLocalisationFile
        val editor = myFixture.editor
        editor.selectionModel.removeSelection()
        editor.caretModel.moveToOffset(file.text.indexOf("l_simp_chinese"))
        val names = ParadoxPsiSequenceBuilder.selectedLocalisations(editor, file).map { it.name }.toList()
        Assert.assertEquals(listOf("K3", "K4", "K5"), names)
    }

    @Test
    fun selectedLocalisations_singleProperty() {
        myFixture.configureByFile("features/walking/walking_test_1.test.yml")
        val file = myFixture.file as ParadoxLocalisationFile
        val editor = myFixture.editor
        editor.selectionModel.removeSelection()
        editor.caretModel.moveToOffset(file.text.indexOf("K4"))
        val names = ParadoxPsiSequenceBuilder.selectedLocalisations(editor, file).map { it.name }.toList()
        Assert.assertEquals(listOf("K4"), names)
    }

    @Test
    fun selectedLocalisations_selectionSameProperty() {
        myFixture.configureByFile("features/walking/walking_test_1.test.yml")
        val file = myFixture.file as ParadoxLocalisationFile
        val editor = myFixture.editor
        val start = file.text.indexOf("K4")
        editor.caretModel.moveToOffset(start)
        editor.selectionModel.setSelection(start, start + 1)
        val names = ParadoxPsiSequenceBuilder.selectedLocalisations(editor, file).map { it.name }.toList()
        Assert.assertEquals(listOf("K4"), names)
    }

    @Test
    fun selectedLocalisations_selectionToEof() {
        myFixture.configureByFile("features/walking/walking_test_1.test.yml")
        val file = myFixture.file as ParadoxLocalisationFile
        val editor = myFixture.editor
        val start = file.text.indexOf("K3")
        val end = file.textLength
        editor.caretModel.moveToOffset(start)
        editor.selectionModel.setSelection(start, end)
        val names = ParadoxPsiSequenceBuilder.selectedLocalisations(editor, file).map { it.name }.toList()
        Assert.assertEquals(listOf("K3", "K4", "K5"), names)
    }

    @Test
    fun members_basicAndOptions() {
        myFixture.configureByFile("features/walking/walking_test_1.test.txt")
        val file = myFixture.file as ParadoxScriptFile

        run {
            val names = ParadoxPsiSequenceBuilder.members(file).map { it.text.trim() }.toList()
            Assert.assertEquals(3, names.size)
            Assert.assertTrue(names[0].startsWith("a"))
            Assert.assertTrue(names[1].startsWith("b"))
            Assert.assertTrue(names[2].startsWith("e"))
        }
        run {
            val names = ParadoxPsiSequenceBuilder.members(file)
                .options { conditional(true) }
                .map { it.text.trim() }
                .toList()
            Assert.assertEquals(5, names.size)
            Assert.assertTrue(names.any { it.startsWith("c") })
            Assert.assertTrue(names.any { it.startsWith("d") })
        }
        run {
            val names = ParadoxPsiSequenceBuilder.members(file)
                .options {
                    conditional(true)
                    forward(false)
                }
                .map { it.text.trim() }
                .toList()
            Assert.assertEquals(listOf("e = 5", "d = 4", "c = 3", "b = 2", "a = 1"), names)
        }
    }

    @Test
    fun members_blockElement() {
        myFixture.configureByFile("features/walking/walking_test_2.test.txt")
        val file = myFixture.file as ParadoxScriptFile
        val block = file.block
        Assert.assertNotNull(block)
        val membersFromFile = ParadoxPsiSequenceBuilder.members(file).map { it.text.trim() }.toList()
        val membersFromBlock = ParadoxPsiSequenceBuilder.members(block!!).map { it.text.trim() }.toList()
        Assert.assertEquals(membersFromFile, membersFromBlock)
    }

    @Test
    fun members_nonScriptFile() {
        myFixture.configureByFile("features/walking/walking_test_2.test.yml")
        val file = myFixture.file
        val members = ParadoxPsiSequenceBuilder.members(file).toList()
        Assert.assertTrue(members.isEmpty())
    }

    @Test
    fun selectedRows_basicAndForward() {
        myFixture.configureByFile("features/walking/walking_test_1.test.csv")
        val file = myFixture.file as ParadoxCsvFile
        val editor = myFixture.editor

        run {
            editor.selectionModel.removeSelection()
            editor.caretModel.moveToOffset(file.text.indexOf("bob"))
            val rows = ParadoxPsiSequenceBuilder.selectedRows(editor, file).map { it.text.trim() }.toList()
            Assert.assertEquals(1, rows.size)
            Assert.assertTrue(rows[0].startsWith("bob"))
        }
        run {
            val start = file.text.indexOf("alice")
            val end = file.text.indexOf("charlie") + 1
            editor.caretModel.moveToOffset(start)
            editor.selectionModel.setSelection(start, end)
            val rows = ParadoxPsiSequenceBuilder.selectedRows(editor, file).map { it.text.trim() }.toList()
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
            val rows = ParadoxPsiSequenceBuilder.selectedRows(editor, file)
                .options { forward(false) }
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
            val columns = ParadoxPsiSequenceBuilder.selectedColumns(editor, file).map { it.text.trim() }.toList()
            Assert.assertEquals(listOf("bob"), columns)
        }
        run {
            val start = file.text.indexOf("alice") + 1
            val end = file.text.indexOf("student") + 2
            editor.caretModel.moveToOffset(start)
            editor.selectionModel.setSelection(start, end)
            val columns = ParadoxPsiSequenceBuilder.selectedColumns(editor, file).map { it.text.trim() }.toList()
            Assert.assertEquals(listOf("alice", "18", "student"), columns)
        }
        run {
            val start = file.text.indexOf("alice") + 1
            val end = file.text.indexOf("qa") + 1
            editor.caretModel.moveToOffset(start)
            editor.selectionModel.setSelection(start, end)
            val columns = ParadoxPsiSequenceBuilder.selectedColumns(editor, file).map { it.text.trim() }.toList()
            Assert.assertEquals(listOf("alice", "bob", "20", "qa"), columns)
        }
    }
}
