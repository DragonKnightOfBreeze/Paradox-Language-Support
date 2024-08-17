package icu.windea.pls.ep.expression

import com.intellij.openapi.extensions.*
import com.intellij.psi.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.lang.expression.*

/**
 * 用于匹配脚本表达式和CWT规则表达式。
 * @see ParadoxExpressionMatcher
 * @see ParadoxExpressionMatcher.Result
 */
interface ParadoxScriptExpressionMatcher {
    /**
     * 匹配脚本表达式和CWT规则表达式。
     * @param element 上下文PSI元素。
     * @param expression 脚本表达式
     * @param configExpression CWT规则表达式。
     * @param config 上下文CWT规则。
     * @param configGroup 规则分组。
     * @return 匹配结果，类型必须为[ParadoxExpressionMatcher.Result]或者[Boolean]，否则视为不匹配。
     * @see ParadoxExpressionMatcher
     * @see ParadoxExpressionMatcher.Result
     */
    fun matches(
        element: PsiElement,
        expression: ParadoxDataExpression,
        configExpression: CwtDataExpression,
        config: CwtConfig<*>?,
        configGroup: CwtConfigGroup,
        options: Int = ParadoxExpressionMatcher.Options.Default
    ): ParadoxExpressionMatcher.Result?
    
    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<ParadoxScriptExpressionMatcher>("icu.windea.pls.dataExpressionMatcher")
        
        /**
         * @see ParadoxScriptExpressionMatcher.matches
         */
        fun matches(
            element: PsiElement,
            expression: ParadoxDataExpression,
            configExpression: CwtDataExpression,
            config: CwtConfig<*>?,
            configGroup: CwtConfigGroup,
            options: Int
        ): ParadoxExpressionMatcher.Result {
            EP_NAME.extensionList.forEach f@{ ep ->
                val r = ep.matches(element, expression, configExpression, config, configGroup, options)
                if(r != null) return r
            }
            return ParadoxExpressionMatcher.Result.FallbackMatch
        }
    }
}
