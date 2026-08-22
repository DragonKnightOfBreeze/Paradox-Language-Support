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
 * @see UnsupportedInlineMathInspection
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class UnsupportedInlineMathInspectionTest : BasePlatformTestCase(), ChronicleTestScope {

    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        // initInjectedConfigGroups(project, ParadoxGameType.Stellaris) // unnecessary
        myFixture.enableInspections(UnsupportedInlineMathInspection::class.java)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    @Test
    fun txtFile_stellaris() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test/test.stellaris.txt")
        myFixture.configureByText("test.stellaris.txt", "key = @[ 1 + 1 ]")
        myFixture.checkHighlighting()
    }

    @Test
    fun txtFile_eu5() {
        markFileInfo(ParadoxGameType.Eu5, "common/test/test.eu5.txt")
        myFixture.configureByText("test.eu5.txt", "key = @[ 1 + 1 ]")
        myFixture.checkHighlighting()
    }

    @Test
    fun assetFile_stellaris() {
        val tag = ChronicleInspectionBundle.message("script.unsupportedInlineMath.desc.1").toWarningTag()

        markFileInfo(ParadoxGameType.Stellaris, "common/test/test.stellaris.asset")
        myFixture.configureByText("test.stellaris.asset", "key = ${tag.start}@[ 1 + 1 ]${tag.end}")
        myFixture.checkHighlighting()
    }

    @Test
    fun assetFile_eu5() {
        markFileInfo(ParadoxGameType.Eu5, "common/test/test.eu5.asset")
        myFixture.configureByText("test.eu5.asset", "key = @[ 1 + 1 ]")
        myFixture.checkHighlighting()
    }
}
