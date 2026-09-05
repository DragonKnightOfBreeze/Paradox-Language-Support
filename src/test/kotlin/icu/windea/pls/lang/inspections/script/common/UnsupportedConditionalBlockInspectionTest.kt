package icu.windea.pls.lang.inspections.script.common

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.lang.inspections.ChronicleInspectionBundle
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.ChronicleTestScope
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see UnsupportedConditionalBlockInspection
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class UnsupportedConditionalBlockInspectionTest : BasePlatformTestCase(), ChronicleTestScope {

    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("features/inspections")
        markConfigDirectory("features/inspections/.config")
        initInjectedConfigGroups(project, ParadoxGameType.Stellaris, ParadoxGameType.Eu5)
        myFixture.enableInspections(UnsupportedConditionalBlockInspection::class.java)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    @Test
    fun normalScriptFile_stellaris() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test.txt")
        myFixture.configureByText("test.txt", "[[PARAM] key = value ]")
        myFixture.checkHighlighting()
    }

    @Test
    fun normalScriptFile_eu5() {
        markFileInfo(ParadoxGameType.Eu5, "common/test.txt")
        myFixture.configureByText("test.txt", "[[PARAM] key = value ]")
        myFixture.checkHighlighting()
    }

    @Test
    fun inlineScriptFile_stellaris() {
        val tag = ChronicleInspectionBundle.message("script.unsupportedConditionalBlock.desc.1").toWarningTag()

        markFileInfo(ParadoxGameType.Stellaris, "common/inline_scripts/test.txt")
        myFixture.configureByText("test.txt", "${tag.start}[[PARAM] key = value ]${tag.end}")
        myFixture.checkHighlighting()
    }

    @Test
    fun inlineScriptFile_eu5() {
        markFileInfo(ParadoxGameType.Eu5, "common/inline_scripts/test.txt")
        myFixture.configureByText("test.txt", "[[PARAM] key = value ]")
        myFixture.checkHighlighting()
    }
}
