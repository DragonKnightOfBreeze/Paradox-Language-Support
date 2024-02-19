package icu.windea.pls.lang.expression

import com.intellij.openapi.extensions.*
import com.intellij.psi.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.CwtConfigMatcher.Result

/**
 * 用于匹配脚本表达式和CWT规则表达式。
 * @see CwtConfigMatcher
 * @see CwtConfigMatcher.Result
 */
interface CwtDataExpressionMatcher {
    /**
     * 匹配脚本表达式和CWT规则表达式。
     * @param element 上下文PSI元素。
     * @param expression 脚本表达式
     * @param configExpression CWT规则表达式。
     * @param config 上下文CWT规则。
     * @param configGroup CWT规则分组。
     * @return 匹配结果，类型必须为[CwtConfigMatcher.Result]或者[Boolean]，否则视为不匹配。
     * @see CwtConfigMatcher
     * @see CwtConfigMatcher.Result
     */
    fun matches(
        element: PsiElement,
        expression: ParadoxDataExpression,
        configExpression: CwtDataExpression,
        config: CwtConfig<*>?,
        configGroup: CwtConfigGroup,
        options: Int = CwtConfigMatcher.Options.Default
    ): Any?
    
    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<CwtDataExpressionMatcher>("icu.windea.pls.dataExpressionMatcher")
        
        /**
         * @see CwtDataExpressionMatcher.matches
         */
        fun matches(
            element: PsiElement,
            expression: ParadoxDataExpression,
            configExpression: CwtDataExpression,
            config: CwtConfig<*>?,
            configGroup: CwtConfigGroup,
            options: Int
        ): Result {
            EP_NAME.extensionList.forEachFast f@{ ep ->
                val r = ep.matches(element, expression, configExpression, config, configGroup, options)
                when(r) {
                    is Result -> return r
                    is Boolean -> return Result.of(r)
                }
            }
            return Result.FallbackMatch
        }
    }
}