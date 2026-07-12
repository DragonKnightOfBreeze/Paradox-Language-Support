package icu.windea.pls.ep.inspections

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.base.annotations.WithGameTypeEP
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.lang.inspections.ParadoxExpressionInspectionContext
import icu.windea.pls.lang.psi.ParadoxExpressionElement

/**
 * 不正确的表达式的检查器。
 *
 * 说明：
 * - 适用于脚本文件和 CSV 文件中的各种表达式。
 * - 仅当存在完全匹配的规则时才可用。
 * - 某些表达式在语义匹配阶段可能采用更宽松的匹配策略，在检查阶段才会报告更多问题。
 * - 例如，数值超出区间、作用域不匹配等。
 *
 * @see icu.windea.pls.lang.inspections.script.expression.IncorrectExpressionInspection
 * @see icu.windea.pls.lang.inspections.csv.expression.IncorrectExpressionInspection
 */
@WithGameTypeEP
interface ParadoxIncorrectExpressionChecker {
    /**
     * 执行检查。
     *
     * @param element 要检查的 [ParadoxExpressionElement]。
     * @param config 完全匹配的规则。
     * @param context 文件级别的上下文。
     * @return 是否继续执行下一个检查器。
     */
    fun check(element: ParadoxExpressionElement, config: CwtMemberConfig<*>, context: ParadoxExpressionInspectionContext): Boolean

    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName<ParadoxIncorrectExpressionChecker>("icu.windea.pls.incorrectExpressionChecker")
    }
}
