package icu.windea.pls.core.inspections

import com.intellij.codeInspection.InspectionProfileEntry
import com.intellij.codeInspection.ex.ScopeToolState
import com.intellij.openapi.project.Project
import com.intellij.profile.codeInspection.InspectionProfileManager
import com.intellij.psi.PsiElement

/** 根据检查项短名获取对应的 [ScopeToolState]。 */
fun getInspectionToolState(shortName: String, element: PsiElement?, project: Project): ScopeToolState? {
    val currentProfile = InspectionProfileManager.getInstance(project).currentProfile
    val tools = currentProfile.getToolsOrNull(shortName, project) ?: return null
    return tools.getState(element)
}

/** 若检查项启用则返回实际的 [InspectionProfileEntry]，否则返回 null。 */
val ScopeToolState.enabledTool: InspectionProfileEntry? get() = if (isEnabled) tool.tool else null
