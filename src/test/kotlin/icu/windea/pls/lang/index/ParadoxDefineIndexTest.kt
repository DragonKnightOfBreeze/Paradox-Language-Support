package icu.windea.pls.lang.index

import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.indexing.FileBasedIndex
import icu.windea.pls.lang.search.ParadoxDefineSearch
import icu.windea.pls.lang.search.processQuery
import icu.windea.pls.lang.search.selector.define
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.indexInfo.ParadoxDefineIndexInfo
import icu.windea.pls.test.PlsTestUtil
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxDefineIndexTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Test
    fun defineIndex_Basic() {
        val file = myFixture.file.virtualFile
        myFixture.configureByFile("features/index/common/defines/defines_basic_stellaris.test.txt")
        PlsTestUtil.injectFileInfo(file, "common/defines/defines_basic_stellaris.test.txt", ParadoxGameType.Stellaris)
        FileBasedIndex.getInstance().requestReindex(file)

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

    @Test
    fun defineSearcher_ByNamespaceAndVariable() {
        val file = myFixture.file.virtualFile
        myFixture.configureByFile("features/index/common/defines/defines_basic_stellaris.test.txt")
        PlsTestUtil.injectFileInfo(file, "common/defines/defines_basic_stellaris.test.txt", ParadoxGameType.Stellaris)
        FileBasedIndex.getInstance().requestReindex(file)

        // act
        val selector = selector(project, file).define()
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
}
