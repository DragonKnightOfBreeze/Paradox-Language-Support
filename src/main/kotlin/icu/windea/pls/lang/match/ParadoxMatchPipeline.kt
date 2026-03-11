package icu.windea.pls.lang.match

import com.intellij.psi.PsiElement
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.FastList
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.collections.mapFast
import icu.windea.pls.ep.match.ParadoxScriptExpressionMatchOptimizer
import icu.windea.pls.lang.PlsStates
import icu.windea.pls.lang.resolve.dynamic
import icu.windea.pls.lang.resolve.expression.ParadoxScriptExpression

object ParadoxMatchPipeline {
    /**
     * 根据来自 [matchResultProvider] 的匹配结果，从输入的一组成员规则 [configs] 收集匹配候选项。
     */
    @Optimized
    inline fun <T : CwtMemberConfig<*>> collectCandidates(configs: List<T>, matchResultProvider: (T) -> ParadoxMatchResult): List<ParadoxMatchCandidate> {
        if (configs.isEmpty()) return emptyList()
        val result = buildList {
            configs.forEachFast f@{ config ->
                val matchResult = matchResultProvider(config)
                if (matchResult == ParadoxMatchResult.NotMatch) return@f
                val matchCandidate = ParadoxMatchCandidate(config, matchResult)
                this += matchCandidate
            }
        }
        return result
    }

    /**
     * 处理输入的一组匹配候选项 [candidates]，进行进一步的匹配。基于匹配结果的类型。
     */
    @Optimized
    fun process(candidates: List<ParadoxMatchCandidate>, options: ParadoxMatchOptions? = null): List<CwtMemberConfig<*>> {
        // 步骤：
        // - 处理精确匹配（`ExactMatch` `ToleratedExactMatch`），如果有结果，则仅使用这些结果，并直接返回
        // - 处理需要检测子句内容的匹配（`LazyBlockAwareMatch`），如果存在匹配项，则保留所有匹配项或者第一个候选项
        // - 处理需要检测作用域上下文的匹配（`LazyScopeAwareMatch`），如果存在匹配项，则保留所有匹配项或者第一个候选项
        // - 处理其余的各种直接匹配，如果有结果，则仅使用这些结果
        // - 处理通配符匹配（`WildcardMatch`，不验证其中某部分在解析引用后是否合法），如果有结果，则仅使用这些结果
        // - 处理更宽松的通配符匹配（`RelaxWildcardMatch`，这意味着存在另一种更精确的格式），如果有结果，则仅使用这些结果
        // - 处理部分匹配（`PartialMatch`），如果有结果，则仅使用这些结果
        // - 处理回退匹配（`FallbackMatch`），如果有结果，则仅使用这些结果
        // - 如果不是直接返回的情况，还需要处理带参数的匹配（`ParameterizedMatch`），如果有结果，则需要加入最终的结果中

        if (candidates.isEmpty()) return emptyList()
        val matched = FastList<ParadoxMatchCandidate>()
        processNormal(candidates, matched, options) { it.result is ParadoxMatchResult.ExactMatch || it.result is ParadoxMatchResult.ToleratedExactMatch }
        if (matched.isNotEmpty()) return matched.mapFast { it.value }
        processMain(candidates, matched, options)
        processNormal(candidates, matched, options) { it.result is ParadoxMatchResult.ParameterizedMatch }
        return matched.mapFast { it.value }
    }

    private fun processMain(candidates: List<ParadoxMatchCandidate>, matched: MutableList<ParadoxMatchCandidate>, options: ParadoxMatchOptions?) {
        processLazy(candidates, matched, options) { it.result is ParadoxMatchResult.LazyBlockAwareMatch }
        processLazy(candidates, matched, options) { it.result is ParadoxMatchResult.LazyScopeAwareMatch }

        processNormal(candidates, matched, options) { it.result is ParadoxMatchResult.DirectMatch }
        if (matched.isNotEmpty()) return

        processNormal(candidates, matched, options) { it.result is ParadoxMatchResult.WildcardMatch }
        if (matched.isNotEmpty()) return
        processNormal(candidates, matched, options) { it.result is ParadoxMatchResult.RelaxWildcardMatch }
        if (matched.isNotEmpty()) return
        processNormal(candidates, matched, options) { it.result is ParadoxMatchResult.PartialMatch }
        if (matched.isNotEmpty()) return

        processNormal(candidates, matched, options) { it.result is ParadoxMatchResult.FallbackMatch }
    }

    private inline fun processNormal(candidates: List<ParadoxMatchCandidate>, matched: MutableList<ParadoxMatchCandidate>, options: ParadoxMatchOptions?, predicate: (ParadoxMatchCandidate) -> Boolean) {
        candidates.forEachFast f@{
            if (it.processed) return@f
            if (!predicate(it)) return@f
            it.processed = true
            if (!it.result.get(options)) return@f
            matched += it
        }
    }

    private inline fun processLazy(candidates: List<ParadoxMatchCandidate>, matched: MutableList<ParadoxMatchCandidate>, options: ParadoxMatchOptions?, predicate: (ParadoxMatchCandidate) -> Boolean) {
        val lazyMatched = FastList<ParadoxMatchCandidate>()
        processNormal(candidates, lazyMatched, options, predicate)
        val lazyMatchedSize = lazyMatched.size
        if (lazyMatchedSize == 1) {
            matched += lazyMatched.first()
        } else if (lazyMatchedSize > 1) {
            val oldMatchedSize = matched.size
            lazyMatched.forEachFast f@{
                if (!it.result.get(options)) return@f
                matched += it
            }
            if (oldMatchedSize == matched.size) matched += lazyMatched.first()
        }
    }

    /**
     * 对输入的一组已处理过的成员规则 [configs] 进行后续优化。基于 [ParadoxScriptExpressionMatchOptimizer]。
     */
    @Optimized
    fun <T : CwtMemberConfig<*>> optimize(configs: List<T>, element: PsiElement, expression: ParadoxScriptExpression, options: ParadoxMatchOptions? = null): List<T> {
        if (configs.isEmpty()) return emptyList()
        val configGroup = configs.first().configGroup
        var result = configs
        var dynamic = false

        val context = ParadoxScriptExpressionMatchOptimizerContext(element, expression, configGroup, options)
        val optimizers = ParadoxScriptExpressionMatchOptimizer.EP_NAME.extensionList
        optimizers.forEachFast f@{ optimizer ->
            val optimized = optimizer.optimize(result, context)
            if (optimized == null) return@f
            if (optimizer.isDynamic(context)) dynamic = true
            result = optimized
        }

        // NOTE 2.1.2 如果是动态的优化器，需要把正在解析的规则上下文标记为动态的
        if (dynamic) PlsStates.resolvingConfigContextStack.get()?.peekLast()?.dynamic = true

        return result
    }
}
