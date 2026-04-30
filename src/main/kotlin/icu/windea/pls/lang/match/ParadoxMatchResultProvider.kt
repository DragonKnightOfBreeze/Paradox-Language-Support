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
import icu.windea.pls.core.createCachedValue
import icu.windea.pls.core.normalizePath
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.RegistedKeyWithFactory
import icu.windea.pls.core.util.getOrPutUserData
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.withDependencyItems
import icu.windea.pls.ep.match.ParadoxScriptExpressionMatcher.*
import icu.windea.pls.lang.PlsModificationTrackers
import icu.windea.pls.lang.psi.members
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxDatabaseObjectExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxDefineReferenceExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxLinkedExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxNameFormatExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxScopeFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxValueFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxVariableFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.attributes.ParadoxComplexExpressionAttributesEvaluator
import icu.windea.pls.lang.resolve.complexExpression.linkNodes
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.selectRootFile
import icu.windea.pls.lang.util.ParadoxScopeManager
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.propertyValue

private typealias MatchResultCache = CancelableCache<String, ParadoxMatchResult>
private typealias MatchResultNestedCache = NestedCache<VirtualFile, String, ParadoxMatchResult, MatchResultCache>
private typealias KeyForCache = RegistedKeyWithFactory<CachedValue<MatchResultNestedCache>, CwtConfigGroup>

object ParadoxMatchResultProvider {
    object Keys : KeyRegistry() {
        val cacheForDefinitions by createKeyForCache(PlsModificationTrackers.ScriptFile)
        val cacheForLocalisations by createKeyForCache(PlsModificationTrackers.LocalisationFile, PlsModificationTrackers.PreferredLocale)
        val cacheForSyncedLocalisations by createKeyForCache(PlsModificationTrackers.LocalisationFile, PlsModificationTrackers.PreferredLocale)
        val cacheForPathReferences by createKeyForCache(PlsModificationTrackers.FilePath)
        val cacheForComplexEnumValues by createKeyForCache(PlsModificationTrackers.ScriptFile)
        val cacheForModifiers by createKeyForCache(PlsModificationTrackers.ScriptFile)
        val cacheForTemplates by createKeyForCache(PlsModificationTrackers.ScriptFile, PlsModificationTrackers.LocalisationFile, PlsModificationTrackers.PreferredLocale)

        private fun createKeyForCache(vararg dependencies: Any) = registerKey<CachedValue<MatchResultNestedCache>, CwtConfigGroup>(Keys) {
            // rootFile -> cacheKey -> configMatchResult
            createCachedValue(project) {
                createNestedCache<VirtualFile, _, _, _> {
                    CacheBuilder().build<String, ParadoxMatchResult>().cancelable()
                }.withDependencyItems(*dependencies)
            }
        }
    }

    fun forRangedInt(value: String, configExpression: CwtDataExpression): ParadoxMatchResult? {
        val intRange = configExpression.intRange ?: return null
        val intValue = value.toIntOrNull() ?: return null
        val r = intValue in intRange
        // 即使数值不在范围之内，也不会直接认为不匹配
        return if (r) ParadoxMatchResult.ExactMatch else ParadoxMatchResult.ToleratedExactMatch
    }

    fun forRangedFloat(value: String, configExpression: CwtDataExpression): ParadoxMatchResult? {
        val floatRange = configExpression.floatRange ?: return null
        val floatValue = value.toFloatOrNull() ?: return null
        val r = floatValue in floatRange
        // 即使数值不在范围之内，也不会直接认为不匹配
        return if (r) ParadoxMatchResult.ExactMatch else ParadoxMatchResult.ToleratedExactMatch
    }

    fun forBlock(element: PsiElement, config: CwtMemberConfig<*>): ParadoxMatchResult {
        val blockElement = when (element) {
            is ParadoxScriptProperty -> element.propertyValue()
            is ParadoxScriptBlock -> element
            else -> null
        } ?: return ParadoxMatchResult.NotMatch
        // 如果子句规则内容为空，则仅当子句内容为空时才认为匹配
        if (config.configs.isNullOrEmpty()) {
            val r = blockElement.members().none()
            return ParadoxMatchResult.exactOrFallback(r)
        }
        // 使用检测子句内容的匹配
        return ParadoxMatchResult.LazyBlockAwareMatch { ParadoxMatchProvider.matchesBlock(blockElement, config) }
    }

    fun getCached(element: PsiElement, project: Project, key: KeyForCache, cacheKey: String, matchResultProvider: (String) -> ParadoxMatchResult): ParadoxMatchResult {
        ProgressManager.checkCanceled()
        val rootFile = selectRootFile(element) ?: return ParadoxMatchResult.NotMatch
        val configGroup = PlsFacade.getConfigGroup(project, selectGameType(rootFile))
        val cache = configGroup.getOrPutUserData(key).value.get(rootFile)
        return cache.get(cacheKey, matchResultProvider)
    }

    fun forDefinition(element: PsiElement, project: Project, expression: String, configExpression: CwtDataExpression): ParadoxMatchResult {
        // indexing -> should not visit indices -> treat as wildcard match
        if (ParadoxMatchService.skipIndex()) return ParadoxMatchResult.WildcardMatch

        val typeExpression = configExpression.value ?: return ParadoxMatchResult.NotMatch // invalid cwt config
        val suffixes = configExpression.suffixes.orEmpty()
        val key = Keys.cacheForDefinitions
        val cacheKey = when {
            suffixes.isEmpty() -> "${typeExpression}#${expression}"
            else -> "${suffixes.joinToString(",")}#${typeExpression}#${expression}"
        }
        return getCached(element, project, key, cacheKey) {
            ParadoxMatchResult.LazyIndexAwareMatch {
                when {
                    suffixes.isEmpty() -> ParadoxMatchProvider.matchesDefinition(element, project, expression, typeExpression)
                    else -> suffixes.any { ParadoxMatchProvider.matchesDefinition(element, project, expression + it, typeExpression) }
                }
            }
        }
    }

    fun forLocalisation(element: PsiElement, project: Project, expression: String, configExpression: CwtDataExpression): ParadoxMatchResult {
        // indexing -> should not visit indices -> treat as wildcard match
        if (ParadoxMatchService.skipIndex()) return ParadoxMatchResult.WildcardMatch

        val suffixes = configExpression.suffixes.orEmpty()
        val key = Keys.cacheForLocalisations
        val cacheKey = when {
            suffixes.isEmpty() -> expression
            else -> "${suffixes.joinToString(",")}#${expression}"
        }
        return getCached(element, project, key, cacheKey) {
            ParadoxMatchResult.LazyIndexAwareMatch {
                when {
                    suffixes.isEmpty() -> ParadoxMatchProvider.matchesLocalisation(element, project, expression)
                    else -> suffixes.any { ParadoxMatchProvider.matchesLocalisation(element, project, expression + it) }
                }
            }
        }
    }

    fun forSyncedLocalisation(element: PsiElement, project: Project, expression: String, configExpression: CwtDataExpression): ParadoxMatchResult {
        // indexing -> should not visit indices -> treat as wildcard match
        if (ParadoxMatchService.skipIndex()) return ParadoxMatchResult.WildcardMatch

        val suffixes = configExpression.suffixes.orEmpty()
        val key = Keys.cacheForSyncedLocalisations
        val cacheKey = when {
            suffixes.isEmpty() -> expression
            else -> "${suffixes.joinToString(",")}#${expression}"
        }
        return getCached(element, project, key, cacheKey) {
            ParadoxMatchResult.LazyIndexAwareMatch {
                when {
                    suffixes.isEmpty() -> ParadoxMatchProvider.matchesSyncedLocalisation(element, project, expression)
                    else -> suffixes.any { ParadoxMatchProvider.matchesSyncedLocalisation(element, project, expression + it) }
                }
            }
        }
    }

    fun forPathReference(element: PsiElement, project: Project, expression: String, configExpression: CwtDataExpression): ParadoxMatchResult {
        // indexing -> should not visit indices -> treat as wildcard match
        if (ParadoxMatchService.skipIndex()) return ParadoxMatchResult.WildcardMatch

        val pathReference = expression.normalizePath()
        val key = Keys.cacheForPathReferences
        val cacheKey = "${pathReference}#${configExpression}"
        return getCached(element, project, key, cacheKey) {
            ParadoxMatchResult.LazyIndexAwareMatch {
                ParadoxMatchProvider.matchesPathReference(element, project, pathReference, configExpression)
            }
        }
    }

    fun forComplexEnumValue(element: PsiElement, project: Project, name: String, enumName: String, complexEnumConfig: CwtComplexEnumConfig): ParadoxMatchResult {
        // indexing -> should not visit indices -> treat as wildcard match
        if (ParadoxMatchService.skipIndex()) return ParadoxMatchResult.WildcardMatch

        // with search scope type -> not cached
        val searchScopeType = complexEnumConfig.searchScopeType
        if (searchScopeType != null) {
            return ParadoxMatchResult.LazyIndexAwareMatch {
                ParadoxMatchProvider.matchesComplexEnumValue(element, project, name, enumName, searchScopeType)
            }
        }

        val key = Keys.cacheForComplexEnumValues
        val cacheKey = "${enumName}#${name}"
        return getCached(element, project, key, cacheKey) {
            ParadoxMatchResult.LazyIndexAwareMatch {
                ParadoxMatchProvider.matchesComplexEnumValue(element, project, name, enumName)
            }
        }
    }

    fun forModifier(element: PsiElement, configGroup: CwtConfigGroup, name: String): ParadoxMatchResult {
        // indexing -> should not visit indices -> treat as wildcard match
        if (ParadoxMatchService.skipIndex()) return ParadoxMatchResult.WildcardMatch

        val key = Keys.cacheForModifiers
        val cacheKey = name
        return getCached(element, configGroup.project, key, cacheKey) {
            ParadoxMatchResult.LazyIndexAwareMatch {
                ParadoxMatchProvider.matchesModifier(element, configGroup, name)
            }
        }
    }

    fun forTemplate(element: PsiElement, configGroup: CwtConfigGroup, expression: String, configExpression: CwtDataExpression, options: ParadoxMatchOptions? = null): ParadoxMatchResult {
        // NOTE 2.1.5 indexing -> should not visit indices -> still need to match constant snippets
        // if (ParadoxMatchService.skipIndex()) return ParadoxMatchResult.ExactMatch

        val template = configExpression.expressionString
        val key = Keys.cacheForTemplates
        val cacheKey = "${template}#${expression}\u0000${options.toHashString(forMatched = false)}"
        options.toHashString(forMatched = false)
        return getCached(element, configGroup.project, key, cacheKey) {
            ParadoxMatchResult.LazyTemplateAwareMatch {
                ParadoxMatchProvider.matchesTemplate(element, configGroup, expression, template, options)
            }
        }
    }

    fun forScopeField(element: PsiElement, configGroup: CwtConfigGroup, scopeFieldExpression: ParadoxScopeFieldExpression, configExpression: CwtDataExpression): ParadoxMatchResult {
        return when (configExpression.type) {
            CwtDataTypes.ScopeField -> forComplexExpressionFromAttributes(scopeFieldExpression)
            CwtDataTypes.Scope -> {
                val expectedScope = configExpression.value ?: return forComplexExpressionFromAttributes(scopeFieldExpression)
                ParadoxMatchResult.LazyScopeAwareMatch {
                    val scopeContext = ParadoxScopeManager.getScopeContext(element, scopeFieldExpression, configExpression)
                    ParadoxScopeManager.matchesScope(scopeContext, expectedScope, configGroup)
                }
            }
            CwtDataTypes.ScopeGroup -> {
                val expectedScopeGroup = configExpression.value ?: return forComplexExpressionFromAttributes(scopeFieldExpression)
                ParadoxMatchResult.LazyScopeAwareMatch {
                    val scopeContext = ParadoxScopeManager.getScopeContext(element, scopeFieldExpression, configExpression)
                    ParadoxScopeManager.matchesScopeGroup(scopeContext, expectedScopeGroup, configGroup)
                }
            }
            else -> ParadoxMatchResult.NotMatch
        }
    }

    fun forScopeFieldExpression(configGroup: CwtConfigGroup, text: String, configExpression: CwtDataExpression, element: PsiElement): ParadoxMatchResult {
        val complexExpression = ParadoxScopeFieldExpression.resolve(text, null, configGroup) ?: return ParadoxMatchResult.NotMatch
        if (complexExpression.getAllErrors().isNotEmpty()) return ParadoxMatchResult.PartialMatch
        return forScopeField(element, configGroup, complexExpression, configExpression)
    }

    fun forValueFieldExpression(configGroup: CwtConfigGroup, text: String): ParadoxMatchResult {
        val complexExpression = ParadoxValueFieldExpression.resolve(text, null, configGroup) ?: return ParadoxMatchResult.NotMatch
        if (complexExpression.getAllErrors().isNotEmpty()) return ParadoxMatchResult.PartialMatch
        return forComplexExpressionFromAttributes(complexExpression)
    }

    fun forVariableFieldExpression(configGroup: CwtConfigGroup, text: String): ParadoxMatchResult {
        val complexExpression = ParadoxVariableFieldExpression.resolve(text, null, configGroup) ?: return ParadoxMatchResult.NotMatch
        if (complexExpression.getAllErrors().isNotEmpty()) return ParadoxMatchResult.PartialMatch
        return forComplexExpressionFromAttributes(complexExpression)
    }

    fun forDatabaseObjectExpression(configGroup: CwtConfigGroup, text: String): ParadoxMatchResult {
        val complexExpression = ParadoxDatabaseObjectExpression.resolve(text, null, configGroup) ?: return ParadoxMatchResult.NotMatch
        if (complexExpression.getAllErrors().isNotEmpty()) return ParadoxMatchResult.PartialMatch
        return forComplexExpressionFromAttributes(complexExpression)
    }

    fun forDefineReferenceExpression(configGroup: CwtConfigGroup, text: String): ParadoxMatchResult {
        val complexExpression = ParadoxDefineReferenceExpression.resolve(text, null, configGroup) ?: return ParadoxMatchResult.NotMatch
        if (complexExpression.getAllErrors().isNotEmpty()) return ParadoxMatchResult.PartialMatch
        return forComplexExpressionFromAttributes(complexExpression)
    }

    fun forNameFormatExpression(configGroup: CwtConfigGroup, text: String, config: CwtConfig<*>): ParadoxMatchResult {
        val complexExpression = ParadoxNameFormatExpression.resolve(text, null, configGroup, config) ?: return ParadoxMatchResult.NotMatch
        if (complexExpression.getAllErrors().isNotEmpty()) return ParadoxMatchResult.PartialMatch
        return forComplexExpressionFromAttributes(complexExpression)
    }

    private fun forComplexExpressionFromAttributes(complexExpression: ParadoxComplexExpression): ParadoxMatchResult {
        // 对于链式表达式，只检查最后一个链接节点的属性即可确定匹配结果
        val nodeToCheck = if (complexExpression is ParadoxLinkedExpression) {
            complexExpression.linkNodes.lastOrNull() ?: complexExpression
        } else {
            complexExpression
        }
        val attributes = ParadoxComplexExpressionAttributesEvaluator.DEFAULT.evaluate(nodeToCheck)
        if (attributes.relaxDynamicDataInvolved) return ParadoxMatchResult.RelaxWildcardMatch
        if (attributes.dynamicDataInvolved) return ParadoxMatchResult.WildcardMatch
        return ParadoxMatchResult.ExactMatch
    }
}
