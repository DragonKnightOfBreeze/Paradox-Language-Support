package icu.windea.pls.lang.inspections.script.common

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.PlsBundle
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.InspectionTestScope
import icu.windea.pls.test.clearIntegrationTest
import icu.windea.pls.test.markFileInfo
import icu.windea.pls.test.markIntegrationTest
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
class UnsupportedInlineMathInspectionTest : BasePlatformTestCase(), InspectionTestScope {

    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
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
        val description = PlsBundle.message("inspection.script.unsupportedInlineMath.desc.1")
        val tag = description.toWarningTag()

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
