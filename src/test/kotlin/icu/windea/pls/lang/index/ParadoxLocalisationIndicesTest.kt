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
import icu.windea.pls.model.ParadoxFileInfo
import icu.windea.pls.model.ParadoxFileType
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxRootInfo
import icu.windea.pls.model.paths.ParadoxPath
import org.junit.Assert

@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxLocalisationIndicesTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    fun testLocalisationNameIndex_Basic() {
        myFixture.configureByFile("features/index/localisation/ui/ui_l_english.test.yml")
        injectFileInfo("localisation/ui/ui_l_english.test.yml", ParadoxGameType.Stellaris)
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

    fun testLocalisationSearch_ByName() {
        myFixture.configureByFile("features/index/localisation/ui/ui_l_english.test.yml")
        injectFileInfo("localisation/ui/ui_l_english.test.yml", ParadoxGameType.Stellaris)
        val project = project
        val selector = selector(project, myFixture.file).localisation()
        val results = mutableListOf<String>()
        ParadoxLocalisationSearch.search("UI_OK", selector).processQuery(false) { p ->
            results += p.name
            true
        }
        Assert.assertEquals(listOf("UI_OK"), results)
    }

    fun testSyncedLocalisationNameIndex_Basic() {
        myFixture.configureByFile("features/index/localisation_synced/ui/ui_l_english.test.yml")
        injectFileInfo("localisation_synced/ui/ui_l_english.test.yml", ParadoxGameType.Stellaris)
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

    fun testSyncedLocalisationSearch_ByName() {
        myFixture.configureByFile("features/index/localisation_synced/ui/ui_l_english.test.yml")
        injectFileInfo("localisation_synced/ui/ui_l_english.test.yml", ParadoxGameType.Stellaris)
        val project = project
        val selector = selector(project, myFixture.file).localisation()
        val results = mutableListOf<String>()
        ParadoxSyncedLocalisationSearch.search("SYNC_TITLE", selector).processQuery(false) { p ->
            results += p.name
            true
        }
        Assert.assertEquals(listOf("SYNC_TITLE"), results)
    }

    private fun injectFileInfo(relPath: String, gameType: ParadoxGameType) {
        val vFile = myFixture.file.virtualFile
        val fileInfo = ParadoxFileInfo(ParadoxPath.resolve(relPath), "", ParadoxFileType.Localisation, ParadoxRootInfo.Injected(gameType))
        vFile.putUserData(icu.windea.pls.lang.PlsKeys.injectedFileInfo, fileInfo)
        vFile.putUserData(icu.windea.pls.lang.PlsKeys.injectedGameType, gameType)
    }
}
