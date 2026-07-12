package icu.windea.pls.lang.index

import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.testFramework.TestDataFile
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
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

/**
 * @see ParadoxLocalisationNameIndex
 * @see ParadoxSyncedLocalisationNameIndex
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxLocalisationIndexTest : BasePlatformTestCase() {
    private val gameType = ParadoxGameType.Stellaris

    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() = markIntegrationTest()

    @After
    fun doTearDown() = clearIntegrationTest()

    private fun markAndConfigureByFile(@TestDataFile testDataPath: String, relPath: String = testDataPath.removePrefix("features/index/")): PsiFile {
        markFileInfo(gameType, relPath)
        return myFixture.configureByFile(testDataPath)
    }

    // region Normal Localisation

    @Test
    fun testNormalLocalisation_Basic() {
        markAndConfigureByFile("features/index/localisation/ui/ui_l_english.test.yml")

        val scope = GlobalSearchScope.projectScope(project)
        val elements = StubIndex.getElements(
            ChronicleIndexKeys.LocalisationName,
            "UI_OK",
            project,
            scope,
            ParadoxLocalisationProperty::class.java
        )
        Assert.assertEquals(1, elements.size)
        Assert.assertEquals("UI_OK", elements.single().name)
    }

    // endregion

    // region Synced Localisation

    @Test
    fun testSyncedLocalisation_Basic() {
        markAndConfigureByFile("features/index/localisation_synced/ui/ui_l_english.test.yml")

        val scope = GlobalSearchScope.projectScope(project)
        val elements = StubIndex.getElements(
            ChronicleIndexKeys.SyncedLocalisationName,
            "SYNC_TITLE",
            project,
            scope,
            ParadoxLocalisationProperty::class.java
        )
        Assert.assertEquals(1, elements.size)
        Assert.assertEquals("SYNC_TITLE", elements.single().name)
    }

    // endregion
}
