package icu.windea.pls.lang.codeInsight.completion

import com.intellij.application.options.CodeStyle
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.editor.EditorModificationUtil
import com.intellij.openapi.progress.ProgressManager
import com.intellij.ui.JBColor
import icu.windea.pls.ChronicleIcons
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtRowType
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.CwtMacroConfig
import icu.windea.pls.config.config.delegated.CwtSubtypeConfig
import icu.windea.pls.config.config.delegated.CwtTypeConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.manipulation.CwtConfigManipulationService
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.codeInsight.LimitedCompletionProcessor
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.icon
import icu.windea.pls.core.letIf
import icu.windea.pls.core.match.PathMatcher
import icu.windea.pls.core.processAsync
import icu.windea.pls.core.runSmartReadAction
import icu.windea.pls.core.util.Tuple2
import icu.windea.pls.core.util.values.singletonListOrEmpty
import icu.windea.pls.core.util.values.to
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.ParadoxCsvFile
import icu.windea.pls.csv.psi.ParadoxCsvHeader
import icu.windea.pls.csv.psi.ParadoxCsvPsiService
import icu.windea.pls.ep.util.data.StellarisGameConceptData
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.getDefinitionData
import icu.windea.pls.lang.match.CwtTypeConfigMatchContext
import icu.windea.pls.lang.match.ParadoxConfigMatchService
import icu.windea.pls.lang.match.ParadoxMatchOccurrence
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.resolve.ParadoxConfigService
import icu.windea.pls.lang.resolve.ParadoxDefinitionService
import icu.windea.pls.lang.resolve.ParadoxLocalisationIconService
import icu.windea.pls.lang.resolve.ParadoxMemberService
import icu.windea.pls.lang.resolve.inRoot
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.lang.search.util.filterBy
import icu.windea.pls.lang.search.util.preferLocale
import icu.windea.pls.lang.settings.ChronicleInternalSettings
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.lang.util.ParadoxCsvManager
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.lang.util.ParadoxLocalisationFileManager
import icu.windea.pls.lang.util.ParadoxScopeManager
import icu.windea.pls.localisation.ParadoxLocalisationFileType
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxLocalisationType
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.model.paths.ParadoxMemberPath
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

object ParadoxCompletionManager {
    // region Entry Completion Methods

    fun addKeyCompletions(context: ParadoxCompletionContext, result: CompletionResultSet, memberElement: ParadoxScriptMember) {
        val configContext = ParadoxConfigManager.getConfigContext(memberElement) ?: return

        // 仅提示不在定义声明中的 key（顶级键和类型键）
        if (!configContext.inRoot()) {
            // 忽略 rootKeys 深度超出限制，或者带参数的情况
            val maxDepth = ChronicleInternalSettings.getInstance().maxDefinitionDepth
            val memberPath = ParadoxMemberService.getPath(memberElement, maxDepth = maxDepth, parameterAware = false) ?: return
            val typeKeyPrefix = lazy { ParadoxMemberService.getKeyPrefix(context.contextElement) }
            val context = context.copy(isKey = true)
            completeKey(context, result, memberPath, typeKeyPrefix)
            return
        }

        // 这里不要使用合并后的子规则，需要先尝试精确匹配或者合并所有非精确匹配的规则，最后得到子规则列表
        val parentConfigs = ParadoxConfigManager.getConfigs(memberElement, ParadoxMatchOptions(forDeclarationRoot = true, lenient = true))
        val configs = mutableListOf<CwtPropertyConfig>()
        parentConfigs.forEach { c1 ->
            c1.configs?.forEach { c2 ->
                if (c2 is CwtPropertyConfig) {
                    configs += CwtConfigManipulationService.inlineSingleAlias(c2) ?: c2 // 这里需要进行必要的内联
                }
            }
        }
        if (configs.isEmpty()) return
        val occurrences = ParadoxConfigManager.getChildOccurrences(memberElement, parentConfigs)

        val scopeContext = ParadoxScopeManager.getScopeContext(memberElement)
        val context = context.copy(isKey = true, scopeContext = scopeContext)
        configs.groupBy { it.key }.forEach { (_, configsWithSameKey) ->
            for (config in configsWithSameKey) {
                if (shouldComplete(config, occurrences)) {
                    val overriddenConfigs = ParadoxConfigService.getOverriddenConfigs(context.contextElement, config)
                    if (overriddenConfigs.isNotEmpty()) {
                        for (overriddenConfig in overriddenConfigs) {
                            val context = context.copy(config = overriddenConfig)
                            ParadoxExpressionCompletionManager.completeScriptExpression(context, result)
                        }
                        continue
                    }
                    val context = context.copy(config = config, configs = configsWithSameKey)
                    ParadoxExpressionCompletionManager.completeScriptExpression(context, result)
                }
            }
        }
    }

    fun addValueCompletions(context: ParadoxCompletionContext, result: CompletionResultSet, memberElement: ParadoxScriptMember) {
        val configContext = ParadoxConfigManager.getConfigContext(memberElement) ?: return

        if (!configContext.inRoot()) return

        // 这里不要使用合并后的子规则，需要先尝试精确匹配或者合并所有非精确匹配的规则，最后得到子规则列表
        val parentConfigs = ParadoxConfigManager.getConfigs(memberElement, ParadoxMatchOptions(forDeclarationRoot = true, lenient = true))
        val configs = mutableListOf<CwtValueConfig>()
        parentConfigs.forEach { c1 ->
            c1.configs?.forEach { c2 ->
                if (c2 is CwtValueConfig) {
                    configs += c2
                }
            }
        }
        if (configs.isEmpty()) return
        val occurrences = ParadoxConfigManager.getChildOccurrences(memberElement, parentConfigs)

        val scopeContext = ParadoxScopeManager.getScopeContext(memberElement)
        val context = context.copy(isKey = false, scopeContext = scopeContext)
        for (config in configs) {
            if (shouldComplete(config, occurrences)) {
                val overriddenConfigs = ParadoxConfigService.getOverriddenConfigs(context.contextElement, config)
                if (overriddenConfigs.isNotEmpty()) {
                    for (overriddenConfig in overriddenConfigs) {
                        val context = context.copy(config = overriddenConfig)
                        ParadoxExpressionCompletionManager.completeScriptExpression(context, result)
                    }
                    continue
                }
                val context = context.copy(config = config)
                ParadoxExpressionCompletionManager.completeScriptExpression(context, result)
            }
        }
    }

    fun addPropertyValueCompletions(context: ParadoxCompletionContext, result: CompletionResultSet, element: ParadoxScriptStringExpressionElement, propertyElement: ParadoxScriptProperty) {
        val configContext = ParadoxConfigManager.getConfigContext(element) ?: return

        if (!configContext.inRoot()) return

        val configs = configContext.getConfigs()
        if (configs.isEmpty()) return

        val scopeContext = ParadoxScopeManager.getScopeContext(propertyElement)
        val context = context.copy(isKey = false, scopeContext = scopeContext)
        for (config in configs) {
            if (config is CwtValueConfig) {
                val context = context.copy(config = config)
                ParadoxExpressionCompletionManager.completeScriptExpression(context, result)
            }
        }
    }

    private fun shouldComplete(config: CwtPropertyConfig, occurrences: Map<CwtDataExpression, ParadoxMatchOccurrence>): Boolean {
        val expression = config.keyExpression
        // 如果类型是 `aliasName`，则无论 `cardinality` 如何定义，都应该提供补全（某些规则文件未正确编写）
        if (expression.type == CwtDataTypes.AliasName) return true
        val actualCount = occurrences[expression]?.actual ?: 0
        // 如果写明了 `cardinality`，则为 `cardinality.max` ，否则如果类型为常量，则为1，否则为 `null`，`null` 表示没有限制
        // 如果上限是动态的值（如，基于 `define` 的值），也不作限制
        val cardinality = config.optionData.cardinality
        val maxCount = when {
            cardinality == null -> if (expression.type == CwtDataTypes.Constant) 1 else null
            config.optionData.cardinalityMaxDefine != null -> null
            else -> cardinality.max
        }
        return maxCount == null || actualCount < maxCount
    }

    private fun shouldComplete(config: CwtValueConfig, occurrences: Map<CwtDataExpression, ParadoxMatchOccurrence>): Boolean {
        val expression = config.valueExpression
        val actualCount = occurrences[expression]?.actual ?: 0
        // 如果写明了 `cardinality`，则为 `cardinality.max`，否则如果类型为常量，则为1，否则为 `null`，`null` 表示没有限制
        // 如果上限是动态的值（如，基于 `define` 的值），也不作限制
        val cardinality = config.optionData.cardinality
        val maxCount = when {
            cardinality == null -> if (expression.type == CwtDataTypes.Constant) 1 else null
            config.optionData.cardinalityMaxDefine != null -> null
            else -> cardinality.max
        }
        return maxCount == null || actualCount < maxCount
    }

    fun addColumnCompletions(context: ParadoxCompletionContext, result: CompletionResultSet, columnElement: ParadoxCsvColumn) {
        if (context.file !is ParadoxCsvFile) return

        if (ParadoxCsvPsiService.isHeaderColumn(columnElement)) {
            ProgressManager.checkCanceled()
            completeHeaderColumn(context, result)
            return
        }

        val columnConfig = ParadoxCsvManager.getColumnConfig(columnElement) ?: return
        val config = columnConfig.valueConfig ?: return
        val context = context.copy(isKey = null, config = config)
        completeColumn(context, result)
    }

    // endregion

    // region General Completion Methods

    fun completeKey(context: ParadoxCompletionContext, result: CompletionResultSet, memberPath: ParadoxMemberPath, rootKeyPrefix: Lazy<String?>) {
        // 从以下来源收集需要提示的键（顶级键和类型键）
        // - skip_root_key
        // - type_key_filter
        // - type_key_prefix

        val fileInfo = context.file.fileInfo ?: return
        val path = fileInfo.path

        val typeConfigs = context.configGroup.types.values
        val infoMapForTag = mutableMapOf<String, MutableList<CwtTypeConfig>>()
        val infoMapForKey = mutableMapOf<String, MutableList<Tuple2<CwtTypeConfig, CwtSubtypeConfig?>>>()
        for (typeConfig in typeConfigs) {
            run {
                val typeKeyPrefix = typeConfig.typeKeyPrefix
                if (typeKeyPrefix == null) return@run
                if (rootKeyPrefix.value != null) return@run // avoid complete prefix again after an existing prefix
                val matchContext = CwtTypeConfigMatchContext(context.configGroup, path)
                if (!ParadoxConfigMatchService.matchesTypeByUnknownDeclaration(matchContext, typeConfig)) return@run
                infoMapForTag.getOrPut(typeKeyPrefix) { mutableListOf() }.add(typeConfig)
            }

            val matchContext = CwtTypeConfigMatchContext(context.configGroup, path, typeKeyPrefix = rootKeyPrefix)
            if (!ParadoxConfigMatchService.matchesTypeByUnknownDeclaration(matchContext, typeConfig)) continue
            val skipRootKeyConfig = typeConfig.skipRootKey
            if (skipRootKeyConfig.isEmpty()) {
                if (memberPath.isEmpty()) {
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
                    val relative = PathMatcher.relative(memberPath.subPaths, skipConfig, ignoreCase = true, usePattern = true, useAny = true) ?: continue
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
            val icon = ChronicleIcons.Nodes.Tag
            val tailText = typeConfigsToUse.joinToString(", ", " for ") { typeConfig ->
                typeConfig.name
            }
            val typeFile = config.pointer.containingFile
            val context = context.copy(isKey = false, config = config)
            val lookupElement = LookupElementBuilder.create(key).withPsiElement(element)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withCaseSensitivity(false)
                .withPatchableIcon(icon)
                .withPatchableTailText(tailText)
                .withPriority(ChronicleCompletionPriorities.rootKey)
                .forExpression(context)
            result.addElement(lookupElement, context)
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
            val config = when {
                typeToUse == null -> null
                typeConfigToUse.typeKeyPrefix != null -> typeConfigToUse.typeKeyPrefixConfig
                else -> ParadoxDefinitionService.resolveDeclaration(context.contextElement, typeToUse, subtypesToUse, context.configGroup)
            }
            val element = config?.pointer?.element
            val icon = if (config == null) ChronicleIcons.Nodes.Property else ChronicleIcons.Nodes.Definition(typeToUse)
            val tailText = if (tuples.isEmpty()) null else tuples.joinToString(", ", " for ") { (typeConfig, subTypeConfig) ->
                if (subTypeConfig == null) typeConfig.name else "${typeConfig.name}.${subTypeConfig.name}"
            }
            val typeFile = config?.pointer?.containingFile
            val context = context.copy(config = config)
            val lookupElement = LookupElementBuilder.create(key).withPsiElement(element)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withCaseSensitivity(false)
                .withPatchableIcon(icon)
                .withPatchableTailText(tailText)
                .withForceInsertCurlyBraces(tuples.isEmpty())
                .withPriority(ChronicleCompletionPriorities.rootKey)
                .forExpression(context)
            result.addElement(lookupElement, context)
        }
    }

    fun completeInlineScriptUsage(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val configGroup = context.configGroup
        val configs = configGroup.macrosModel.forInlineScripts.orNull() ?: return
        for (config in configs) {
            ProgressManager.checkCanceled()
            val context = context.copy(config = config, isKey = true)
            val name = config.name
            val element = config.pointer.element ?: continue
            val typeFile = config.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withIcon(ChronicleIcons.Nodes.Macro)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withCaseSensitivity(false)
                .withPriority(ChronicleCompletionPriorities.constant)
                .forExpression(context)
            result.addElement(lookupElement, context)
        }
    }

    fun completeDefinitionInjectionExpression(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val configGroup = context.configGroup
        val config = configGroup.macrosModel.forDefinitionInjections ?: return
        val element = context.contextElement.castOrNull<ParadoxScriptStringExpressionElement>() ?: return
        val fileInfo = context.file.fileInfo ?: return
        val path = fileInfo.path
        val matchContext = CwtTypeConfigMatchContext(configGroup, path)
        val typeConfig = ParadoxConfigMatchService.getMatchedTypeConfigForInjection(matchContext) ?: return
        val index = context.keyword.indexOf(':')
        if (index == -1) {
            // complete mode
            completeDefinitionInjectionMode(context, result, config)
        } else {
            // complete definition reference
            ProgressManager.checkCanceled()
            val keywordToUse = context.keyword.substring(index + 1)
            val resultToUse = result.withPrefixMatcher(keywordToUse)
            val type = typeConfig.name
            val config = ParadoxDefinitionService.resolveDeclaration(element, type, configGroup = configGroup)
            val context = context.copy(config = config, isKey = true, patchableTailText = "", keyword = keywordToUse)

            val selector = ParadoxDefinitionSearch.selector(context.project, context.file).contextSensitive().distinct()
            ParadoxDefinitionSearch.searchProperty(null, type, selector).processAsync {
                ParadoxCompletionUtil.processDefinition(context, resultToUse, it)
            }

            ParadoxExtendedCompletionManager.completeExtendedDefinition(context, resultToUse)
        }
    }

    fun completeDefinitionInjectionMode(context: ParadoxCompletionContext, result: CompletionResultSet, config: CwtMacroConfig.DefinitionInjection) {
        ProgressManager.checkCanceled()
        val context = context.copy(isKey = null)
        val modeConfigs = config.modeConfigs.values
        val tailText = " from definition injection modes"
        for (modeConfig in modeConfigs) {
            val context = context.copy(config = modeConfig)
            val name = modeConfig.value
            val element = modeConfig.pointer.element ?: continue
            val typeFile = modeConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withBoldness(true)
                .withIcon(ChronicleIcons.Nodes.Macro)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withCaseSensitivity(false)
                .withInsertHandler(ChronicleInsertHandlers.addColon())
                .withPriority(ChronicleCompletionPriorities.macro)
                .withPatchableTailText(tailText)
                .forExpression(context)
            result.addElement(lookupElement, context)
        }
    }

    fun completeLocale(context: ParadoxCompletionContext, result: CompletionResultSet) {
        val localeIdFromFileName = context.file.castOrNull<ParadoxLocalisationFile>()?.let { ParadoxLocalisationFileManager.getLocaleIdFromFileName(it) }

        // 批量提示
        val lookupElements = mutableSetOf<LookupElement>()
        val locales = context.configGroup.supportedLocales
        for (locale in locales) {
            ProgressManager.checkCanceled()
            val element = locale.pointer.element ?: continue
            val typeFile = locale.pointer.containingFile
            val matched = localeIdFromFileName?.let { it == locale.id }
            val lookupElement = LookupElementBuilder.create(element, locale.id)
                .withIcon(ChronicleIcons.Nodes.LocalisationLocale)
                .withTailText(" " + locale.text) // 前面需要加一个空格
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withInsertHandler(localisationLocaleInsertHandler)
                .letIf(matched == false) {
                    it.withItemTextForeground(JBColor.GRAY) // 将不匹配的语言环境的提示项置灰
                }
                .letIf(matched == true) {
                    it.withPriority(ChronicleCompletionPriorities.pinned) // 优先提示与文件名匹配的语言环境
                }
            lookupElements.add(lookupElement)
        }
        result.addAllElements(lookupElements)
    }

    fun completeLocalisationName(context: ParadoxCompletionContext, result: CompletionResultSet) {
        val file = context.file as? ParadoxLocalisationFile ?: return
        val type = ParadoxLocalisationType.resolve(file) ?: return

        // 本地化的提示结果可能有上千条，因此这里改为先按照输入的关键字过滤结果，关键字变更时重新提示
        result.restartCompletionOnAnyPrefixChange()

        // 提示 `localisation` 或者 `synced_localisation`
        val selector = ParadoxLocalisationSearch.selector(context.project, file)
            .contextSensitive()
            .preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
            .filterBy { it.name != context.keyword } // skip if name = input
        val processor = LimitedCompletionProcessor<ParadoxLocalisationProperty> {
            ProgressManager.checkCanceled()
            val name = it.name
            val icon = it.icon
            val typeFile = it.containingFile
            val lookupElement = LookupElementBuilder.create(it, name)
                .withIcon(icon)
                .withTypeText(typeFile.name, typeFile.icon, true)
            result.addElement(lookupElement)
            true
        }
        // 保证索引在此 readAction 中可用
        runSmartReadAction(context.project, inSmartMode = true) {
            ParadoxLocalisationSearch.processVariants(type, result.prefixMatcher, selector, processor)
        }
    }

    fun completeIcon(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ParadoxLocalisationIconService.complete(context, result)
    }

    fun completeConcept(context: ParadoxCompletionContext, result: CompletionResultSet) {
        val conceptSelector = ParadoxDefinitionSearch.selector(context.project, context.file).contextSensitive().distinct()
        val keysToDistinct = mutableSetOf<String>()
        ParadoxDefinitionSearch.searchProperty(null, ParadoxDefinitionTypes.gameConcept, conceptSelector).processAsync p@{ concept ->
            val tailText = " from concepts"
            val typeFile = concept.containingFile
            val icon = ChronicleIcons.Nodes.LocalisationConceptCommand
            run action@{
                val key = concept.name
                if (key.isEmpty()) return@action
                if (!keysToDistinct.add(key)) return@action
                val lookupElement = LookupElementBuilder.create(concept, key)
                    .withIcon(icon)
                    .withTailText(tailText, true)
                    .withTypeText(typeFile?.name, typeFile?.icon, true)
                    .withCompletionId()
                result.addElement(lookupElement, context)
            }
            concept.getDefinitionData<StellarisGameConceptData>()?.alias?.forEach action@{ alias ->
                val key = alias
                if (key.isEmpty()) return@action
                if (!keysToDistinct.add(key)) return@action
                val lookupElement = LookupElementBuilder.create(concept, key)
                    .withIcon(icon)
                    .withTailText(tailText, true)
                    .withTypeText(typeFile?.name, typeFile?.icon, true)
                    .withCompletionId()
                result.addElement(lookupElement, context)
            }
            true
        }
    }

    fun completeTextFormat(context: ParadoxCompletionContext, result: CompletionResultSet) {
        val definitionType = ParadoxDefinitionTypes.textFormat
        val icon = ChronicleIcons.Nodes.LocalisationTextFormat // 使用特定图标
        val tailText = " from <$definitionType>"
        val definitionSelector = ParadoxDefinitionSearch.selector(context.project, context.file).contextSensitive().distinct()
        ParadoxDefinitionSearch.searchProperty(null, definitionType, definitionSelector).processAsync p@{ definition ->
            ProgressManager.checkCanceled()
            val definitionInfo = definition.definitionInfo ?: return@p true
            val name = definitionInfo.name
            if (name.isEmpty()) return@p true

            val typeFile = definition.containingFile
            val lookupElement = LookupElementBuilder.create(definition, name).withIcon(icon)
                .withTailText(tailText, true)
                .withTypeText(typeFile.name, typeFile.icon, true)
                .withCompletionId()
                .withCaseSensitivity(false)
            result.addElement(lookupElement, context)
            true
        }
    }

    fun completeTextIcon(context: ParadoxCompletionContext, result: CompletionResultSet) {
        val definitionType = ParadoxDefinitionTypes.textIcon
        val icon = ChronicleIcons.Nodes.LocalisationTextIcon // 使用特定图标
        val tailText = " from <$definitionType>"
        val definitionSelector = ParadoxDefinitionSearch.selector(context.project, context.file).contextSensitive().distinct()
        ParadoxDefinitionSearch.searchProperty(null, definitionType, definitionSelector).processAsync p@{ definition ->
            ProgressManager.checkCanceled()
            val definitionInfo = definition.definitionInfo ?: return@p true
            val name = definitionInfo.name
            if (name.isEmpty()) return@p true

            val typeFile = definition.containingFile
            val lookupElement = LookupElementBuilder.create(definition, name).withIcon(icon)
                .withTailText(tailText, true)
                .withTypeText(typeFile.name, typeFile.icon, true)
                .withCompletionId()
                .withCaseSensitivity(false)
            result.addElement(lookupElement, context)
            true
        }
    }

    fun completeHeaderColumn(context: ParadoxCompletionContext, result: CompletionResultSet) {
        val column = context.contextElement.castOrNull<ParadoxCsvColumn>() ?: return
        val header = column.parent?.castOrNull<ParadoxCsvHeader>() ?: return
        val rowConfig = ParadoxCsvManager.getRowConfig(header) ?: return
        val columnConfigs = rowConfig.columns
        if (columnConfigs.isEmpty()) return
        // #134 updated logic to get expected column configs
        val expectedColumnConfigs = when (rowConfig.type) {
            CwtRowType.Key -> {
                val columnNames = ParadoxCsvPsiService.getColumnNames(header)
                columnConfigs.filter { it.key !in columnNames }
            }
            CwtRowType.Index -> {
                val columnIndex = ParadoxCsvPsiService.getColumnIndex(column)
                columnConfigs.getOrNull(columnIndex).to.singletonListOrEmpty()
            }
        }
        for (columnConfig in expectedColumnConfigs) {
            ProgressManager.checkCanceled()
            val context = context.copy(config = columnConfig)
            val name = columnConfig.key
            val element = columnConfig.pointer.element ?: continue
            val typeFile = columnConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withIcon(ChronicleIcons.Nodes.Column)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withPriority(ChronicleCompletionPriorities.constant)
            result.addElement(lookupElement, context)
        }
    }

    fun completeColumn(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ParadoxExpressionCompletionManager.completeCsvExpression(context, result)
    }

    // endregion

    // region Insert Handlers

    private val localisationLocaleInsertHandler = InsertHandler<LookupElement> { context, _ ->
        // 如果之后没有英文冒号，则插入英文冒号（如果之后没有更多行，则还要插入换行符和必要的缩进），否则光标移到冒号之后
        val editor = context.editor
        val chars = editor.document.charsSequence
        val colonIndex = chars.indexOf(':', context.startOffset)
        if (colonIndex != -1) {
            editor.caretModel.moveToOffset(colonIndex + 1)
        } else {
            val settings = CodeStyle.getSettings(context.file)
            val indentOptions = settings.getIndentOptions(ParadoxLocalisationFileType)
            val insertLineBreak = editor.document.getLineNumber(editor.caretModel.offset) == editor.document.lineCount - 1
            val s = buildString {
                append(":")
                if (insertLineBreak) {
                    append("\n")
                    repeat(indentOptions.INDENT_SIZE) { append(" ") }
                }
            }
            EditorModificationUtil.insertStringAtCaret(editor, s)
        }
    }

    // endregion
}
