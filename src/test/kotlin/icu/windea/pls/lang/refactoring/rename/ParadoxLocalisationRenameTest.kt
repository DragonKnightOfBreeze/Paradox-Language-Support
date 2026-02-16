package icu.windea.pls.lang.refactoring.rename

import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.refactoring.rename.RenameProcessor
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.lang.refactoring.rename.naming.AutomaticLocalisationsRenamer
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.clearIntegrationTest
import icu.windea.pls.test.initConfigGroups
import icu.windea.pls.test.injectFileInfo
import icu.windea.pls.test.markConfigDirectory
import icu.windea.pls.test.markFileInfo
import icu.windea.pls.test.markIntegrationTest
import icu.windea.pls.test.markRootDirectory
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.io.File
import java.nio.file.Path

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxLocalisationRenameTest: BasePlatformTestCase() {
    private val gameType = ParadoxGameType.Stellaris

    override fun getTestDataPath() = "src/test/testData"

    private fun addAllowedRoots() {
        val additionalAllowedRoots = listOf(Path.of(testDataPath).toAbsolutePath().normalize().toString())
        val oldValue = System.getProperty("vfs.additional-allowed-roots").orEmpty()
        val newValue = (listOf(oldValue).filter { it.isNotBlank() } + additionalAllowedRoots)
            .distinct()
            .joinToString(File.pathSeparator)
        System.setProperty("vfs.additional-allowed-roots", newValue)
    }

    private fun commitAndSaveDocuments() {
        PsiDocumentManager.getInstance(project).commitAllDocuments()
        FileDocumentManager.getInstance().saveAllDocuments()
    }

    @Before
    fun setup() {
        addAllowedRoots()
        markIntegrationTest()
        markRootDirectory("features/refactoring")
        markConfigDirectory("features/refactoring/.config")
        initConfigGroups(project, gameType)
    }

    @After
    fun clear() {
        PsiDocumentManager.getInstance(project).commitAllDocuments()
        FileDocumentManager.getInstance().saveAllDocuments()
        clearIntegrationTest()
    }

    @Test
    fun testRename_Localisation_OverridesAndReferences() {
        // Arrange
        val localisationEnglishPath = "localisation/neuro_l_english.test.yml"
        markFileInfo(gameType, localisationEnglishPath)
        val mainTestDataPath = "features/refactoring/localisation/neuro_l_english.test.yml"
        myFixture.configureByFile(mainTestDataPath)

        val localisationChinesePath = "localisation/neuro_l_simp_chinese.test.yml"
        val localisationChineseFile = myFixture.copyFileToProject(
            "features/refactoring/localisation/neuro_l_simp_chinese.test.yml",
            localisationChinesePath
        )
        localisationChineseFile.injectFileInfo(gameType, localisationChinesePath)

        // Ensure indexed
        myFixture.configureFromTempProjectFile(localisationChinesePath)
        myFixture.configureByFile(mainTestDataPath)

        // Act
        val elementAtCaret = myFixture.file.findElementAt(myFixture.caretOffset)
        val localisation = PsiTreeUtil.getParentOfType(elementAtCaret, ParadoxLocalisationProperty::class.java, false)!!

        val newName = "evil_neuro"
        val automaticRenamer = AutomaticLocalisationsRenamer(localisation, newName)

        RenameProcessor(project, localisation, newName, false, false).run()
        for ((e, n) in automaticRenamer.renames) {
            RenameProcessor(project, e, n, false, false).run()
        }
        commitAndSaveDocuments()

        // Assert
        myFixture.checkResultByFile("features/refactoring/localisation/neuro_l_english.after.test.yml")

        myFixture.configureFromTempProjectFile(localisationChinesePath)
        myFixture.checkResultByFile("features/refactoring/localisation/neuro_l_simp_chinese.after.test.yml")
    }
}
