package icu.windea.pls.lang.match

import com.github.benmanes.caffeine.cache.Cache
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.delegated.CwtComplexEnumConfig
import icu.windea.pls.config.configContext.CwtConfigContext
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configExpression.CwtTemplateExpression
import icu.windea.pls.config.configExpression.suffixes
import icu.windea.pls.config.configExpression.value
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.createCachedValue
import icu.windea.pls.core.normalizePath
import icu.windea.pls.core.util.CacheBuilder
import icu.windea.pls.core.util.cancelable
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.createNestedCache
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.withDependencyItems
import icu.windea.pls.lang.ParadoxModificationTrackers
import icu.windea.pls.lang.resolve.complexExpression.ParadoxScopeFieldExpression
import icu.windea.pls.lang.search.ParadoxComplexEnumValueSearch
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.ParadoxFilePathSearch
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.ParadoxSyncedLocalisationSearch
import icu.windea.pls.lang.search.selector.complexEnumValue
import icu.windea.pls.lang.search.selector.definition
import icu.windea.pls.lang.search.selector.file
import icu.windea.pls.lang.search.selector.localisation
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.search.selector.withSearchScopeType
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.selectRootFile
import icu.windea.pls.lang.util.CwtTemplateExpressionManager
import icu.windea.pls.lang.util.ParadoxModifierManager
import icu.windea.pls.lang.util.ParadoxScopeManager
import icu.windea.pls.lang.util.PlsCoreManager

object ParadoxMatchResultProvider {
    // rootFile -> cacheKey -> configMatchResult
    // depends on config group and indices
    private val CwtConfigGroup.configMatchResultCache by createKey(CwtConfigContext.Keys) {
        createCachedValue(project) {
            createNestedCache<VirtualFile, String, ParadoxMatchResult, Cache<String, ParadoxMatchResult>> {
                CacheBuilder().build<String, ParadoxMatchResult>().cancelable()
            }.withDependencyItems(ParadoxModificationTrackers.FileTracker)
        }
    }

    // 兼容 scriptedVariableReference inlineMath parameter

    fun getCachedMatchResult(element: PsiElement, cacheKey: String, predicate: () -> Boolean): ParadoxMatchResult {
        // indexing -> should not visit indices -> treat as exact match
        if (PlsCoreManager.processMergedIndex.get() == true) return ParadoxMatchResult.ExactMatch

        ProgressManager.checkCanceled()
        val psiFile = element.containingFile ?: return ParadoxMatchResult.NotMatch
        val project = psiFile.project
        val rootFile = selectRootFile(psiFile) ?: return ParadoxMatchResult.NotMatch
        val configGroup = PlsFacade.getConfigGroup(project, selectGameType(rootFile))
        val cache = configGroup.configMatchResultCache.value.get(rootFile)
        return cache.get(cacheKey) { ParadoxMatchResult.LazyIndexAwareMatch(predicate) }
    }

    fun getDefinitionMatchResult(element: PsiElement, project: Project, name: String, configExpression: CwtDataExpression): ParadoxMatchResult {
        val typeExpression = configExpression.value ?: return ParadoxMatchResult.NotMatch // invalid cwt config
        val suffixes = configExpression.suffixes.orEmpty()
        val cacheKey = "d#${typeExpression}#${name}".let { if (suffixes.isEmpty()) it else "s#${suffixes.joinToString(",")}$it" }
        return getCachedMatchResult(element, cacheKey) {
            val fullNames = if (suffixes.isEmpty()) listOf(name) else suffixes.map { name + it }
            fullNames.any { fullName ->
                val selector = selector(project, element).definition()
                ParadoxDefinitionSearch.search(fullName, typeExpression, selector).findFirst() != null
            }
        }
    }

    fun getLocalisationMatchResult(element: PsiElement, project: Project, name: String, configExpression: CwtDataExpression): ParadoxMatchResult {
        val suffixes = configExpression.suffixes.orEmpty()
        val cacheKey = "l#$name".let { if (suffixes.isEmpty()) it else "s#${suffixes.joinToString(",")}$it" }
        return getCachedMatchResult(element, cacheKey) {
            val fullNames = if (suffixes.isEmpty()) listOf(name) else suffixes.map { name + it }
            fullNames.any { fullName ->
                val selector = selector(project, element).localisation()
                ParadoxLocalisationSearch.search(fullName, selector).findFirst() != null
            }
        }
    }

    fun getSyncedLocalisationMatchResult(element: PsiElement, project: Project, name: String, configExpression: CwtDataExpression): ParadoxMatchResult {
        val suffixes = configExpression.suffixes.orEmpty()
        val cacheKey = "ls#$name".let { if (suffixes.isEmpty()) it else "s#${suffixes.joinToString(",")}$it" }
        return getCachedMatchResult(element, cacheKey) {
            val fullNames = if (suffixes.isEmpty()) listOf(name) else suffixes.map { name + it }
            fullNames.any { fullName ->
                val selector = selector(project, element).localisation()
                ParadoxSyncedLocalisationSearch.search(fullName, selector).findFirst() != null
            }
        }
    }

    fun getPathReferenceMatchResult(element: PsiElement, project: Project, expression: String, configExpression: CwtDataExpression): ParadoxMatchResult {
        val pathReference = expression.normalizePath()
        val cacheKey = "p#${pathReference}#${configExpression}"
        return getCachedMatchResult(element, cacheKey) {
            val selector = selector(project, element).file()
            ParadoxFilePathSearch.search(pathReference, configExpression, selector).findFirst() != null
        }
    }

    fun getComplexEnumValueMatchResult(element: PsiElement, project: Project, name: String, enumName: String, complexEnumConfig: CwtComplexEnumConfig): ParadoxMatchResult {
        val searchScope = complexEnumConfig.searchScopeType
        if (searchScope == null) {
            val cacheKey = "ce#${enumName}#${name}"
            return getCachedMatchResult(element, cacheKey) {
                val selector = selector(project, element).complexEnumValue()
                ParadoxComplexEnumValueSearch.search(name, enumName, selector).findFirst() != null
            }
        }
        return ParadoxMatchResult.LazyIndexAwareMatch {
            val selector = selector(project, element).complexEnumValue().withSearchScopeType(searchScope)
            ParadoxComplexEnumValueSearch.search(name, enumName, selector).findFirst() != null
        }
    }

    fun getScopeFieldMatchResult(element: PsiElement, scopeFieldExpression: ParadoxScopeFieldExpression, configExpression: CwtDataExpression, configGroup: CwtConfigGroup): ParadoxMatchResult {
        when (configExpression.type) {
            CwtDataTypes.ScopeField -> return ParadoxMatchResult.ExactMatch
            CwtDataTypes.Scope -> {
                val expectedScope = configExpression.value ?: return ParadoxMatchResult.ExactMatch
                return ParadoxMatchResult.LazyScopeAwareMatch p@{
                    val scopeContext = ParadoxScopeManager.getSwitchedScopeContext(element, scopeFieldExpression, configExpression)
                    ParadoxScopeManager.matchesScope(scopeContext, expectedScope, configGroup)
                }
            }
            CwtDataTypes.ScopeGroup -> {
                val expectedScopeGroup = configExpression.value ?: return ParadoxMatchResult.ExactMatch
                return ParadoxMatchResult.LazyScopeAwareMatch p@{
                    val scopeContext = ParadoxScopeManager.getSwitchedScopeContext(element, scopeFieldExpression, configExpression)
                    ParadoxScopeManager.matchesScopeGroup(scopeContext, expectedScopeGroup, configGroup)
                }
            }
            else -> return ParadoxMatchResult.NotMatch
        }
    }

    fun getModifierMatchResult(element: PsiElement, name: String, configGroup: CwtConfigGroup): ParadoxMatchResult {
        val cacheKey = "m#${name}"
        return getCachedMatchResult(element, cacheKey) {
            ParadoxModifierManager.matchesModifier(name, element, configGroup)
        }
    }

    fun getTemplateMatchResult(element: PsiElement, expression: String, configExpression: CwtDataExpression, configGroup: CwtConfigGroup): ParadoxMatchResult {
        val template = configExpression.expressionString
        val cacheKey = "t#${template}#${expression}"
        return getCachedMatchResult(element, cacheKey) {
            CwtTemplateExpressionManager.matches(element, expression, CwtTemplateExpression.resolve(template), configGroup)
        }
    }
}
