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
 * @see UnsupportedParameterInspection
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class UnsupportedParameterInspectionTest : BasePlatformTestCase(), ChronicleTestScope {

    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("features/inspections")
        markConfigDirectory("features/inspections/.config")
        initInjectedConfigGroups(project, ParadoxGameType.Stellaris)
        myFixture.enableInspections(UnsupportedParameterInspection::class.java)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    @Test
    fun normalFile() {
        val tag = ChronicleInspectionBundle.message("script.unsupportedParameter.desc.1").toWarningTag()

        markFileInfo(ParadoxGameType.Stellaris, "common/test.txt")
        myFixture.configureByText("test.txt", "key = ${tag.start}\$PARAM$${tag.end}")
        myFixture.checkHighlighting()
    }

    @Test
    fun normalFile_withDefaultValue() {
        val tag = ChronicleInspectionBundle.message("script.unsupportedParameter.desc.1").toWarningTag()

        markFileInfo(ParadoxGameType.Stellaris, "common/test.txt")
        myFixture.configureByText("test.txt", "key = ${tag.start}\$PARAM|0$${tag.end}")
        myFixture.checkHighlighting()
    }

    @Test
    fun normalFile_condition() {
        val tag = ChronicleInspectionBundle.message("script.unsupportedParameter.desc.2").toWarningTag()

        markFileInfo(ParadoxGameType.Stellaris, "common/test.txt")
        myFixture.configureByText("test.txt", "[[${tag.start}PARAM${tag.end}] text ]")
        myFixture.checkHighlighting()
    }

    @Test
    fun inlineScriptFile() {
        markFileInfo(ParadoxGameType.Stellaris, "common/inline_scripts/test.txt")
        myFixture.configureByText("test.txt", "key = \$PARAM$")
        myFixture.checkHighlighting()
    }

    @Test
    fun inlineScriptFile_withDefaultValue() {
        val tag = ChronicleInspectionBundle.message("script.unsupportedParameter.desc.3").toWarningTag()

        markFileInfo(ParadoxGameType.Stellaris, "common/inline_scripts/test.txt")
        myFixture.configureByText("test.txt", "key = ${tag.start}\$PARAM|0$${tag.end}")
        myFixture.checkHighlighting()
    }
}
