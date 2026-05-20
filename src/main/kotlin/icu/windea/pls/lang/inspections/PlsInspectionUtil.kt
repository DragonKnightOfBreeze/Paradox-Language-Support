package icu.windea.pls.lang.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.HighlightSeverity
import icu.windea.pls.lang.inspections.overrides.IncorrectOverrideForDefineVariableInspection
import icu.windea.pls.lang.inspections.overrides.IncorrectOverrideForScriptedVariableInspection
import icu.windea.pls.lang.inspections.script.common.ConflictingResolvedExpressionInspection
import icu.windea.pls.lang.inspections.script.common.IncorrectExpressionInspection
import icu.windea.pls.lang.inspections.script.common.MissingExpressionInspection
import icu.windea.pls.lang.inspections.script.common.TooManyExpressionInspection
import icu.windea.pls.lang.inspections.script.common.UnresolvedExpressionInspection

object PlsInspectionUtil {
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
