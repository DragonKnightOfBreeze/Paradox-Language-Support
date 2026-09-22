package icu.windea.pls.ep.resolve.expression

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.aliasConfig
import icu.windea.pls.config.config.delegated.CwtAliasConfig
import icu.windea.pls.config.config.resolved
import icu.windea.pls.config.configExpression.CwtDataExpressionRole
import icu.windea.pls.config.manipulation.CwtConfigExpansionService
import icu.windea.pls.config.util.CwtConfigManager
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.isLeftQuoted
import icu.windea.pls.core.isNotNullOrEmpty
import icu.windea.pls.core.normalizePath
import icu.windea.pls.core.runWithRecursionGuard
import icu.windea.pls.core.toPsiFile
import icu.windea.pls.core.toVirtualFile
import icu.windea.pls.core.util.ProcessorFactory
import icu.windea.pls.core.util.values.singletonListOrEmpty
import icu.windea.pls.core.util.values.to
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionContext
import icu.windea.pls.lang.codeInsight.completion.ParadoxExpressionCompletionManager
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.manipulation.ParadoxConfigExpansionService
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.resolve.ParadoxExpressionService
import icu.windea.pls.lang.resolve.util.ParadoxExpressionSupportFactory
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.ParadoxFilePathSearch
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.lang.search.util.preferLocale
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.model.expressions.ParadoxExpression
import icu.windea.pls.model.type.ParadoxExpressionRole
import icu.windea.pls.script.highlighting.ParadoxScriptHighlighterColors
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey

// Core

interface ParadoxCoreScriptExpressionSupport : ParadoxScriptExpressionSupport {
    /**
     * @see CwtDataTypes.Definition
     * @see CwtDataTypes.SuffixAwareDefinition
     */
    class ForDefinition : ParadoxCoreScriptExpressionSupport {
        override fun supports(dataType: CwtDataType): Boolean {
            return dataType == CwtDataTypes.Definition || dataType == CwtDataTypes.SuffixAwareDefinition
        }

        override fun annotate(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, holder: AnnotationHolder): Boolean {
            val attributesKey = ParadoxScriptHighlighterColors.DEFINITION_REFERENCE
            if (config.configExpression?.type?.isSuffixAware == true) {
                // 使用特殊的高亮（HIGHLIGHTED_REFERENCE）
                ParadoxExpressionSupportFactory.annotateExpressionAsHighlightedReference(element, rangeInExpression, holder)
            } else {
                ParadoxExpressionSupportFactory.annotateExpression(element, rangeInExpression, holder, attributesKey)
            }
            return true
        }

        override fun resolve(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, role: ParadoxExpressionRole): PsiElement? {
            val fullNames = CwtConfigManager.getFullNamesFromSuffixAware(config, text)
            val name = fullNames.singleOrNull() ?: return null
            val configGroup = config.configGroup
            val project = configGroup.project
            val typeExpression = config.configExpression?.metadata?.value ?: return null
            val type = typeExpression.substringBefore('.') // 匹配和解析定义时忽略子类型
            val selector = ParadoxDefinitionSearch.selector(project, element).contextSensitive()
            return ParadoxDefinitionSearch.searchElement(name, type, selector).find()
        }

        override fun resolveAll(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, role: ParadoxExpressionRole): List<PsiElement> {
            val fullNames = CwtConfigManager.getFullNamesFromSuffixAware(config, text)
            val configGroup = config.configGroup
            val project = configGroup.project
            val typeExpression = config.configExpression?.metadata?.value ?: return emptyList()
            val type = typeExpression.substringBefore('.') // 匹配和解析定义时忽略子类型
            return fullNames.flatMap { fullName ->
                val selector = ParadoxDefinitionSearch.selector(project, element).contextSensitive()
                ParadoxDefinitionSearch.searchElement(fullName, type, selector).findAll()
            }
        }

        override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
            if (context.config?.configExpression?.metadata?.suffixes.isNotNullOrEmpty()) return // TODO SUFFIX_AWARE 排除需要带上后缀的情况，目前不支持
            if (context.keyword.isParameterized()) return // 排除可能带参数的情况
            ParadoxExpressionCompletionManager.completeDefinition(context, result)
        }
    }

    /**
     * @see CwtDataTypes.Localisation
     * @see CwtDataTypes.SuffixAwareLocalisation
     */
    class ForLocalisation : ParadoxCoreScriptExpressionSupport {
        override fun supports(dataType: CwtDataType): Boolean {
            return dataType == CwtDataTypes.Localisation || dataType == CwtDataTypes.SuffixAwareLocalisation
        }

        override fun annotate(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, holder: AnnotationHolder): Boolean {
            val attributesKey = ParadoxScriptHighlighterColors.LOCALISATION_REFERENCE
            if (config.configExpression?.type?.isSuffixAware == true) {
                // 使用特殊的高亮（HIGHLIGHTED_REFERENCE）
                ParadoxExpressionSupportFactory.annotateExpressionAsHighlightedReference(element, rangeInExpression, holder)
            } else {
                ParadoxExpressionSupportFactory.annotateExpression(element, rangeInExpression, holder, attributesKey)
            }
            return true
        }

        override fun resolve(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, role: ParadoxExpressionRole): PsiElement? {
            val fullNames = CwtConfigManager.getFullNamesFromSuffixAware(config, text)
            val name = fullNames.singleOrNull() ?: return null
            val configGroup = config.configGroup
            val project = configGroup.project
            val selector = ParadoxLocalisationSearch.selector(project, element).contextSensitive().preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
            return ParadoxLocalisationSearch.searchNormal(name, selector).find()
        }

        override fun resolveAll(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, role: ParadoxExpressionRole): List<PsiElement> {
            val fullNames = CwtConfigManager.getFullNamesFromSuffixAware(config, text)
            val configGroup = config.configGroup
            val project = configGroup.project
            return fullNames.flatMap { fullName ->
                val selector = ParadoxLocalisationSearch.selector(project, element).contextSensitive().preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
                ParadoxLocalisationSearch.searchNormal(fullName, selector).findAll()
            }
        }

        override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
            if (context.config?.configExpression?.metadata?.suffixes.isNotNullOrEmpty()) return // TODO SUFFIX_AWARE 排除需要带上后缀的情况，目前不支持
            if (context.keyword.isParameterized()) return // 排除可能带参数的情况
            ParadoxExpressionCompletionManager.completeLocalisation(context, result)
        }
    }

    /**
     * @see CwtDataTypes.SyncedLocalisation
     * @see CwtDataTypes.SuffixAwareSyncedLocalisation
     */
    class ForSyncedLocalisation : ParadoxCoreScriptExpressionSupport {
        override fun supports(dataType: CwtDataType): Boolean {
            return dataType == CwtDataTypes.SyncedLocalisation || dataType == CwtDataTypes.SuffixAwareSyncedLocalisation
        }

        override fun annotate(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, holder: AnnotationHolder): Boolean {
            val attributesKey = ParadoxScriptHighlighterColors.LOCALISATION_REFERENCE
            if (config.configExpression?.type?.isSuffixAware == true) {
                // 使用特殊的高亮（HIGHLIGHTED_REFERENCE）
                ParadoxExpressionSupportFactory.annotateExpressionAsHighlightedReference(element, rangeInExpression, holder)
            } else {
                ParadoxExpressionSupportFactory.annotateExpression(element, rangeInExpression, holder, attributesKey)
            }
            return true
        }

        override fun resolve(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, role: ParadoxExpressionRole): PsiElement? {
            val fullNames = CwtConfigManager.getFullNamesFromSuffixAware(config, text)
            val name = fullNames.singleOrNull() ?: return null
            val configGroup = config.configGroup
            val project = configGroup.project
            val selector = ParadoxLocalisationSearch.selector(project, element).contextSensitive().preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
            return ParadoxLocalisationSearch.searchSynced(name, selector).find()
        }

        override fun resolveAll(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, role: ParadoxExpressionRole): List<PsiElement> {
            val fullNames = CwtConfigManager.getFullNamesFromSuffixAware(config, text)
            val configGroup = config.configGroup
            val project = configGroup.project
            return fullNames.flatMap { fullName ->
                val selector = ParadoxLocalisationSearch.selector(project, element).contextSensitive().preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
                return ParadoxLocalisationSearch.searchSynced(fullName, selector).findAll()
            }
        }

        override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
            if (context.config?.configExpression?.metadata?.suffixes.isNotNullOrEmpty()) return // TODO SUFFIX_AWARE 排除需要带上后缀的情况，目前不支持
            if (context.keyword.isParameterized()) return // 排除可能带参数的情况
            ParadoxExpressionCompletionManager.completeSyncedLocalisation(context, result)
        }
    }

    /**
     * @see CwtDataTypes.InlineLocalisation
     */
    class ForInlineLocalisation : ParadoxCoreScriptExpressionSupport {
        override fun supports(dataType: CwtDataType): Boolean {
            return dataType == CwtDataTypes.InlineLocalisation
        }

        override fun annotate(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, holder: AnnotationHolder): Boolean {
            if (text.isLeftQuoted()) return false
            val attributesKey = ParadoxScriptHighlighterColors.LOCALISATION_REFERENCE
            ParadoxExpressionSupportFactory.annotateExpression(element, rangeInExpression, holder, attributesKey)
            return true
        }

        override fun resolve(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, role: ParadoxExpressionRole): PsiElement? {
            if (element.text.isLeftQuoted()) return null // inline string
            val configGroup = config.configGroup
            val project = configGroup.project
            val selector = ParadoxLocalisationSearch.selector(project, element).contextSensitive().preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
            return ParadoxLocalisationSearch.searchNormal(text, selector).find()
        }

        override fun resolveAll(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, role: ParadoxExpressionRole): List<PsiElement> {
            if (element.text.isLeftQuoted()) return emptyList() // specific expression
            val configGroup = config.configGroup
            val project = configGroup.project
            val selector = ParadoxLocalisationSearch.selector(project, element).contextSensitive().preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
            return ParadoxLocalisationSearch.searchNormal(text, selector).findAll()
        }

        override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
            if (context.keyword.isParameterized()) return // 排除可能带参数的情况
            if (context.leftQuoted) return
            ParadoxExpressionCompletionManager.completeLocalisation(context, result)
        }
    }

    /**
     * @see CwtDataTypes.Modifier
     */
    class ForModifier : ParadoxCoreScriptExpressionSupport {
        override fun supports(dataType: CwtDataType): Boolean {
            return dataType == CwtDataTypes.Modifier
        }

        override fun annotate(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, holder: AnnotationHolder): Boolean {
            val attributesKey = ParadoxScriptHighlighterColors.MODIFIER
            ParadoxExpressionSupportFactory.annotateExpression(element, rangeInExpression, holder, attributesKey)
            return true
        }

        override fun resolve(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, role: ParadoxExpressionRole): PsiElement? {
            val configGroup = config.configGroup
            return ParadoxExpressionSupportFactory.resolveModifier(element, text, configGroup)
        }

        override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
            if (context.keyword.isParameterized()) return // 排除可能带参数的情况
            ParadoxExpressionCompletionManager.completeModifier(context, result)
        }
    }

    /**
     * @see CwtDataTypes.EnumValue
     */
    class ForEnumValue : ParadoxCoreScriptExpressionSupport {
        override fun supports(dataType: CwtDataType): Boolean {
            return dataType == CwtDataTypes.EnumValue
        }

        override fun annotate(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, holder: AnnotationHolder): Boolean {
            val configGroup = config.configGroup
            val enumName = config.configExpression?.metadata?.value ?: return false
            val attributesKey = when {
                configGroup.enums[enumName] != null -> ParadoxScriptHighlighterColors.ENUM_VALUE
                configGroup.complexEnums[enumName] != null -> ParadoxScriptHighlighterColors.COMPLEX_ENUM_VALUE
                else -> ParadoxScriptHighlighterColors.ENUM_VALUE
            }
            ParadoxExpressionSupportFactory.annotateExpression(element, rangeInExpression, holder, attributesKey)
            return true
        }

        override fun resolve(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, role: ParadoxExpressionRole): PsiElement? {
            return ParadoxExpressionSupportFactory.resolveEnumValue(element, text, config)
        }

        override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
            if (context.keyword.isParameterized()) return // 排除可能带参数的情况
            ParadoxExpressionCompletionManager.completeEnumValue(context, result)
        }
    }

    /**
     * @see CwtDataTypes.UnionValue
     */
    class ForUnionValue : ParadoxCoreScriptExpressionSupport {
        override fun supports(dataType: CwtDataType): Boolean {
            return dataType == CwtDataTypes.UnionValue
        }

        // NOTE 3.0.1 recursion guard is required here for various operations
        override fun annotate(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, holder: AnnotationHolder): Boolean {
            val configGroup = config.configGroup
            val configExpression = config.configExpression ?: return false
            val unionName = configExpression.metadata.value ?: return false
            // NOTE 3.0.1 recursion guard is required here
            // NOTE 3.0.3 use first matched config directly atm, event if the result from this config is null or empty
            val processor = ProcessorFactory.find<CwtValueConfig>()
            runWithRecursionGuard("scriptExpression.annotate.union", unionName) {
                val expression = ParadoxExpression.resolve(element)
                ParadoxConfigExpansionService.expandMatchedUnion(element, expression, unionName, configGroup) {
                    processor.process(it)
                }
            }
            val unionValueConfig = processor.result ?: return false
            return ParadoxExpressionService.annotateScriptExpression(element, text, rangeInExpression, unionValueConfig, holder)
        }

        override fun resolve(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, role: ParadoxExpressionRole): PsiElement? {
            val configGroup = config.configGroup
            val configExpression = config.configExpression ?: return null
            val unionName = configExpression.metadata.value ?: return null
            // NOTE 3.0.1 recursion guard is required here
            // NOTE 3.0.3 use first matched config directly atm, event if the result from this config is null or empty
            val processor = ProcessorFactory.find<CwtValueConfig>()
            runWithRecursionGuard("scriptExpression.resolve.union", unionName) {
                val expression = ParadoxExpression.resolve(element)
                ParadoxConfigExpansionService.expandMatchedUnion(element, expression, unionName, configGroup) {
                    processor.process(it)
                }
            }
            val unionValueConfig = processor.result ?: return null
            return ParadoxExpressionService.resolveScriptExpression(element, text, rangeInExpression, unionValueConfig, role)
        }

        override fun resolveAll(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, role: ParadoxExpressionRole): List<PsiElement> {
            val configGroup = config.configGroup
            val configExpression = config.configExpression ?: return emptyList()
            val unionName = configExpression.metadata.value ?: return emptyList()
            // NOTE 3.0.1 recursion guard is required here
            // NOTE 3.0.3 use first matched config directly atm, event if the result from this config is null or empty
            val processor = ProcessorFactory.find<CwtValueConfig>()
            runWithRecursionGuard("scriptExpression.resolveAll.union", unionName) {
                val expression = ParadoxExpression.resolve(element)
                ParadoxConfigExpansionService.expandMatchedUnion(element, expression, unionName, configGroup) {
                    processor.process(it)
                }
            }
            val unionValueConfig = processor.result ?: return emptyList()
            return ParadoxExpressionService.resolveAllScriptExpression(element, text, rangeInExpression, unionValueConfig, role)
        }

        override fun getReferences(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, role: ParadoxExpressionRole): List<PsiReference> {
            // #374 `union[x]` 同样需要兼容这里，目前来说，这是和 `alias_keys_field[x]` 不同的地方（例如，对于 `union[test_union] = { value[test_flag] }`，其中的 `value[test_flag]` 可以匹配多个节点）
            val configGroup = config.configGroup
            val configExpression = config.configExpression ?: return emptyList()
            val unionName = configExpression.metadata.value ?: return emptyList()
            // NOTE 3.0.1 recursion guard is required here
            // NOTE 3.0.3 use first matched config directly atm, event if the result from this config is null or empty
            val processor = ProcessorFactory.find<CwtValueConfig>()
            runWithRecursionGuard("scriptExpression.getReferences.union", unionName) {
                val expression = ParadoxExpression.resolve(element)
                ParadoxConfigExpansionService.expandMatchedUnion(element, expression, unionName, configGroup) {
                    processor.process(it)
                }
            }
            val unionValueConfig = processor.result ?: return emptyList()
            return ParadoxExpressionService.getScriptExpressionReferences(element, text, rangeInExpression, unionValueConfig, role)
        }

        override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
            val configGroup = context.configGroup
            val configExpression = context.config?.configExpression ?: return
            ProgressManager.checkCanceled()
            // NOTE 3.0.3 recursion guard is required here
            CwtConfigExpansionService.expandUnion(configExpression, configGroup, "scriptExpression.complete") { _, unionValueConfig ->
                val context = context.copy(config = unionValueConfig, configs = setOf(unionValueConfig))
                ParadoxExpressionCompletionManager.completeScriptExpression(context, result)
                true
            }
        }
    }

    /**
     * @see CwtDataTypes.AliasKeysField
     * @see CwtDataTypes.AliasName
     */
    class ForAliasName : ParadoxCoreScriptExpressionSupport {
        override fun supports(dataType: CwtDataType): Boolean {
            return dataType == CwtDataTypes.AliasKeysField || dataType == CwtDataTypes.AliasName
        }

        // NOTE 3.0.1 recursion guard is required here for various operations

        override fun annotate(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, holder: AnnotationHolder): Boolean {
            val configGroup = config.configGroup
            val configExpression = config.configExpression ?: return false
            val aliasName = configExpression.metadata.value ?: return false
            val aliasGroup = configGroup.aliasGroups.get(aliasName) ?: return false
            // NOTE 3.0.1 recursion guard is required here
            val processor = ProcessorFactory.any<Unit>()
            runWithRecursionGuard("scriptExpression.annotate.alias", aliasName) {
                val expression = ParadoxExpression.resolve(element)
                ParadoxConfigExpansionService.expandMatchedAliasKeys(element, expression, aliasName, configGroup) p@{ key ->
                    val aliasConfig = aliasGroup[key]?.firstOrNull() ?: return@p true
                    val r = ParadoxExpressionService.annotateScriptExpression(element, text, rangeInExpression, aliasConfig, holder)
                    if (!r) return@p true
                    processor.process(Unit)
                }
            }
            return processor.result
        }

        override fun resolve(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, role: ParadoxExpressionRole): PsiElement? {
            val configGroup = config.configGroup
            val configExpression = config.configExpression ?: return null
            val aliasName = configExpression.metadata.value ?: return null
            val aliasGroup = configGroup.aliasGroups[aliasName] ?: return null
            // NOTE 3.0.1 recursion guard is required here
            val processor = ProcessorFactory.find<PsiElement>()
            runWithRecursionGuard("scriptExpression.resolve.alias", aliasName) {
                val expression = ParadoxExpression.resolve(element)
                ParadoxConfigExpansionService.expandMatchedAliasKeys(element, expression, aliasName, configGroup) p@{ key ->
                    val aliasConfig = aliasGroup[key]?.firstOrNull() ?: return@p true
                    val r = ParadoxExpressionService.resolveScriptExpression(element, text, rangeInExpression, aliasConfig, role)
                    if (r == null) return@p true
                    processor.process(r)
                }
            }
            return processor.result
        }

        override fun resolveAll(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, role: ParadoxExpressionRole): List<PsiElement> {
            val configGroup = config.configGroup
            val configExpression = config.configExpression ?: return emptyList()
            val aliasName = configExpression.metadata.value ?: return emptyList()
            val aliasGroup = configGroup.aliasGroups[aliasName] ?: return emptyList()
            // NOTE 3.0.1 recursion guard is required here
            val processor = ProcessorFactory.find<List<PsiElement>>()
            runWithRecursionGuard("scriptExpression.resolveAll.alias", aliasName) {
                val expression = ParadoxExpression.resolve(element)
                ParadoxConfigExpansionService.expandMatchedAliasKeys(element, expression, aliasName, configGroup) p@{ key ->
                    val aliasConfig = aliasGroup[key]?.firstOrNull() ?: return@p true
                    val r = ParadoxExpressionService.resolveAllScriptExpression(element, text, rangeInExpression, aliasConfig, role).orNull()
                    if (r == null) return@p true
                    processor.process(r)
                }
            }
            return processor.result.orEmpty()
        }

        override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
            val configGroup = context.configGroup
            val configExpression = context.config?.configExpression ?: return
            ProgressManager.checkCanceled()
            // NOTE 3.0.3 recursion guard is required here
            CwtConfigExpansionService.expandAlias(configExpression, configGroup, "scriptExpression.complete") { _, aliasConfigs ->
                val context = context.copy(config = aliasConfigs.first(), configs = aliasConfigs)
                ParadoxExpressionCompletionManager.completeScriptExpression(context, result)
                true
            }
        }
    }

    /**
     * @see CwtDataTypeSets.PathReference
     */
    class ForPathReference : ParadoxCoreScriptExpressionSupport {
        override fun supports(dataType: CwtDataType): Boolean {
            return dataType in CwtDataTypeSets.PathReference
        }

        override fun annotate(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, holder: AnnotationHolder): Boolean {
            val attributesKey = ParadoxScriptHighlighterColors.PATH_REFERENCE
            ParadoxExpressionSupportFactory.annotateExpression(element, rangeInExpression, holder, attributesKey)
            return true
        }

        override fun resolve(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, role: ParadoxExpressionRole): PsiElement? {
            if (text.isEmpty()) return null

            val configExpression = config.configExpression ?: return null
            val configGroup = config.configGroup
            val project = configGroup.project

            // absolute file path -> use `VfsUtil.findFile`
            if (configExpression.type == CwtDataTypes.AbsoluteFilePath) return text.toVirtualFile()?.toPsiFile(project)

            val pathReference = text.normalizePath()
            if (pathReference.isEmpty()) return null
            val selector = ParadoxFilePathSearch.selector(project, element).contextSensitive()
            return ParadoxFilePathSearch.search(pathReference, configExpression, selector).find()?.toPsiFile(project)
        }

        override fun resolveAll(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, role: ParadoxExpressionRole): List<PsiElement> {
            val configExpression = config.configExpression ?: return emptyList()
            val configGroup = config.configGroup
            val project = configGroup.project

            if (configExpression.type == CwtDataTypes.AbsoluteFilePath) {
                return text.toVirtualFile()?.toPsiFile(project).to.singletonListOrEmpty()
            }

            val pathReference = text.normalizePath()
            if (pathReference.isEmpty()) return emptyList()
            val selector = ParadoxFilePathSearch.selector(project, element).contextSensitive()
            return ParadoxFilePathSearch.search(pathReference, configExpression, selector).findAll().mapNotNull { it.toPsiFile(project) }
        }

        override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
            if (context.keyword.isParameterized()) return // 排除可能带参数的情况
            ParadoxExpressionCompletionManager.completePathReference(context, result)
        }
    }

    /**
     * @see CwtDataTypes.Constant
     */
    class ForConstant : ParadoxCoreScriptExpressionSupport {
        override fun supports(dataType: CwtDataType): Boolean {
            return dataType == CwtDataTypes.Constant
        }

        override fun annotate(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, holder: AnnotationHolder): Boolean {
            val annotated = annotateByAliasName(element, rangeInExpression, holder, config)
            if (annotated) return false
            val configExpression = config.configExpression ?: return false
            val role = configExpression.role
            when (role) {
                CwtDataExpressionRole.Other -> {
                    // unnecessary
                    return false
                }
                CwtDataExpressionRole.Value -> {
                    // skip
                    return false
                }
                CwtDataExpressionRole.Key -> {
                    // unnecessary
                    if (element is ParadoxScriptPropertyKey && rangeInExpression.startOffset == 0 && rangeInExpression.endOffset == text.length) return false

                    val attributesKey = ParadoxScriptHighlighterColors.PROPERTY_KEY
                    ParadoxExpressionSupportFactory.annotateExpression(element, rangeInExpression, holder, attributesKey)
                    return true
                }
            }
        }

        private fun annotateByAliasName(element: ParadoxExpressionElement, rangeInExpression: TextRange, holder: AnnotationHolder, config: CwtConfig<*>): Boolean {
            val aliasConfig = when {
                config is CwtPropertyConfig -> config.aliasConfig
                config is CwtAliasConfig -> config
                else -> null
            } ?: return false
            val type = aliasConfig.configExpression.type
            if (type !in CwtDataTypeSets.ConstantAware) return false
            val aliasName = aliasConfig.name
            val attributesKey = when {
                aliasName == "modifier" -> ParadoxScriptHighlighterColors.MODIFIER
                aliasName == "trigger" -> ParadoxScriptHighlighterColors.TRIGGER
                aliasName == "effect" -> ParadoxScriptHighlighterColors.EFFECT
                else -> return false
            }
            ParadoxExpressionSupportFactory.annotateExpression(element, rangeInExpression, holder, attributesKey)
            return true
        }

        override fun resolve(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, role: ParadoxExpressionRole): PsiElement? {
            return config.resolved().pointer.element
        }

        override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
            if (context.keyword.isParameterized()) return // 排除可能带参数的情况
            ParadoxExpressionCompletionManager.completeConstant(context, result)
        }
    }
}
