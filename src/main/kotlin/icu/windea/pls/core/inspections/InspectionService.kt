package icu.windea.pls.core.inspections

import com.intellij.codeInspection.InspectionProfileEntry
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ex.ScopeToolState
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.project.Project
import com.intellij.profile.codeInspection.InspectionProfileManager
import com.intellij.psi.PsiElement

@Suppress("unused")
object InspectionService {
    context(tool: LocalInspectionTool)
    fun getWeakerHighlightType(condition: Boolean = true): ProblemHighlightType {
        if (!condition) return ProblemHighlightType.GENERIC_ERROR_OR_WARNING
        val current = tool.defaultLevel.severity
        return when {
            current > HighlightSeverity.WARNING -> ProblemHighlightType.WARNING
            current > HighlightSeverity.WEAK_WARNING -> ProblemHighlightType.WEAK_WARNING
            else -> ProblemHighlightType.GENERIC_ERROR_OR_WARNING
        }
    }

    fun getToolState(shortName: String, project: Project, element: PsiElement? = null): ScopeToolState? {
        val currentProfile = InspectionProfileManager.getInstance(project).currentProfile
        val tools = currentProfile.getToolsOrNull(shortName, project) ?: return null
        return tools.getState(element)
    }

    fun getTool(shortName: String, project: Project, element: PsiElement? = null): InspectionProfileEntry? {
        val toolState = getToolState(shortName, project, element) ?: return null
        return toolState.tool.tool
    }

    fun isEnabled(shortName: String, project: Project, element: PsiElement? = null): Boolean {
        val toolState = getToolState(shortName, project, element) ?: return false
        return toolState.isEnabled
    }

    fun getEnabledTool(shortName: String, project: Project, element: PsiElement? = null): InspectionProfileEntry? {
        val toolState = getToolState(shortName, project, element) ?: return null
        return if (toolState.isEnabled) toolState.tool.tool else null
    }
}
