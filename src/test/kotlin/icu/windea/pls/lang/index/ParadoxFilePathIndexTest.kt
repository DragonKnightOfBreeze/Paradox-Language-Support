package icu.windea.pls.lang.index

import com.intellij.psi.PsiFile
import com.intellij.testFramework.TestDataFile
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.indexing.FileBasedIndex
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.clearIntegrationTest
import icu.windea.pls.test.markFileInfo
import icu.windea.pls.test.markIntegrationTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see ParadoxFilePathIndex
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxFilePathIndexTest : BasePlatformTestCase() {
    private val gameType = ParadoxGameType.Stellaris

    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() = markIntegrationTest()

    @After
    fun doTearDown() = clearIntegrationTest()

    private fun markAndConfigureByFile(@TestDataFile testDataPath: String, relPath: String = testDataPath.removePrefix("features/index/")): PsiFile {
        markFileInfo(gameType, relPath)
        return myFixture.configureByFile(testDataPath)
    }

    // region Basic

    @Test
    fun test_Basic() {
        markAndConfigureByFile("features/index/common/test/local_vars.test.txt")

        val path = "common/test/local_vars.test.txt"
        val allKeys = FileBasedIndex.getInstance().getAllKeys(ChronicleIndexKeys.FilePath, project)
        Assert.assertTrue(path in allKeys)
    }

    @Test
    fun test_Localisation() {
        // 本地化文件应当被索引
        markAndConfigureByFile("features/index/localisation/ui/ui_l_english.test.yml")

        val path = "localisation/ui/ui_l_english.test.yml"
        val allKeys = FileBasedIndex.getInstance().getAllKeys(ChronicleIndexKeys.FilePath, project)
        Assert.assertTrue(path in allKeys)
    }

    // endregion
}
