package icu.windea.pls.lang.index

import com.intellij.psi.PsiFile
import com.intellij.testFramework.TestDataFile
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.core.process
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.ChronicleTestScope
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see ParadoxLocalisationSearch
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxLocalisationSearchTest : BasePlatformTestCase(), ChronicleTestScope {
    private val gameType = ParadoxGameType.Stellaris

    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() = markIntegrationTest()

    @After
    fun doTearDown() = clearIntegrationTest()

    // region Normal Localisation

    @Test
    fun testNormalLocalisation_byName() {
        configureMarkedFile("features/index/localisation/ui/ui_l_english.test.yml")

        val selector = ParadoxLocalisationSearch.selector(project, myFixture.file)
        val results = mutableListOf<String>()
        ParadoxLocalisationSearch.searchNormal("UI_OK", selector).process { p ->
            results += p.name
            true
        }
        Assert.assertEquals(listOf("UI_OK"), results)
    }

    @Test
    fun testNormalLocalisation_notFound() {
        configureMarkedFile("features/index/localisation/ui/ui_l_english.test.yml")

        val selector = ParadoxLocalisationSearch.selector(project, myFixture.file)
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
    fun testSyncedLocalisation_byName() {
        configureMarkedFile("features/index/localisation_synced/ui/ui_l_english.test.yml")

        val selector = ParadoxLocalisationSearch.selector(project, myFixture.file)
        val results = mutableListOf<String>()
        ParadoxLocalisationSearch.searchSynced("SYNC_TITLE", selector).process { p ->
            results += p.name
            true
        }
        Assert.assertEquals(listOf("SYNC_TITLE"), results)
    }

    @Test
    fun testSyncedLocalisation_notFound() {
        configureMarkedFile("features/index/localisation_synced/ui/ui_l_english.test.yml")

        val selector = ParadoxLocalisationSearch.selector(project, myFixture.file)
        val results = mutableListOf<String>()
        ParadoxLocalisationSearch.searchSynced("NOT_EXISTS", selector).process { p ->
            results += p.name
            true
        }
        Assert.assertTrue(results.isEmpty())
    }

    // endregion

    private fun configureMarkedFile(@TestDataFile testDataPath: String, path: String = testDataPath.removePrefix("features/index/")): PsiFile {
        markFileInfo(gameType, path)
        return myFixture.configureByFile(testDataPath)
    }
}
