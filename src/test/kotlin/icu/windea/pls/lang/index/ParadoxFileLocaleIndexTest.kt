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
 * @see ParadoxFileLocaleIndex
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxFileLocaleIndexTest : BasePlatformTestCase() {
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
    fun test_English() {
        markAndConfigureByFile("features/index/localisation/ui/ui_l_english.test.yml")

        val data = FileBasedIndex.getInstance().getFileData(ChronicleIndexKeys.FileLocale, myFixture.file.virtualFile, project)
        val key = data.keys.singleOrNull()
        Assert.assertEquals("l_english", key)
    }

    @Test
    fun test_SimpChinese() {
        markAndConfigureByFile("features/index/localisation/simp_chinese/ui_l_simp_chinese.test.yml")

        val data = FileBasedIndex.getInstance().getFileData(ChronicleIndexKeys.FileLocale, myFixture.file.virtualFile, project)
        val key = data.keys.singleOrNull()
        Assert.assertEquals("l_simp_chinese", key)
    }

    // endregion
}
