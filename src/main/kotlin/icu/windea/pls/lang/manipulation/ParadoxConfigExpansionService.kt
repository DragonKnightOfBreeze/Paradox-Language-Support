package icu.windea.pls.lang.manipulation

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import icu.windea.pls.config.config.delegated.CwtAliasConfig
import icu.windea.pls.config.config.delegated.CwtUnionConfig
import icu.windea.pls.config.config.detached.CwtExpandedAliasConfig
import icu.windea.pls.config.config.detached.CwtExpandedUnionConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.annotations.RecursionSensitive
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.collections.process
import icu.windea.pls.core.collections.processFast
import icu.windea.pls.core.runWithRecursionGuard
import icu.windea.pls.core.util.stream.BiPushStream
import icu.windea.pls.csv.ParadoxCsvLanguage
import icu.windea.pls.lang.match.ParadoxExpressionMatchContext
import icu.windea.pls.lang.match.ParadoxExpressionMatchService.matchScriptExpression
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.match.ParadoxMatchResult
import icu.windea.pls.model.expressions.ParadoxExpression
import icu.windea.pls.script.ParadoxScriptLanguage

@Optimized
object ParadoxConfigExpansionService {
    /**
     * 将 [name] 作为并集规则的名字，展开并匹配此规则的所有候选项。
     *
     * 说明：
     * - 如果可以进行常量匹配，则直接快速返回。
     * - 如果是脚本表达式，则兼容表达式带参数的情况。
     * - 排除绝对不匹配的情况（参见 [ParadoxMatchResult.NotMatch]）。
     *
     * **注意**：调用时需要避免递归。
     *
     * @see CwtUnionConfig
     * @see CwtExpandedUnionConfig
     */
    @RecursionSensitive
    fun expandAndMatchUnion(
        element: PsiElement,
        expression: ParadoxExpression,
        name: String,
        configGroup: CwtConfigGroup,
        options: ParadoxMatchOptions? = null,
        guardName: String,
    ): BiPushStream<CwtExpandedUnionConfig, ParadoxMatchResult> {
        // for script files and csv files
        val language = element.language
        if (language != ParadoxScriptLanguage && language != ParadoxCsvLanguage) return BiPushStream.of()
        return BiPushStream.from {
            val guardKey = "u:$name"
            runWithRecursionGuard(guardName, guardKey) {
                expandAndMatchUnionWith(element, expression, name, configGroup, options)
            }
        }
    }

    context(processor: (CwtExpandedUnionConfig, ParadoxMatchResult) -> Boolean)
    private fun expandAndMatchUnionWith(
        element: PsiElement,
        expression: ParadoxExpression,
        name: String,
        configGroup: CwtConfigGroup,
        options: ParadoxMatchOptions?,
    ): Boolean {
        // NOTE 3.0.3 fast return if can be matched constantly (case-insensitive), otherwise, try further match
        // NOTE 3.0.3 should also include non-const keys if the expression is parameterized
        val model = configGroup.unionModel
        run {
            val const = model.expandedForConst[name]?.get(expression.value)
            if (const == null) return@run
            val matchResult = ParadoxMatchResult.ExactMatch
            return processor(const, matchResult) // fast return
        }
        run {
            if (element.language !== ParadoxScriptLanguage) return@run
            if (!expression.isParameterized()) return@run
            val consts = model.expandedForConst[name]?.values.orNull()
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
            val nonConsts = model.expandedForNonConst[name].orNull()
            if (nonConsts == null) return@run
            ProgressManager.checkCanceled() // check cancellation
            val matchContext = ParadoxExpressionMatchContext(element, expression, configGroup, options)
            nonConsts.processFast p@{ nonConst ->
                ProgressManager.checkCanceled() // check cancellation
                val matchResult = matchScriptExpression(matchContext, nonConst.configExpression, null)
                val matched = matchResult !== ParadoxMatchResult.NotMatch
                if (matched) processor(nonConst, matchResult) else true
            }.let { if (!it) return false }
        }
        return true
    }

    /**
     * 将 [name] 作为别名规则的名字，展开并匹配此规则的所有候选项的键名。
     *
     * 说明：
     * - 如果可以进行常量匹配，则直接快速返回。
     * - 如果是脚本表达式，则兼容表达式带参数的情况。
     * - 排除绝对不匹配的情况（参见 [ParadoxMatchResult.NotMatch]）。
     *
     * **注意**：调用时需要避免递归。
     *
     * @see CwtAliasConfig
     * @see CwtExpandedAliasConfig
     */
    @RecursionSensitive
    fun expandAndMatchAlias(
        element: PsiElement,
        expression: ParadoxExpression,
        name: String,
        configGroup: CwtConfigGroup,
        options: ParadoxMatchOptions? = null,
        guardName: String,
    ): BiPushStream<CwtExpandedAliasConfig, ParadoxMatchResult> {
        // only for script files
        val language = element.language
        if (language !== ParadoxScriptLanguage) return BiPushStream.of()
        return BiPushStream.from {
            val guardKey = "a:$name"
            runWithRecursionGuard(guardName, guardKey) {
                expandAndMatchAliasWith(element, expression, name, configGroup, options)
            }
        }
    }

    context(processor: (CwtExpandedAliasConfig, ParadoxMatchResult) -> Boolean)
    private fun expandAndMatchAliasWith(
        element: PsiElement,
        expression: ParadoxExpression,
        name: String,
        configGroup: CwtConfigGroup,
        options: ParadoxMatchOptions? = null,
    ): Boolean {
        // NOTE 3.0.3 fast return if can be matched constantly (case-insensitive), otherwise, try further match
        // NOTE 3.0.3 should also include non-const keys if the expression is parameterized
        val model = configGroup.aliasModel
        run {
            val const = model.expandedForConst[name]?.get(expression.value)
            if (const == null) return@run
            val matchResult = ParadoxMatchResult.ExactMatch
            return processor(const, matchResult) // fast return
        }
        run {
            if (!expression.isParameterized()) return@run
            val consts = model.expandedForConst[name]?.values.orNull()
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
            val nonConsts = model.expandedForNonConst[name].orNull()
            if (nonConsts == null) return@run
            ProgressManager.checkCanceled() // check cancellation
            val matchContext = ParadoxExpressionMatchContext(element, expression, configGroup, options)
            nonConsts.processFast p@{ nonConst ->
                ProgressManager.checkCanceled() // check cancellation
                val matchResult = matchScriptExpression(matchContext, nonConst.configExpression, null)
                val matched = matchResult !== ParadoxMatchResult.NotMatch
                if (matched) processor(nonConst, matchResult) else true
            }.let { if (!it) return false }
        }
        return true
    }
}
