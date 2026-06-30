package icu.windea.pls.ep.inspections

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.base.annotations.WithGameTypeEP
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.lang.psi.ParadoxExpressionElement

/**
 * 不正确的表达式的检查器。
 *
 * 说明：
 * - 适用于脚本文件和 CSV 文件中的各种表达式。
 * - 某些表达式在语义匹配阶段可能采用更宽松的匹配策略，在检查阶段才会通过代码检查报告更多问题。
 * - 例如，数值超出区间、作用域不匹配等。
 *
 * @see icu.windea.pls.lang.inspections.script.expression.IncorrectExpressionInspection
 * @see icu.windea.pls.lang.inspections.csv.expression.IncorrectExpressionInspection
 */
@WithGameTypeEP
interface ParadoxIncorrectExpressionChecker {
    fun check(element: ParadoxExpressionElement, config: CwtMemberConfig<*>, holder: ProblemsHolder)

    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName<ParadoxIncorrectExpressionChecker>("icu.windea.pls.incorrectExpressionChecker")
    }
}
