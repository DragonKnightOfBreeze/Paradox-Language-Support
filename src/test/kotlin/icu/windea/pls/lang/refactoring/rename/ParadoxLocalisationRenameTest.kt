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
class ParadoxLocalisationRenameTest : BasePlatformTestCase(), ChronicleTestScope {
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
    fun testRename_Localisation_OverridesAndReferences() {
        // Arrange
        val localisationEnglishPath = configureMarkedFile("features/refactoring/localisation/localisations_l_english.test.yml")
        val localisationChinesePath = configureMarkedFile("features/refactoring/localisation/localisations_l_simp_chinese.test.yml")
        val fanPath = configureMarkedFile("features/refactoring/common/vtuber_fans/vtuber_fan_1.test.txt")

        // Ensure indexed
        IndexingTestUtil.waitUntilIndexesAreReady(project)

        // Act
        val newName = "evil_neuro"
        myFixture.configureFromTempProjectFile(localisationEnglishPath)
        myFixture.renameElementAtCaretUsingHandler(newName)

        // Assert
        checkMarkedResult(localisationEnglishPath, "after")
        checkMarkedResult(localisationChinesePath, "after")
        checkMarkedResult(fanPath, "after_localisation")
    }

    @Test
    fun testRename_Localisation_ReferencesInScript_Multiple() {
        // Arrange
        val localisationEnglishPath = configureMarkedFile("features/refactoring/localisation/localisations_l_english.test.yml")
        val localisationChinesePath = configureMarkedFile("features/refactoring/localisation/localisations_l_simp_chinese.test.yml")
        val fanPath = configureMarkedFile("features/refactoring/common/vtuber_fans/vtuber_fan_2.test.txt")

        // Ensure indexed
        IndexingTestUtil.waitUntilIndexesAreReady(project)

        // Act
        val newName = "evil_neuro"
        myFixture.configureFromTempProjectFile(localisationEnglishPath)
        myFixture.renameElementAtCaretUsingHandler(newName)

        // Assert
        checkMarkedResult(localisationEnglishPath, "after")
        checkMarkedResult(localisationChinesePath, "after")
        checkMarkedResult(fanPath, "after_localisation")
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
