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
import icu.windea.pls.config.configExpression.floatRange
import icu.windea.pls.config.configExpression.intRange
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
import icu.windea.pls.lang.PlsStates
import icu.windea.pls.lang.resolve.complexExpression.ParadoxScopeFieldExpression
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.selectRootFile
import icu.windea.pls.lang.util.ParadoxScopeManager

private typealias KeyForCache = KeyWithFactory<CachedValue<MatchResultNestedCache>, CwtConfigGroup>
private typealias MatchResultNestedCache = NestedCache<VirtualFile, String, ParadoxMatchResult, MatchResultCache>
private typealias MatchResultCache = CancelableCache<String, ParadoxMatchResult>

object ParadoxMatchResultProvider {
    object Keys : KeyRegistry() {
        val cacheForDefinitions by createKeyForCache(ParadoxModificationTrackers.ScriptFile)
        val cacheForLocalisations by createKeyForCache(ParadoxModificationTrackers.LocalisationFile)
        val cacheForSyncedLocalisations by createKeyForCache(ParadoxModificationTrackers.LocalisationFile)
        val cacheForPathReferences by createKeyForCache(ParadoxModificationTrackers.FilePath)
        val cacheForComplexEnumValues by createKeyForCache(ParadoxModificationTrackers.ScriptFile)
        val cacheForModifiers by createKeyForCache(ParadoxModificationTrackers.ScriptFile)
        val cacheForTemplates by createKeyForCache(ParadoxModificationTrackers.ScriptFile, ParadoxModificationTrackers.LocalisationFile)

        private fun createKeyForCache(vararg dependencyItems: Any) = createKey<CachedValue<MatchResultNestedCache>, CwtConfigGroup>(Keys) {
            // rootFile -> cacheKey -> configMatchResult
            createCachedValue(project) {
                createNestedCache<VirtualFile, _, _, _> {
                    CacheBuilder().build<String, ParadoxMatchResult>().cancelable()
                }.withDependencyItems(*dependencyItems)
            }
        }
    }

    fun forRangedInt(value: String, configExpression: CwtDataExpression): ParadoxMatchResult? {
        val intRange = configExpression.intRange ?: return null
        val intValue = value.toIntOrNull() ?: return null
        return ParadoxMatchResult.LazySimpleMatch { intValue in intRange }
    }

    fun forRangedFloat(value: String, configExpression: CwtDataExpression): ParadoxMatchResult? {
        val floatRange = configExpression.floatRange ?: return null
        val floatValue = value.toFloatOrNull() ?: return null
        return ParadoxMatchResult.LazySimpleMatch { floatValue in floatRange }
    }

    fun getCached(element: PsiElement, key: KeyForCache, cacheKey: String, predicate: () -> Boolean): ParadoxMatchResult {
        // indexing -> should not visit indices -> treat as exact match
        if (PlsStates.processMergedIndex.get() == true) return ParadoxMatchResult.ExactMatch

        ProgressManager.checkCanceled()
        val rootFile = selectRootFile(element) ?: return ParadoxMatchResult.NotMatch
        val configGroup = PlsFacade.getConfigGroup(element.project, selectGameType(rootFile))
        val cache = configGroup.getUserDataOrDefault(key).value.get(rootFile)
        return cache.get(cacheKey) { ParadoxMatchResult.LazyIndexAwareMatch(predicate) }
    }

    fun forDefinition(element: PsiElement, project: Project, expression: String, configExpression: CwtDataExpression): ParadoxMatchResult {
        val typeExpression = configExpression.value ?: return ParadoxMatchResult.NotMatch // invalid cwt config
        val suffixes = configExpression.suffixes.orEmpty()
        val key = Keys.cacheForDefinitions
        val cacheKey = when {
            suffixes.isEmpty() -> "${typeExpression}#${expression}"
            else -> "${suffixes.joinToString(",")}#${typeExpression}#${expression}"
        }
        return getCached(element, key, cacheKey) {
            when {
                suffixes.isEmpty() -> ParadoxMatchProvider.matchesDefinition(element, project, expression, typeExpression)
                else -> suffixes.any { ParadoxMatchProvider.matchesDefinition(element, project, expression + it, typeExpression) }
            }
        }
    }

    fun forLocalisation(element: PsiElement, project: Project, expression: String, configExpression: CwtDataExpression): ParadoxMatchResult {
        val suffixes = configExpression.suffixes.orEmpty()
        val key = Keys.cacheForLocalisations
        val cacheKey = when {
            suffixes.isEmpty() -> expression
            else -> "${suffixes.joinToString(",")}#${expression}"
        }
        return getCached(element, key, cacheKey) {
            when {
                suffixes.isEmpty() -> ParadoxMatchProvider.matchesLocalisation(element, project, expression)
                else -> suffixes.any { ParadoxMatchProvider.matchesLocalisation(element, project, expression + it) }
            }
        }
    }

    fun forSyncedLocalisation(element: PsiElement, project: Project, expression: String, configExpression: CwtDataExpression): ParadoxMatchResult {
        val suffixes = configExpression.suffixes.orEmpty()
        val key = Keys.cacheForSyncedLocalisations
        val cacheKey = when {
            suffixes.isEmpty() -> expression
            else -> "${suffixes.joinToString(",")}#${expression}"
        }
        return getCached(element, key, cacheKey) {
            when {
                suffixes.isEmpty() -> ParadoxMatchProvider.matchesSyncedLocalisation(element, project, expression)
                else -> suffixes.any { ParadoxMatchProvider.matchesSyncedLocalisation(element, project, expression + it) }
            }
        }
    }

    fun forPathReference(element: PsiElement, project: Project, expression: String, configExpression: CwtDataExpression): ParadoxMatchResult {
        val pathReference = expression.normalizePath()
        val key = Keys.cacheForPathReferences
        val cacheKey = "${pathReference}#${configExpression}"
        return getCached(element, key, cacheKey) {
            ParadoxMatchProvider.matchesPathReference(element, project, pathReference, configExpression)
        }
    }

    fun forComplexEnumValue(element: PsiElement, project: Project, name: String, enumName: String, complexEnumConfig: CwtComplexEnumConfig): ParadoxMatchResult {
        val searchScopeType = complexEnumConfig.searchScopeType
        if (searchScopeType != null) {
            return ParadoxMatchResult.LazyIndexAwareMatch {
                ParadoxMatchProvider.matchesComplexEnumValue(element, project, name, enumName, searchScopeType)
            }
        }
        val key = Keys.cacheForComplexEnumValues
        val cacheKey = "${enumName}#${name}"
        return getCached(element, key, cacheKey) {
            ParadoxMatchProvider.matchesComplexEnumValue(element, project, name, enumName)
        }
    }

    fun forModifier(element: PsiElement, configGroup: CwtConfigGroup, name: String): ParadoxMatchResult {
        val key = Keys.cacheForModifiers
        val cacheKey = name
        return getCached(element, key, cacheKey) {
            ParadoxMatchProvider.matchesModifier(element, configGroup, name)
        }
    }

    fun forTemplate(element: PsiElement, configGroup: CwtConfigGroup, expression: String, configExpression: CwtDataExpression): ParadoxMatchResult {
        val template = configExpression.expressionString
        val key = Keys.cacheForTemplates
        val cacheKey = "${template}#${expression}"
        return getCached(element, key, cacheKey) {
            ParadoxMatchProvider.matchesTemplate(element, configGroup, expression, template)
        }
    }

    fun forScopeField(element: PsiElement, configGroup: CwtConfigGroup, scopeFieldExpression: ParadoxScopeFieldExpression, configExpression: CwtDataExpression): ParadoxMatchResult {
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
}
