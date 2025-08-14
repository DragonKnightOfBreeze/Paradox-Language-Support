package icu.windea.pls.lang.util

import com.google.common.cache.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configContext.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.config.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.ep.expression.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.expression.*
import icu.windea.pls.lang.expression.complex.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

object ParadoxExpressionMatcher {
    object Options {
        /** 默认的匹配方式，先尝试通过[Result.ExactMatch]进行匹配，然后再尝试通过其他匹配方式进行匹配。 */
        const val Default = 0x00
        /** 对于[Result.LazySimpleMatch]和[Result.LazyBlockAwareMatch]，匹配结果直接返回true。 */
        const val Relax = 0x01
        /** 对于[Result.LazyIndexAwareMatch]，匹配结果直接返回true。 */
        const val SkipIndex = 0x02
        /** 对于[Result.LazyScopeAwareMatch]，匹配结果直接返回true。 */
        const val SkipScope = 0x04

        ///** 对于最终匹配得到的那个结果，不需要再次判断是否精确匹配。 */
        //const val Fast = 0x08
        /** 允许匹配定义自身。（当要匹配表达式的是一个键时） */
        const val AcceptDefinition = 0x10
    }

    sealed class Result {
        abstract fun get(options: Int = Options.Default): Boolean

        data object NotMatch : Result() {
            override fun get(options: Int) = false
        }

        data object ExactMatch : Result() {
            override fun get(options: Int) = true
        }

        data object FallbackMatch : Result() {
            override fun get(options: Int) = true
        }

        data object PartialMatch : Result() {
            override fun get(options: Int) = true
        }

        data object ParameterizedMatch : Result() {
            override fun get(options: Int) = true
        }

        sealed class LazyMatch(predicate: () -> Boolean) : Result() {
            //use manual lazy implementation instead of kotlin Lazy to optimize memory
            @Volatile
            private var value: Any = predicate

            override fun get(options: Int): Boolean {
                if (skip(options)) return true
                if (value is Boolean) return value as Boolean
                val r = doGetCatching()
                value = r
                return r
            }

            private fun skip(options: Int): Boolean {
                return when {
                    this is LazySimpleMatch -> BitUtil.isSet(options, Options.Relax)
                    this is LazyBlockAwareMatch -> BitUtil.isSet(options, Options.Relax)
                    this is LazyIndexAwareMatch -> BitUtil.isSet(options, Options.SkipIndex) || PlsCoreManager.processMergedIndex.get() == true
                    this is LazyScopeAwareMatch -> BitUtil.isSet(options, Options.SkipScope) || PlsCoreManager.processMergedIndex.get() == true
                    else -> false
                }
            }

            private fun doGetCatching(): Boolean {
                //it should be necessary to suppress outputting error logs and throwing exceptions here

                //java.lang.Throwable: Indexing process should not rely on non-indexed file data.
                //java.lang.AssertionError: Reentrant indexing
                //com.intellij.openapi.project.IndexNotReadyException

                return runCatchingCancelable {
                    @Suppress("UNCHECKED_CAST")
                    (value as () -> Boolean)()
                }.getOrDefault(true)
            }
        }

        class LazySimpleMatch(predicate: () -> Boolean) : LazyMatch(predicate)

        class LazyBlockAwareMatch(predicate: () -> Boolean) : LazyMatch(predicate)

        class LazyIndexAwareMatch(predicate: () -> Boolean) : LazyMatch(predicate)

        class LazyScopeAwareMatch(predicate: () -> Boolean) : LazyMatch(predicate)

        companion object {
            fun of(value: Boolean) = if (value) ExactMatch else NotMatch

            fun ofFallback(value: Boolean) = if (value) FallbackMatch else NotMatch
        }
    }

    data class ResultValue<out T>(val value: T, val result: Result)

    //rootFile -> cacheKey -> configMatchResult
    //depends on config group and indices
    private val CwtConfigGroup.configMatchResultCache by createKey(CwtConfigContext.Keys) {
        createCachedValue(project) {
            createNestedCache<VirtualFile, _, _, _> {
                CacheBuilder.newBuilder().buildCache<String, Result>()
            }.withDependencyItems(
                ParadoxModificationTrackers.FileTracker
            )
        }
    }

    //兼容scriptedVariableReference inlineMath parameter

    fun getCachedMatchResult(element: PsiElement, cacheKey: String, predicate: () -> Boolean): Result {
        // indexing -> should not visit indices -> treat as exact match
        if (PlsCoreManager.processMergedIndex.get() == true) return Result.ExactMatch

        ProgressManager.checkCanceled()
        val psiFile = element.containingFile ?: return Result.NotMatch
        val project = psiFile.project
        val rootFile = selectRootFile(psiFile) ?: return Result.NotMatch
        val configGroup = PlsFacade.getConfigGroup(project, selectGameType(rootFile))
        val cache = configGroup.configMatchResultCache.value.get(rootFile)
        return cache.get(cacheKey) { Result.LazyIndexAwareMatch(predicate) }
    }

    fun getLocalisationMatchResult(element: PsiElement, name: String, project: Project): Result {
        val cacheKey = "l#$name"
        return getCachedMatchResult(element, cacheKey) {
            val selector = selector(project, element).localisation()
            ParadoxLocalisationSearch.search(name, selector).findFirst() != null
        }
    }

    fun getSyncedLocalisationMatchResult(element: PsiElement, name: String, project: Project): Result {
        val cacheKey = "ls#$name"
        return getCachedMatchResult(element, cacheKey) {
            val selector = selector(project, element).localisation()
            ParadoxSyncedLocalisationSearch.search(name, selector).findFirst() != null
        }
    }

    fun getDefinitionMatchResult(element: PsiElement, name: String, configExpression: CwtDataExpression, project: Project): Result {
        val typeExpression = configExpression.value ?: return Result.NotMatch //invalid cwt config
        val cacheKey = "d#${typeExpression}#${name}"
        return getCachedMatchResult(element, cacheKey) {
            val selector = selector(project, element).definition()
            ParadoxDefinitionSearch.search(name, typeExpression, selector).findFirst() != null
        }
    }

    fun getPathReferenceMatchResult(element: PsiElement, expression: String, configExpression: CwtDataExpression, project: Project): Result {
        val pathReference = expression.normalizePath()
        val cacheKey = "p#${pathReference}#${configExpression}"
        return getCachedMatchResult(element, cacheKey) {
            val selector = selector(project, element).file()
            ParadoxFilePathSearch.search(pathReference, configExpression, selector).findFirst() != null
        }
    }

    fun getComplexEnumValueMatchResult(element: PsiElement, name: String, enumName: String, complexEnumConfig: CwtComplexEnumConfig, project: Project): Result {
        val searchScope = complexEnumConfig.searchScopeType
        if (searchScope == null) {
            val cacheKey = "ce#${enumName}#${name}"
            return getCachedMatchResult(element, cacheKey) {
                val selector = selector(project, element).complexEnumValue()
                ParadoxComplexEnumValueSearch.search(name, enumName, selector).findFirst() != null
            }
        }
        return Result.LazyIndexAwareMatch {
            val selector = selector(project, element).complexEnumValue().withSearchScopeType(searchScope)
            ParadoxComplexEnumValueSearch.search(name, enumName, selector).findFirst() != null
        }
    }

    fun getScopeFieldMatchResult(element: PsiElement, scopeFieldExpression: ParadoxScopeFieldExpression, configExpression: CwtDataExpression, configGroup: CwtConfigGroup): Result {
        when (configExpression.type) {
            CwtDataTypes.ScopeField -> return Result.ExactMatch
            CwtDataTypes.Scope -> {
                val expectedScope = configExpression.value ?: return Result.ExactMatch
                return Result.LazyScopeAwareMatch p@{
                    val scopeContext = ParadoxScopeManager.getSwitchedScopeContext(element, scopeFieldExpression, configExpression)
                    ParadoxScopeManager.matchesScope(scopeContext, expectedScope, configGroup)
                }
            }
            CwtDataTypes.ScopeGroup -> {
                val expectedScopeGroup = configExpression.value ?: return Result.ExactMatch
                return Result.LazyScopeAwareMatch p@{
                    val scopeContext = ParadoxScopeManager.getSwitchedScopeContext(element, scopeFieldExpression, configExpression)
                    ParadoxScopeManager.matchesScopeGroup(scopeContext, expectedScopeGroup, configGroup)
                }
            }
            else -> return Result.NotMatch
        }
    }

    fun getModifierMatchResult(element: PsiElement, name: String, configGroup: CwtConfigGroup): Result {
        val cacheKey = "m#${name}"
        return getCachedMatchResult(element, cacheKey) {
            ParadoxModifierManager.matchesModifier(name, element, configGroup)
        }
    }

    fun getTemplateMatchResult(element: PsiElement, expression: String, configExpression: CwtDataExpression, configGroup: CwtConfigGroup): Result {
        val template = configExpression.expressionString
        val cacheKey = "t#${template}#${expression}"
        return getCachedMatchResult(element, cacheKey) {
            CwtTemplateExpressionManager.matches(element, expression, CwtTemplateExpression.resolve(template), configGroup)
        }
    }
}
