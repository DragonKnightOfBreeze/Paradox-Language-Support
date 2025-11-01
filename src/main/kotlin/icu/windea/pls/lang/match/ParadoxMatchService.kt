package icu.windea.pls.lang.match

import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.optionData
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.util.withOperator
import icu.windea.pls.ep.match.ParadoxCsvExpressionMatcher
import icu.windea.pls.ep.match.ParadoxScriptExpressionMatcher
import icu.windea.pls.lang.resolve.expression.ParadoxScriptExpression
import icu.windea.pls.script.psi.ParadoxScriptBlockElement
import icu.windea.pls.script.psi.findProperty
import icu.windea.pls.script.psi.stringValue

object ParadoxMatchService {
    /**
     * @see ParadoxScriptExpressionMatcher.match
     */
    fun matchScriptExpression(
        element: PsiElement,
        expression: ParadoxScriptExpression,
        configExpression: CwtDataExpression,
        config: CwtConfig<*>?,
        configGroup: CwtConfigGroup,
        options: Int = ParadoxMatchOptions.Default
    ): ParadoxMatchResult {
        ParadoxScriptExpressionMatcher.EP_NAME.extensionList.forEach f@{ ep ->
            val r = ep.match(element, expression, configExpression, config, configGroup, options)
            if (r != null) return r
        }
        return ParadoxMatchResult.NotMatch
    }

    /**
     * @see ParadoxCsvExpressionMatcher.match
     */
    fun matchCsvExpression(
        element: PsiElement,
        expressionText: String,
        configExpression: CwtDataExpression,
        configGroup: CwtConfigGroup,
    ): ParadoxMatchResult {
        ParadoxCsvExpressionMatcher.EP_NAME.extensionList.forEach f@{ ep ->
            val r = ep.match(element, expressionText, configExpression, configGroup)
            if (r != null) return r
        }
        return ParadoxMatchResult.NotMatch
    }

    /**
     * 根据附加到 [config] 上的 `## predicate` 选项中的元数据，以及 [element] 所在的块（[ParadoxScriptBlockElement]）中的结构，进行简单的结构匹配。
     */
    fun matchesByPredicate(element: PsiElement, config: CwtMemberConfig<*>): Boolean {
        run {
            val predicate = config.optionData { predicate }
            if (predicate.isEmpty()) return@run
            val parentBlock = element.parentOfType<ParadoxScriptBlockElement>(withSelf = false) ?: return@run
            predicate.forEach f@{ (pk, pv) ->
                val p1 = parentBlock.findProperty(pk, inline = true)
                val pv1 = p1?.propertyValue?.stringValue()
                val pr = pv.withOperator { it == pv1 }
                if (!pr) return false
            }
        }
        return true
    }
}
