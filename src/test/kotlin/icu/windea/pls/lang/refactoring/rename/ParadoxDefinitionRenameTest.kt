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
class ParadoxDefinitionRenameTest : BasePlatformTestCase() {
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
    fun testRename_Definition_Overrides() {
        // Arrange
        val mainPath = "common/vtubers/vtuber_1.test.txt"
        markFileInfo(gameType, mainPath)
        myFixture.copyFileToProject("features/refactoring/common/vtubers/vtuber_1.test.txt", mainPath)

        val otherPath = "common/vtubers/vtuber_2.test.txt"
        markFileInfo(gameType, otherPath)
        myFixture.copyFileToProject("features/refactoring/common/vtubers/vtuber_2.test.txt", otherPath)

        // Ensure indexed
        IndexingTestUtil.waitUntilIndexesAreReadyInAllOpenedProjects()

        // Act
        myFixture.configureFromTempProjectFile(mainPath)
        val newName = "evil_neuro"
        myFixture.renameElementAtCaretUsingHandler(newName)

        // Assert
        myFixture.checkResultByFile(mainPath, "features/refactoring/common/vtubers/vtuber_1.after.test.txt", true)
        myFixture.checkResultByFile(otherPath, "features/refactoring/common/vtubers/vtuber_2.after.test.txt", true)
    }

    @Test
    fun testRename_Definition_RelatedLocalisations() {
        // Arrange
        val mainPath = "common/vtubers/vtuber_1.test.txt"
        markFileInfo(gameType, mainPath)
        myFixture.copyFileToProject("features/refactoring/common/vtubers/vtuber_1.test.txt", mainPath)

        val localisationEnglishPath = "localisation/definitions_l_english.test.yml"
        markFileInfo(gameType, localisationEnglishPath)
        myFixture.copyFileToProject("features/refactoring/localisation/definitions_l_english.test.yml", localisationEnglishPath)

        val localisationChinesePath = "localisation/definitions_l_simp_chinese.test.yml"
        markFileInfo(gameType, localisationChinesePath)
        myFixture.copyFileToProject("features/refactoring/localisation/definitions_l_simp_chinese.test.yml", localisationChinesePath)

        // Ensure indexed
        IndexingTestUtil.waitUntilIndexesAreReadyInAllOpenedProjects()

        // Act
        myFixture.configureFromTempProjectFile(mainPath)
        val newName = "evil_neuro"
        myFixture.renameElementAtCaretUsingHandler(newName)

        // Assert
        myFixture.checkResultByFile(mainPath, "features/refactoring/common/vtubers/vtuber_1.after.test.txt", true)
        myFixture.checkResultByFile(localisationEnglishPath, "features/refactoring/localisation/definitions_l_english.after.test.yml", true)
        myFixture.checkResultByFile(localisationChinesePath, "features/refactoring/localisation/definitions_l_simp_chinese.after.test.yml", true)
    }

    @Test
    fun testRename_Definition_References() {
        // Arrange
        val mainPath = "common/vtubers/vtuber_1.test.txt"
        markFileInfo(gameType, mainPath)
        myFixture.copyFileToProject("features/refactoring/common/vtubers/vtuber_1.test.txt", mainPath)

        val otherPath = "common/vtubers/vtuber_2.test.txt"
        markFileInfo(gameType, otherPath)
        myFixture.copyFileToProject("features/refactoring/common/vtubers/vtuber_2.test.txt", otherPath)

        val fanPath = "common/vtuber_fans/vtuber_fan_1.test.txt"
        markFileInfo(gameType, fanPath)
        myFixture.copyFileToProject("features/refactoring/common/vtuber_fans/vtuber_fan_1.test.txt", fanPath)

        // Ensure indexed
        IndexingTestUtil.waitUntilIndexesAreReadyInAllOpenedProjects()

        // Act
        myFixture.configureFromTempProjectFile(mainPath)
        val newName = "evil_neuro"
        myFixture.renameElementAtCaretUsingHandler(newName)

        // Assert
        myFixture.checkResultByFile(mainPath, "features/refactoring/common/vtubers/vtuber_1.after.test.txt", true)
        myFixture.checkResultByFile(otherPath, "features/refactoring/common/vtubers/vtuber_2.after.test.txt", true)
        myFixture.checkResultByFile(fanPath, "features/refactoring/common/vtuber_fans/vtuber_fan_1.after_definition.test.txt", true)
    }

    @Test
    fun testRename_Definition_Combined() {
        // Arrange
        val mainPath = "common/vtubers/vtuber_1.test.txt"
        markFileInfo(gameType, mainPath)
        myFixture.copyFileToProject("features/refactoring/common/vtubers/vtuber_1.test.txt", mainPath)

        val otherPath = "common/vtubers/vtuber_2.test.txt"
        markFileInfo(gameType, otherPath)
        myFixture.copyFileToProject("features/refactoring/common/vtubers/vtuber_2.test.txt", otherPath)

        val localisationEnglishPath = "localisation/definitions_l_english.test.yml"
        markFileInfo(gameType, localisationEnglishPath)
        myFixture.copyFileToProject("features/refactoring/localisation/definitions_l_english.test.yml", localisationEnglishPath)

        val localisationChinesePath = "localisation/definitions_l_simp_chinese.test.yml"
        markFileInfo(gameType, localisationChinesePath)
        myFixture.copyFileToProject("features/refactoring/localisation/definitions_l_simp_chinese.test.yml", localisationChinesePath)

        val fanPath = "common/vtuber_fans/vtuber_fan_1.test.txt"
        markFileInfo(gameType, fanPath)
        myFixture.copyFileToProject("features/refactoring/common/vtuber_fans/vtuber_fan_1.test.txt", fanPath)

        // Ensure indexed
        IndexingTestUtil.waitUntilIndexesAreReadyInAllOpenedProjects()

        // Act
        myFixture.configureFromTempProjectFile(mainPath)
        val newName = "evil_neuro"
        myFixture.renameElementAtCaretUsingHandler(newName)

        // Assert
        myFixture.checkResultByFile(mainPath, "features/refactoring/common/vtubers/vtuber_1.after.test.txt", true)
        myFixture.checkResultByFile(otherPath, "features/refactoring/common/vtubers/vtuber_2.after.test.txt", true)
        myFixture.checkResultByFile(localisationEnglishPath, "features/refactoring/localisation/definitions_l_english.after.test.yml", true)
        myFixture.checkResultByFile(localisationChinesePath, "features/refactoring/localisation/definitions_l_simp_chinese.after.test.yml", true)
        myFixture.checkResultByFile(fanPath, "features/refactoring/common/vtuber_fans/vtuber_fan_1.after_definition_combined.test.txt", true)
    }

    @Test
    fun testRename_Definition_ReferencesInScript_Multiple() {
        // Arrange
        val mainPath = "common/vtubers/vtuber_1.test.txt"
        markFileInfo(gameType, mainPath)
        myFixture.copyFileToProject("features/refactoring/common/vtubers/vtuber_1.test.txt", mainPath)

        val fanPath = "common/vtuber_fans/vtuber_fan_2.test.txt"
        markFileInfo(gameType, fanPath)
        myFixture.copyFileToProject("features/refactoring/common/vtuber_fans/vtuber_fan_2.test.txt", fanPath)

        // Ensure indexed
        IndexingTestUtil.waitUntilIndexesAreReadyInAllOpenedProjects()

        // Act
        myFixture.configureFromTempProjectFile(mainPath)
        val newName = "evil_neuro"
        myFixture.renameElementAtCaretUsingHandler(newName)

        // Assert
        myFixture.checkResultByFile(mainPath, "features/refactoring/common/vtubers/vtuber_1.after.test.txt", true)
        myFixture.checkResultByFile(fanPath, "features/refactoring/common/vtuber_fans/vtuber_fan_2.after_definition.test.txt", true)
    }

    // TODO 2.1.3 暂不验证以下类型的关联重命名：定义的相关图片、定义的生成的修正
}
