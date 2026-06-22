package icu.windea.pls.lang.inspections.localisation.common

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.PlsBundle
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
 * @see DuplicatePropertiesInspection
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class DuplicatePropertiesInspectionTest : BasePlatformTestCase(), HighlightingTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        initConfigGroups(project, ParadoxGameType.Stellaris)
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

        myFixture.configureFromExistingVirtualFile(myFixture.file.virtualFile)
        myFixture.checkHighlighting(true, true, true)
    }

    @Test
    fun smokeTest_differentKeys() {
        markFileInfo(ParadoxGameType.Stellaris, "localisation/test.yml")

        myFixture.configureByText("test.yml", """
            l_english:
              message: "Hello world"
              new_message: "Hello the real world"
        """.trimIndent())

        myFixture.configureFromExistingVirtualFile(myFixture.file.virtualFile)
        myFixture.checkHighlighting(true, true, true)
    }

    @Test
    fun smokeTest_failed() {
        markFileInfo(ParadoxGameType.Stellaris, "localisation/test.yml")

        val key = "l_neko"
        val description = PlsBundle.message("inspection.localisation.duplicateProperties.desc", key)
        val tag = description.toErrorTag()

        myFixture.configureByText("test.yml", """
            l_english:
              ${tag.start}message${tag.end}: "Hello world"
              ${tag.start}message${tag.end}: "Hello the real world"
        """.trimIndent())

        myFixture.configureFromExistingVirtualFile(myFixture.file.virtualFile)
        myFixture.checkHighlighting(true, true, true)
    }
}
