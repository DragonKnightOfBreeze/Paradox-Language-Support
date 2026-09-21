package icu.windea.pls.lang.manipulation

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.CwtAliasConfig
import icu.windea.pls.config.config.delegated.CwtUnionConfig
import icu.windea.pls.config.config.expandUnionValues
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.collections.process
import icu.windea.pls.csv.ParadoxCsvLanguage
import icu.windea.pls.lang.match.ParadoxExpressionMatchContext
import icu.windea.pls.lang.match.ParadoxExpressionMatchService.matchCsvExpression
import icu.windea.pls.lang.match.ParadoxExpressionMatchService.matchScriptExpression
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.match.ParadoxMatchResult
import icu.windea.pls.model.expressions.ParadoxExpression
import icu.windea.pls.script.ParadoxScriptLanguage

object ParadoxConfigManipulationService {
    /**
     * 展开并匹配名为 [unionName] 的并集规则的所有作为候选项的值规则。
     *
     * 排除绝对不匹配的情况（参见 [ParadoxMatchResult.NotMatch]）。
     *
     * @see CwtUnionConfig
     */
    fun expandAndMatchUnionValues(
        element: PsiElement,
        expression: ParadoxExpression,
        unionName: String,
        configGroup: CwtConfigGroup,
        options: ParadoxMatchOptions? = null,
        processor: (CwtValueConfig, ParadoxMatchResult) -> Boolean,
    ): Boolean {
        // NOTE 3.0.3 use processor pattern to optimize performance

        val language = element.language
        if (language != ParadoxScriptLanguage && language != ParadoxCsvLanguage) return true
        val unionConfig = configGroup.unions[unionName] ?: return true
        val matchContext = ParadoxExpressionMatchContext(element, expression, configGroup, options)
        when (language) {
            ParadoxScriptLanguage -> {
                unionConfig.expandUnionValues { valueConfig ->
                    ProgressManager.checkCanceled()
                    val matchResult = matchScriptExpression(matchContext, valueConfig.configExpression, valueConfig)
                    val matched = matchResult !== ParadoxMatchResult.NotMatch
                    if (matched) processor(valueConfig, matchResult) else true
                }.let { if (!it) return false }
            }
            ParadoxCsvLanguage -> {
                unionConfig.expandUnionValues { valueConfig ->
                    ProgressManager.checkCanceled()
                    val matchResult = matchCsvExpression(matchContext, valueConfig.configExpression)
                    val matched = matchResult !== ParadoxMatchResult.NotMatch
                    if (matched) processor(valueConfig, matchResult) else true
                }.let { if (!it) return false }
            }
        }
        return true
    }

    /**
     * 展开名为 [unionName] 的并集规则的所有匹配的作为候选项的值规则。
     *
     * @see CwtUnionConfig
     */
    fun expandMatchedUnionValues(
        element: PsiElement,
        expression: ParadoxExpression,
        unionName: String,
        configGroup: CwtConfigGroup,
        options: ParadoxMatchOptions? = null,
        processor: (CwtValueConfig) -> Boolean,
    ) {
        expandAndMatchUnionValues(element, expression, unionName, configGroup, options) { unionValueConfig, matchResult ->
            if (matchResult.get(options)) processor(unionValueConfig) else true
        }
    }

    /**
     * 展开并匹配名为 [aliasName] 的别名规则的所有别名键（即 `alias[x:y] = ...` 中的 `y`）。
     *
     * 排除绝对不匹配的情况（参见 [ParadoxMatchResult.NotMatch]）。
     *
     * @see CwtAliasConfig
     */
    fun expandAndMatchAliasKeys(
        element: PsiElement,
        expression: ParadoxExpression,
        aliasName: String,
        configGroup: CwtConfigGroup,
        options: ParadoxMatchOptions? = null,
        processor: (String, ParadoxMatchResult) -> Boolean,
    ): Boolean {
        // NOTE 3.0.3 fast return if the alias key can be matched constantly (case-insensitive), otherwise, try further match
        // NOTE 3.0.3 should also include non-const keys if the expression is parameterized
        // NOTE 3.0.3 use processor pattern to optimize performance

        run {
            val constKey = configGroup.aliasModel.name2ConstKeys[aliasName]?.get(expression.value)
            if (constKey == null) return@run
            val matchResult = ParadoxMatchResult.ExactMatch
            return processor(constKey, matchResult) // fast return
        }
        run {
            if (!expression.isParameterized()) return@run
            val constKeys = configGroup.aliasModel.name2ConstKeys.keys.orNull()
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
            val nonConstKeys = configGroup.aliasModel.name2NonConstKeys[aliasName].orNull()
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
     * 展开名为 [aliasName] 的别名规则的所有匹配的别名键（即 `alias[x:y] = ...` 中的 `y`）。
     *
     * @see CwtAliasConfig
     */
    fun expandMatchedAliasKeys(
        element: PsiElement,
        expression: ParadoxExpression,
        aliasName: String,
        configGroup: CwtConfigGroup,
        options: ParadoxMatchOptions? = null,
        processor: (String) -> Boolean,
    ): Boolean {
        return expandAndMatchAliasKeys(element, expression, aliasName, configGroup, options) { key, matchResult ->
            if (matchResult.get(options)) processor(key) else true
        }
    }
}
