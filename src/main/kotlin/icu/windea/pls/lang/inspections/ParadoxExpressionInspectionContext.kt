package icu.windea.pls.lang.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.model.ParadoxGameType

data class ParadoxExpressionInspectionContext(
    val tool: LocalInspectionTool,
    val holder: ProblemsHolder,
    val configGroup: CwtConfigGroup,
) {
    val project: Project get() = holder.project
    val gameType: ParadoxGameType get() = configGroup.gameType
}
