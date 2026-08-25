package icu.windea.pls.lang.inspections.overrides

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.lang.inspections.ChronicleInspectionBundle
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.overrides.ParadoxOverrideStrategy
import icu.windea.pls.test.ChronicleTestScope
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see IncorrectOverrideForDefineVariableInspection
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class IncorrectOverrideForDefineVariableInspectionTest : BasePlatformTestCase(), ChronicleTestScope {
    private val gameType = ParadoxGameType.Stellaris

    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("features/inspections")
        markConfigDirectory("features/inspections/.config")
        initConfigGroups(project, gameType)
        myFixture.enableInspections(IncorrectOverrideForDefineVariableInspection::class.java)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    @Test
    fun simple_fios() {
        val key = "Namespace.Variable"
        val overrideStrategy = ParadoxOverrideStrategy.LIOS
        val tag = ChronicleInspectionBundle.message("overrides.incorrectOverrideForDefineVariable.desc", key, overrideStrategy).toWarningTag()

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

        myFixture.configureFromExistingVirtualFile(myFixture.file.virtualFile)
        myFixture.checkHighlighting()
    }

    @Test
    fun simple_lios() {
        markFileInfo(gameType, "common/defines/01_defines.txt")
        myFixture.configureByText("01_defines.txt", """
            Namespace = {
                Variable = 1
            }
        """.trimIndent())

        markFileInfo(gameType, "common/defines/99_defines.txt")
        myFixture.configureByText("99_defines.txt", """
            Namespace = {
                Variable = 1
            }
        """.trimIndent())

        myFixture.configureFromExistingVirtualFile(myFixture.file.virtualFile)
        myFixture.checkHighlighting()
    }

    @Test
    fun simple_samePath() {
        markFileInfo(gameType, "common/defines/01_defines.txt")
        myFixture.configureByText("01_defines.txt", """
            Namespace = {
                Variable = 1
            }
        """.trimIndent())

        markFileInfo(gameType, "common/defines/01_defines.txt")
        myFixture.configureByText("01_defines.copy.txt", """
            Namespace = {
                Variable = 1
            }
        """.trimIndent())

        myFixture.configureFromExistingVirtualFile(myFixture.file.virtualFile)
        myFixture.checkHighlighting()
    }
}
