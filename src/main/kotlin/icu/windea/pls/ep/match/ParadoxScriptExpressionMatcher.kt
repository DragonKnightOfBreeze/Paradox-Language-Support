package icu.windea.pls.ep.match

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.PsiElement
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.match.ParadoxMatchResult
import icu.windea.pls.lang.match.ParadoxMatchResultProvider
import icu.windea.pls.lang.resolve.expression.ParadoxScriptExpression
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement

/**
 * 用于匹配脚本表达式与规则表达式。
 *
 * @see ParadoxScriptExpressionElement
 * @see ParadoxScriptExpression
 * @see CwtDataExpression
 */
interface ParadoxScriptExpressionMatcher {
    /**
     * 匹配脚本表达式和规则表达式。
     *
     * @param element 上下文 PSI 元素。
     * @param expression 脚本表达式
     * @param configExpression 规则表达式。
     * @param config 上下文规则。
     * @param configGroup 规则分组。
     * @return 匹配结果。
     *
     * @see ParadoxMatchResult
     * @see ParadoxMatchResultProvider
     */
    fun matches(
        element: PsiElement,
        expression: ParadoxScriptExpression,
        configExpression: CwtDataExpression,
        config: CwtConfig<*>?,
        configGroup: CwtConfigGroup,
        options: Int = ParadoxMatchOptions.Default
    ): ParadoxMatchResult?

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxScriptExpressionMatcher>("icu.windea.pls.scriptExpressionMatcher")

        /**
         * @see ParadoxScriptExpressionMatcher.matches
         */
        fun matches(
            element: PsiElement,
            expression: ParadoxScriptExpression,
            configExpression: CwtDataExpression,
            config: CwtConfig<*>?,
            configGroup: CwtConfigGroup,
            options: Int = ParadoxMatchOptions.Default
        ): ParadoxMatchResult {
            EP_NAME.extensionList.forEach f@{ ep ->
                val r = ep.matches(element, expression, configExpression, config, configGroup, options)
                if (r != null) return r
            }
            return ParadoxMatchResult.NotMatch
        }
    }
}
