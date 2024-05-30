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
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.core.util.*
import icu.windea.pls.ep.expression.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.CwtConfigMatcher.Result
import icu.windea.pls.model.expression.*
import icu.windea.pls.model.expression.complex.*
import icu.windea.pls.script.psi.*

object CwtConfigMatcher {
    object Options {
        /** 默认的匹配方式，先尝试通过[Result.ExactMatch]进行匹配，然后再尝试通过其他匹配方式进行匹配。 */
        const val Default = 0x00
        /** 对于[Result.LazySimpleMatch]和[Result.LazyBlockAwareMatch]，匹配结果直接返回true。 */
        const val Relax = 0x01
        /** 对于[Result.LazyIndexAwareMatch]，匹配结果直接返回true。 */
        const val SkipIndex = 0x02
        /** 对于[Result.LazyScopeAwareMatch]，匹配结果直接返回true。 */
        const val SkipScope = 0x04
        
        /** 对于最终匹配得到的那个结果，不需要再次判断是否精确匹配。 */
        const val Fast = 0x08
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
        
        data object ParameterizedMatch : Result() {
            override fun get(options: Int) = true
        }
        
        sealed class LazyMatch(predicate: () -> Boolean) : Result() {
            //use manual lazy implementation instead of kotlin Lazy to optimize memory
            @Volatile private var value: Any = predicate
            
            override fun get(options: Int): Boolean {
                if(skip(options)) return true
                if(value is Boolean) return value as Boolean
                val r = doGetCatching()
                value = r
                return r
            }
            
            private fun skip(options: Int): Boolean {
                return when {
                    this is LazySimpleMatch -> BitUtil.isSet(options, Options.Relax)
                    this is LazyBlockAwareMatch -> BitUtil.isSet(options, Options.Relax)
                    this is LazyIndexAwareMatch -> BitUtil.isSet(options, Options.SkipIndex) || PlsStatus.indexing.get() == true
                    this is LazyScopeAwareMatch -> BitUtil.isSet(options, Options.SkipScope) || PlsStatus.indexing.get() == true
                    else -> false
                }
            }
            
            private fun doGetCatching(): Boolean {
                //it's necessary to suppress outputting error logs and throwing exceptions here
                
                //java.lang.Throwable: Indexing process should not rely on non-indexed file data.
                //java.lang.AssertionError: Reentrant indexing
                //com.intellij.openapi.project.IndexNotReadyException
                
                return disableLogger {
                    runCatchingCancelable {
                        @Suppress("UNCHECKED_CAST")
                        (value as () -> Boolean)()
                    }.getOrDefault(true)
                }
            }
        }
        
        class LazySimpleMatch(predicate: () -> Boolean) : LazyMatch(predicate)
        
        class LazyBlockAwareMatch(predicate: () -> Boolean) : LazyMatch(predicate)
        
        class LazyIndexAwareMatch(predicate: () -> Boolean) : LazyMatch(predicate)
        
        class LazyScopeAwareMatch(predicate: () -> Boolean) : LazyMatch(predicate)
        
        companion object {
            fun of(value: Boolean) = if(value) ExactMatch else NotMatch
            
            fun ofFallback(value: Boolean) = if(value) FallbackMatch else NotMatch
        }
    }
    
    data class ResultValue<out T>(val value: T, val result: Result)
    
    //兼容scriptedVariableReference inlineMath parameter
    
    fun matches(
        element: PsiElement,
        expression: ParadoxDataExpression,
        configExpression: CwtDataExpression,
        config: CwtConfig<*>?,
        configGroup: CwtConfigGroup,
        options: Int = Options.Default
    ): Result {
        return CwtDataExpressionMatcher.matches(element, expression, configExpression, config, configGroup, options)
    }
    
    private fun matchesScriptExpressionInBlock(element: PsiElement, config: CwtMemberConfig<*>): Boolean {
        val block = when {
            element is ParadoxScriptProperty -> element.propertyValue()
            element is ParadoxScriptBlock -> element
            else -> null
        } ?: return false
        //简单判断：如果block中包含configsInBlock声明的必须的任意propertyKey（作为常量字符串，忽略大小写），则认为匹配
        //注意：不同的子句规则可以拥有部分相同的propertyKey
        val keys = CwtConfigHandler.getInBlockKeys(config)
        if(keys.isEmpty()) return true
        val actualKeys = mutableSetOf<String>()
        //注意这里需要考虑内联和可选的情况
        block.processData(conditional = true, inline = true) {
            if(it is ParadoxScriptProperty) actualKeys.add(it.name)
            true
        }
        return actualKeys.any { it in keys }
    }
    
    object Impls {
        fun getCachedMatchResult(element: PsiElement, cacheKey: String, predicate: () -> Boolean): Result {
            ProgressManager.checkCanceled()
            if(PlsStatus.indexing.get() == true) return Result.ExactMatch // indexing -> should not visit indices -> treat as exact match
            val psiFile = element.containingFile ?: return Result.NotMatch
            val project = psiFile.project
            val rootFile = selectRootFile(psiFile) ?: return Result.NotMatch
            val configGroup = getConfigGroup(project, selectGameType(rootFile))
            val cache = configGroup.configMatchResultCache.value.get(rootFile)
            return cache.get(cacheKey) { Result.LazyIndexAwareMatch(predicate) }
        }
        
        fun getLocalisationMatchResult(element: PsiElement, expression: ParadoxDataExpression, project: Project): Result {
            val name = expression.value
            val cacheKey = "l#$name"
            return getCachedMatchResult(element, cacheKey) {
                val selector = localisationSelector(project, element)
                ParadoxLocalisationSearch.search(name, selector).findFirst() != null
            }
        }
        
        fun getSyncedLocalisationMatchResult(element: PsiElement, expression: ParadoxDataExpression, project: Project): Result {
            val name = expression.value
            val cacheKey = "ls#$name"
            return getCachedMatchResult(element, cacheKey) {
                val selector = localisationSelector(project, element)
                ParadoxSyncedLocalisationSearch.search(name, selector).findFirst() != null
            }
        }
        
        fun getDefinitionMatchResult(element: PsiElement, expression: ParadoxDataExpression, configExpression: CwtDataExpression, project: Project): Result {
            val name = expression.value
            val typeExpression = configExpression.value ?: return Result.NotMatch //invalid cwt config
            val cacheKey = "d#${typeExpression}#${name}"
            return getCachedMatchResult(element, cacheKey) {
                val selector = definitionSelector(project, element)
                ParadoxDefinitionSearch.search(name, typeExpression, selector).findFirst() != null
            }
        }
        
        fun getPathReferenceMatchResult(element: PsiElement, expression: ParadoxDataExpression, configExpression: CwtDataExpression, project: Project): Result {
            val pathReference = expression.value.normalizePath()
            val cacheKey = "p#${pathReference}#${configExpression}"
            return getCachedMatchResult(element, cacheKey) {
                val selector = fileSelector(project, element)
                ParadoxFilePathSearch.search(pathReference, configExpression, selector).findFirst() != null
            }
        }
        
        fun getComplexEnumValueMatchResult(element: PsiElement, name: String, enumName: String, complexEnumConfig: CwtComplexEnumConfig, project: Project): Result {
            val searchScope = complexEnumConfig.searchScopeType
            if(searchScope == null) {
                val cacheKey = "ce#${enumName}#${name}"
                return getCachedMatchResult(element, cacheKey) {
                    val selector = complexEnumValueSelector(project, element)
                    ParadoxComplexEnumValueSearch.search(name, enumName, selector).findFirst() != null
                }
            }
            return Result.LazyIndexAwareMatch {
                val selector = complexEnumValueSelector(project, element).withSearchScopeType(searchScope)
                ParadoxComplexEnumValueSearch.search(name, enumName, selector).findFirst() != null
            }
        }
        
        @Suppress("UNUSED_PARAMETER")
        fun getDynamicValueMatchResult(element: PsiElement, name: String, dynamicValueType: String, project: Project): Result {
            //总是认为匹配
            return Result.FallbackMatch
        }
        
        fun getScopeFieldMatchResult(element: PsiElement, expression: ParadoxDataExpression, configExpression: CwtDataExpression, configGroup: CwtConfigGroup): Result {
            val textRange = TextRange.create(0, expression.value.length)
            val scopeFieldExpression = ParadoxScopeFieldExpression.resolve(expression.value, textRange, configGroup)
            if(scopeFieldExpression == null) return Result.NotMatch
            when(configExpression.type) {
                CwtDataTypes.ScopeField -> return Result.ExactMatch
                CwtDataTypes.Scope -> {
                    val expectedScope = configExpression.value ?: return Result.ExactMatch
                    return Result.LazyScopeAwareMatch p@{
                        val memberElement = element.parentOfType<ParadoxScriptMemberElement>(withSelf = false) ?: return@p true
                        val parentScopeContext = ParadoxScopeHandler.getScopeContext(memberElement) ?: return@p true
                        val scopeContext = ParadoxScopeHandler.getScopeContext(element, scopeFieldExpression, parentScopeContext)
                        if(ParadoxScopeHandler.matchesScope(scopeContext, expectedScope, configGroup)) return@p true
                        false
                    }
                }
                CwtDataTypes.ScopeGroup -> {
                    val expectedScopeGroup = configExpression.value ?: return Result.ExactMatch
                    return Result.LazyScopeAwareMatch p@{
                        val memberElement = element.parentOfType<ParadoxScriptMemberElement>(withSelf = false) ?: return@p true
                        val parentScopeContext = ParadoxScopeHandler.getScopeContext(memberElement) ?: return@p true
                        val scopeContext = ParadoxScopeHandler.getScopeContext(element, scopeFieldExpression, parentScopeContext)
                        if(ParadoxScopeHandler.matchesScopeGroup(scopeContext, expectedScopeGroup, configGroup)) return@p true
                        false
                    }
                }
                else -> return Result.NotMatch
            }
        }
        
        fun getModifierMatchResult(element: PsiElement, expression: ParadoxDataExpression, configGroup: CwtConfigGroup): Result {
            val name = expression.value
            val cacheKey = "m#${name}"
            return getCachedMatchResult(element, cacheKey) {
                ParadoxModifierHandler.matchesModifier(name, element, configGroup)
            }
        }
        
        fun getTemplateMatchResult(element: PsiElement, expression: ParadoxDataExpression, configExpression: CwtDataExpression, configGroup: CwtConfigGroup): Result {
            val exp = expression.value
            val template = configExpression.expressionString
            val cacheKey = "t#${template}#${exp}"
            return getCachedMatchResult(element, cacheKey) {
                CwtTemplateExpression.resolve(template).matches(exp, element, configGroup)
            }
        }
    }
}

//rootFile -> cacheKey -> configMatchResult
//depends on config group and indices
private val CwtConfigGroup.configMatchResultCache by createKeyDelegate(CwtConfigContext.Keys) {
    createCachedValue(project) {
        val trackerProvider = ParadoxModificationTrackers
        createNestedCache<VirtualFile, _, _, _> {
            CacheBuilder.newBuilder().buildCache<String, Result>()
        }.withDependencyItems(
            trackerProvider.ScriptFileTracker,
            trackerProvider.LocalisationFileTracker,
        )
    }
}
