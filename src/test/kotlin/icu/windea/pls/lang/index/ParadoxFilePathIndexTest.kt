package icu.windea.pls.lang.index

import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.indexing.FileBasedIndex
import icu.windea.pls.core.processQuery
import icu.windea.pls.lang.PlsKeys
import icu.windea.pls.lang.search.ParadoxFilePathSearch
import icu.windea.pls.lang.search.selector.file
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.model.ParadoxFileInfo
import icu.windea.pls.model.ParadoxFileType
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxRootInfo
import icu.windea.pls.model.paths.ParadoxPath
import icu.windea.pls.test.PlsTestUtil
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxFilePathIndexTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Test
    fun testFilePathIndex_Basic() {
        myFixture.configureByFile("script/syntax/code_settings.test.txt")
        val relPath = "common/scripted_variables/code_settings.test.txt"
        PlsTestUtil.injectFileInfo(myFixture.file.virtualFile, relPath, ParadoxGameType.Stellaris)

        val project = project
        val scope = GlobalSearchScope.projectScope(project)
        val values = FileBasedIndex.getInstance().getValues(
            PlsIndexKeys.FilePath,
            relPath,
            scope
        )
        Assert.assertTrue(values.isNotEmpty())
        val info = values.single()
        Assert.assertEquals("common/scripted_variables", info.directory)
        Assert.assertEquals(ParadoxGameType.Stellaris, info.gameType)
        Assert.assertTrue(info.included)
    }

    @Test
    fun testFilePathSearcher_ExactPath() {
        myFixture.configureByFile("script/syntax/code_settings.test.txt")
        val relPath = "common/scripted_variables/code_settings.test.txt"
        PlsTestUtil.injectFileInfo(myFixture.file.virtualFile, relPath, ParadoxGameType.Stellaris)

        val project = project
        val selector = selector(project, myFixture.file).file()
        val results = mutableListOf<String>()
        ParadoxFilePathSearch.search(relPath, selector = selector).processQuery { vf ->
            results += vf.path
            true
        }
        Assert.assertEquals(1, results.size)
    }

    @Test
    fun testFilePathIndex_Localisation() {
        // index should record localisation yml files as included with correct directory and gameType
        myFixture.configureByFile("features/index/localisation/ui/ui_l_english.test.yml")
        val relPath = "localisation/ui/ui_l_english.test.yml"
        PlsTestUtil.injectFileInfo(myFixture.file.virtualFile, relPath, ParadoxGameType.Stellaris)

        val project = project
        val scope = GlobalSearchScope.projectScope(project)
        val values = FileBasedIndex.getInstance().getValues(
            PlsIndexKeys.FilePath,
            relPath,
            scope
        )
        Assert.assertTrue(values.isNotEmpty())
        val info = values.single()
        Assert.assertEquals("localisation/ui", info.directory)
        Assert.assertEquals(ParadoxGameType.Stellaris, info.gameType)
        Assert.assertTrue(info.included)
    }

    @Test
    fun testFilePathSearcher_NotFound_ReturnsEmpty() {
        myFixture.configureByFile("features/index/localisation/ui/ui_l_english.test.yml")
        PlsTestUtil.injectFileInfo(myFixture.file.virtualFile, "localisation/ui/ui_l_english.test.yml", ParadoxGameType.Stellaris)

        val project = project
        val selector = selector(project, myFixture.file).file()
        val results = mutableListOf<String>()
        ParadoxFilePathSearch.search("common/does/not/exist.txt", selector = selector).processQuery { vf ->
            results += vf.path
            true
        }
        Assert.assertTrue(results.isEmpty())
    }

    @Test
    fun testFilePathIndex_ExcludedTopDirectory() {
        // files under excluded top directories (e.g., jomini) should be marked as included=false
        val copied = myFixture.copyFileToProject(
            "features/index/jomini/gfx/interface/icons/my_icon.png",
            "jomini/gfx/interface/icons/my_icon.png"
        )
        val relPath = "jomini/gfx/interface/icons/my_icon.png"

        // inject fileInfo for the file and parent directories so isIncluded() can recurse to 'jomini'
        run {
            val fileInfo = ParadoxFileInfo(ParadoxPath.resolve(relPath), "", ParadoxFileType.Other, ParadoxRootInfo.Injected(ParadoxGameType.Stellaris))
            copied.putUserData(PlsKeys.injectedFileInfo, fileInfo)
            copied.putUserData(PlsKeys.injectedGameType, ParadoxGameType.Stellaris)
            var dir = copied.parent
            val parts = listOf("jomini/gfx/interface/icons", "jomini/gfx/interface", "jomini/gfx", "jomini")
            parts.forEach { p ->
                dir?.putUserData(PlsKeys.injectedFileInfo, ParadoxFileInfo(ParadoxPath.resolve(p), "", ParadoxFileType.Other, ParadoxRootInfo.Injected(ParadoxGameType.Stellaris)))
                dir = dir?.parent
            }
        }
        FileBasedIndex.getInstance().requestReindex(copied)

        val project = project
        val scope = GlobalSearchScope.projectScope(project)
        val values = FileBasedIndex.getInstance().getValues(
            PlsIndexKeys.FilePath,
            relPath,
            scope
        )
        Assert.assertTrue(values.isNotEmpty())
        val info = values.single()
        Assert.assertEquals("jomini/gfx/interface/icons", info.directory)
        Assert.assertEquals(ParadoxGameType.Stellaris, info.gameType)
        Assert.assertFalse("Expected included=false for excluded top directory", info.included)
    }

    @Test
    fun testFilePathIndex_HiddenFile() {
        // hidden files (name starts with dot) should be marked as included=false
        myFixture.configureByFile("features/index/common/scripted_variables/.hidden.test.txt")
        val relPath = "common/scripted_variables/.hidden.test.txt"
        PlsTestUtil.injectFileInfo(myFixture.file.virtualFile, relPath, ParadoxGameType.Stellaris)
        val scope = GlobalSearchScope.projectScope(this.project)
        val values = FileBasedIndex.getInstance().getValues(PlsIndexKeys.FilePath, relPath, scope)
        Assert.assertTrue(values.isNotEmpty())
        val info = values.single()
        Assert.assertEquals("common/scripted_variables", info.directory)
        Assert.assertEquals(ParadoxGameType.Stellaris, info.gameType)
        Assert.assertFalse("Expected included=false for hidden file", info.included)
    }
}
