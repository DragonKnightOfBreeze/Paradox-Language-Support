package icu.windea.pls.lang.codeInsight.hints.csv

import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.utils.inlays.declarative.DeclarativeInlayHintsProviderTestCase
import icu.windea.pls.core.loadText
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.ChronicleTestScope
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see ParadoxDynamicValueInfoHintsProvider
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxDynamicValueInfoHintsProviderTest : DeclarativeInlayHintsProviderTestCase(), ChronicleTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("features/inlayHints")
        markConfigDirectory("chronicle/.config")
        initConfigGroups(project, ParadoxGameType.Stellaris)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    @Test
    fun preview() {
        markFileInfo(ParadoxGameType.Stellaris, "common/chapters/00_chapters.txt")
        myFixture.configureByFile("features/inlayHints/common/chapters/00_chapters.txt")
        markFileInfo(ParadoxGameType.Stellaris, "common/chapters/categories/00_chapter_categories.txt")
        myFixture.configureByFile("features/inlayHints/common/chapters/categories/00_chapter_categories.txt")

        markFileInfo(ParadoxGameType.Stellaris, "common/chapters/00_chapter_pages.csv")
        val text = loadText("/inlayProviders/paradox.csv.dynamicValueInfo/preview.csv")
        doTest(text)
    }

    private fun doTest(text: String) {
        IndexingTestUtil.waitUntilIndexesAreReady(project)
        doTestProvider("test.csv", text, ParadoxDynamicValueInfoHintsProvider(), verifyHintsPresence = true, testMode = ProviderTestMode.SIMPLE)
    }
}
