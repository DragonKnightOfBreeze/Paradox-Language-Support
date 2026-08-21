package icu.windea.pls.lang.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.core.inspections.InspectionService
import icu.windea.pls.lang.selectGameType

data class ParadoxExpressionInspectionContext(
    val tool: LocalInspectionTool,
    val holder: ProblemsHolder,
    val ignoredByConfig: Boolean = false,
    val showExpect: Boolean = true,
    val truncateExpect: Int = -1,
) {
    val project: Project = holder.project
    val gameType = selectGameType(holder.file)
    val configGroup = ChronicleFacade.getConfigGroup(holder.project, gameType)

    fun getWeakerHighlightType(): ProblemHighlightType {
        return with(tool) { InspectionService.getWeakerHighlightType() }
    }

    interface Aware {
        fun createContext(holder: ProblemsHolder): ParadoxExpressionInspectionContext
    }
}
