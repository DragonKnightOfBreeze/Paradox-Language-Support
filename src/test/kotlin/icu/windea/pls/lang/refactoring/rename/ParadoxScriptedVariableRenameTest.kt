package icu.windea.pls.lang.refactoring.rename

import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.refactoring.rename.RenameProcessor
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.lang.refactoring.rename.naming.AutomaticScriptedVariableRelatedLocalisationsRenamer
import icu.windea.pls.lang.refactoring.rename.naming.AutomaticScriptedVariablesRenamer
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
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
class ParadoxScriptedVariableRenameTest: BasePlatformTestCase() {
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
    fun testRename_ScriptedVariable_Overrides() {
        // Arrange
        val mainPath = "common/scripted_variables/neuro_vars_1.test.txt"
        markFileInfo(gameType, mainPath)
        val mainTestDataPath = "features/refactoring/scripted_variables/neuro_vars_1.test.txt"
        myFixture.configureByFile(mainTestDataPath)

        val otherPath = "common/scripted_variables/neuro_vars_2.test.txt"
        val otherFile = myFixture.copyFileToProject(
            "features/refactoring/scripted_variables/neuro_vars_2.test.txt",
            otherPath
        )
        otherFile.injectFileInfo(gameType, otherPath)

        // Ensure indexed
        myFixture.configureFromTempProjectFile(otherPath)
        myFixture.configureByFile(mainTestDataPath)

        // Act
        val elementAtCaret = myFixture.file.findElementAt(myFixture.caretOffset)
        val scriptedVariable = PsiTreeUtil.getParentOfType(elementAtCaret, ParadoxScriptScriptedVariable::class.java, false)!!

        val newName = "evil_neuro"
        val automaticRenamer = AutomaticScriptedVariablesRenamer(scriptedVariable, newName)

        RenameProcessor(project, scriptedVariable, newName, false, false).run()
        for ((e, n) in automaticRenamer.renames) {
            RenameProcessor(project, e, n, false, false).run()
        }
        commitAndSaveDocuments()

        // Assert
        myFixture.checkResultByFile("features/refactoring/scripted_variables/neuro_vars_1.after.test.txt")

        myFixture.configureFromTempProjectFile(otherPath)
        myFixture.checkResultByFile("features/refactoring/scripted_variables/neuro_vars_2.after.test.txt")
    }

    @Test
    fun testRename_ScriptedVariable_RelatedLocalisations() {
        // Arrange
        val mainPath = "common/scripted_variables/neuro_vars_1.test.txt"
        markFileInfo(gameType, mainPath)
        val mainTestDataPath = "features/refactoring/scripted_variables/neuro_vars_1.test.txt"
        myFixture.configureByFile(mainTestDataPath)

        val localisationEnglishPath = "localisation/sv_localisation_l_english.test.yml"
        val localisationEnglishFile = myFixture.copyFileToProject(
            "features/refactoring/scripted_variables/sv_localisation_l_english.test.yml",
            localisationEnglishPath
        )
        localisationEnglishFile.injectFileInfo(gameType, localisationEnglishPath)

        val localisationChinesePath = "localisation/sv_localisation_l_simp_chinese.test.yml"
        val localisationChineseFile = myFixture.copyFileToProject(
            "features/refactoring/scripted_variables/sv_localisation_l_simp_chinese.test.yml",
            localisationChinesePath
        )
        localisationChineseFile.injectFileInfo(gameType, localisationChinesePath)

        // Ensure indexed
        myFixture.configureFromTempProjectFile(localisationEnglishPath)
        myFixture.configureFromTempProjectFile(localisationChinesePath)
        myFixture.configureByFile(mainTestDataPath)

        // Act
        val elementAtCaret = myFixture.file.findElementAt(myFixture.caretOffset)
        val scriptedVariable = PsiTreeUtil.getParentOfType(elementAtCaret, ParadoxScriptScriptedVariable::class.java, false)!!

        val newName = "evil_neuro"
        val automaticRenamer = AutomaticScriptedVariableRelatedLocalisationsRenamer(scriptedVariable, newName)

        RenameProcessor(project, scriptedVariable, newName, false, false).run()
        for ((e, n) in automaticRenamer.renames) {
            RenameProcessor(project, e, n, false, false).run()
        }
        commitAndSaveDocuments()

        // Assert
        myFixture.checkResultByFile("features/refactoring/scripted_variables/neuro_vars_1.after.test.txt")

        myFixture.configureFromTempProjectFile(localisationEnglishPath)
        myFixture.checkResultByFile("features/refactoring/scripted_variables/sv_localisation_l_english.after.test.yml")

        myFixture.configureFromTempProjectFile(localisationChinesePath)
        myFixture.checkResultByFile("features/refactoring/scripted_variables/sv_localisation_l_simp_chinese.after.test.yml")
    }
}
