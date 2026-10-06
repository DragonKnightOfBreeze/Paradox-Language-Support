package icu.windea.pls.lang.refactoring.rename

import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.TestDataFile
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.core.convertPath
import icu.windea.pls.lang.psi.light.ParadoxDefinitionSnippetLightElement
import icu.windea.pls.lang.refactoring.rename.naming.ParadoxDefinitionSnippetAutomaticRenamer
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.ChronicleTestScope
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see ParadoxDefinitionSnippetAutomaticRenamer
 * @see ParadoxDefinitionSnippetLightElement
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxDefinitionSnippetRenameTest : BasePlatformTestCase(), ChronicleTestScope {
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

    // region Tests

    @Test
    fun testRename_DefinitionSnippet_RelatedDefinitions() {
        // Arrange
        val definitionPath = configureMarkedFile("features/refactoring/common/test_types/00_test_types.test.txt")
        val usagePath = configureMarkedFile("features/refactoring/common/test_types/01_usage.test.txt")

        // Ensure indexed
        IndexingTestUtil.waitUntilIndexesAreReady(project)

        // Act
        val newName = "bar"
        myFixture.configureFromTempProjectFile(usagePath)
        myFixture.renameElementAtCaretUsingHandler(newName)

        // Assert
        checkMarkedResult(definitionPath, "after_definition")
        checkMarkedResult(usagePath, "after")
    }

    // endregion

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
