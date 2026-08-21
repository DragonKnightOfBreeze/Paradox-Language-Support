package icu.windea.pls.lang.inspections.localisation.common

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.ChronicleTestScope
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see DuplicatePropertiesInspection
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class DuplicatePropertiesInspectionTest : BasePlatformTestCase(), ChronicleTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        // initInjectedConfigGroups(project, ParadoxGameType.Stellaris) // unnecessary
        myFixture.enableInspections(DuplicatePropertiesInspection::class.java)
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
    fun smokeTest_differentKeys() {
        markFileInfo(ParadoxGameType.Stellaris, "localisation/test.yml")
        myFixture.configureByText("test.yml", """
            l_english:
              message: "Hello world"
              new_message: "Hello the real world"
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    @Test
    fun smokeTest_failed() {
        val key = "message"
        val tag = ChronicleBundle.message("inspection.localisation.duplicateProperties.desc", key).toWarningTag()

        markFileInfo(ParadoxGameType.Stellaris, "localisation/test.yml")
        myFixture.configureByText("test.yml", """
            l_english:
              ${tag.start}message${tag.end}: "Hello world"
              ${tag.start}message${tag.end}: "Hello the real world"
        """.trimIndent())
        myFixture.checkHighlighting()
    }
}
