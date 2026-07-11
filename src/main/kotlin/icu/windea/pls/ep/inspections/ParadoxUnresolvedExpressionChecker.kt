package icu.windea.pls.ep.inspections

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.base.annotations.WithGameTypeEP
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.lang.inspections.ParadoxExpressionInspectionContext
import icu.windea.pls.lang.psi.ParadoxExpressionElement

/**
 * 无法解析的表达式的检查器。
 *
 * 说明：
 * - 适用于脚本文件和 CSV 文件中的各种表达式。
 * - 仅当存在作为上下文的规则，但不存在匹配的规则时才可用。
 *
 * @see icu.windea.pls.lang.inspections.script.expression.UnresolvedExpressionInspection
 * @see icu.windea.pls.lang.inspections.csv.expression.UnresolvedExpressionInspection
 */
@WithGameTypeEP
interface ParadoxUnresolvedExpressionChecker {
    /**
     * 执行检查。
     *
     * @param element 要检查的 [ParadoxExpressionElement]。
     * @param expectedConfigs 期望的一组规则。可能为空。
     * @param context 文件级别的上下文。
     * @return 是否继续执行下一个检查器。
     */
    fun check(element: ParadoxExpressionElement, expectedConfigs: List<CwtMemberConfig<*>>, context: ParadoxExpressionInspectionContext): Boolean

    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName<ParadoxUnresolvedExpressionChecker>("icu.windea.pls.unresolvedExpressionChecker")
    }
}
