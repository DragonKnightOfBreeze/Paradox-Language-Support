package icu.windea.pls.ep.expression

import com.intellij.psi.PsiElement
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.memberConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.expression.ParadoxScriptExpression
import icu.windea.pls.lang.util.ParadoxExpressionMatcher
import icu.windea.pls.lang.util.ParadoxExpressionMatcher.Result

class ParadoxScriptPredicateBasedExpressionMatcher : ParadoxScriptExpressionMatcher {
    override fun matches(element: PsiElement, expression: ParadoxScriptExpression, configExpression: CwtDataExpression, config: CwtConfig<*>?, configGroup: CwtConfigGroup, options: Int): Result? {
        // 如果附有 `## predicate = {...}` 选项，则根据上下文进行匹配
        // 这里的 config 也可能是属性值对应的规则，因此下面需要传入 config.memberConfig
        val memberConfig = if (config is CwtMemberConfig<*>) config.memberConfig else null
        if (memberConfig == null) return null
        if (!ParadoxExpressionMatcher.matchesByPredicate(element, memberConfig)) return Result.NotMatch
        return null
    }
}
