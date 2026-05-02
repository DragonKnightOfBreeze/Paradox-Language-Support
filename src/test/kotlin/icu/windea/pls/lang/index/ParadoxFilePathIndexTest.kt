package icu.windea.pls.lang.index

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.indexing.FileBasedIndex
import icu.windea.pls.test.clearIntegrationTest
import icu.windea.pls.test.markFileInfo
import icu.windea.pls.test.markIntegrationTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import icu.windea.pls.model.ParadoxGameType

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxFilePathIndexTest : BasePlatformTestCase() {
    private val gameType = ParadoxGameType.Stellaris

    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() = markIntegrationTest()

    @After
    fun doTearDown() = clearIntegrationTest()

    // region Basic

    @Test
    fun testFilePathIndex_Basic() {
        val relPath = "common/example.test.txt"
        markFileInfo(gameType, relPath)
        myFixture.configureByFile("script/syntax/example.test.txt")

        val allKeys = FileBasedIndex.getInstance().getAllKeys(PlsIndexKeys.FilePath, project)
        Assert.assertTrue(relPath in allKeys)
    }

    @Test
    fun testFilePathIndex_Localisation() {
        // 本地化文件应当被索引
        val relPath = "localisation/ui/ui_l_english.test.yml"
        markFileInfo(gameType, relPath)
        myFixture.configureByFile("features/index/localisation/ui/ui_l_english.test.yml")

        val allKeys = FileBasedIndex.getInstance().getAllKeys(PlsIndexKeys.FilePath, project)
        Assert.assertTrue(relPath in allKeys)
    }

    // endregion
}
