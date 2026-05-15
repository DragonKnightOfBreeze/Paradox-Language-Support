package icu.windea.pls.lang.inspections.overrides

import com.intellij.testFramework.IndexingTestUtil
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
 * @see OverrideForScriptedVariableInspection
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class OverrideForScriptedVariableInspectionTest : BasePlatformTestCase(), InspectionTestScope {
    private val gameType = ParadoxGameType.Stellaris

    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        myFixture.enableInspections(OverrideForScriptedVariableInspection::class.java)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    @Test
    fun simple_fios() {
        val key = "var"
        val description = PlsBundle.message("inspection.overrideForScriptedVariable.desc", key)
        val tag = description.toWeakWarningTag()

        markFileInfo(gameType, "common/scripted_variables/99_scripted_variables.txt")
        myFixture.configureByText("99_scripted_variables.txt", """
            @var = 1
        """.trimIndent())

        markFileInfo(gameType, "common/scripted_variables/01_scripted_variables.txt")
        myFixture.configureByText("01_scripted_variables.txt", """
            ${tag.start}@var${tag.end} = 1
        """.trimIndent())

        myFixture.configureFromExistingVirtualFile(myFixture.file.virtualFile) // necessary
        myFixture.checkHighlighting()
    }

    @Test
    fun simple_lios() {
        val key = "var"
        val description = PlsBundle.message("inspection.overrideForScriptedVariable.desc", key)
        val tag = description.toWeakWarningTag()

        markFileInfo(gameType, "common/scripted_variables/01_scripted_variables.txt")
        myFixture.configureByText("01_scripted_variables.txt", """
            @var = 1
        """.trimIndent())

        markFileInfo(gameType, "common/scripted_variables/99_scripted_variables.txt")
        myFixture.configureByText("99_scripted_variables.txt", """
            ${tag.start}@var${tag.end} = 1
        """.trimIndent())

        myFixture.configureFromExistingVirtualFile(myFixture.file.virtualFile) // necessary
        myFixture.checkHighlighting()
    }

    @Test
    fun simple_samePath() {
        val key = "var"
        val description = PlsBundle.message("inspection.overrideForScriptedVariable.desc", key)
        val tag = description.toWeakWarningTag()

        markFileInfo(gameType, "common/scripted_variables/01_scripted_variables.txt")
        myFixture.configureByText("01_scripted_variables.txt", """
            @var = 1
        """.trimIndent())

        markFileInfo(gameType, "common/scripted_variables/01_scripted_variables.txt")
        myFixture.configureByText("01_scripted_variables.copy.txt", """
            ${tag.start}@var${tag.end} = 1
        """.trimIndent())

        myFixture.configureFromExistingVirtualFile(myFixture.file.virtualFile) // necessary
        myFixture.checkHighlighting()
    }
}
