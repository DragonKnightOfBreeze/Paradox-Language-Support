package icu.windea.pls.lang.match

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValue
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.delegated.CwtComplexEnumConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configExpression.floatRange
import icu.windea.pls.config.configExpression.intRange
import icu.windea.pls.config.configExpression.suffixes
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.cache.CacheBuilder
import icu.windea.pls.core.cache.CancelableCache
import icu.windea.pls.core.cache.NestedCache
import icu.windea.pls.core.cache.cancelable
import icu.windea.pls.core.cache.createNestedCache
import icu.windea.pls.core.collections.options
import icu.windea.pls.core.createCachedValue
import icu.windea.pls.core.normalizePath
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.KeyWithFactory
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getUserDataOrDefault
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.withDependencyItems
import icu.windea.pls.ep.match.ParadoxScriptExpressionMatcher.*
import icu.windea.pls.lang.ParadoxModificationTrackers
import icu.windea.pls.lang.psi.conditional
import icu.windea.pls.lang.psi.inline
import icu.windea.pls.lang.resolve.complexExpression.ParadoxDatabaseObjectExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxDefineReferenceExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxScopeFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxValueFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxVariableFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.StellarisNameFormatExpression
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.selectRootFile
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.ParadoxScopeManager
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.lang.psi.members
import icu.windea.pls.script.psi.propertyValue

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

    fun forBlock(element: PsiElement, config: CwtMemberConfig<*>): ParadoxMatchResult {
        val blockElement = when (element) {
            is ParadoxScriptProperty -> element.propertyValue()
            is ParadoxScriptBlock -> element
            else -> null
        } ?: return ParadoxMatchResult.NotMatch

        // 如果存在子句规则内容为空，则仅当子句内容为空时才认为匹配
        if (config.configs.isNullOrEmpty()) {
            if (blockElement.members().none()) return ParadoxMatchResult.ExactMatch
            return ParadoxMatchResult.FallbackMatch
        }

        return ParadoxMatchResult.LazyBlockAwareMatch p@{
            val keys = ParadoxExpressionManager.getInBlockKeys(config)
            if (keys.isEmpty()) return@p true

            // 根据其中存在的属性键进行过滤（注意这里需要考虑内联和可选的情况）
            // 如果子句中包含对应的任意子句规则中的任意必须的属性键（忽略大小写），则认为匹配
            val actualKeys = mutableSetOf<String>()
            blockElement.members().options { conditional().inline() }.forEach {
                if (it is ParadoxScriptProperty) actualKeys.add(it.name)
            }
            actualKeys.any { it in keys }
        }
    }

    fun getCached(element: PsiElement, key: KeyForCache, cacheKey: String, predicate: () -> Boolean): ParadoxMatchResult {
        ProgressManager.checkCanceled()
        val rootFile = selectRootFile(element) ?: return ParadoxMatchResult.NotMatch
        val configGroup = PlsFacade.getConfigGroup(element.project, selectGameType(rootFile))
        val cache = configGroup.getUserDataOrDefault(key).value.get(rootFile)
        return cache.get(cacheKey) { ParadoxMatchResult.LazyIndexAwareMatch(predicate) }
    }

    fun forDefinition(element: PsiElement, project: Project, expression: String, configExpression: CwtDataExpression): ParadoxMatchResult {
        // indexing -> should not visit indices -> treat as exact match
        if (ParadoxMatchUtil.skipIndex()) return ParadoxMatchResult.ExactMatch

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
        // indexing -> should not visit indices -> treat as exact match
        if (ParadoxMatchUtil.skipIndex()) return ParadoxMatchResult.ExactMatch

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
        // indexing -> should not visit indices -> treat as exact match
        if (ParadoxMatchUtil.skipIndex()) return ParadoxMatchResult.ExactMatch

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
        // indexing -> should not visit indices -> treat as exact match
        if (ParadoxMatchUtil.skipIndex()) return ParadoxMatchResult.ExactMatch

        val pathReference = expression.normalizePath()
        val key = Keys.cacheForPathReferences
        val cacheKey = "${pathReference}#${configExpression}"
        return getCached(element, key, cacheKey) {
            ParadoxMatchProvider.matchesPathReference(element, project, pathReference, configExpression)
        }
    }

    fun forComplexEnumValue(element: PsiElement, project: Project, name: String, enumName: String, complexEnumConfig: CwtComplexEnumConfig): ParadoxMatchResult {
        // indexing -> should not visit indices -> treat as exact match
        if (ParadoxMatchUtil.skipIndex()) return ParadoxMatchResult.ExactMatch

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
        // indexing -> should not visit indices -> treat as exact match
        if (ParadoxMatchUtil.skipIndex()) return ParadoxMatchResult.ExactMatch

        val key = Keys.cacheForModifiers
        val cacheKey = name
        return getCached(element, key, cacheKey) {
            ParadoxMatchProvider.matchesModifier(element, configGroup, name)
        }
    }

    fun forTemplate(element: PsiElement, configGroup: CwtConfigGroup, expression: String, configExpression: CwtDataExpression): ParadoxMatchResult {
        // indexing -> should not visit indices -> treat as exact match
        if (ParadoxMatchUtil.skipIndex()) return ParadoxMatchResult.ExactMatch

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

    fun forScopeFieldExpression(configGroup: CwtConfigGroup, text: String, configExpression: CwtDataExpression, element: PsiElement): ParadoxMatchResult {
        val complexExpression = ParadoxScopeFieldExpression.resolve(text, null, configGroup)
        if (complexExpression == null) return ParadoxMatchResult.NotMatch
        if (complexExpression.getAllErrors(null).isNotEmpty()) return ParadoxMatchResult.PartialMatch
        return forScopeField(element, configGroup, complexExpression, configExpression)
    }

    fun forValueFieldExpression(configGroup: CwtConfigGroup, text: String): ParadoxMatchResult {
        val complexExpression = ParadoxValueFieldExpression.resolve(text, null, configGroup)
        if (complexExpression == null) return ParadoxMatchResult.NotMatch
        if (complexExpression.getAllErrors(null).isNotEmpty()) return ParadoxMatchResult.PartialMatch
        return ParadoxMatchResult.ExactMatch
    }

    fun forVariableFieldExpression(configGroup: CwtConfigGroup, text: String): ParadoxMatchResult {
        val complexExpression = ParadoxVariableFieldExpression.resolve(text, null, configGroup)
        if (complexExpression == null) return ParadoxMatchResult.NotMatch
        if (complexExpression.getAllErrors(null).isNotEmpty()) return ParadoxMatchResult.PartialMatch
        return ParadoxMatchResult.ExactMatch
    }

    fun forDatabaseObjectExpression(configGroup: CwtConfigGroup, text: String): ParadoxMatchResult {
        val complexExpression = ParadoxDatabaseObjectExpression.resolve(text, null, configGroup)
        if (complexExpression == null) return ParadoxMatchResult.NotMatch
        if (complexExpression.getAllErrors(null).isNotEmpty()) return ParadoxMatchResult.PartialMatch
        return ParadoxMatchResult.ExactMatch
    }

    fun forDefineReferenceExpression(configGroup: CwtConfigGroup, text: String): ParadoxMatchResult {
        val complexExpression = ParadoxDefineReferenceExpression.resolve(text, null, configGroup)
        if (complexExpression == null) return ParadoxMatchResult.NotMatch
        if (complexExpression.getAllErrors(null).isNotEmpty()) return ParadoxMatchResult.PartialMatch
        return ParadoxMatchResult.ExactMatch
    }

    fun forStellarisNameFormatExpression(configGroup: CwtConfigGroup, text: String, config: CwtConfig<*>): ParadoxMatchResult {
        val complexExpression = StellarisNameFormatExpression.resolve(text, null, configGroup, config)
        if (complexExpression == null) return ParadoxMatchResult.NotMatch
        if (complexExpression.getAllErrors(null).isNotEmpty()) return ParadoxMatchResult.PartialMatch
        return ParadoxMatchResult.ExactMatch
    }
}
