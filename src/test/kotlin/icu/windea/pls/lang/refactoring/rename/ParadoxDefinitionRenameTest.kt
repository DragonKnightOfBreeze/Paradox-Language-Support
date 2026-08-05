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
class ParadoxDefinitionRenameTest : BasePlatformTestCase(), ChronicleTestScope {
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
    fun testRename_Definition_Overrides() {
        // Arrange
        val mainPath = configureMarkedFile("features/refactoring/common/vtubers/vtuber_1.test.txt")
        val otherPath = configureMarkedFile("features/refactoring/common/vtubers/vtuber_2.test.txt")

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
    fun testRename_Definition_RelatedLocalisations() {
        // Arrange
        val mainPath = configureMarkedFile("features/refactoring/common/vtubers/vtuber_1.test.txt")
        val localisationEnglishPath = configureMarkedFile("features/refactoring/localisation/definitions_l_english.test.yml")
        val localisationChinesePath = configureMarkedFile("features/refactoring/localisation/definitions_l_simp_chinese.test.yml")

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

    @Test
    fun testRename_Definition_References() {
        // Arrange
        val mainPath = configureMarkedFile("features/refactoring/common/vtubers/vtuber_1.test.txt")
        val otherPath = configureMarkedFile("features/refactoring/common/vtubers/vtuber_2.test.txt")
        val fanPath = configureMarkedFile("features/refactoring/common/vtuber_fans/vtuber_fan_1.test.txt")

        // Ensure indexed
        IndexingTestUtil.waitUntilIndexesAreReady(project)

        // Act
        val newName = "evil_neuro"
        myFixture.configureFromTempProjectFile(mainPath)
        myFixture.renameElementAtCaretUsingHandler(newName)

        // Assert
        checkMarkedResult(mainPath, "after")
        checkMarkedResult(otherPath, "after")
        checkMarkedResult(fanPath, "after_definition")
    }

    @Test
    fun testRename_Definition_Combined() {
        // Arrange
        val mainPath = configureMarkedFile("features/refactoring/common/vtubers/vtuber_1.test.txt")
        val otherPath = configureMarkedFile("features/refactoring/common/vtubers/vtuber_2.test.txt")
        val localisationEnglishPath = configureMarkedFile("features/refactoring/localisation/definitions_l_english.test.yml")
        val localisationChinesePath = configureMarkedFile("features/refactoring/localisation/definitions_l_simp_chinese.test.yml")
        val fanPath = configureMarkedFile("features/refactoring/common/vtuber_fans/vtuber_fan_1.test.txt")

        // Ensure indexed
        IndexingTestUtil.waitUntilIndexesAreReady(project)

        // Act
        val newName = "evil_neuro"
        myFixture.configureFromTempProjectFile(mainPath)
        myFixture.renameElementAtCaretUsingHandler(newName)

        // Assert
        checkMarkedResult(mainPath, "after")
        checkMarkedResult(otherPath, "after")
        checkMarkedResult(localisationEnglishPath, "after")
        checkMarkedResult(localisationChinesePath, "after")
        checkMarkedResult(fanPath, "after_definition_combined")
    }

    @Test
    fun testRename_Definition_ReferencesInScript_Multiple() {
        // Arrange
        val mainPath = configureMarkedFile("features/refactoring/common/vtubers/vtuber_1.test.txt")
        val fanPath = configureMarkedFile("features/refactoring/common/vtuber_fans/vtuber_fan_2.test.txt")

        // Ensure indexed
        IndexingTestUtil.waitUntilIndexesAreReady(project)

        // Act
        val newName = "evil_neuro"
        myFixture.configureFromTempProjectFile(mainPath)
        myFixture.renameElementAtCaretUsingHandler(newName)

        // Assert
        checkMarkedResult(mainPath, "after")
        checkMarkedResult(fanPath, "after_definition")
    }

    // TODO 2.1.3+ 暂不验证以下类型的关联重命名：定义的相关图片、定义的生成的修正

    // endregion

    private fun configureMarkedFile(@TestDataFile testDataPath: String, path: String = testDataPath.removePrefix("features/refactoring/")): String {
        markFileInfo(gameType, path)
        myFixture.configureByFile(testDataPath)
        return testDataPath
    }

    private fun checkMarkedResult(@TestDataFile testDataPath: String, tag: String) {
        val expectedPath = testDataPath.convertPath { b, e -> "$b.$tag$e" }
        myFixture.checkResultByFile(testDataPath, expectedPath, true)
    }
}
