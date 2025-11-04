package icu.windea.pls.lang.match

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValue
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.delegated.CwtComplexEnumConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configExpression.CwtTemplateExpression
import icu.windea.pls.config.configExpression.suffixes
import icu.windea.pls.config.configExpression.value
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.createCachedValue
import icu.windea.pls.core.normalizePath
import icu.windea.pls.core.util.CacheBuilder
import icu.windea.pls.core.util.CancelableCache
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.KeyWithFactory
import icu.windea.pls.core.util.NestedCache
import icu.windea.pls.core.util.cancelable
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.createNestedCache
import icu.windea.pls.core.util.getUserDataOrDefault
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

private typealias KeyForCache = KeyWithFactory<CachedValue<MatchResultNestedCache>, CwtConfigGroup>
private typealias MatchResultNestedCache = NestedCache<VirtualFile, String, ParadoxMatchResult, MatchResultCache>
private typealias MatchResultCache = CancelableCache<String, ParadoxMatchResult>

object ParadoxMatchResultProvider {
    object Keys : KeyRegistry() {
        val cacheForDefinitions by createKeyForCache(ParadoxModificationTrackers.ScriptFileTracker)
        val cacheForLocalisations by createKeyForCache(ParadoxModificationTrackers.LocalisationFileTracker)
        val cacheForSyncedLocalisations by createKeyForCache(ParadoxModificationTrackers.LocalisationFileTracker)
        val cacheForPathReferences by createKeyForCache(ParadoxModificationTrackers.FileTracker)
        val cacheForComplexEnumValues by createKeyForCache(ParadoxModificationTrackers.ScriptFileTracker)
        val cacheForModifiers by createKeyForCache(ParadoxModificationTrackers.ScriptFileTracker)
        val cacheForTemplates by createKeyForCache(ParadoxModificationTrackers.ScriptFileTracker, ParadoxModificationTrackers.LocalisationFileTracker)

        // rootFile -> cacheKey -> configMatchResult
        // depends on config group and indices

        private fun createKeyForCache(vararg dependencyItems: Any) = createKey<CachedValue<MatchResultNestedCache>, CwtConfigGroup>(Keys) {
            createCachedValue(project) {
                createNestedCache<VirtualFile, _, _, _> {
                    CacheBuilder().build<String, ParadoxMatchResult>().cancelable()
                }.withDependencyItems(*dependencyItems)
            }
        }
    }

    fun getCachedMatchResult(element: PsiElement, key: KeyForCache, cacheKey: String, predicate: () -> Boolean): ParadoxMatchResult {
        // indexing -> should not visit indices -> treat as exact match
        if (PlsCoreManager.processMergedIndex.get() == true) return ParadoxMatchResult.ExactMatch

        ProgressManager.checkCanceled()
        val rootFile = selectRootFile(element) ?: return ParadoxMatchResult.NotMatch
        val configGroup = PlsFacade.getConfigGroup(element.project, selectGameType(rootFile))
        val cache = configGroup.getUserDataOrDefault(key).value.get(rootFile)
        return cache.get(cacheKey) { ParadoxMatchResult.LazyIndexAwareMatch(predicate) }
    }

    fun getDefinitionMatchResult(element: PsiElement, project: Project, expression: String, configExpression: CwtDataExpression): ParadoxMatchResult {
        val typeExpression = configExpression.value ?: return ParadoxMatchResult.NotMatch // invalid cwt config
        val suffixes = configExpression.suffixes.orEmpty()
        val key = Keys.cacheForDefinitions
        val cacheKey = when {
            suffixes.isEmpty() -> "${typeExpression}#${expression}"
            else -> "${suffixes.joinToString(",")}#${typeExpression}#${expression}"
        }
        return getCachedMatchResult(element, key, cacheKey) {
            when {
                suffixes.isEmpty() -> matchesDefinition(element, project, expression, typeExpression)
                else -> suffixes.any { matchesDefinition(element, project, expression + it, typeExpression) }
            }
        }
    }

    private fun matchesDefinition(element: PsiElement, project: Project, name: String, typeExpression: String): Boolean {
        val selector = selector(project, element).definition()
        return ParadoxDefinitionSearch.search(name, typeExpression, selector).findFirst() != null
    }

    fun getLocalisationMatchResult(element: PsiElement, project: Project, expression: String, configExpression: CwtDataExpression): ParadoxMatchResult {
        val suffixes = configExpression.suffixes.orEmpty()
        val key = Keys.cacheForLocalisations
        val cacheKey = when {
            suffixes.isEmpty() -> expression
            else -> "${suffixes.joinToString(",")}#${expression}"
        }
        return getCachedMatchResult(element, key, cacheKey) {
            when {
                suffixes.isEmpty() -> matchesLocalisation(element, project, expression)
                else -> suffixes.any { matchesLocalisation(element, project, expression + it) }
            }
        }
    }

    private fun matchesLocalisation(element: PsiElement, project: Project, fullName: String): Boolean {
        val selector = selector(project, element).localisation()
        return ParadoxLocalisationSearch.search(fullName, selector).findFirst() != null
    }

    fun getSyncedLocalisationMatchResult(element: PsiElement, project: Project, expression: String, configExpression: CwtDataExpression): ParadoxMatchResult {
        val suffixes = configExpression.suffixes.orEmpty()
        val key = Keys.cacheForSyncedLocalisations
        val cacheKey = when {
            suffixes.isEmpty() -> expression
            else -> "${suffixes.joinToString(",")}#${expression}"
        }
        return getCachedMatchResult(element, key, cacheKey) {
            when {
                suffixes.isEmpty() -> matchesSyncedLocalisation(element, project, expression)
                else -> suffixes.any { matchesSyncedLocalisation(element, project, expression + it) }
            }
        }
    }

    private fun matchesSyncedLocalisation(element: PsiElement, project: Project, name: String): Boolean {
        val selector = selector(project, element).localisation()
        return ParadoxSyncedLocalisationSearch.search(name, selector).findFirst() != null
    }

    fun getPathReferenceMatchResult(element: PsiElement, project: Project, expression: String, configExpression: CwtDataExpression): ParadoxMatchResult {
        val pathReference = expression.normalizePath()
        val key = Keys.cacheForPathReferences
        val cacheKey = "${pathReference}#${configExpression}"
        return getCachedMatchResult(element, key, cacheKey) {
            matchesPathReference(element, project, pathReference, configExpression)
        }
    }

    private fun matchesPathReference(element: PsiElement, project: Project, pathReference: String, configExpression: CwtDataExpression): Boolean {
        val selector = selector(project, element).file()
        return ParadoxFilePathSearch.search(pathReference, configExpression, selector).findFirst() != null
    }

    fun getComplexEnumValueMatchResult(element: PsiElement, project: Project, name: String, enumName: String, complexEnumConfig: CwtComplexEnumConfig): ParadoxMatchResult {
        val searchScopeType = complexEnumConfig.searchScopeType
        if (searchScopeType != null) {
            return ParadoxMatchResult.LazyIndexAwareMatch {
                matchesComplexEnumValue(element, project, name, enumName, searchScopeType)
            }
        }
        val key = Keys.cacheForComplexEnumValues
        val cacheKey = "${enumName}#${name}"
        return getCachedMatchResult(element, key, cacheKey) {
            matchesComplexEnumValue(element, project, name, enumName)
        }
    }

    private fun matchesComplexEnumValue(element: PsiElement, project: Project, name: String, enumName: String, searchScopeType: String? = null): Boolean {
        val selector = selector(project, element).complexEnumValue().withSearchScopeType(searchScopeType)
        return ParadoxComplexEnumValueSearch.search(name, enumName, selector).findFirst() != null
    }

    fun getScopeFieldMatchResult(element: PsiElement, scopeFieldExpression: ParadoxScopeFieldExpression, configExpression: CwtDataExpression, configGroup: CwtConfigGroup): ParadoxMatchResult {
        return when (configExpression.type) {
            CwtDataTypes.ScopeField -> ParadoxMatchResult.ExactMatch
            CwtDataTypes.Scope -> {
                val expectedScope = configExpression.value ?: return ParadoxMatchResult.ExactMatch
                ParadoxMatchResult.LazyScopeAwareMatch {
                    val scopeContext = ParadoxScopeManager.getSwitchedScopeContext(element, scopeFieldExpression, configExpression)
                    ParadoxScopeManager.matchesScope(scopeContext, expectedScope, configGroup)
                }
            }
            CwtDataTypes.ScopeGroup -> {
                val expectedScopeGroup = configExpression.value ?: return ParadoxMatchResult.ExactMatch
                ParadoxMatchResult.LazyScopeAwareMatch {
                    val scopeContext = ParadoxScopeManager.getSwitchedScopeContext(element, scopeFieldExpression, configExpression)
                    ParadoxScopeManager.matchesScopeGroup(scopeContext, expectedScopeGroup, configGroup)
                }
            }
            else -> ParadoxMatchResult.NotMatch
        }
    }

    fun getModifierMatchResult(element: PsiElement, name: String, configGroup: CwtConfigGroup): ParadoxMatchResult {
        val key = Keys.cacheForModifiers
        val cacheKey = name
        return getCachedMatchResult(element, key, cacheKey) {
            ParadoxModifierManager.matchesModifier(name, element, configGroup)
        }
    }

    fun getTemplateMatchResult(element: PsiElement, expression: String, configExpression: CwtDataExpression, configGroup: CwtConfigGroup): ParadoxMatchResult {
        val template = configExpression.expressionString
        val key = Keys.cacheForTemplates
        val cacheKey = "${template}#${expression}"
        return getCachedMatchResult(element, key, cacheKey) {
            CwtTemplateExpressionManager.matches(element, expression, CwtTemplateExpression.resolve(template), configGroup)
        }
    }

}
