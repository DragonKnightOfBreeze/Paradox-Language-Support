package icu.windea.pls.lang.index

import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.lang.search.ParadoxFilePathSearch
import icu.windea.pls.lang.search.processQuery
import icu.windea.pls.lang.search.selector.file
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.model.ParadoxFileInfo
import icu.windea.pls.model.ParadoxFileType
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxRootInfo
import icu.windea.pls.model.paths.ParadoxPath
import org.junit.Assert

@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxFilePathIndexTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    fun testFilePathIndex_Basic() {
        myFixture.configureByFile("script/syntax/_code_settings.test.txt")
        val relPath = "common/scripted_variables/_code_settings.test.txt"
        injectFileInfo(relPath, ParadoxGameType.Stellaris)

        val project = project
        val scope = GlobalSearchScope.projectScope(project)
        val values = FileBasedIndex.getInstance().getValues(
            ParadoxIndexKeys.FilePath,
            relPath,
            scope
        )
        Assert.assertTrue(values.isNotEmpty())
        val info = values.single()
        Assert.assertEquals("common/scripted_variables", info.directory)
        Assert.assertEquals(ParadoxGameType.Stellaris, info.gameType)
        Assert.assertTrue(info.included)
    }

    fun testFilePathSearcher_ExactPath() {
        myFixture.configureByFile("script/syntax/_code_settings.test.txt")
        val relPath = "common/scripted_variables/_code_settings.test.txt"
        injectFileInfo(relPath, ParadoxGameType.Stellaris)

        val project = project
        val selector = selector(project, myFixture.file).file()
        val results = mutableListOf<String>()
        ParadoxFilePathSearch.search(relPath, selector = selector).processQuery(false) { vf ->
            results += vf.path
            true
        }
        Assert.assertEquals(1, results.size)
    }

    private fun injectFileInfo(relPath: String, gameType: ParadoxGameType) {
        val vFile = myFixture.file.virtualFile
        val fileInfo = ParadoxFileInfo(ParadoxPath.resolve(relPath), "", ParadoxFileType.Script, ParadoxRootInfo.Injected(gameType))
        vFile.putUserData(icu.windea.pls.lang.PlsKeys.injectedFileInfo, fileInfo)
        vFile.putUserData(icu.windea.pls.lang.PlsKeys.injectedGameType, gameType)
    }
}
