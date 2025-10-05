package icu.windea.pls.lang.index

import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.ParadoxSyncedLocalisationSearch
import icu.windea.pls.lang.search.processQuery
import icu.windea.pls.lang.search.selector.localisation
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.PlsTestUtil
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxLocalisationIndicesTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Test
    fun localisationNameIndex_Basic() {
        val file = myFixture.file.virtualFile
        myFixture.configureByFile("features/index/localisation/ui/ui_l_english.test.yml")
        PlsTestUtil.injectFileInfo(file, "localisation/ui/ui_l_english.test.yml", ParadoxGameType.Stellaris)
        val project = project
        val scope = GlobalSearchScope.projectScope(project)
        val elements = StubIndex.getElements(
            ParadoxIndexKeys.LocalisationName,
            "UI_OK",
            project,
            scope,
            ParadoxLocalisationProperty::class.java
        )
        Assert.assertEquals(1, elements.size)
        Assert.assertEquals("UI_OK", elements.single().name)
    }

    @Test
    fun localisationSearch_ByName() {
        val file = myFixture.file.virtualFile
        myFixture.configureByFile("features/index/localisation/ui/ui_l_english.test.yml")
        PlsTestUtil.injectFileInfo(file, "localisation/ui/ui_l_english.test.yml", ParadoxGameType.Stellaris)
        val project = project
        val selector = selector(project, file).localisation()
        val results = mutableListOf<String>()
        ParadoxLocalisationSearch.search("UI_OK", selector).processQuery(false) { p ->
            results += p.name
            true
        }
        Assert.assertEquals(listOf("UI_OK"), results)
    }

    @Test
    fun syncedLocalisationNameIndex_Basic() {
        val file = myFixture.file.virtualFile
        myFixture.configureByFile("features/index/localisation_synced/ui/ui_l_english.test.yml")
        PlsTestUtil.injectFileInfo(file, "localisation_synced/ui/ui_l_english.test.yml", ParadoxGameType.Stellaris)
        val project = project
        val scope = GlobalSearchScope.projectScope(project)
        val elements = StubIndex.getElements(
            ParadoxIndexKeys.SyncedLocalisationName,
            "SYNC_TITLE",
            project,
            scope,
            ParadoxLocalisationProperty::class.java
        )
        Assert.assertEquals(1, elements.size)
        Assert.assertEquals("SYNC_TITLE", elements.single().name)
    }

    @Test
    fun syncedLocalisationSearch_ByName() {
        val file = myFixture.file.virtualFile
        myFixture.configureByFile("features/index/localisation_synced/ui/ui_l_english.test.yml")
        PlsTestUtil.injectFileInfo(file, "localisation_synced/ui/ui_l_english.test.yml", ParadoxGameType.Stellaris)
        val project = project
        val selector = selector(project, file).localisation()
        val results = mutableListOf<String>()
        ParadoxSyncedLocalisationSearch.search("SYNC_TITLE", selector).processQuery(false) { p ->
            results += p.name
            true
        }
        Assert.assertEquals(listOf("SYNC_TITLE"), results)
    }

    @Test
    fun localisationSearch_NotFound() {
        val file = myFixture.file.virtualFile
        myFixture.configureByFile("features/index/localisation/ui/ui_l_english.test.yml")
        PlsTestUtil.injectFileInfo(file, "localisation/ui/ui_l_english.test.yml", ParadoxGameType.Stellaris)
        val project = project
        val selector = selector(project, file).localisation()
        val results = mutableListOf<String>()
        ParadoxLocalisationSearch.search("NOT_EXISTS", selector).processQuery(false) { p ->
            results += p.name
            true
        }
        Assert.assertTrue(results.isEmpty())
    }

    @Test
    fun syncedLocalisationSearch_NotFound() {
        val file = myFixture.file.virtualFile
        myFixture.configureByFile("features/index/localisation_synced/ui/ui_l_english.test.yml")
        PlsTestUtil.injectFileInfo(file, "localisation_synced/ui/ui_l_english.test.yml", ParadoxGameType.Stellaris)
        val project = project
        val selector = selector(project, file).localisation()
        val results = mutableListOf<String>()
        ParadoxSyncedLocalisationSearch.search("NOT_EXISTS", selector).processQuery(false) { p ->
            results += p.name
            true
        }
        Assert.assertTrue(results.isEmpty())
    }
}
