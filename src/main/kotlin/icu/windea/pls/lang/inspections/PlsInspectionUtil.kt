package icu.windea.pls.lang.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.HighlightSeverity
import icu.windea.pls.lang.inspections.script.common.ConflictingResolvedExpressionInspection
import icu.windea.pls.lang.inspections.script.common.IncorrectExpressionInspection
import icu.windea.pls.lang.inspections.script.common.MissingExpressionInspection
import icu.windea.pls.lang.inspections.script.common.TooManyExpressionInspection
import icu.windea.pls.lang.inspections.script.common.UnresolvedExpressionInspection

object PlsInspectionUtil {
    fun getExpressionInspectionTypesForScriptFiles(): Array<Class<out LocalInspectionTool>> {
        return arrayOf(
            UnresolvedExpressionInspection::class.java,
            ConflictingResolvedExpressionInspection::class.java,
            MissingExpressionInspection::class.java,
            TooManyExpressionInspection::class.java,
            IncorrectExpressionInspection::class.java,
        )
    }

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
