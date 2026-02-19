package icu.windea.pls.lang.index

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.core.process
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.selector.selector
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

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxLocalisationSearcherTest : BasePlatformTestCase() {
    private val gameType = ParadoxGameType.Stellaris

    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun setup() = markIntegrationTest()

    @After
    fun clear() = clearIntegrationTest()

    // region Normal Localisation

    @Test
    fun testLocalisationSearch_ByName() {
        markFileInfo(gameType, "localisation/ui/ui_l_english.test.yml")
        myFixture.configureByFile("features/index/localisation/ui/ui_l_english.test.yml")
        val project = project
        val selector = selector(project, myFixture.file).localisation()
        val results = mutableListOf<String>()
        ParadoxLocalisationSearch.searchNormal("UI_OK", selector).process { p ->
            results += p.name
            true
        }
        Assert.assertEquals(listOf("UI_OK"), results)
    }

    @Test
    fun testLocalisationSearch_NotFound() {
        markFileInfo(gameType, "localisation/ui/ui_l_english.test.yml")
        myFixture.configureByFile("features/index/localisation/ui/ui_l_english.test.yml")
        val project = project
        val selector = selector(project, myFixture.file).localisation()
        val results = mutableListOf<String>()
        ParadoxLocalisationSearch.searchNormal("NOT_EXISTS", selector).process { p ->
            results += p.name
            true
        }
        Assert.assertTrue(results.isEmpty())
    }

    // endregion

    // region Synced Localisation

    @Test
    fun testSyncedLocalisationSearch_ByName() {
        markFileInfo(gameType, "localisation_synced/ui/ui_l_english.test.yml")
        myFixture.configureByFile("features/index/localisation_synced/ui/ui_l_english.test.yml")
        val project = project
        val selector = selector(project, myFixture.file).localisation()
        val results = mutableListOf<String>()
        ParadoxLocalisationSearch.searchSynced("SYNC_TITLE", selector).process { p ->
            results += p.name
            true
        }
        Assert.assertEquals(listOf("SYNC_TITLE"), results)
    }

    @Test
    fun testSyncedLocalisationSearch_NotFound() {
        markFileInfo(gameType, "localisation_synced/ui/ui_l_english.test.yml")
        myFixture.configureByFile("features/index/localisation_synced/ui/ui_l_english.test.yml")
        val project = project
        val selector = selector(project, myFixture.file).localisation()
        val results = mutableListOf<String>()
        ParadoxLocalisationSearch.searchSynced("NOT_EXISTS", selector).process { p ->
            results += p.name
            true
        }
        Assert.assertTrue(results.isEmpty())
    }

    // endregion
}
