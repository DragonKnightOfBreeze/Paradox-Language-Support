package icu.windea.pls.lang.index

import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.indexing.FileBasedIndex
import icu.windea.pls.lang.search.ParadoxDefineSearch
import icu.windea.pls.lang.search.processQuery
import icu.windea.pls.lang.search.selector.define
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.search.selector.withGameType
import icu.windea.pls.model.ParadoxFileInfo
import icu.windea.pls.model.ParadoxFileType
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxRootInfo
import icu.windea.pls.model.indexInfo.ParadoxDefineIndexInfo
import icu.windea.pls.model.paths.ParadoxPath
import org.junit.Assert

@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxDefineIndexTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    fun testDefineIndex_Basic() {
        // arrange
        myFixture.configureByFile("features/index/common/defines/defines_basic_stellaris.test.txt")
        val relPath = "common/defines/defines_basic_stellaris.test.txt"
        injectFileInfo(relPath, ParadoxGameType.Stellaris)

        // act
        val scope = GlobalSearchScope.projectScope(project)
        val values = FileBasedIndex.getInstance().getValues(
            ParadoxIndexKeys.Define,
            "NGameplay",
            scope
        )

        // assert
        Assert.assertTrue(values.isNotEmpty())
        val map = values.single()
        Assert.assertTrue(map.containsKey("")) // namespace info
        Assert.assertTrue(map.containsKey("MARINE"))
        Assert.assertTrue(map.containsKey("FLEET_POWER"))
        val info: ParadoxDefineIndexInfo = map.getValue("MARINE")
        Assert.assertEquals("NGameplay", info.namespace)
        Assert.assertEquals("MARINE", info.variable)
        Assert.assertEquals(ParadoxGameType.Stellaris, info.gameType)
        Assert.assertTrue(info.elementOffsets.isNotEmpty())
    }

    fun testDefineSearcher_ByNamespaceAndVariable() {
        // arrange
        myFixture.configureByFile("features/index/common/defines/defines_basic_stellaris.test.txt")
        val relPath = "common/defines/defines_basic_stellaris.test.txt"
        injectFileInfo(relPath, ParadoxGameType.Stellaris)

        // act
        val selector = selector(project, myFixture.file).define().withGameType(ParadoxGameType.Stellaris)
        val results = mutableListOf<ParadoxDefineIndexInfo>()
        ParadoxDefineSearch.search("NGameplay", "MARINE", selector).processQuery(false) { info ->
            results += info
            true
        }

        // assert
        Assert.assertEquals(1, results.size)
        val info = results.single()
        Assert.assertEquals("NGameplay", info.namespace)
        Assert.assertEquals("MARINE", info.variable)
        Assert.assertEquals("defines_basic_stellaris.test.txt", info.virtualFile?.name)
    }

    private fun injectFileInfo(relPath: String, gameType: ParadoxGameType) {
        val vFile = myFixture.file.virtualFile
        val fileInfo = ParadoxFileInfo(ParadoxPath.resolve(relPath), "", ParadoxFileType.Script, ParadoxRootInfo.Injected(gameType))
        vFile.putUserData(icu.windea.pls.lang.PlsKeys.injectedFileInfo, fileInfo)
        vFile.putUserData(icu.windea.pls.lang.PlsKeys.injectedGameType, gameType)
    }
}
