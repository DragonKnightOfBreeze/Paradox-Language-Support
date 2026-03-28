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
class ParadoxComplexEnumValueRenameTest : BasePlatformTestCase() {
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

    private fun configureFile(path: String): String {
        markFileInfo(gameType, path)
        myFixture.copyFileToProject("features/refactoring/$path", path)
        return path
    }

    @Test
    fun testRename_ComplexEnumValue() {
        val mainPath = "common/vtubers/1_1_vtubers.test.txt"
        val otherPath = "common/vtuber_tags/1_1_vtuber_tags.test.txt"

        // Arrange
        configureFile(mainPath)
        configureFile(otherPath)

        // Ensure indexed
        IndexingTestUtil.waitUntilIndexesAreReady(project)

        // Act
        val newName = "tag_not_ai"
        myFixture.configureFromTempProjectFile(mainPath)
        myFixture.renameElementAtCaretUsingHandler(newName)

        // Assert
        myFixture.checkResultByFile(mainPath, "features/refactoring/common/vtubers/1_1_vtubers.after.test.txt", true)
        myFixture.checkResultByFile(otherPath, "features/refactoring/common/vtuber_tags/1_1_vtuber_tags.after.test.txt", true)
    }

    @Test
    fun testRename_ComplexEnumValue_FromDeclaration() {
        val mainPath = "common/vtuber_tags/1_2_vtuber_tags.test.txt"
        val otherPath = "common/vtubers/1_2_vtubers.test.txt"

        // Arrange
        configureFile(mainPath)
        configureFile(otherPath)

        // Ensure indexed
        IndexingTestUtil.waitUntilIndexesAreReady(project)

        // Act
        val newName = "tag_not_ai"
        myFixture.configureFromTempProjectFile(mainPath)
        myFixture.renameElementAtCaretUsingHandler(newName)

        // Assert
        myFixture.checkResultByFile(mainPath, "features/refactoring/common/vtuber_tags/1_2_vtuber_tags.after.test.txt", true)
        myFixture.checkResultByFile(otherPath, "features/refactoring/common/vtubers/1_2_vtubers.after.test.txt", true)
    }

    @Test
    fun testRename_ComplexEnumValue_RelatedLocalisations() {
        val mainPath = "common/vtubers/1_3_vtubers.test.txt"
        val otherPath = "common/vtuber_tags/1_3_vtuber_tags.test.txt"
        val localisationEnglishPath = "localisation/1_3_main_l_english.test.yml"
        val localisationChinesePath = "localisation/1_3_main_l_simp_chinese.test.yml"

        // Arrange
        configureFile(mainPath)
        configureFile(otherPath) // necessary
        configureFile(localisationEnglishPath)
        configureFile(localisationChinesePath)

        // Ensure indexed
        IndexingTestUtil.waitUntilIndexesAreReady(project)

        // Act
        val newName = "tag_not_ai"
        myFixture.configureFromTempProjectFile(mainPath)
        myFixture.renameElementAtCaretUsingHandler(newName)

        // Assert
        myFixture.checkResultByFile(mainPath, "features/refactoring/common/vtubers/1_3_vtubers.after.test.txt", true)
        myFixture.checkResultByFile(otherPath, "features/refactoring/common/vtuber_tags/1_3_vtuber_tags.after.test.txt", true)
        myFixture.checkResultByFile(localisationEnglishPath, "features/refactoring/localisation/1_3_main_l_english.after.test.yml", true)
        myFixture.checkResultByFile(localisationChinesePath, "features/refactoring/localisation/1_3_main_l_simp_chinese.after.test.yml", true)
    }
}
