package icu.windea.pls.core.inspections

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see InspectionService
 */
@RunWith(JUnit4::class)
class InspectionServiceTest : BasePlatformTestCase() {
    // region getWeakerHighlightType

    @Test
    fun getWeakerHighlightType_basic() {
        val errorTool = toolWithLevel(HighlightDisplayLevel.ERROR)
        val warningTool = toolWithLevel(HighlightDisplayLevel.WARNING)
        val weakWarningTool = toolWithLevel(HighlightDisplayLevel.WEAK_WARNING)

        // 比 ERROR 更弱的是 WARNING
        assertEquals(ProblemHighlightType.WARNING, InspectionService.getWeakerHighlightType(errorTool))
        // 比 WARNING 更弱的是 WEAK_WARNING
        assertEquals(ProblemHighlightType.WEAK_WARNING, InspectionService.getWeakerHighlightType(warningTool))
        // 不高于 WEAK_WARNING 时回退到通用类型
        assertEquals(ProblemHighlightType.GENERIC_ERROR_OR_WARNING, InspectionService.getWeakerHighlightType(weakWarningTool))
    }

    @Test
    fun getWeakerHighlightType_withConditionFalse() {
        val errorTool = toolWithLevel(HighlightDisplayLevel.ERROR)
        // 条件为 false 时，始终返回通用类型
        assertEquals(ProblemHighlightType.GENERIC_ERROR_OR_WARNING, InspectionService.getWeakerHighlightType(errorTool, false))
    }

    // endregion

    // region getToolState / getTool / isEnabled / getEnabledTool

    @Test
    fun getToolStateAndTool_basic() {
        val inspection = SampleInspection()
        myFixture.enableInspections(inspection)
        val project = myFixture.project
        val shortName = inspection.shortName

        val toolState = InspectionService.getToolState(shortName, project)
        assertNotNull(toolState)
        assertTrue(toolState!!.isEnabled)

        val tool = InspectionService.getTool(shortName, project)
        assertNotNull(tool)
        assertTrue(tool is LocalInspectionTool)

        val enabledTool = InspectionService.getEnabledTool(shortName, project)
        assertNotNull(enabledTool)

        assertTrue(InspectionService.isEnabled(shortName, project))
    }

    @Test
    fun getToolStateAndTool_unknownShortName() {
        val project = myFixture.project
        val shortName = "NonexistentInspection"

        assertNull(InspectionService.getToolState(shortName, project))
        assertNull(InspectionService.getTool(shortName, project))
        assertNull(InspectionService.getEnabledTool(shortName, project))
        assertFalse(InspectionService.isEnabled(shortName, project))
    }

    // endregion

    private class SampleInspection : LocalInspectionTool()

    private fun toolWithLevel(level: HighlightDisplayLevel): LocalInspectionTool {
        return object : LocalInspectionTool() {
            override fun getDefaultLevel(): HighlightDisplayLevel = level
        }
    }
}
