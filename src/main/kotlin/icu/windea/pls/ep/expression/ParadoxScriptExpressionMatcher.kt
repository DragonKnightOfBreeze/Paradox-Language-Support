package icu.windea.pls.ep.expression

import com.intellij.openapi.extensions.*
import com.intellij.psi.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configExpression.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.lang.expression.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.lang.util.ParadoxExpressionMatcher.Options
import icu.windea.pls.lang.util.ParadoxExpressionMatcher.Result
import icu.windea.pls.script.psi.*

/**
 * 用于匹配脚本表达式与CWT规则表达式。
 *
 * @see ParadoxScriptExpressionElement
 * @see ParadoxScriptExpression
 * @see CwtDataExpression
 */
interface ParadoxScriptExpressionMatcher {
    /**
     * 匹配脚本表达式和CWT规则表达式。
     *
     * @param element 上下文PSI元素。
     * @param expression 脚本表达式
     * @param configExpression CWT规则表达式。
     * @param config 上下文CWT规则。
     * @param configGroup 规则分组。
     * @return 匹配结果。
     * @see ParadoxExpressionMatcher
     * @see ParadoxExpressionMatcher.Result
     */
    fun matches(
        element: PsiElement,
        expression: ParadoxScriptExpression,
        configExpression: CwtDataExpression,
        config: CwtConfig<*>?,
        configGroup: CwtConfigGroup,
        options: Int = Options.Default
    ): Result?

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
            options: Int = Options.Default
        ): Result {
            EP_NAME.extensionList.forEach f@{ ep ->
                val r = ep.matches(element, expression, configExpression, config, configGroup, options)
                if (r != null) return r
            }
            return Result.NotMatch
        }
    }
}
