package icu.windea.pls.lang.index

import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.core.process
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.selector.localisation
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
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
class ParadoxLocalisationIndexTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun setup() = markIntegrationTest()

    @After
    fun clear() = clearIntegrationTest()

    @Test
    fun testLocalisationNameIndex_Basic() {
        markFileInfo(ParadoxGameType.Stellaris, "localisation/ui/ui_l_english.test.yml")
        myFixture.configureByFile("features/index/localisation/ui/ui_l_english.test.yml")
        val project = project
        val scope = GlobalSearchScope.projectScope(project)
        val elements = StubIndex.getElements(
            PlsIndexKeys.LocalisationName,
            "UI_OK",
            project,
            scope,
            ParadoxLocalisationProperty::class.java
        )
        Assert.assertEquals(1, elements.size)
        Assert.assertEquals("UI_OK", elements.single().name)
    }

    @Test
    fun testLocalisationSearch_ByName() {
        markFileInfo(ParadoxGameType.Stellaris, "localisation/ui/ui_l_english.test.yml")
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
    fun testSyncedLocalisationNameIndex_Basic() {
        markFileInfo(ParadoxGameType.Stellaris, "localisation_synced/ui/ui_l_english.test.yml")
        myFixture.configureByFile("features/index/localisation_synced/ui/ui_l_english.test.yml")
        val project = project
        val scope = GlobalSearchScope.projectScope(project)
        val elements = StubIndex.getElements(
            PlsIndexKeys.SyncedLocalisationName,
            "SYNC_TITLE",
            project,
            scope,
            ParadoxLocalisationProperty::class.java
        )
        Assert.assertEquals(1, elements.size)
        Assert.assertEquals("SYNC_TITLE", elements.single().name)
    }

    @Test
    fun testSyncedLocalisationSearch_ByName() {
        markFileInfo(ParadoxGameType.Stellaris, "localisation_synced/ui/ui_l_english.test.yml")
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
    fun testLocalisationSearch_NotFound() {
        markFileInfo(ParadoxGameType.Stellaris, "localisation/ui/ui_l_english.test.yml")
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

    @Test
    fun testSyncedLocalisationSearch_NotFound() {
        markFileInfo(ParadoxGameType.Stellaris, "localisation_synced/ui/ui_l_english.test.yml")
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
}
