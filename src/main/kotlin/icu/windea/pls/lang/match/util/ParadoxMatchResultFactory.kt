package icu.windea.pls.lang.match.util

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.delegated.CwtComplexEnumConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configExpression.CwtTemplateExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.anyFast
import icu.windea.pls.core.normalizePath
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.match.ParadoxMatchOptionsService
import icu.windea.pls.lang.match.ParadoxMatchResult
import icu.windea.pls.lang.match.ParadoxMatchResultService
import icu.windea.pls.lang.match.ParadoxTemplateMatchService
import icu.windea.pls.lang.match.toHashString
import icu.windea.pls.lang.psi.members
import icu.windea.pls.lang.resolve.complexExpression.ParadoxArrayDefineReferenceExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxDatabaseObjectExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxDefineReferenceExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxLinkedExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxNameFormatExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxScopeFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxScriptValueReferenceExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxTagsExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxTemplateExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxValueFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxVariableFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.attributes.ParadoxComplexExpressionAttributesEvaluator
import icu.windea.pls.lang.scope.ParadoxScopeMatchService
import icu.windea.pls.lang.util.ParadoxScopeManager
import icu.windea.pls.model.ParadoxLocalisationType
import icu.windea.pls.model.expressions.ParadoxExpression
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.propertyValue

@Optimized
object ParadoxMatchResultFactory {
    /**
     * @see CwtDataTypes.Int
     */
    fun forRangedInt(expression: ParadoxExpression, configExpression: CwtDataExpression): ParadoxMatchResult? {
        val intRange = configExpression.metadata.intRange ?: return null
        val intValue = expression.value.toIntOrNull() ?: return null
        val r = intValue in intRange
        return ParadoxMatchResult.exactOrLenientExact(r) // 即使数值不在范围之内，也不会直接认为不匹配
    }

    /**
     * @see CwtDataTypes.Float
     */
    fun forRangedFloat(expression: ParadoxExpression, configExpression: CwtDataExpression): ParadoxMatchResult? {
        val floatRange = configExpression.metadata.floatRange ?: return null
        val floatValue = expression.value.toFloatOrNull() ?: return null
        val r = floatValue in floatRange
        return ParadoxMatchResult.exactOrLenientExact(r) // 即使数值不在范围之内，也不会直接认为不匹配
    }

    /**
     * @see CwtDataTypes.Block
     */
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
        ProgressManager.checkCanceled() // check cancellation before lazy match
        return ParadoxMatchResult.LazyBlockAwareMatch { ParadoxMatchFactory.matchesBlock(blockElement, config) }
    }

    /**
     * 定义引用的匹配。
     *
     * 说明：
     * - 匹配定义时不忽略子类型。
     *
     * @see CwtDataTypes.Definition
     */
    fun forDefinition(element: PsiElement, project: Project, expression: String, configExpression: CwtDataExpression): ParadoxMatchResult {
        // indexing -> should not visit indices -> treat as wildcard match
        if (ParadoxMatchOptionsService.skipIndex()) return ParadoxMatchResult.WildcardMatch

        val typeExpression = configExpression.metadata.value ?: return ParadoxMatchResult.NotMatch // invalid cwt config
        val key = ParadoxMatchResultService.Keys.cacheForDefinitions
        val cacheKey = "${typeExpression}#${expression}"
        return ParadoxMatchResultService.getFromCache(element, project, key, cacheKey) {
            ProgressManager.checkCanceled() // check cancellation before lazy match
            ParadoxMatchResult.LazyIndexAwareMatch {
                ParadoxMatchFactory.matchesDefinition(element, project, expression, typeExpression)
            }
        }
    }

    /**
     * 定义引用片段的匹配。
     *
     * 说明：
     * - 采用宽松策略，仅要求至少一个模板参数能够解析为对应类型的定义。
     * - 匹配定义时不忽略子类型。
     *
     * @see CwtDataTypes.DefinitionSnippet
     */
    fun forDefinitionSnippet(element: PsiElement, project: Project, expression: String, configExpression: CwtDataExpression): ParadoxMatchResult {
        // indexing -> should not visit indices -> treat as wildcard match
        if (ParadoxMatchOptionsService.skipIndex()) return ParadoxMatchResult.WildcardMatch

        val typeExpression = configExpression.metadata.value ?: return ParadoxMatchResult.NotMatch // invalid cwt config
        val templates = configExpression.metadata.snippetTemplates ?: return ParadoxMatchResult.NotMatch // invalid cwt config
        if (templates.isEmpty()) return ParadoxMatchResult.NotMatch
        val key = ParadoxMatchResultService.Keys.cacheForDefinitionSnippets
        val cacheKey = "${configExpression.expressionString}#${expression}"
        return ParadoxMatchResultService.getFromCache(element, project, key, cacheKey) {
            ProgressManager.checkCanceled() // check cancellation before lazy match
            ParadoxMatchResult.LazyIndexAwareMatch {
                templates.anyFast {
                    val fullName = it.resolve(expression)
                    ParadoxMatchFactory.matchesDefinition(element, project, fullName, typeExpression)
                }
            }
        }
    }

    /**
     * 本地化引用的匹配。
     *
     * 说明：
     * - 可以指定本地化类型 [type]，默认为普通本地化 [ParadoxLocalisationType.Normal]。
     *
     * @see CwtDataTypes.Localisation
     * @see CwtDataTypes.SyncedLocalisation
     */
    fun forLocalisation(element: PsiElement, project: Project, expression: String, type: ParadoxLocalisationType = ParadoxLocalisationType.Normal): ParadoxMatchResult {
        // indexing -> should not visit indices -> treat as wildcard match
        if (ParadoxMatchOptionsService.skipIndex()) return ParadoxMatchResult.WildcardMatch

        val key = ParadoxMatchResultService.Keys.cacheForLocalisations
        val cacheKey = expression
        return ParadoxMatchResultService.getFromCache(element, project, key, cacheKey) {
            ProgressManager.checkCanceled() // check cancellation before lazy match
            ParadoxMatchResult.LazyIndexAwareMatch {
                ParadoxMatchFactory.matchesLocalisation(element, project, expression, type)
            }
        }
    }

    /**
     * 本地化引用片段的匹配。
     *
     * 说明：
     * - 采用宽松策略，仅要求至少一个模板参数能够解析为本地化。
     *
     * @see CwtDataTypes.LocalisationSnippet
     */
    fun forLocalisationSnippet(element: PsiElement, project: Project, expression: String, configExpression: CwtDataExpression): ParadoxMatchResult {
        // indexing -> should not visit indices -> treat as wildcard match
        if (ParadoxMatchOptionsService.skipIndex()) return ParadoxMatchResult.WildcardMatch

        val templates = configExpression.metadata.snippetTemplates ?: return ParadoxMatchResult.NotMatch // invalid cwt config
        if (templates.isEmpty()) return ParadoxMatchResult.NotMatch
        val key = ParadoxMatchResultService.Keys.cacheForLocalisationSnippets
        val cacheKey = "${configExpression.expressionString}#${expression}"
        return ParadoxMatchResultService.getFromCache(element, project, key, cacheKey) {
            ProgressManager.checkCanceled() // check cancellation before lazy match
            ParadoxMatchResult.LazyIndexAwareMatch {
                templates.anyFast {
                    val fullName = it.resolve(expression)
                    ParadoxMatchFactory.matchesLocalisation(element, project, fullName)
                }
            }
        }
    }

    /**
     * @see CwtDataTypeSets.PathReference
     */
    fun forPathReference(element: PsiElement, project: Project, expression: String, configExpression: CwtDataExpression): ParadoxMatchResult {
        if (expression.isEmpty()) return ParadoxMatchResult.NotMatch

        // absolute file path -> treat as wildcard match
        if (configExpression.type == CwtDataTypes.AbsoluteFilePath) return ParadoxMatchResult.WildcardMatch

        // indexing -> should not visit indices -> treat as wildcard match
        if (ParadoxMatchOptionsService.skipIndex()) return ParadoxMatchResult.WildcardMatch

        val pathReference = expression.normalizePath()
        if (pathReference.isEmpty()) return ParadoxMatchResult.NotMatch
        val key = ParadoxMatchResultService.Keys.cacheForPathReferences
        val cacheKey = "${pathReference}#${configExpression}"
        return ParadoxMatchResultService.getFromCache(element, project, key, cacheKey) {
            ProgressManager.checkCanceled() // check cancellation before lazy match
            ParadoxMatchResult.LazyIndexAwareMatch {
                ParadoxMatchFactory.matchesPathReference(element, project, pathReference, configExpression)
            }
        }
    }

    /**
     * @see CwtDataTypes.EnumValue
     */
    fun forComplexEnumValue(element: PsiElement, project: Project, name: String, enumName: String, complexEnumConfig: CwtComplexEnumConfig): ParadoxMatchResult {
        // indexing -> should not visit indices -> treat as wildcard match
        if (ParadoxMatchOptionsService.skipIndex()) return ParadoxMatchResult.WildcardMatch

        // with search scope type -> not cached
        val searchScopeType = complexEnumConfig.searchScopeType
        if (searchScopeType != null) {
            ProgressManager.checkCanceled() // check cancellation before lazy match
            return ParadoxMatchResult.LazyIndexAwareMatch {
                ParadoxMatchFactory.matchesComplexEnumValue(element, project, name, enumName, searchScopeType)
            }
        }

        val key = ParadoxMatchResultService.Keys.cacheForComplexEnumValues
        val cacheKey = "${enumName}#${name}"
        return ParadoxMatchResultService.getFromCache(element, project, key, cacheKey) {
            ProgressManager.checkCanceled() // check cancellation before lazy match
            ParadoxMatchResult.LazyIndexAwareMatch {
                ParadoxMatchFactory.matchesComplexEnumValue(element, project, name, enumName)
            }
        }
    }

    /**
     * @see CwtDataTypes.Modifier
     */
    fun forModifier(element: PsiElement, configGroup: CwtConfigGroup, name: String): ParadoxMatchResult {
        // indexing -> should not visit indices -> treat as wildcard match
        if (ParadoxMatchOptionsService.skipIndex()) return ParadoxMatchResult.WildcardMatch

        val key = ParadoxMatchResultService.Keys.cacheForModifiers
        val cacheKey = name
        return ParadoxMatchResultService.getFromCache(element, configGroup.project, key, cacheKey) {
            ProgressManager.checkCanceled() // check cancellation before lazy match
            ParadoxMatchResult.LazyIndexAwareMatch {
                ParadoxMatchFactory.matchesModifier(element, configGroup, name)
            }
        }
    }

    /**
     * @see CwtDataTypes.Template
     */
    fun forTemplate(element: PsiElement, configGroup: CwtConfigGroup, text: String, config: CwtConfig<*>, options: ParadoxMatchOptions? = null): ParadoxMatchResult {
        // 3.0.3 fallback match -> continue to check reference snippets
        val fastResult = forTemplateExpression(configGroup, text, config)
        if (fastResult !== ParadoxMatchResult.FallbackMatch) return fastResult

        // 3.0.3 indexing -> should not visit indices -> still need to match constant snippets -> already matched and checked by fast result
        if (ParadoxMatchOptionsService.skipIndex()) return ParadoxMatchResult.WildcardMatch

        val template = config.configExpression?.expressionString ?: return ParadoxMatchResult.NotMatch
        val key = ParadoxMatchResultService.Keys.cacheForTemplates
        val cacheKey = "${template}#${text}\u0000${options.toHashString(forMatched = false)}"
        return ParadoxMatchResultService.getFromCache(element, configGroup.project, key, cacheKey) {
            ProgressManager.checkCanceled() // check cancellation before lazy match
            ParadoxMatchResult.LazyTemplateAwareMatch {
                val templateExpression = CwtTemplateExpression.resolve(template)
                ParadoxTemplateMatchService.matches(text, templateExpression, element, configGroup, options)
            }
        }
    }

    /**
     * @see CwtDataTypeSets.ScopeField
     */
    fun forScopeField(element: PsiElement, configGroup: CwtConfigGroup, scopeFieldExpression: ParadoxScopeFieldExpression, configExpression: CwtDataExpression): ParadoxMatchResult {
        return when (configExpression.type) {
            CwtDataTypes.ScopeField -> forComplexExpressionFromAttributes(scopeFieldExpression)
            CwtDataTypes.Scope -> {
                val expectedScope = configExpression.metadata.value ?: return forComplexExpressionFromAttributes(scopeFieldExpression)
                ProgressManager.checkCanceled() // check cancellation before lazy match
                ParadoxMatchResult.LazyScopeAwareMatch {
                    val scopeContext = ParadoxScopeManager.getScopeContext(element, scopeFieldExpression, configExpression)
                    ParadoxScopeMatchService.matchesScope(scopeContext, expectedScope, configGroup)
                }
            }
            CwtDataTypes.ScopeGroup -> {
                val expectedScopeGroup = configExpression.metadata.value ?: return forComplexExpressionFromAttributes(scopeFieldExpression)
                ProgressManager.checkCanceled() // check cancellation before lazy match
                ParadoxMatchResult.LazyScopeAwareMatch {
                    val scopeContext = ParadoxScopeManager.getScopeContext(element, scopeFieldExpression, configExpression)
                    ParadoxScopeMatchService.matchesScopeGroup(scopeContext, expectedScopeGroup, configGroup)
                }
            }
            else -> ParadoxMatchResult.NotMatch
        }
    }

    fun forScopeFieldExpression(element: PsiElement, configGroup: CwtConfigGroup, text: String, configExpression: CwtDataExpression): ParadoxMatchResult {
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

    fun forScriptValueReferenceExpression(configGroup: CwtConfigGroup, text: String): ParadoxMatchResult {
        val complexExpression = ParadoxScriptValueReferenceExpression.resolve(text, null, configGroup) ?: return ParadoxMatchResult.NotMatch
        if (complexExpression.getAllErrors().isNotEmpty()) return ParadoxMatchResult.PartialMatch
        return forComplexExpressionFromAttributes(complexExpression)
    }

    fun forDefineReferenceExpression(configGroup: CwtConfigGroup, text: String): ParadoxMatchResult {
        val complexExpression = ParadoxDefineReferenceExpression.resolve(text, null, configGroup) ?: return ParadoxMatchResult.NotMatch
        if (complexExpression.getAllErrors().isNotEmpty()) return ParadoxMatchResult.PartialMatch
        return forComplexExpressionFromAttributes(complexExpression)
    }

    fun forArrayDefineReferenceExpression(configGroup: CwtConfigGroup, text: String): ParadoxMatchResult {
        val complexExpression = ParadoxArrayDefineReferenceExpression.resolve(text, null, configGroup) ?: return ParadoxMatchResult.NotMatch
        if (complexExpression.getAllErrors().isNotEmpty()) return ParadoxMatchResult.PartialMatch
        return forComplexExpressionFromAttributes(complexExpression)
    }

    fun forTagsExpression(configGroup: CwtConfigGroup, text: String, config: CwtConfig<*>): ParadoxMatchResult {
        val complexExpression = ParadoxTagsExpression.resolve(text, null, configGroup, config) ?: return ParadoxMatchResult.NotMatch
        if (complexExpression.getAllErrors().isNotEmpty()) return ParadoxMatchResult.PartialMatch
        return forComplexExpressionFromAttributes(complexExpression)
    }

    fun forDatabaseObjectExpression(configGroup: CwtConfigGroup, text: String): ParadoxMatchResult {
        val complexExpression = ParadoxDatabaseObjectExpression.resolve(text, null, configGroup) ?: return ParadoxMatchResult.NotMatch
        if (complexExpression.getAllErrors().isNotEmpty()) return ParadoxMatchResult.PartialMatch
        return forComplexExpressionFromAttributes(complexExpression)
    }

    fun forNameFormatExpression(configGroup: CwtConfigGroup, text: String, config: CwtConfig<*>): ParadoxMatchResult {
        val complexExpression = ParadoxNameFormatExpression.resolve(text, null, configGroup, config) ?: return ParadoxMatchResult.NotMatch
        if (complexExpression.getAllErrors().isNotEmpty()) return ParadoxMatchResult.PartialMatch
        return forComplexExpressionFromAttributes(complexExpression)
    }

    fun forTemplateExpression(configGroup: CwtConfigGroup, text: String, config: CwtConfig<*>): ParadoxMatchResult {
        val complexExpression = ParadoxTemplateExpression.resolve(text, null, configGroup, config) ?: return ParadoxMatchResult.NotMatch
        if (complexExpression.getAllErrors().isNotEmpty()) return ParadoxMatchResult.PartialMatch
        return ParadoxMatchResult.FallbackMatch
    }

    fun forComplexExpressionFromAttributes(complexExpression: ParadoxComplexExpression): ParadoxMatchResult {
        // 对于链式表达式，只检查最后一个链接节点的属性即可确定匹配结果
        val nodeToCheck = if (complexExpression is ParadoxLinkedExpression) {
            complexExpression.linkNodes.lastOrNull() ?: complexExpression
        } else {
            complexExpression
        }
        val attributes = ParadoxComplexExpressionAttributesEvaluator.DEFAULT.evaluate(nodeToCheck)
        if (attributes.lenientDynamicDataInvolved) return ParadoxMatchResult.LenientWildcardMatch
        if (attributes.dynamicDataInvolved) return ParadoxMatchResult.WildcardMatch
        return ParadoxMatchResult.ExactMatch
    }
}
