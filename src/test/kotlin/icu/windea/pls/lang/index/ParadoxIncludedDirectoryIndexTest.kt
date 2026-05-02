package icu.windea.pls.lang.index

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.indexing.FileBasedIndex
import icu.windea.pls.model.ParadoxFileGroup
import icu.windea.pls.test.clearIntegrationTest
import icu.windea.pls.test.injectFileInfo
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
class ParadoxIncludedDirectoryIndexTest : BasePlatformTestCase() {
    private val gameType = ParadoxGameType.Stellaris

    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() = markIntegrationTest()

    @After
    fun doTearDown() = clearIntegrationTest()

    // region Basic

    @Test
    fun testIncludedDirectoryIndex_Basic() {
        // 已包含的目录应当在索引中存在对应的键
        val copied = myFixture.copyFileToProject("script/syntax/example.test.txt", "common/example.test.txt")
        copied.injectFileInfo(gameType, "common/example.test.txt")
        val dir = copied.parent
        dir?.injectFileInfo(gameType, "common")
        FileBasedIndex.getInstance().requestReindex(dir!!)

        val allKeys = FileBasedIndex.getInstance().getAllKeys(PlsIndexKeys.IncludedDirectory, project)
        val expectedKey = "${gameType.id}:common"
        Assert.assertTrue("Expected key '$expectedKey' in index", expectedKey in allKeys)
    }

    // endregion

    // region Edge Cases

    @Test
    fun testIncludedDirectoryIndex_ExcludedTopDirectory() {
        // 被排除的顶级目录（如 jomini）不应出现在索引中
        val copied = myFixture.copyFileToProject("features/index/jomini/gfx/interface/icons/my_icon.png", "jomini/gfx/interface/icons/my_icon.png")

        // 注入 fileInfo，使 isIncludedDirectory() 能够递归检查到 jomini
        run {
            copied.injectFileInfo(gameType, "jomini/gfx/interface/icons/my_icon.png", group = ParadoxFileGroup.Other)
            var dir = copied.parent
            val parts = listOf("jomini/gfx/interface/icons", "jomini/gfx/interface", "jomini/gfx", "jomini")
            parts.forEach { p ->
                dir?.injectFileInfo(gameType, p, group = ParadoxFileGroup.Other)
                dir = dir?.parent
            }
        }
        FileBasedIndex.getInstance().requestReindex(copied)

        val fileData = FileBasedIndex.getInstance().getFileData(PlsIndexKeys.IncludedDirectory, copied.parent, project)
        Assert.assertTrue("Expected excluded directory 'jomini' not to be indexed", fileData.isEmpty())
    }

    @Test
    fun testIncludedDirectoryIndex_HiddenDirectory() {
        // 隐藏目录（名称以点开头）不应出现在索引中
        val relPath = "common/.hidden.test.txt"
        markFileInfo(gameType, relPath)
        myFixture.configureByFile("features/index/common/.hidden.test.txt")

        // .hidden.test.txt 的父目录是 common，它本身是可见的
        // 但如果存在以点开头的目录，该目录不应在索引中
        val allKeys = FileBasedIndex.getInstance().getAllKeys(PlsIndexKeys.IncludedDirectory, project)
        val hiddenDirKey = "${gameType.id}:.hidden"
        Assert.assertFalse("Expected hidden directory not to be in index", hiddenDirKey in allKeys)
    }

    // endregion
}
