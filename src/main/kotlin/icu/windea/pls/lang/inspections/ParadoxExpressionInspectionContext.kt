package icu.windea.pls.lang.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxConfigManager

data class ParadoxExpressionInspectionContext(
    val tool: LocalInspectionTool,
    val holder: ProblemsHolder,
    val ignoredByConfigs: Boolean = false,
    val ignoredFileNames: String = "",
    val showExpect: Boolean = true,
    val truncateExpect: Int = -1,
    val firstOnly: Boolean = false,
    val firstOnlyOnFile: Boolean = true,
) {
    val project: Project = holder.project
    val gameType = selectGameType(holder.file)
    val configGroup = ChronicleFacade.getConfigGroup(holder.project, gameType)
    val rowConfig = ParadoxConfigManager.getRowConfig(holder.file)
}
