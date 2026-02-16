package icu.windea.pls.lang.index

import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.indexing.FileBasedIndex
import icu.windea.pls.model.ParadoxFileGroup
import icu.windea.pls.model.ParadoxGameType
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

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxFilePathIndexTest : BasePlatformTestCase() {
    private val gameType = ParadoxGameType.Stellaris

    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun setup() = markIntegrationTest()

    @After
    fun clear() = clearIntegrationTest()

    // region Basic

    @Test
    fun testFilePathIndex_Basic() {
        val relPath = "common/code_style_settings.test.txt"
        markFileInfo(gameType, relPath)
        myFixture.configureByFile("script/syntax/code_style_settings.test.txt")

        val project = project
        val scope = GlobalSearchScope.projectScope(project)
        val values = FileBasedIndex.getInstance().getValues(PlsIndexKeys.FilePath, relPath, scope)
        Assert.assertTrue(values.isNotEmpty())
        val info = values.single()
        Assert.assertEquals("common", info.directory)
        Assert.assertEquals(gameType, info.gameType)
        Assert.assertTrue(info.included)
    }

    @Test
    fun testFilePathIndex_Localisation() {
        // index should record localisation yml files as included with correct directory and gameType
        val relPath = "localisation/ui/ui_l_english.test.yml"
        markFileInfo(gameType, relPath)
        myFixture.configureByFile("features/index/localisation/ui/ui_l_english.test.yml")

        val project = project
        val scope = GlobalSearchScope.projectScope(project)
        val values = FileBasedIndex.getInstance().getValues(PlsIndexKeys.FilePath, relPath, scope)
        Assert.assertTrue(values.isNotEmpty())
        val info = values.single()
        Assert.assertEquals("localisation/ui", info.directory)
        Assert.assertEquals(gameType, info.gameType)
        Assert.assertTrue(info.included)
    }

    // endregion

    // region Edge Cases

    @Test
    fun testFilePathIndex_ExcludedTopDirectory() {
        // files under excluded top directories (e.g., jomini) should be marked as included=false
        val copied = myFixture.copyFileToProject("features/index/jomini/gfx/interface/icons/my_icon.png", "jomini/gfx/interface/icons/my_icon.png")
        val relPath = "jomini/gfx/interface/icons/my_icon.png"

        // inject fileInfo for the file and parent directories so isIncluded() can recurse to 'jomini'
        run {
            copied.injectFileInfo(gameType, relPath, group = ParadoxFileGroup.Other)
            var dir = copied.parent
            val parts = listOf("jomini/gfx/interface/icons", "jomini/gfx/interface", "jomini/gfx", "jomini")
            parts.forEach { p ->
                dir?.injectFileInfo(gameType, p, group = ParadoxFileGroup.Other)
                dir = dir?.parent
            }
        }
        FileBasedIndex.getInstance().requestReindex(copied)

        val project = project
        val scope = GlobalSearchScope.projectScope(project)
        val values = FileBasedIndex.getInstance().getValues(PlsIndexKeys.FilePath, relPath, scope)
        Assert.assertTrue(values.isNotEmpty())
        val info = values.single()
        Assert.assertEquals("jomini/gfx/interface/icons", info.directory)
        Assert.assertEquals(gameType, info.gameType)
        Assert.assertFalse("Expected included=false for excluded top directory", info.included)
    }

    @Test
    fun testFilePathIndex_HiddenFile() {
        // hidden files (name starts with dot) should be marked as included=false
        val relPath = "common/.hidden.test.txt"
        markFileInfo(gameType, relPath)
        myFixture.configureByFile("features/index/common/.hidden.test.txt")
        val scope = GlobalSearchScope.projectScope(this.project)
        val values = FileBasedIndex.getInstance().getValues(PlsIndexKeys.FilePath, relPath, scope)
        Assert.assertTrue(values.isNotEmpty())
        val info = values.single()
        Assert.assertEquals("common", info.directory)
        Assert.assertEquals(gameType, info.gameType)
        Assert.assertFalse("Expected included=false for hidden file", info.included)
    }

    // endregion
}
