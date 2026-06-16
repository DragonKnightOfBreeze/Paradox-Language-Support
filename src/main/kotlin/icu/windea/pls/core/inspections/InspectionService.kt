package icu.windea.pls.core.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.HighlightSeverity

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
}
