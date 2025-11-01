package icu.windea.pls.lang.codeInsight.completion

import com.intellij.application.options.CodeStyle
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.editor.EditorModificationUtil
import com.intellij.openapi.progress.ProgressManager
import com.intellij.patterns.StandardPatterns
import com.intellij.util.ProcessingContext
import icu.windea.pls.PlsFacade
import icu.windea.pls.PlsIcons
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.CwtAliasConfig
import icu.windea.pls.config.config.delegated.CwtLinkConfig
import icu.windea.pls.config.config.delegated.CwtSubtypeConfig
import icu.windea.pls.config.config.delegated.CwtTypeConfig
import icu.windea.pls.config.config.optionData
import icu.windea.pls.config.configContext.isDefinitionOrMember
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configExpression.value
import icu.windea.pls.config.configGroup.aliasGroups
import icu.windea.pls.config.configGroup.complexEnums
import icu.windea.pls.config.configGroup.declarations
import icu.windea.pls.config.configGroup.dynamicValueTypes
import icu.windea.pls.config.configGroup.enums
import icu.windea.pls.config.configGroup.inlineConfigGroup
import icu.windea.pls.config.configGroup.types
import icu.windea.pls.config.resolved
import icu.windea.pls.config.util.manipulators.CwtConfigManipulator
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.children
import icu.windea.pls.core.codeInsight.LimitedCompletionProcessor
import icu.windea.pls.core.collections.filterIsInstance
import icu.windea.pls.core.collections.synced
import icu.windea.pls.core.icon
import icu.windea.pls.core.match.PathMatcher
import icu.windea.pls.core.processQueryAsync
import icu.windea.pls.core.toPsiFile
import icu.windea.pls.core.util.Tuple2
import icu.windea.pls.core.util.listOrEmpty
import icu.windea.pls.core.util.singleton
import icu.windea.pls.core.util.takeWithOperator
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.ParadoxCsvFile
import icu.windea.pls.csv.psi.ParadoxCsvHeader
import icu.windea.pls.csv.psi.isHeaderColumn
import icu.windea.pls.ep.config.CwtOverriddenConfigProvider
import icu.windea.pls.ep.configContext.CwtDeclarationConfigContextProvider
import icu.windea.pls.ep.expression.ParadoxCsvExpressionSupport
import icu.windea.pls.ep.expression.ParadoxLocalisationExpressionSupport
import icu.windea.pls.ep.expression.ParadoxPathReferenceExpressionSupport
import icu.windea.pls.ep.expression.ParadoxScriptExpressionSupport
import icu.windea.pls.ep.scope.ParadoxDefinitionSupportedScopesProvider
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.psi.mock.ParadoxComplexEnumValueElement
import icu.windea.pls.lang.psi.mock.ParadoxDynamicValueElement
import icu.windea.pls.lang.search.ParadoxComplexEnumValueSearch
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.ParadoxDynamicValueSearch
import icu.windea.pls.lang.search.ParadoxFilePathSearch
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.ParadoxSyncedLocalisationSearch
import icu.windea.pls.lang.search.selector.complexEnumValue
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.definition
import icu.windea.pls.lang.search.selector.distinctByFilePath
import icu.windea.pls.lang.search.selector.distinctByName
import icu.windea.pls.lang.search.selector.dynamicValue
import icu.windea.pls.lang.search.selector.file
import icu.windea.pls.lang.search.selector.localisation
import icu.windea.pls.lang.search.selector.preferLocale
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.search.selector.withFileExtensions
import icu.windea.pls.lang.search.selector.withSearchScopeType
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxCsvFileManager
import icu.windea.pls.lang.util.ParadoxDefinitionManager
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.lang.util.ParadoxModifierManager
import icu.windea.pls.lang.util.ParadoxParameterManager
import icu.windea.pls.lang.util.ParadoxScopeManager
import icu.windea.pls.lang.util.ParadoxScriptFileManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.Occurrence
import icu.windea.pls.model.constants.PlsPatternConstants
import icu.windea.pls.model.paths.ParadoxElementPath
import icu.windea.pls.script.codeStyle.ParadoxScriptCodeStyleSettings
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

object ParadoxCompletionManager {
    // region Predefined Lookup Elements

    val yesLookupElement = LookupElementBuilder.create("yes").bold()
        .withPriority(ParadoxCompletionPriorities.keyword)
        .withCompletionId()

    val noLookupElement = LookupElementBuilder.create("no").bold()
        .withPriority(ParadoxCompletionPriorities.keyword)
        .withCompletionId()

    val blockLookupElement = LookupElementBuilder.create("")
        .withPresentableText("{...}")
        .withInsertHandler { c, _ ->
            val editor = c.editor
            val customSettings = CodeStyle.getCustomSettings(c.file, ParadoxScriptCodeStyleSettings::class.java)
            val spaceWithinBraces = customSettings.SPACE_WITHIN_BRACES
            val text = if (spaceWithinBraces) "{  }" else "{}"
            val length = if (spaceWithinBraces) text.length - 2 else text.length - 1
            EditorModificationUtil.insertStringAtCaret(editor, text, false, true, length)
        }
        .withPriority(ParadoxCompletionPriorities.keyword)
        .withCompletionId()

    // endregion

    // region Core Methods

    fun initializeContext(parameters: CompletionParameters, context: ProcessingContext) {
        context.parameters = parameters
        context.completionIds = mutableSetOf<String>().synced()

        val gameType = selectGameType(parameters.originalFile)
        context.gameType = gameType

        val project = parameters.originalFile.project
        val configGroup = PlsFacade.getConfigGroup(project, gameType)
        context.configGroup = configGroup
    }

    fun addKeyCompletions(memberElement: ParadoxScriptMember, context: ProcessingContext, result: CompletionResultSet) {
        val configContext = ParadoxExpressionManager.getConfigContext(memberElement)
        if (configContext == null) return

        // 仅提示不在定义声明中的key（顶级键和类型键）
        if (!configContext.isDefinitionOrMember()) {
            val elementPath = ParadoxScriptFileManager.getElementPath(memberElement, PlsFacade.getInternalSettings().maxDefinitionDepth) ?: return
            if (elementPath.path.isParameterized()) return // 忽略表达式路径带参数的情况
            val typeKeyPrefix = lazy { context.contextElement?.let { ParadoxScriptFileManager.getKeyPrefixes(it).firstOrNull() } }
            context.isKey = true
            completeKey(context, result, elementPath, typeKeyPrefix)
            return
        }

        // 这里不要使用合并后的子规则，需要先尝试精确匹配或者合并所有非精确匹配的规则，最后得到子规则列表
        val matchOptions = ParadoxMatchOptions.Default or ParadoxMatchOptions.Relax or ParadoxMatchOptions.AcceptDefinition
        val parentConfigs = ParadoxExpressionManager.getConfigs(memberElement, matchOptions = matchOptions)
        val configs = mutableListOf<CwtPropertyConfig>()
        parentConfigs.forEach { c1 ->
            c1.configs?.forEach { c2 ->
                if (c2 is CwtPropertyConfig) {
                    configs += CwtConfigManipulator.inlineSingleAlias(c2) ?: c2 // 这里需要进行必要的内联
                }
            }
        }
        if (configs.isEmpty()) return
        val occurrenceMap = ParadoxExpressionManager.getChildOccurrenceMap(memberElement, parentConfigs)

        context.isKey = true
        context.scopeContext = ParadoxScopeManager.getSwitchedScopeContext(memberElement)

        configs.groupBy { it.key }.forEach { (_, configsWithSameKey) ->
            for (config in configsWithSameKey) {
                if (shouldComplete(config, occurrenceMap)) {
                    val overriddenConfigs = CwtOverriddenConfigProvider.getOverriddenConfigs(context.contextElement!!, config)
                    if (overriddenConfigs.isNotEmpty()) {
                        for (overriddenConfig in overriddenConfigs) {
                            context.config = overriddenConfig
                            completeScriptExpression(context, result)
                        }
                        continue
                    }
                    context.config = config
                    context.configs = configsWithSameKey
                    completeScriptExpression(context, result)
                }
            }
        }
    }

    fun addValueCompletions(memberElement: ParadoxScriptMember, context: ProcessingContext, result: CompletionResultSet) {
        val configContext = ParadoxExpressionManager.getConfigContext(memberElement)
        if (configContext == null) return

        if (!configContext.isDefinitionOrMember()) return

        // 这里不要使用合并后的子规则，需要先尝试精确匹配或者合并所有非精确匹配的规则，最后得到子规则列表
        val matchOptions = ParadoxMatchOptions.Default or ParadoxMatchOptions.Relax or ParadoxMatchOptions.AcceptDefinition
        val parentConfigs = ParadoxExpressionManager.getConfigs(memberElement, matchOptions = matchOptions)
        val configs = mutableListOf<CwtValueConfig>()
        parentConfigs.forEach { c1 ->
            c1.configs?.forEach { c2 ->
                if (c2 is CwtValueConfig) {
                    configs += c2
                }
            }
        }
        if (configs.isEmpty()) return
        val occurrenceMap = ParadoxExpressionManager.getChildOccurrenceMap(memberElement, parentConfigs)

        context.isKey = false
        context.scopeContext = ParadoxScopeManager.getSwitchedScopeContext(memberElement)

        for (config in configs) {
            if (shouldComplete(config, occurrenceMap)) {
                val overriddenConfigs = CwtOverriddenConfigProvider.getOverriddenConfigs(context.contextElement!!, config)
                if (overriddenConfigs.isNotEmpty()) {
                    for (overriddenConfig in overriddenConfigs) {
                        context.config = overriddenConfig
                        completeScriptExpression(context, result)
                    }
                    continue
                }
                context.config = config
                completeScriptExpression(context, result)
            }
        }
    }

    fun addPropertyValueCompletions(element: ParadoxScriptStringExpressionElement, propertyElement: ParadoxScriptProperty, context: ProcessingContext, result: CompletionResultSet) {
        val configContext = ParadoxExpressionManager.getConfigContext(element)
        if (configContext == null) return

        if (!configContext.isDefinitionOrMember()) return

        val configs = configContext.getConfigs()
        if (configs.isEmpty()) return

        context.isKey = false
        context.scopeContext = ParadoxScopeManager.getSwitchedScopeContext(propertyElement)

        for (config in configs) {
            if (config is CwtValueConfig) {
                context.config = config
                completeScriptExpression(context, result)
            }
        }
    }

    fun addColumnCompletions(columnElement: ParadoxCsvColumn, context: ProcessingContext, result: CompletionResultSet) {
        val file = context.parameters?.originalFile ?: return
        if (file !is ParadoxCsvFile) return

        if (columnElement.isHeaderColumn()) {
            completeHeaderColumn(context, result)
            return
        }

        val columnConfig = ParadoxCsvFileManager.getColumnConfig(columnElement) ?: return
        val config = columnConfig.valueConfig ?: return
        context.config = config
        completeCsvExpression(context, result)
    }

    private fun shouldComplete(config: CwtPropertyConfig, occurrenceMap: Map<CwtDataExpression, Occurrence>): Boolean {
        val expression = config.keyExpression
        // 如果类型是aliasName，则无论cardinality如何定义，都应该提供补全（某些cwt规则文件未正确编写）
        if (expression.type == CwtDataTypes.AliasName) return true
        val actualCount = occurrenceMap[expression]?.actual ?: 0
        // 如果写明了cardinality，则为cardinality.max，否则如果类型为常量，则为1，否则为null，null表示没有限制
        // 如果上限是动态的值（如，基于define的值），也不作限制
        val cardinality = config.optionData { cardinality }
        val maxCount = when {
            cardinality == null -> if (expression.type == CwtDataTypes.Constant) 1 else null
            config.optionData { cardinalityMaxDefine } != null -> null
            else -> cardinality.max
        }
        return maxCount == null || actualCount < maxCount
    }

    private fun shouldComplete(config: CwtValueConfig, occurrenceMap: Map<CwtDataExpression, Occurrence>): Boolean {
        val expression = config.valueExpression
        val actualCount = occurrenceMap[expression]?.actual ?: 0
        // 如果写明了cardinality，则为cardinality.max，否则如果类型为常量，则为1，否则为null，null表示没有限制
        // 如果上限是动态的值（如，基于define的值），也不作限制
        val cardinality = config.optionData { cardinality }
        val maxCount = when {
            cardinality == null -> if (expression.type == CwtDataTypes.Constant) 1 else null
            config.optionData { cardinalityMaxDefine } != null -> null
            else -> cardinality.max
        }
        return maxCount == null || actualCount < maxCount
    }

    fun getExpressionTailText(context: ProcessingContext, config: CwtConfig<*>?, withConfigExpression: Boolean = true, withFileName: Boolean = true): String {
        context.expressionTailText?.let { return it }

        return buildString {
            if (withConfigExpression) {
                val configExpression = config?.configExpression
                if (configExpression != null) {
                    append(" by ").append(configExpression)
                }
            }
            if (withFileName) {
                val fileName = config?.resolved()?.pointer?.containingFile?.name
                if (fileName != null) {
                    append(" in ").append(fileName)
                }
            }
        }
    }

    // endregion

    // region General Completion Methods

    fun completeKey(context: ProcessingContext, result: CompletionResultSet, elementPath: ParadoxElementPath, rootKeyPrefix: Lazy<String?>) {
        // 从以下来源收集需要提示的key（顶级键和类型键）
        // - skip_root_key
        // - type_key_filter
        // - type_key_prefix

        val originalFile = context.parameters?.originalFile ?: return
        val fileInfo = originalFile.fileInfo ?: return
        val configGroup = context.configGroup ?: return
        val path = fileInfo.path

        val typeConfigs = configGroup.types.values
        val infoMapForTag = mutableMapOf<String, MutableList<CwtTypeConfig>>()
        val infoMapForKey = mutableMapOf<String, MutableList<Tuple2<CwtTypeConfig, CwtSubtypeConfig?>>>()
        for (typeConfig in typeConfigs) {
            run {
                val typeKeyPrefix = typeConfig.typeKeyPrefix
                if (typeKeyPrefix == null) return@run
                if (rootKeyPrefix.value != null) return@run // avoid complete prefix again after an existing prefix
                if (!ParadoxDefinitionManager.matchesTypeByUnknownDeclaration(typeConfig, path, null, null, null)) return@run
                infoMapForTag.getOrPut(typeKeyPrefix) { mutableListOf() }.add(typeConfig)
            }

            if (!ParadoxDefinitionManager.matchesTypeByUnknownDeclaration(typeConfig, path, null, null, rootKeyPrefix)) continue
            val skipRootKeyConfig = typeConfig.skipRootKey
            if (skipRootKeyConfig.isNullOrEmpty()) {
                if (elementPath.isEmpty()) {
                    typeConfig.typeKeyFilter?.takeWithOperator()?.forEach {
                        infoMapForKey.getOrPut(it) { mutableListOf() }.add(typeConfig to null)
                    }
                    typeConfig.subtypes.values.forEach { subtypeConfig ->
                        subtypeConfig.typeKeyFilter?.takeWithOperator()?.forEach {
                            infoMapForKey.getOrPut(it) { mutableListOf() }.add(typeConfig to subtypeConfig)
                        }
                    }
                }
            } else {
                for (skipConfig in skipRootKeyConfig) {
                    val relative = PathMatcher.relative(elementPath.subPaths, skipConfig, ignoreCase = true, useAny = true, usePattern = true) ?: continue
                    if (relative.isEmpty()) {
                        typeConfig.typeKeyFilter?.takeWithOperator()?.forEach {
                            infoMapForKey.getOrPut(it) { mutableListOf() }.add(typeConfig to null)
                        }
                        typeConfig.subtypes.values.forEach { subtypeConfig ->
                            subtypeConfig.typeKeyFilter?.takeWithOperator()?.forEach {
                                infoMapForKey.getOrPut(it) { mutableListOf() }.add(typeConfig to subtypeConfig)
                            }
                        }
                    } else {
                        infoMapForKey.getOrPut(relative) { mutableListOf() }
                    }
                    break
                }
            }
        }

        for ((key, typeConfigsToUse) in infoMapForTag) {
            val typeConfigToUse = typeConfigsToUse.firstOrNull() ?: continue
            val config = typeConfigToUse.typeKeyPrefixConfig ?: continue
            val element = config.pointer.element
            val icon = PlsIcons.Nodes.Tag
            val tailText = typeConfigsToUse.joinToString(", ", " for ") { typeConfig ->
                typeConfig.name
            }
            val typeFile = config.pointer.containingFile
            context.isKey = false
            context.config = config
            val lookupElement = LookupElementBuilder.create(key).withPsiElement(element)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withCaseSensitivity(false)
                .withPatchableIcon(icon)
                .withPatchableTailText(tailText)
                .withPriority(ParadoxCompletionPriorities.rootKey)
                .forScriptExpression(context)
            result.addElement(lookupElement, context)
            context.isKey = null
            context.config = null
        }
        for ((key, tuples) in infoMapForKey) {
            if (key == "any") return // skip any wildcard
            val typeConfigToUse = tuples.map { it.first }.distinctBy { it.name }.singleOrNull()
            val typeToUse = typeConfigToUse?.name
            // 需要考虑不指定子类型的情况
            val subtypesToUse = when {
                typeConfigToUse == null || tuples.isEmpty() -> null
                else -> tuples.mapNotNull { it.second }.ifEmpty { null }?.distinctBy { it.name }?.map { it.name }
            }
            val config = run {
                if (typeToUse == null) return@run null
                if (typeConfigToUse.typeKeyPrefix != null) return@run typeConfigToUse.typeKeyPrefixConfig
                val declarationConfig = configGroup.declarations.get(typeToUse) ?: return@run null
                val declarationConfigContext = CwtDeclarationConfigContextProvider.getContext(context.contextElement!!, null, typeToUse, subtypesToUse, configGroup)
                declarationConfigContext?.getConfig(declarationConfig)
            }
            val element = config?.pointer?.element
            val icon = if (config == null) PlsIcons.Nodes.Property else PlsIcons.Nodes.Definition(typeToUse)
            val tailText = if (tuples.isEmpty()) null else tuples.joinToString(", ", " for ") { (typeConfig, subTypeConfig) ->
                if (subTypeConfig == null) typeConfig.name else "${typeConfig.name}.${subTypeConfig.name}"
            }
            val typeFile = config?.pointer?.containingFile
            context.config = config
            val lookupElement = LookupElementBuilder.create(key).withPsiElement(element)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withCaseSensitivity(false)
                .withPatchableIcon(icon)
                .withPatchableTailText(tailText)
                .withForceInsertCurlyBraces(tuples.isEmpty())
                .withPriority(ParadoxCompletionPriorities.rootKey)
                .forScriptExpression(context)
            result.addElement(lookupElement, context)
            context.config = null
        }
    }

    fun completeScriptExpression(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val configExpression = context.config!!.configExpression ?: return
        val config = context.config!!
        val configGroup = context.configGroup!!
        val scopeMatched = context.scopeMatched
        val scopeContext = context.scopeContext

        if (configExpression.expressionString.isEmpty()) return

        // 匹配作用域
        if (scopeMatched) {
            val supportedScopes = when {
                config is CwtPropertyConfig -> config.optionData { supportedScopes }
                config is CwtAliasConfig -> config.supportedScopes
                config is CwtLinkConfig -> config.inputScopes
                else -> null
            }
            val scopeMatched1 = when {
                scopeContext == null -> true
                else -> ParadoxScopeManager.matchesScope(scopeContext, supportedScopes, configGroup)
            }
            if (!scopeMatched1 && PlsFacade.getSettings().completion.completeOnlyScopeIsMatched) return
            context.scopeMatched = scopeMatched1
        }

        ParadoxScriptExpressionSupport.complete(context, result)

        context.scopeMatched = scopeMatched
        context.scopeContext = scopeContext
    }

    fun completeLocalisationExpression(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        ParadoxLocalisationExpressionSupport.complete(context, result)
    }

    fun completeCsvExpression(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        ParadoxCsvExpressionSupport.complete(context, result)
    }

    fun completeLocalisation(context: ProcessingContext, result: CompletionResultSet) {
        val config = context.config ?: return
        val keyword = context.keyword

        // 优化：如果已经输入的关键词不是合法的本地化的名字，不要尝试进行本地化的代码补全
        if (keyword.isNotEmpty() && !PlsPatternConstants.localisationName.matches(keyword)) return

        // 本地化的提示结果可能有上千条，因此这里改为先按照输入的关键字过滤结果，关键字变更时重新提示
        result.restartCompletionOnPrefixChange(StandardPatterns.string().shorterThan(keyword.length))

        val configGroup = config.configGroup
        val project = configGroup.project
        val contextElement = context.contextElement
        val tailText = getExpressionTailText(context, config)
        val selector = selector(project, contextElement).localisation()
            .contextSensitive()
            .preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
        // .distinctByName() // 这里selector不需要指定去重
        val processor = LimitedCompletionProcessor<ParadoxLocalisationProperty> p@{ localisation ->
            // apply extraFilter since it's necessary
            if (context.extraFilter?.invoke(localisation) == false) return@p true

            val name = localisation.name // =localisation.paradoxLocalisationInfo?.name
            val typeFile = localisation.containingFile
            val lookupElement = LookupElementBuilder.create(localisation, name)
                .withTypeText(typeFile.name, typeFile.icon, true)
                .withPatchableIcon(PlsIcons.Nodes.Localisation)
                .withPatchableTailText(tailText)
                .forScriptExpression(context)
            result.addElement(lookupElement, context)
            true
        }
        // 保证索引在此readAction中可用
        ReadAction.nonBlocking<Unit> {
            ParadoxLocalisationSearch.processVariants(result.prefixMatcher, selector, processor)
        }.inSmartMode(project).executeSynchronously()
    }

    fun completeSyncedLocalisation(context: ProcessingContext, result: CompletionResultSet) {
        val config = context.config ?: return
        val keyword = context.keyword

        // 优化：如果已经输入的关键词不是合法的本地化的名字，不要尝试进行本地化的代码补全
        if (keyword.isNotEmpty() && !PlsPatternConstants.localisationName.matches(keyword)) return

        // 本地化的提示结果可能有上千条，因此这里改为先按照输入的关键字过滤结果，关键字变更时重新提示
        result.restartCompletionOnPrefixChange(StandardPatterns.string().shorterThan(keyword.length))

        val configGroup = config.configGroup
        val project = configGroup.project
        val contextElement = context.contextElement
        val tailText = getExpressionTailText(context, config)
        val selector = selector(project, contextElement).localisation()
            .contextSensitive()
            .preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
        // .distinctByName() // 这里selector不需要指定去重
        val processor = LimitedCompletionProcessor<ParadoxLocalisationProperty> p@{ syncedLocalisation ->
            // apply extraFilter since it's necessary
            if (context.extraFilter?.invoke(syncedLocalisation) == false) return@p true

            val name = syncedLocalisation.name // =localisation.paradoxLocalisationInfo?.name
            val typeFile = syncedLocalisation.containingFile
            val lookupElement = LookupElementBuilder.create(syncedLocalisation, name)
                .withTypeText(typeFile.name, typeFile.icon, true)
                .withPatchableIcon(PlsIcons.Nodes.Localisation)
                .withPatchableTailText(tailText)
                .forScriptExpression(context)
            result.addElement(lookupElement, context)
            true
        }
        // 保证索引在此readAction中可用
        ReadAction.nonBlocking<Unit> {
            ParadoxSyncedLocalisationSearch.processVariants(result.prefixMatcher, selector, processor)
        }.inSmartMode(project).executeSynchronously()
    }

    fun completeDefinition(context: ProcessingContext, result: CompletionResultSet) {
        val config = context.config ?: return
        val scopeContext = context.scopeContext
        val typeExpression = config.configExpression?.value ?: return
        val configGroup = config.configGroup
        val project = configGroup.project
        val contextElement = context.contextElement
        val tailText = getExpressionTailText(context, config)
        val selector = selector(project, contextElement).definition().contextSensitive().distinctByName()
        ParadoxDefinitionSearch.search(null, typeExpression, selector).processQueryAsync p@{ definition ->
            ProgressManager.checkCanceled()
            val definitionInfo = definition.definitionInfo ?: return@p true
            if (definitionInfo.name.isEmpty()) return@p true // ignore anonymous definitions

            // apply extraFilter since it's necessary
            if (context.extraFilter?.invoke(definition) == false) return@p true

            // 排除不匹配可能存在的 `supported_scopes` 的情况
            val supportedScopes = ParadoxDefinitionSupportedScopesProvider.getSupportedScopes(definition, definitionInfo)
            val scopeMatched = ParadoxScopeManager.matchesScope(scopeContext, supportedScopes, configGroup)
            if (!scopeMatched && PlsFacade.getSettings().completion.completeOnlyScopeIsMatched) return@p true

            val name = definitionInfo.name
            val typeFile = definition.containingFile
            val lookupElement = LookupElementBuilder.create(definition, name)
                .withTypeText(typeFile.name, typeFile.icon, true)
                .withPatchableIcon(PlsIcons.Nodes.Definition(definitionInfo.type))
                .withPatchableTailText(tailText)
                .withScopeMatched(scopeMatched)
                .withDefinitionLocalizedNamesIfNecessary(definition)
                .forScriptExpression(context)
            result.addElement(lookupElement, context)
            true
        }

        ParadoxExtendedCompletionManager.completeExtendedDefinition(context, result)
    }

    fun completePathReference(context: ProcessingContext, result: CompletionResultSet) {
        val contextFile = context.parameters?.originalFile ?: return
        val project = contextFile.project
        val config = context.config ?: return
        val configExpression = config.configExpression ?: return
        val contextElement = context.contextElement
        val pathReferenceExpressionSupport = ParadoxPathReferenceExpressionSupport.get(configExpression)
        if (pathReferenceExpressionSupport != null) {
            val tailText = getExpressionTailText(context, config)
            val fileExtensions = when (config) {
                is CwtMemberConfig<*> -> config.optionData { fileExtensions }
                else -> emptySet()
            }
            // 仅提示匹配 `file_extensions` 选项指定的扩展名的，如果存在
            val selector = selector(project, contextElement).file().contextSensitive()
                .withFileExtensions(fileExtensions)
                .distinctByFilePath()
            ParadoxFilePathSearch.search(null, configExpression, selector).processQueryAsync p@{ virtualFile ->
                ProgressManager.checkCanceled()
                val file = virtualFile.toPsiFile(project) ?: return@p true
                val filePath = virtualFile.fileInfo?.path?.path ?: return@p true
                val name = pathReferenceExpressionSupport.extract(configExpression, contextFile, filePath) ?: return@p true
                val icon = when {
                    config.configExpression == ParadoxInlineScriptManager.inlineScriptPathExpression -> PlsIcons.Nodes.InlineScript
                    else -> PlsIcons.Nodes.PathReference
                }
                val lookupElement = LookupElementBuilder.create(file, name)
                    .withTypeText(file.name, file.icon, true)
                    .withPatchableIcon(icon)
                    .withPatchableTailText(tailText)
                    .forScriptExpression(context)
                result.addElement(lookupElement, context)
                true
            }

            if (config.configExpression == ParadoxInlineScriptManager.inlineScriptPathExpression) {
                ParadoxExtendedCompletionManager.completeExtendedInlineScript(context, result)
            }
        }
    }

    fun completeEnumValue(context: ProcessingContext, result: CompletionResultSet) {
        val config = context.config ?: return
        val enumName = config.configExpression?.value ?: return
        val configGroup = config.configGroup
        val project = configGroup.project
        val contextElement = context.contextElement!!
        val tailText = getExpressionTailText(context, config)
        // 提示简单枚举
        run {
            val enumConfig = configGroup.enums[enumName] ?: return@run
            ProgressManager.checkCanceled()
            val enumValueConfigs = enumConfig.valueConfigMap.values
            if (enumValueConfigs.isEmpty()) return
            val typeFile = enumConfig.pointer.containingFile
            for (enumValueConfig in enumValueConfigs) {
                val name = enumValueConfig.value
                val element = enumValueConfig.pointer.element ?: continue
                val lookupElement = LookupElementBuilder.create(element, name)
                    .withTypeText(typeFile?.name, typeFile?.icon, true)
                    .withCaseSensitivity(false)
                    .withPriority(ParadoxCompletionPriorities.enumValue)
                    .withPatchableIcon(PlsIcons.Nodes.EnumValue)
                    .withPatchableTailText(tailText)
                    .forScriptExpression(context)
                result.addElement(lookupElement, context)
            }
        }
        // 提示复杂枚举
        run {
            val complexEnumConfig = configGroup.complexEnums[enumName] ?: return@run
            ProgressManager.checkCanceled()
            val typeFile = complexEnumConfig.pointer.containingFile
            val searchScope = complexEnumConfig.searchScopeType
            val selector = selector(project, contextElement).complexEnumValue()
                .withSearchScopeType(searchScope)
                .contextSensitive()
                .distinctByName()
            ParadoxComplexEnumValueSearch.search(null, enumName, selector).processQueryAsync { info ->
                ProgressManager.checkCanceled()
                val (name, _, readWriteAccess, _, gameType) = info
                val element = ParadoxComplexEnumValueElement(contextElement, name, enumName, readWriteAccess, gameType, project)
                val lookupElement = LookupElementBuilder.create(element, name)
                    .withTypeText(typeFile?.name, typeFile?.icon, true)
                    .withPriority(ParadoxCompletionPriorities.complexEnumValue)
                    .withPatchableIcon(PlsIcons.Nodes.EnumValue)
                    .withPatchableTailText(tailText)
                    .forScriptExpression(context)
                result.addElement(lookupElement, context)
                true
            }
        }

        ParadoxExtendedCompletionManager.completeExtendedComplexEnumValue(context, result)
    }

    fun completeDynamicValue(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()

        val config = context.config
        val configs = context.configs
        val finalConfigs = configs.ifEmpty { config.singleton.listOrEmpty() }
        if (finalConfigs.isEmpty()) return
        for (finalConfig in finalConfigs) {
            val keyword = context.keyword
            val contextElement = context.contextElement!!
            val configGroup = context.configGroup!!
            val project = configGroup.project
            val configExpression = finalConfig.configExpression ?: return
            val dynamicValueType = configExpression.value ?: return

            // 提示预定义的动态值
            run {
                if (configExpression.type != CwtDataTypes.Value && configExpression.type != CwtDataTypes.DynamicValue) return@run
                ProgressManager.checkCanceled()
                val tailText = getExpressionTailText(context, finalConfig)
                val valueConfig = configGroup.dynamicValueTypes[dynamicValueType] ?: return
                val dynamicValueTypeConfigs = valueConfig.valueConfigMap.values
                for (dynamicValueTypeConfig in dynamicValueTypeConfigs) {
                    val name = dynamicValueTypeConfig.value
                    val element = dynamicValueTypeConfig.pointer.element ?: continue
                    val typeFile = valueConfig.pointer.containingFile
                    val lookupElement = LookupElementBuilder.create(element, name)
                        .withTypeText(typeFile?.name, typeFile?.icon, true)
                        .withPatchableIcon(PlsIcons.Nodes.DynamicValue)
                        .withPatchableTailText(tailText)
                        .forScriptExpression(context)
                    result.addElement(lookupElement, context)
                }
            }
            // 提示来自脚本文件和本地化文件的动态值
            run {
                ProgressManager.checkCanceled()
                val tailText = " by $configExpression"
                val selector = selector(project, contextElement).dynamicValue().distinctByName()
                ParadoxDynamicValueSearch.search(null, dynamicValueType, selector).processQueryAsync p@{ info ->
                    ProgressManager.checkCanceled()
                    val name = info.name
                    if (name == keyword) return@p true // 排除和当前输入的同名的
                    val element = ParadoxDynamicValueElement(contextElement, name, dynamicValueType, info.readWriteAccess, info.gameType, project)
                    val icon = PlsIcons.Nodes.DynamicValue(dynamicValueType)
                    val lookupElement = LookupElementBuilder.create(element, name)
                        .withPatchableIcon(icon)
                        .withPatchableTailText(tailText)
                        .forScriptExpression(context)
                    result.addElement(lookupElement, context)
                    true
                }
            }
        }

        ParadoxExtendedCompletionManager.completeExtendedDynamicValue(context, result)
    }

    fun completeAliasName(context: ProcessingContext, result: CompletionResultSet, aliasName: String) {
        ProgressManager.checkCanceled()

        val configGroup = context.configGroup ?: return
        val config = context.config ?: return
        val configs = context.configs

        val aliasGroup = configGroup.aliasGroups[aliasName] ?: return
        for (aliasConfigs in aliasGroup.values) {
            if (context.isKey == true) {
                context.config = aliasConfigs.first()
                context.configs = aliasConfigs
                completeScriptExpression(context, result)
            } else {
                context.config = aliasConfigs.first()
                completeScriptExpression(context, result)
            }
            context.config = config
            context.configs = configs
        }
    }

    fun completeConstant(context: ProcessingContext, result: CompletionResultSet) {
        val config = context.config ?: return
        val configExpression = config.configExpression ?: return
        val icon = when {
            configExpression.isKey -> PlsIcons.Nodes.Property
            else -> PlsIcons.Nodes.Value
        }
        val name = configExpression.value ?: return
        if (!configExpression.isKey) {
            // 常量的值也可能是yes/no
            if (name == "yes") {
                if (context.quoted) return
                result.addElement(yesLookupElement, context)
                return
            }
            if (name == "no") {
                if (context.quoted) return
                result.addElement(noLookupElement, context)
                return
            }
        }
        val element = config.resolved().pointer.element ?: return
        val typeFile = config.resolved().pointer.containingFile
        val lookupElement = LookupElementBuilder.create(element, name)
            .withTypeText(typeFile?.name, typeFile?.icon, true)
            .withCaseSensitivity(false)
            .withPriority(ParadoxCompletionPriorities.constant)
            .withPatchableIcon(icon)
            .withScopeMatched(context.scopeMatched)
            .forScriptExpression(context)
        result.addElement(lookupElement, context)
    }

    fun completeModifier(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()

        ParadoxModifierManager.completeModifier(context, result)
    }

    fun completeParameter(context: ProcessingContext, result: CompletionResultSet) {
        val config = context.config ?: return
        // 提示参数名（仅限key）
        val contextElement = context.contextElement!!
        val isKey = context.isKey
        if (isKey != true || config !is CwtPropertyConfig) return
        ParadoxParameterManager.completeArguments(contextElement, context, result)
    }

    fun completeInlineScriptInvocation(context: ProcessingContext, result: CompletionResultSet) {
        val configGroup = context.configGroup ?: return
        configGroup.inlineConfigGroup["inline_script"]?.forEach f@{ inlineConfig ->
            ProgressManager.checkCanceled()
            context.config = inlineConfig
            context.isKey = true
            val name = inlineConfig.name
            val element = inlineConfig.pointer.element ?: return@f
            val typeFile = inlineConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withCaseSensitivity(false)
                .withPriority(ParadoxCompletionPriorities.constant)
                .withPatchableIcon(PlsIcons.Nodes.InlineScript)
                .forScriptExpression(context)
            result.addElement(lookupElement, context)
        }
    }

    fun completeHeaderColumn(context: ProcessingContext, result: CompletionResultSet) {
        val column = context.contextElement?.castOrNull<ParadoxCsvColumn>() ?: return
        if (!column.isHeaderColumn()) return
        val file = context.parameters?.originalFile ?: return
        if (file !is ParadoxCsvFile) return
        val rowConfig = ParadoxCsvFileManager.getRowConfig(file) ?: return
        val header = column.parent?.castOrNull<ParadoxCsvHeader>() ?: return
        val existingHeaderNames = header.children()
            .mapNotNull { it as? ParadoxCsvColumn }
            .filterIsInstance<ParadoxCsvColumn> { it != column }
            .map { it.value }
            .toSet()
        val columnConfigs = rowConfig.columns.filterNot { it.key in existingHeaderNames }.values
        if (columnConfigs.isEmpty()) return
        columnConfigs.forEach f@{ columnConfig ->
            ProgressManager.checkCanceled()
            context.config = columnConfig
            val name = columnConfig.key
            val element = columnConfig.pointer.element ?: return@f
            val typeFile = columnConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withIcon(PlsIcons.Nodes.Column)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withPriority(ParadoxCompletionPriorities.constant)
            result.addElement(lookupElement, context)
        }
    }

    // endregion
}
