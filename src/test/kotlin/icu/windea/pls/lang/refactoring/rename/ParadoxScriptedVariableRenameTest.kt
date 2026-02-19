package icu.windea.pls.lang.refactoring.rename

import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.addAdditionalAllowedRoots
import icu.windea.pls.test.clearIntegrationTest
import icu.windea.pls.test.initConfigGroups
import icu.windea.pls.test.markConfigDirectory
import icu.windea.pls.test.markFileInfo
import icu.windea.pls.test.markIntegrationTest
import icu.windea.pls.test.markRootDirectory
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxScriptedVariableRenameTest : BasePlatformTestCase() {
    private val gameType = ParadoxGameType.Stellaris

    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun setup() {
        addAdditionalAllowedRoots(testDataPath)
        markIntegrationTest()
        markRootDirectory("features/refactoring")
        markConfigDirectory("features/refactoring/.config")
        initConfigGroups(project, gameType)
    }

    @After
    fun clear() {
        clearIntegrationTest()
        PsiDocumentManager.getInstance(project).commitAllDocuments()
        FileDocumentManager.getInstance().saveAllDocuments()
    }

    @Test
    fun testRename_ScriptedVariable_Overrides() {
        // Arrange
        val mainPath = "common/scripted_variables/neuro_vars_1.test.txt"
        markFileInfo(gameType, mainPath)
        myFixture.copyFileToProject("features/refactoring/common/scripted_variables/neuro_vars_1.test.txt", mainPath)

        val otherPath = "common/scripted_variables/neuro_vars_2.test.txt"
        markFileInfo(gameType, otherPath)
        myFixture.copyFileToProject("features/refactoring/common/scripted_variables/neuro_vars_2.test.txt", otherPath)

        // Ensure indexed
        IndexingTestUtil.waitUntilIndexesAreReadyInAllOpenedProjects()

        // Act
        val newName = "evil_neuro"
        myFixture.configureFromTempProjectFile(mainPath)
        myFixture.renameElementAtCaretUsingHandler(newName)

        // Assert
        myFixture.checkResultByFile(mainPath, "features/refactoring/common/scripted_variables/neuro_vars_1.after.test.txt", true)
        myFixture.checkResultByFile(otherPath, "features/refactoring/common/scripted_variables/neuro_vars_2.after.test.txt", true)
    }

    @Test
    fun testRename_ScriptedVariable_RelatedLocalisations() {
        // Arrange
        val mainPath = "common/scripted_variables/neuro_vars_1.test.txt"
        markFileInfo(gameType, mainPath)
        myFixture.copyFileToProject("features/refactoring/common/scripted_variables/neuro_vars_1.test.txt", mainPath)

        val localisationEnglishPath = "localisation/scripted_variables_l_english.test.yml"
        markFileInfo(gameType, localisationEnglishPath)
        myFixture.copyFileToProject("features/refactoring/localisation/scripted_variables_l_english.test.yml", localisationEnglishPath)

        val localisationChinesePath = "localisation/scripted_variables_l_simp_chinese.test.yml"
        markFileInfo(gameType, localisationChinesePath)
        myFixture.copyFileToProject("features/refactoring/localisation/scripted_variables_l_simp_chinese.test.yml", localisationChinesePath)

        // Ensure indexed
        IndexingTestUtil.waitUntilIndexesAreReadyInAllOpenedProjects()

        // Act
        val newName = "evil_neuro"
        myFixture.configureFromTempProjectFile(mainPath)
        myFixture.renameElementAtCaretUsingHandler(newName)

        // Assert
        myFixture.checkResultByFile(mainPath, "features/refactoring/common/scripted_variables/neuro_vars_1.after.test.txt", true)
        myFixture.checkResultByFile(localisationEnglishPath, "features/refactoring/localisation/scripted_variables_l_english.after.test.yml", true)
        myFixture.checkResultByFile(localisationChinesePath, "features/refactoring/localisation/scripted_variables_l_simp_chinese.after.test.yml", true)
    }

    @Test
    fun testRename_ScriptedVariable_References() {
        // Arrange
        val mainPath = "common/scripted_variables/neuro_vars_1.test.txt"
        markFileInfo(gameType, mainPath)
        myFixture.copyFileToProject("features/refactoring/common/scripted_variables/neuro_vars_1.test.txt", mainPath)

        val fanPath = "common/vtuber_fans/vtuber_fan_1.test.txt"
        markFileInfo(gameType, fanPath)
        myFixture.copyFileToProject("features/refactoring/common/vtuber_fans/vtuber_fan_1.test.txt", fanPath)

        // Ensure indexed
        IndexingTestUtil.waitUntilIndexesAreReadyInAllOpenedProjects()

        // Act
        val newName = "evil_neuro"
        myFixture.configureFromTempProjectFile(mainPath)
        myFixture.renameElementAtCaretUsingHandler(newName)

        // Assert
        myFixture.checkResultByFile(mainPath, "features/refactoring/common/scripted_variables/neuro_vars_1.after.test.txt", true)
        myFixture.checkResultByFile(fanPath, "features/refactoring/common/vtuber_fans/vtuber_fan_1.after_scripted_variable.test.txt", true)
    }

    @Test
    fun testRename_ScriptedVariable_Combined() {
        // Arrange
        val mainPath = "common/scripted_variables/neuro_vars_1.test.txt"
        markFileInfo(gameType, mainPath)
        myFixture.copyFileToProject("features/refactoring/common/scripted_variables/neuro_vars_1.test.txt", mainPath)

        val otherPath = "common/scripted_variables/neuro_vars_2.test.txt"
        markFileInfo(gameType, otherPath)
        myFixture.copyFileToProject("features/refactoring/common/scripted_variables/neuro_vars_2.test.txt", otherPath)

        val localisationEnglishPath = "localisation/scripted_variables_l_english.test.yml"
        markFileInfo(gameType, localisationEnglishPath)
        myFixture.copyFileToProject("features/refactoring/localisation/scripted_variables_l_english.test.yml", localisationEnglishPath)

        val localisationChinesePath = "localisation/scripted_variables_l_simp_chinese.test.yml"
        markFileInfo(gameType, localisationChinesePath)
        myFixture.copyFileToProject("features/refactoring/localisation/scripted_variables_l_simp_chinese.test.yml", localisationChinesePath)

        val fanPath = "common/vtuber_fans/vtuber_fan_1.test.txt"
        markFileInfo(gameType, fanPath)
        myFixture.copyFileToProject("features/refactoring/common/vtuber_fans/vtuber_fan_1.test.txt", fanPath)

        // Ensure indexed
        IndexingTestUtil.waitUntilIndexesAreReadyInAllOpenedProjects()

        // Act
        val newName = "evil_neuro"
        myFixture.configureFromTempProjectFile(mainPath)
        myFixture.renameElementAtCaretUsingHandler(newName)

        // Assert
        myFixture.checkResultByFile(mainPath, "features/refactoring/common/scripted_variables/neuro_vars_1.after.test.txt", true)
        myFixture.checkResultByFile(otherPath, "features/refactoring/common/scripted_variables/neuro_vars_2.after.test.txt", true)
        myFixture.checkResultByFile(localisationEnglishPath, "features/refactoring/localisation/scripted_variables_l_english.after.test.yml", true)
        myFixture.checkResultByFile(localisationChinesePath, "features/refactoring/localisation/scripted_variables_l_simp_chinese.after.test.yml", true)
        myFixture.checkResultByFile(fanPath, "features/refactoring/common/vtuber_fans/vtuber_fan_1.after_scripted_variable_combined.test.txt", true)
    }

    @Test
    fun testRename_ScriptedVariable_ReferencesInScript_Multiple() {
        // Arrange
        val mainPath = "common/scripted_variables/neuro_vars_1.test.txt"
        markFileInfo(gameType, mainPath)
        myFixture.copyFileToProject("features/refactoring/common/scripted_variables/neuro_vars_1.test.txt", mainPath)

        val fanPath = "common/vtuber_fans/vtuber_fan_2.test.txt"
        markFileInfo(gameType, fanPath)
        myFixture.copyFileToProject("features/refactoring/common/vtuber_fans/vtuber_fan_2.test.txt", fanPath)

        // Ensure indexed
        IndexingTestUtil.waitUntilIndexesAreReadyInAllOpenedProjects()

        // Act
        val newName = "evil_neuro"
        myFixture.configureFromTempProjectFile(mainPath)
        myFixture.renameElementAtCaretUsingHandler(newName)

        // Assert
        myFixture.checkResultByFile(mainPath, "features/refactoring/common/scripted_variables/neuro_vars_1.after.test.txt", true)
        myFixture.checkResultByFile(fanPath, "features/refactoring/common/vtuber_fans/vtuber_fan_2.after_scripted_variable.test.txt", true)
    }
}
