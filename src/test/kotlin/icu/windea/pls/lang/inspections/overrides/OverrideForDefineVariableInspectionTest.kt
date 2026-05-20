package icu.windea.pls.lang.inspections.overrides

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.PlsBundle
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.InspectionTestScope
import icu.windea.pls.test.clearIntegrationTest
import icu.windea.pls.test.initConfigGroups
import icu.windea.pls.test.markFileInfo
import icu.windea.pls.test.markIntegrationTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see OverrideForDefineVariableInspection
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class OverrideForDefineVariableInspectionTest : BasePlatformTestCase(), InspectionTestScope {
    private val gameType = ParadoxGameType.Stellaris

    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        initConfigGroups(project, gameType)
        myFixture.enableInspections(OverrideForDefineVariableInspection::class.java)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    @Test
    fun simple_fios() {
        val key = "Namespace.Variable"
        val description = PlsBundle.message("inspection.overrideForDefineVariable.desc", key)
        val tag = description.toWeakWarningTag()

        markFileInfo(gameType, "common/defines/99_defines.txt")
        myFixture.configureByText("99_defines.txt", """
            Namespace = {
                Variable = 1
            }
        """.trimIndent())

        markFileInfo(gameType, "common/defines/01_defines.txt")
        myFixture.configureByText("01_defines.txt", """
            Namespace = {
                ${tag.start}Variable${tag.end} = 1
            }
        """.trimIndent())

        myFixture.configureFromExistingVirtualFile(myFixture.file.virtualFile) // necessary
        myFixture.checkHighlighting()
    }

    @Test
    fun simple_lios() {
        val key = "Namespace.Variable"
        val description = PlsBundle.message("inspection.overrideForDefineVariable.desc", key)
        val tag = description.toWeakWarningTag()

        markFileInfo(gameType, "common/defines/01_defines.txt")
        myFixture.configureByText("01_defines.txt", """
            Namespace = {
                Variable = 1
            }
        """.trimIndent())

        markFileInfo(gameType, "common/defines/99_defines.txt")
        myFixture.configureByText("99_defines.txt", """
            Namespace = {
                ${tag.start}Variable${tag.end} = 1
            }
        """.trimIndent())

        myFixture.configureFromExistingVirtualFile(myFixture.file.virtualFile) // necessary
        myFixture.checkHighlighting()
    }

    @Test
    fun simple_samePath() {
        val key = "Namespace.Variable"
        val description = PlsBundle.message("inspection.overrideForDefineVariable.desc", key)
        val tag = description.toWeakWarningTag()

        markFileInfo(gameType, "common/defines/01_defines.txt")
        myFixture.configureByText("01_defines.txt", """
            Namespace = {
                Variable = 1
            }
        """.trimIndent())

        markFileInfo(gameType, "common/defines/01_defines.txt")
        myFixture.configureByText("01_defines.copy.txt", """
            Namespace = {
                ${tag.start}Variable${tag.end} = 1
            }
        """.trimIndent())

        myFixture.configureFromExistingVirtualFile(myFixture.file.virtualFile) // necessary
        myFixture.checkHighlighting()
    }
}
