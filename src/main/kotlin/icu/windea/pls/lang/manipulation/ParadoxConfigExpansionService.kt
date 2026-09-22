package icu.windea.pls.lang.manipulation

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.CwtAliasConfig
import icu.windea.pls.config.config.delegated.CwtUnionConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.collections.process
import icu.windea.pls.csv.ParadoxCsvLanguage
import icu.windea.pls.lang.match.ParadoxExpressionMatchContext
import icu.windea.pls.lang.match.ParadoxExpressionMatchService.matchScriptExpression
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.match.ParadoxMatchResult
import icu.windea.pls.model.expressions.ParadoxExpression
import icu.windea.pls.script.ParadoxScriptLanguage

object ParadoxConfigExpansionService {
    /**
     * 将 [name] 作为并集规则的名字，展开并匹配此规则的所有候选项。
     *
     * 说明：
     * - 如果可以进行常量匹配，则直接快速返回。
     * - 如果是脚本表达式，则兼容表达式带参数的情况。
     * - 排除绝对不匹配的情况（参见 [ParadoxMatchResult.NotMatch]）。
     *
     * @see CwtUnionConfig
     */
    fun expandAndMatchUnion(
        element: PsiElement,
        expression: ParadoxExpression,
        name: String,
        configGroup: CwtConfigGroup,
        options: ParadoxMatchOptions? = null,
        processor: (r: CwtValueConfig, mr: ParadoxMatchResult) -> Boolean,
    ): Boolean {
        // for script files and csv files
        val language = element.language
        if (language != ParadoxScriptLanguage && language != ParadoxCsvLanguage) return true
        // NOTE 3.0.3 use processor pattern to optimize performance
        // NOTE 3.0.3 fast return if can be matched constantly (case-insensitive), otherwise, try further match
        // NOTE 3.0.3 should also include non-const keys if the expression is parameterized
        run {
            val const = configGroup.unionModel.forConst[name]?.get(expression.value)
            if (const == null) return@run
            val matchResult = ParadoxMatchResult.ExactMatch
            return processor(const, matchResult) // fast return
        }
        run {
            if (element.language !== ParadoxScriptLanguage) return@run
            if (!expression.isParameterized()) return@run
            val consts = configGroup.unionModel.forConst[name]?.values.orNull()
            if (consts == null) return@run
            ProgressManager.checkCanceled() // check cancellation
            val matchResult = ParadoxMatchResult.ParameterizedMatch
            if (expression.isFullParameterized()) {
                consts.process { const ->
                    processor(const, matchResult)
                }.let { if (!it) return false }
            } else {
                consts.process { const ->
                    val matched = expression.matchesRegex(const.configExpression.expressionString)
                    if (matched) processor(const, matchResult) else true
                }.let { if (!it) return false }
            }
        }
        run {
            val nonConsts = configGroup.unionModel.forNonConstSorted[name].orNull()
            if (nonConsts == null) return@run
            ProgressManager.checkCanceled() // check cancellation
            val matchContext = ParadoxExpressionMatchContext(element, expression, configGroup, options)
            nonConsts.process p@{ nonConst ->
                ProgressManager.checkCanceled() // check cancellation
                val matchResult = matchScriptExpression(matchContext, nonConst.configExpression, null)
                val matched = matchResult !== ParadoxMatchResult.NotMatch
                if (matched) processor(nonConst, matchResult) else true
            }.let { if (!it) return false }
        }
        return true
    }

    /**
     * 展开名为 [unionName] 的并集规则的所有匹配的作为候选项的值规则。
     *
     * @see CwtUnionConfig
     */
    fun expandMatchedUnion(
        element: PsiElement,
        expression: ParadoxExpression,
        unionName: String,
        configGroup: CwtConfigGroup,
        options: ParadoxMatchOptions? = null,
        processor: (r: CwtValueConfig) -> Boolean,
    ) {
        expandAndMatchUnion(element, expression, unionName, configGroup, options) { unionValueConfig, matchResult ->
            if (matchResult.get(options)) processor(unionValueConfig) else true
        }
    }

    /**
     * 将 [name] 作为别名规则的名字，展开并匹配此规则的所有候选项的键名。
     *
     * 说明：
     * - 如果可以进行常量匹配，则直接快速返回。
     * - 如果是脚本表达式，则兼容表达式带参数的情况。
     * - 排除绝对不匹配的情况（参见 [ParadoxMatchResult.NotMatch]）。
     *
     * @see CwtAliasConfig
     */
    fun expandAndMatchAliasKeys(
        element: PsiElement,
        expression: ParadoxExpression,
        name: String,
        configGroup: CwtConfigGroup,
        options: ParadoxMatchOptions? = null,
        processor: (r: String, mr: ParadoxMatchResult) -> Boolean,
    ): Boolean {
        // only for script files
        val language = element.language
        if (language !== ParadoxScriptLanguage) return true
        // NOTE 3.0.3 use processor pattern to optimize performance
        // NOTE 3.0.3 fast return if can be matched constantly (case-insensitive), otherwise, try further match
        // NOTE 3.0.3 should also include non-const keys if the expression is parameterized
        run {
            val constKey = configGroup.aliasModel.forConst[name]?.get(expression.value)
            if (constKey == null) return@run
            val matchResult = ParadoxMatchResult.ExactMatch
            return processor(constKey, matchResult) // fast return
        }
        run {
            // if (element.language !== ParadoxScriptLanguage) return@run
            if (!expression.isParameterized()) return@run
            val constKeys = configGroup.aliasModel.forConst[name]?.values.orNull()
            if (constKeys == null) return@run
            ProgressManager.checkCanceled() // check cancellation
            val matchResult = ParadoxMatchResult.ParameterizedMatch
            if (expression.isFullParameterized()) {
                constKeys.process { key ->
                    processor(key, matchResult)
                }.let { if (!it) return false }
            } else {
                constKeys.process { key ->
                    val matched = expression.matchesRegex(key)
                    if (matched) processor(key, matchResult) else true
                }.let { if (!it) return false }
            }
        }
        run {
            val nonConstKeys = configGroup.aliasModel.forNonConstSorted[name].orNull()
            if (nonConstKeys == null) return@run
            ProgressManager.checkCanceled() // check cancellation
            val matchContext = ParadoxExpressionMatchContext(element, expression, configGroup, options)
            nonConstKeys.process p@{ key ->
                ProgressManager.checkCanceled() // check cancellation
                val matchResult = matchScriptExpression(matchContext, CwtDataExpression.resolve(key), null)
                val matched = matchResult !== ParadoxMatchResult.NotMatch
                if (matched) processor(key, matchResult) else true
            }.let { if (!it) return false }
        }
        return true
    }

    /**
     * 将 [name] 作为别名规则的名字，展开此规则的所有匹配的候选项的键名。
     *
     * @see CwtAliasConfig
     */
    fun expandMatchedAliasKeys(
        element: PsiElement,
        expression: ParadoxExpression,
        name: String,
        configGroup: CwtConfigGroup,
        options: ParadoxMatchOptions? = null,
        processor: (r: String) -> Boolean,
    ): Boolean {
        return expandAndMatchAliasKeys(element, expression, name, configGroup, options) { key, matchResult ->
            if (matchResult.get(options)) processor(key) else true
        }
    }
}
