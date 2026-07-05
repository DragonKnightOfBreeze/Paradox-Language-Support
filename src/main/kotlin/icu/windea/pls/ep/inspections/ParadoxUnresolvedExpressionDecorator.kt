package icu.windea.pls.ep.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.base.annotations.WithGameTypeEP
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.lang.psi.ParadoxExpressionElement

/**
 * 无法解析的表达式的代码检查的装饰器。
 *
 * 说明：
 * - 适用于脚本文件和 CSV 文件中的各种表达式。
 * - 用于调整报错信息和高亮类型，以及提供额外的快速修复。
 *
 * @see icu.windea.pls.lang.inspections.script.expression.UnresolvedExpressionInspection
 * @see icu.windea.pls.lang.inspections.csv.expression.UnresolvedExpressionInspection
 */
@WithGameTypeEP
interface ParadoxUnresolvedExpressionDecorator {
    context(tool: LocalInspectionTool)
    fun getDescription(element: ParadoxExpressionElement, expectedConfigs: List<CwtMemberConfig<*>>): String? = null

    context(tool: LocalInspectionTool)
    fun getHighlightType(element: ParadoxExpressionElement, expectedConfigs: List<CwtMemberConfig<*>>): ProblemHighlightType? = null

    context(tool: LocalInspectionTool)
    fun collectFixes(element: ParadoxExpressionElement, expectedConfigs: List<CwtMemberConfig<*>>, result: MutableList<LocalQuickFix>) {
        // nothing
    }

    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName<ParadoxUnresolvedExpressionDecorator>("icu.windea.pls.unresolvedExpressionDecorator")
    }
}
