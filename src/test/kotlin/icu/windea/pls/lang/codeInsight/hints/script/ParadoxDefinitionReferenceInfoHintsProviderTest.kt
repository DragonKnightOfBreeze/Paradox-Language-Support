package icu.windea.pls.lang.codeInsight.hints.script

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
 * @see ParadoxDefinitionReferenceInfoHintsProvider
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxDefinitionReferenceInfoHintsProviderTest : DeclarativeInlayHintsProviderTestCase(), ChronicleTestScope {
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
        markFileInfo(ParadoxGameType.Stellaris, "common/species/00_species.txt")
        myFixture.configureByFile("features/inlayHints/common/species/00_species.txt")

        markFileInfo(ParadoxGameType.Stellaris, "common/characters/00_characters.txt")
        val text = loadText("/inlayProviders/paradox.script.definitionReferenceInfo/preview.txt")
        doTest(text)
    }

    private fun doTest(text: String) {
        IndexingTestUtil.waitUntilIndexesAreReady(project)
        doTestProvider("test.txt", text, ParadoxDefinitionReferenceInfoHintsProvider(), verifyHintsPresence = true, testMode = ProviderTestMode.SIMPLE)
    }
}
