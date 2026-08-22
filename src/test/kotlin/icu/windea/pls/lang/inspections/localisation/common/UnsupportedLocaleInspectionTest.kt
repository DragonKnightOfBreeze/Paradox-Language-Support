package icu.windea.pls.lang.inspections.localisation.common

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
 * @see UnsupportedLocaleInspection
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class UnsupportedLocaleInspectionTest : BasePlatformTestCase(), ChronicleTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("features/inspections")
        markConfigDirectory("features/inspections/.config")
        initInjectedConfigGroups(project, ParadoxGameType.Stellaris) // on demand
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
        myFixture.checkHighlighting()
    }

    @Test
    fun smokeTest_failed() {
        val key = "l_neko"
        val tag = ChronicleInspectionBundle.message("inspection.localisation.unsupportedLocale.desc.1", key).toErrorTag()

        markFileInfo(ParadoxGameType.Stellaris, "localisation/test.yml")
        myFixture.configureByText("test.yml", """
            ${tag.start}l_neko${tag.end}:
              message: "Meo~ Meo~"
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    @Test
    fun smokeTest_failed_forGameType() {
        val key = "l_turkish"
        val tag = ChronicleInspectionBundle.message("inspection.localisation.unsupportedLocale.desc.2", key, ParadoxGameType.Stellaris).toErrorTag()

        markFileInfo(ParadoxGameType.Stellaris, "localisation/test.yml")
        myFixture.configureByText("test.yml", """
            ${tag.start}l_turkish${tag.end}:
              message: "..."
        """.trimIndent())
        myFixture.checkHighlighting()
    }
}
