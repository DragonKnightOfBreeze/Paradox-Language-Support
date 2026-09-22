package icu.windea.pls.lang.match

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.ep.match.expression.ParadoxCsvExpressionMatcher
import icu.windea.pls.ep.match.expression.ParadoxScriptExpressionMatchOptimizer
import icu.windea.pls.ep.match.expression.ParadoxScriptExpressionMatcher
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.expressions.ParadoxExpression

/**
 * 表达式匹配的上下文。
 *
 * 要匹配的规则和规则表达式并未保存在此上下文对象中。
 *
 * @property element 上下文 PSI 元素。
 * @property expression 脚本表达式。
 * @property configGroup 规则分组。
 *
 * @see ParadoxExpressionMatchService
 * @see ParadoxScriptExpressionMatcher
 * @see ParadoxCsvExpressionMatcher
 * @see ParadoxScriptExpressionMatchOptimizer
 */
data class ParadoxExpressionMatchContext(
    val element: PsiElement,
    val expression: ParadoxExpression,
    val configGroup: CwtConfigGroup,
    val options: ParadoxMatchOptions? = null,
) {
    val project: Project get() = configGroup.project
    val gameType: ParadoxGameType get() = configGroup.gameType

    // 3.0.1 optimize: use attribute to apply fast return
    val usePredicateBasedMatch: Boolean = configGroup.attributes.usePredicateBasedMatch
}
