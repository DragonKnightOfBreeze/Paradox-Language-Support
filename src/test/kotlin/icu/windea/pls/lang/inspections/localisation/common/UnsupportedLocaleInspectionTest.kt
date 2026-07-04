package icu.windea.pls.lang.inspections.localisation.common

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.HighlightingTestScope
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

/**
 * @see UnsupportedLocaleInspection
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class UnsupportedLocaleInspectionTest : BasePlatformTestCase(), HighlightingTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("features/inspections")
        markConfigDirectory("features/inspections/.config")
        initConfigGroups(project, ParadoxGameType.Stellaris)
        myFixture.enableInspections(UnsupportedLocaleInspection::class.java)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    @Test
    fun smokeTest_success() {
        markFileInfo(ParadoxGameType.Stellaris, "localisation/test.yml")
        myFixture.configureByText("test.yml", """
            l_english:
              message: "Hello world"
        """.trimIndent())

        myFixture.configureFromExistingVirtualFile(myFixture.file.virtualFile)
        myFixture.checkHighlighting(true, true, true)
    }

    @Test
    fun smokeTest_failed() {
        val key = "l_neko"
        val tag = ChronicleBundle.message("inspection.localisation.unsupportedLocale.desc.1", key).toErrorTag()

        markFileInfo(ParadoxGameType.Stellaris, "localisation/test.yml")
        myFixture.configureByText("test.yml", """
            ${tag.start}l_neko${tag.end}:
              message: "Meo~ Meo~"
        """.trimIndent())

        myFixture.configureFromExistingVirtualFile(myFixture.file.virtualFile)
        myFixture.checkHighlighting(true, true, true)
    }

    @Test
    fun smokeTest_failed_forGameType() {
        val key = "l_turkish"
        val tag = ChronicleBundle.message("inspection.localisation.unsupportedLocale.desc.2", key, ParadoxGameType.Stellaris).toErrorTag()

        markFileInfo(ParadoxGameType.Stellaris, "localisation/test.yml")
        myFixture.configureByText("test.yml", """
            ${tag.start}l_turkish${tag.end}:
              message: "..."
        """.trimIndent())

        myFixture.configureFromExistingVirtualFile(myFixture.file.virtualFile)
        myFixture.checkHighlighting(true, true, true)
    }
}
