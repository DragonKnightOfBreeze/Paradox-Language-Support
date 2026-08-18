package icu.windea.pls.lang.refactoring.rename

import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.TestDataFile
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.core.convertPath
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.ChronicleTestScope
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxScriptedVariableRenameTest : BasePlatformTestCase(), ChronicleTestScope {
    private val gameType = ParadoxGameType.Stellaris

    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        addAdditionalAllowedRoots(testDataPath)
        markIntegrationTest()
        markRootDirectory("features/refactoring")
        markConfigDirectory("features/refactoring/.config")
        initConfigGroups(project, gameType)
    }

    @After
    fun doTearDown() {
        clearIntegrationTest()
        PsiDocumentManager.getInstance(project).commitAllDocuments()
        FileDocumentManager.getInstance().saveAllDocuments()
    }

    @Test
    fun testRename_ScriptedVariable_Overrides() {
        // Arrange
        val mainPath = configureMarkedFile("features/refactoring/common/scripted_variables/neuro_vars_1.test.txt")
        val otherPath = configureMarkedFile("features/refactoring/common/scripted_variables/neuro_vars_2.test.txt")

        // Ensure indexed
        IndexingTestUtil.waitUntilIndexesAreReady(project)

        // Act
        val newName = "evil_neuro"
        myFixture.configureFromTempProjectFile(mainPath)
        myFixture.renameElementAtCaretUsingHandler(newName)

        // Assert
        checkMarkedResult(mainPath, "after")
        checkMarkedResult(otherPath, "after")
    }

    @Test
    fun testRename_ScriptedVariable_RelatedLocalisations() {
        // Arrange
        val mainPath = configureMarkedFile("features/refactoring/common/scripted_variables/neuro_vars_1.test.txt")
        val localisationEnglishPath = configureMarkedFile("features/refactoring/localisation/scripted_variables_l_english.test.yml")
        val localisationChinesePath = configureMarkedFile("features/refactoring/localisation/scripted_variables_l_simp_chinese.test.yml")

        // Ensure indexed
        IndexingTestUtil.waitUntilIndexesAreReady(project)

        // Act
        val newName = "evil_neuro"
        myFixture.configureFromTempProjectFile(mainPath)
        myFixture.renameElementAtCaretUsingHandler(newName)

        // Assert
        checkMarkedResult(mainPath, "after")
        checkMarkedResult(localisationEnglishPath, "after")
        checkMarkedResult(localisationChinesePath, "after")
    }

    private fun configureMarkedFile(@TestDataFile testDataPath: String, path: String = testDataPath.removePrefix("features/refactoring/")): String {
        markFileInfo(gameType, path)
        myFixture.configureByFile(testDataPath)
        return testDataPath
    }

    @Suppress("SameParameterValue")
    private fun checkMarkedResult(@TestDataFile testDataPath: String, tag: String) {
        val expectedPath = testDataPath.convertPath(greedyExtension = true) { b, e -> "$b.$tag$e" }
        myFixture.checkResultByFile(testDataPath, expectedPath, true)
    }
}
