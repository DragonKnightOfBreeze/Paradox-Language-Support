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
 * @see UnsupportedScriptedVariableInspection
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class UnsupportedScriptedVariableInspectionTest : BasePlatformTestCase(), ChronicleTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("features/inspections")
        markConfigDirectory("features/inspections/.config")
        initInjectedConfigGroups(project, ParadoxGameType.Stellaris)
        myFixture.enableInspections(UnsupportedScriptedVariableInspection::class.java)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    @Test
    fun normalContext() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test.txt")
        myFixture.configureByText("test.txt", "@var = 1")
        myFixture.checkHighlighting()
    }

    @Test
    fun inConditionalBlock() {
        val tag = ChronicleInspectionBundle.message("script.unsupportedScriptedVariable.desc.1").toWarningTag()

        markFileInfo(ParadoxGameType.Stellaris, "common/test.txt")
        myFixture.configureByText("test.txt", "[[PARAM] ${tag.start}@var = 1${tag.end} ]")
        myFixture.checkHighlighting()
    }
}
