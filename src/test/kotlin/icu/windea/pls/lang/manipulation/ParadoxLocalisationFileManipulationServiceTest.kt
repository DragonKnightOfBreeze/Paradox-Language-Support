package icu.windea.pls.lang.manipulation

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.core.collections.context
import icu.windea.pls.core.collections.forward
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.test.clearIntegrationTest
import icu.windea.pls.test.markIntegrationTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see ParadoxLocalisationFileManipulationService
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxLocalisationFileManipulationServiceTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() = markIntegrationTest()

    @After
    fun doTearDown() = clearIntegrationTest()

    @Test
    fun localisations() {
        myFixture.configureByFile("features/walking/walking_test_1.test.yml")
        val file = myFixture.file as ParadoxLocalisationFile
        run {
            val sequence = ParadoxLocalisationFileManipulationService.localisations(file)
            Assert.assertEquals(sequence.toList().size, 5)
        }
        run {
            val sequence = ParadoxLocalisationFileManipulationService.localisations(file.propertyLists[0])
            Assert.assertEquals(sequence.toList().size, 2)
        }
        run {
            val sequence = ParadoxLocalisationFileManipulationService.localisations(file.propertyLists[1])
            Assert.assertEquals(sequence.toList().size, 3)
        }
    }

    @Test
    fun localisations_forwardFalse() {
        myFixture.configureByFile("features/walking/walking_test_1.test.yml")
        val file = myFixture.file as ParadoxLocalisationFile
        val names = ParadoxLocalisationFileManipulationService.localisations(file)
            .context { forward(false) }
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
        val names = ParadoxLocalisationFileManipulationService.selectedLocalisations(editor, file).map { it.name }.toList()
        Assert.assertEquals(listOf("K3", "K4", "K5"), names)
    }

    @Test
    fun selectedLocalisations_singleProperty() {
        myFixture.configureByFile("features/walking/walking_test_1.test.yml")
        val file = myFixture.file as ParadoxLocalisationFile
        val editor = myFixture.editor
        editor.selectionModel.removeSelection()
        editor.caretModel.moveToOffset(file.text.indexOf("K4"))
        val names = ParadoxLocalisationFileManipulationService.selectedLocalisations(editor, file).map { it.name }.toList()
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
        val names = ParadoxLocalisationFileManipulationService.selectedLocalisations(editor, file).map { it.name }.toList()
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
        val names = ParadoxLocalisationFileManipulationService.selectedLocalisations(editor, file).map { it.name }.toList()
        Assert.assertEquals(listOf("K3", "K4", "K5"), names)
    }
}
