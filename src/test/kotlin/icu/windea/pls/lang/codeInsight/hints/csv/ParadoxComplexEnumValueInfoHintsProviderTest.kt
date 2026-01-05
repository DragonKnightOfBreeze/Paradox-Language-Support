package icu.windea.pls.lang.codeInsight.hints.csv

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.utils.inlays.declarative.DeclarativeInlayHintsProviderTestCase
import icu.windea.pls.core.toClasspathUrl
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.clearIntegrationTest
import icu.windea.pls.test.initConfigGroups
import icu.windea.pls.test.markConfigDirectory
import icu.windea.pls.test.markFileInfo
import icu.windea.pls.test.markIntegrationTest
import icu.windea.pls.test.markRootDirectory
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxComplexEnumValueInfoHintsProviderTest: DeclarativeInlayHintsProviderTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun setup() {
        markIntegrationTest()
        markRootDirectory("features/inlayPreviews")
        markConfigDirectory("features/inlayPreviews/.config")
        initConfigGroups(project, ParadoxGameType.Stellaris)
    }

    @After
    fun clear() = clearIntegrationTest()

    @Test
    fun preview() {
        markFileInfo(ParadoxGameType.Stellaris, "common/chracter_tags/00_character_tags.txt")
        myFixture.configureByFile("features/inlayPreviews/common/chracter_tags/00_character_tags.txt")

        markFileInfo(ParadoxGameType.Stellaris, "common/characters/00_characters.txt")
        val text = "/inlayProviders/paradox.csv.complexEnumValueInfo/preview.txt".toClasspathUrl().readText()
        doTest(text)
    }

    private fun doTest(text: String) {
        doTestProvider("test.txt", text, ParadoxComplexEnumValueInfoHintsProvider(), verifyHintsPresence = true, testMode = ProviderTestMode.SIMPLE)
    }
}
