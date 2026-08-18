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
class ParadoxComplexEnumValueRenameTest : BasePlatformTestCase(), ChronicleTestScope {
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
    fun testRename_ComplexEnumValue() {
        // Arrange
        val mainPath = configureMarkedFile("features/refactoring/common/vtubers/1_1_vtubers.test.txt")
        val otherPath = configureMarkedFile("features/refactoring/common/vtuber_tags/1_1_vtuber_tags.test.txt")

        // Ensure indexed
        IndexingTestUtil.waitUntilIndexesAreReady(project)

        // Act
        val newName = "tag_not_ai"
        myFixture.configureFromTempProjectFile(mainPath)
        myFixture.renameElementAtCaretUsingHandler(newName)

        // Assert
        checkMarkedResult(mainPath, "after")
        checkMarkedResult(otherPath, "after")
    }

    @Test
    fun testRename_ComplexEnumValue_FromDeclaration() {
        // Arrange
        val mainPath = configureMarkedFile("features/refactoring/common/vtuber_tags/1_2_vtuber_tags.test.txt")
        val otherPath = configureMarkedFile("features/refactoring/common/vtubers/1_2_vtubers.test.txt")

        // Ensure indexed
        IndexingTestUtil.waitUntilIndexesAreReady(project)

        // Act
        val newName = "tag_not_ai"
        myFixture.configureFromTempProjectFile(mainPath)
        myFixture.renameElementAtCaretUsingHandler(newName)

        // Assert
        checkMarkedResult(mainPath, "after")
        checkMarkedResult(otherPath, "after")
    }

    @Test
    fun testRename_ComplexEnumValue_RelatedLocalisations() {
        // Arrange
        val mainPath = configureMarkedFile("features/refactoring/common/vtubers/1_3_vtubers.test.txt")
        val otherPath = configureMarkedFile("features/refactoring/common/vtuber_tags/1_3_vtuber_tags.test.txt") // necessary
        val localisationEnglishPath = configureMarkedFile("features/refactoring/localisation/1_3_main_l_english.test.yml")
        val localisationChinesePath = configureMarkedFile("features/refactoring/localisation/1_3_main_l_simp_chinese.test.yml")

        // Ensure indexed
        IndexingTestUtil.waitUntilIndexesAreReady(project)

        // Act
        val newName = "tag_not_ai"
        myFixture.configureFromTempProjectFile(mainPath)
        myFixture.renameElementAtCaretUsingHandler(newName)

        // Assert
        checkMarkedResult(mainPath, "after")
        checkMarkedResult(otherPath, "after")
        checkMarkedResult(localisationEnglishPath, "after")
        checkMarkedResult(localisationChinesePath, "after")
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
