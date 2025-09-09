package icu.windea.pls.lang.codeInsight.completion

import com.intellij.application.options.CodeStyle
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.editor.EditorModificationUtil
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.TextRange
import com.intellij.patterns.StandardPatterns
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import icu.windea.pls.PlsFacade
import icu.windea.pls.PlsIcons
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.cardinality
import icu.windea.pls.config.config.cardinalityMaxDefine
import icu.windea.pls.config.config.delegated.CwtAliasConfig
import icu.windea.pls.config.config.delegated.CwtLinkConfig
import icu.windea.pls.config.config.delegated.CwtSubtypeConfig
import icu.windea.pls.config.config.delegated.CwtTypeConfig
import icu.windea.pls.config.config.supportedScopes
import icu.windea.pls.config.configContext.isDefinitionOrMember
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configExpression.value
import icu.windea.pls.config.configGroup.aliasGroups
import icu.windea.pls.config.configGroup.complexEnums
import icu.windea.pls.config.configGroup.databaseObjectTypes
import icu.windea.pls.config.configGroup.declarations
import icu.windea.pls.config.configGroup.dynamicValueTypes
import icu.windea.pls.config.configGroup.enums
import icu.windea.pls.config.configGroup.extendedComplexEnumValues
import icu.windea.pls.config.configGroup.extendedDefinitions
import icu.windea.pls.config.configGroup.extendedDynamicValues
import icu.windea.pls.config.configGroup.extendedGameRules
import icu.windea.pls.config.configGroup.extendedInlineScripts
import icu.windea.pls.config.configGroup.extendedOnActions
import icu.windea.pls.config.configGroup.extendedParameters
import icu.windea.pls.config.configGroup.extendedScriptedVariables
import icu.windea.pls.config.configGroup.inlineConfigGroup
import icu.windea.pls.config.configGroup.links
import icu.windea.pls.config.configGroup.linksOfVariable
import icu.windea.pls.config.configGroup.localisationCommands
import icu.windea.pls.config.configGroup.localisationLinks
import icu.windea.pls.config.configGroup.systemScopes
import icu.windea.pls.config.configGroup.types
import icu.windea.pls.config.matchFromPattern
import icu.windea.pls.config.resolved
import icu.windea.pls.config.sortedByPriority
import icu.windea.pls.config.util.CwtConfigManipulator
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.children
import icu.windea.pls.core.codeInsight.LimitedCompletionProcessor
import icu.windea.pls.core.collections.filterIsInstance
import icu.windea.pls.core.collections.findIsInstance
import icu.windea.pls.core.collections.synced
import icu.windea.pls.core.collections.toListOrThis
import icu.windea.pls.core.emptyPointer
import icu.windea.pls.core.icon
import icu.windea.pls.core.isNotNullOrEmpty
import icu.windea.pls.core.processQueryAsync
import icu.windea.pls.core.toPsiFile
import icu.windea.pls.core.util.Matchers
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
import icu.windea.pls.lang.expression.ParadoxCommandExpression
import icu.windea.pls.lang.expression.ParadoxDatabaseObjectExpression
import icu.windea.pls.lang.expression.ParadoxDefineReferenceExpression
import icu.windea.pls.lang.expression.ParadoxDefinitionTypeExpression
import icu.windea.pls.lang.expression.ParadoxDynamicValueExpression
import icu.windea.pls.lang.expression.ParadoxScopeFieldExpression
import icu.windea.pls.lang.expression.ParadoxScriptValueExpression
import icu.windea.pls.lang.expression.ParadoxTemplateExpression
import icu.windea.pls.lang.expression.ParadoxValueFieldExpression
import icu.windea.pls.lang.expression.ParadoxVariableFieldExpression
import icu.windea.pls.lang.expression.nodes.ParadoxCommandFieldNode
import icu.windea.pls.lang.expression.nodes.ParadoxCommandScopeLinkNode
import icu.windea.pls.lang.expression.nodes.ParadoxComplexExpressionNode
import icu.windea.pls.lang.expression.nodes.ParadoxDataSourceNode
import icu.windea.pls.lang.expression.nodes.ParadoxDatabaseObjectDataSourceNode
import icu.windea.pls.lang.expression.nodes.ParadoxDatabaseObjectNode
import icu.windea.pls.lang.expression.nodes.ParadoxDatabaseObjectTypeNode
import icu.windea.pls.lang.expression.nodes.ParadoxDefineNamespaceNode
import icu.windea.pls.lang.expression.nodes.ParadoxDefineVariableNode
import icu.windea.pls.lang.expression.nodes.ParadoxDynamicCommandFieldNode
import icu.windea.pls.lang.expression.nodes.ParadoxDynamicCommandScopeLinkNode
import icu.windea.pls.lang.expression.nodes.ParadoxDynamicScopeLinkNode
import icu.windea.pls.lang.expression.nodes.ParadoxDynamicValueFieldNode
import icu.windea.pls.lang.expression.nodes.ParadoxDynamicValueNode
import icu.windea.pls.lang.expression.nodes.ParadoxErrorNode
import icu.windea.pls.lang.expression.nodes.ParadoxScopeLinkNode
import icu.windea.pls.lang.expression.nodes.ParadoxScriptValueArgumentNode
import icu.windea.pls.lang.expression.nodes.ParadoxScriptValueArgumentValueNode
import icu.windea.pls.lang.expression.nodes.ParadoxScriptValueNode
import icu.windea.pls.lang.expression.nodes.ParadoxTemplateSnippetConstantNode
import icu.windea.pls.lang.expression.nodes.ParadoxTemplateSnippetNode
import icu.windea.pls.lang.expression.nodes.ParadoxValueFieldNode
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.psi.mock.ParadoxComplexEnumValueElement
import icu.windea.pls.lang.psi.mock.ParadoxDynamicValueElement
import icu.windea.pls.lang.search.ParadoxComplexEnumValueSearch
import icu.windea.pls.lang.search.ParadoxDefineSearch
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.ParadoxDynamicValueSearch
import icu.windea.pls.lang.search.ParadoxFilePathSearch
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.ParadoxSyncedLocalisationSearch
import icu.windea.pls.lang.search.selector.complexEnumValue
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.define
import icu.windea.pls.lang.search.selector.definition
import icu.windea.pls.lang.search.selector.distinctByExpression
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
import icu.windea.pls.lang.util.ParadoxCsvManager
import icu.windea.pls.lang.util.ParadoxDefineManager
import icu.windea.pls.lang.util.ParadoxDefinitionManager
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.ParadoxExpressionMatcher.Options
import icu.windea.pls.lang.util.ParadoxExpressionPathManager
import icu.windea.pls.lang.util.ParadoxFileManager
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.lang.util.ParadoxModifierManager
import icu.windea.pls.lang.util.ParadoxParameterManager
import icu.windea.pls.lang.util.ParadoxScopeManager
import icu.windea.pls.lang.util.PlsCoreManager
import icu.windea.pls.lang.withState
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.Occurrence
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.model.constants.PlsPatternConstants
import icu.windea.pls.model.paths.ParadoxExpressionPath
import icu.windea.pls.script.codeStyle.ParadoxScriptCodeStyleSettings
import icu.windea.pls.script.psi.ParadoxScriptMemberElement
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

object ParadoxCompletionManager {
    //region Predefined Lookup Elements

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

    //endregion

    //region Core Methods

    fun initializeContext(parameters: CompletionParameters, context: ProcessingContext) {
        context.parameters = parameters
        context.completionIds = mutableSetOf<String>().synced()

        val gameType = selectGameType(parameters.originalFile)
        context.gameType = gameType

        val project = parameters.originalFile.project
        val configGroup = PlsFacade.getConfigGroup(project, gameType)
        context.configGroup = configGroup
    }

    fun addRootKeyCompletions(memberElement: ParadoxScriptMemberElement, context: ProcessingContext, result: CompletionResultSet) {
        val elementPath = ParadoxExpressionPathManager.get(memberElement, PlsFacade.getInternalSettings().maxDefinitionDepth) ?: return
        if (elementPath.path.isParameterized()) return //忽略表达式路径带参数的情况
        val rootKeyPrefix = lazy { context.contextElement?.let { ParadoxExpressionPathManager.getKeyPrefixes(it).firstOrNull() } }
        context.isKey = true
        completeRootKey(context, result, elementPath, rootKeyPrefix)
    }

    fun addKeyCompletions(memberElement: ParadoxScriptMemberElement, context: ProcessingContext, result: CompletionResultSet) {
        val configContext = ParadoxExpressionManager.getConfigContext(memberElement)
        if (configContext == null) return

        //仅提示不在定义声明中的rootKey
        if (!configContext.isDefinitionOrMember()) return addRootKeyCompletions(memberElement, context, result)

        //这里不要使用合并后的子规则，需要先尝试精确匹配或者合并所有非精确匹配的规则，最后得到子规则列表
        val matchOptions = Options.Default or Options.Relax or Options.AcceptDefinition
        val parentConfigs = ParadoxExpressionManager.getConfigs(memberElement, matchOptions = matchOptions)
        val configs = mutableListOf<CwtPropertyConfig>()
        parentConfigs.forEach { c1 ->
            c1.configs?.forEach { c2 ->
                if (c2 is CwtPropertyConfig) {
                    configs += CwtConfigManipulator.inlineSingleAlias(c2) ?: c2 //这里需要进行必要的内联
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
                    if (overriddenConfigs.isNotNullOrEmpty()) {
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

    fun addValueCompletions(memberElement: ParadoxScriptMemberElement, context: ProcessingContext, result: CompletionResultSet) {
        val configContext = ParadoxExpressionManager.getConfigContext(memberElement)
        if (configContext == null) return

        if (!configContext.isDefinitionOrMember()) return

        //这里不要使用合并后的子规则，需要先尝试精确匹配或者合并所有非精确匹配的规则，最后得到子规则列表
        val matchOptions = Options.Default or Options.Relax or Options.AcceptDefinition
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
                if (overriddenConfigs.isNotNullOrEmpty()) {
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

        val columnConfig = ParadoxCsvManager.getColumnConfig(columnElement) ?: return
        val config = columnConfig.valueConfig ?: return
        context.config = config
        completeCsvExpression(context, result)
    }

    private fun shouldComplete(config: CwtPropertyConfig, occurrenceMap: Map<CwtDataExpression, Occurrence>): Boolean {
        val expression = config.keyExpression
        //如果类型是aliasName，则无论cardinality如何定义，都应该提供补全（某些cwt规则文件未正确编写）
        if (expression.type == CwtDataTypes.AliasName) return true
        val actualCount = occurrenceMap[expression]?.actual ?: 0
        //如果写明了cardinality，则为cardinality.max，否则如果类型为常量，则为1，否则为null，null表示没有限制
        //如果上限是动态的值（如，基于define的值），也不作限制
        val cardinality = config.cardinality
        val maxCount = when {
            cardinality == null -> if (expression.type == CwtDataTypes.Constant) 1 else null
            config.cardinalityMaxDefine != null -> null
            else -> cardinality.max
        }
        return maxCount == null || actualCount < maxCount
    }

    private fun shouldComplete(config: CwtValueConfig, occurrenceMap: Map<CwtDataExpression, Occurrence>): Boolean {
        val expression = config.valueExpression
        val actualCount = occurrenceMap[expression]?.actual ?: 0
        //如果写明了cardinality，则为cardinality.max，否则如果类型为常量，则为1，否则为null，null表示没有限制
        //如果上限是动态的值（如，基于define的值），也不作限制
        val cardinality = config.cardinality
        val maxCount = when {
            cardinality == null -> if (expression.type == CwtDataTypes.Constant) 1 else null
            config.cardinalityMaxDefine != null -> null
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

    //endregion

    //region Base Completion Methods

    fun completeRootKey(context: ProcessingContext, result: CompletionResultSet, elementPath: ParadoxExpressionPath, rootKeyPrefix: Lazy<String?>) {
        //从以下来源收集需要提示的key（不仅仅是特定类型的定义的rootKey）
        //* skip_root_key
        //* type_key_filter
        //* type_key_prefix
        val originalFile = context.parameters?.originalFile ?: return
        val fileInfo = originalFile.fileInfo ?: return
        val gameType = context.gameType ?: return
        val configGroup = context.configGroup ?: return
        val path = fileInfo.path

        val typeConfigs = configGroup.types.values
        val infoMapForTag = mutableMapOf<String, MutableList<CwtTypeConfig>>()
        val infoMapForKey = mutableMapOf<String, MutableList<Tuple2<CwtTypeConfig, CwtSubtypeConfig?>>>()
        for (typeConfig in typeConfigs) {
            run {
                val typeKeyPrefix = typeConfig.typeKeyPrefix
                if (typeKeyPrefix == null) return@run
                if (rootKeyPrefix.value != null) return@run //avoid complete prefix again after an existing prefix
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
                    val relative = Matchers.PathMatcher.relative(elementPath.subPaths, skipConfig, true, true, true) ?: continue
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
            if (key == "any") return //skip any wildcard
            val typeConfigToUse = tuples.map { it.first }.distinctBy { it.name }.singleOrNull()
            val typeToUse = typeConfigToUse?.name
            //需要考虑不指定子类型的情况
            val subtypesToUse = when {
                typeConfigToUse == null || tuples.isEmpty() -> null
                else -> tuples.mapNotNull { it.second }.ifEmpty { null }?.distinctBy { it.name }?.map { it.name }
            }
            val config = run {
                if (typeToUse == null) return@run null
                if (typeConfigToUse.typeKeyPrefix != null) return@run typeConfigToUse.typeKeyPrefixConfig
                val declarationConfig = configGroup.declarations.get(typeToUse) ?: return@run null
                val declarationConfigContext = CwtDeclarationConfigContextProvider.getContext(context.contextElement!!, null, typeToUse, subtypesToUse, gameType, configGroup)
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

        //匹配作用域
        if (scopeMatched) {
            val supportedScopes = when {
                config is CwtPropertyConfig -> config.supportedScopes
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

        //优化：如果已经输入的关键词不是合法的本地化的名字，不要尝试进行本地化的代码补全
        if (keyword.isNotEmpty() && !PlsPatternConstants.localisationName.matches(keyword)) return

        //本地化的提示结果可能有上千条，因此这里改为先按照输入的关键字过滤结果，关键字变更时重新提示
        result.restartCompletionOnPrefixChange(StandardPatterns.string().shorterThan(keyword.length))

        val configGroup = config.configGroup
        val project = configGroup.project
        val contextElement = context.contextElement
        val tailText = getExpressionTailText(context, config)
        val selector = selector(project, contextElement).localisation()
            .contextSensitive()
            .preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
        //.distinctByName() //这里selector不需要指定去重
        val processor = LimitedCompletionProcessor<ParadoxLocalisationProperty> p@{ localisation ->
            //apply extraFilter since it's necessary
            if (context.extraFilter?.invoke(localisation) == false) return@p true

            val name = localisation.name //=localisation.paradoxLocalisationInfo?.name
            val typeFile = localisation.containingFile
            val lookupElement = LookupElementBuilder.create(localisation, name)
                .withTypeText(typeFile.name, typeFile.icon, true)
                .withPatchableIcon(PlsIcons.Nodes.Localisation)
                .withPatchableTailText(tailText)
                .forScriptExpression(context)
            result.addElement(lookupElement, context)
            true
        }
        //保证索引在此readAction中可用
        ReadAction.nonBlocking<Unit> {
            ParadoxLocalisationSearch.processVariants(result.prefixMatcher, selector, processor)
        }.inSmartMode(project).executeSynchronously()
    }

    fun completeSyncedLocalisation(context: ProcessingContext, result: CompletionResultSet) {
        val config = context.config ?: return
        val keyword = context.keyword

        //优化：如果已经输入的关键词不是合法的本地化的名字，不要尝试进行本地化的代码补全
        if (keyword.isNotEmpty() && !PlsPatternConstants.localisationName.matches(keyword)) return

        //本地化的提示结果可能有上千条，因此这里改为先按照输入的关键字过滤结果，关键字变更时重新提示
        result.restartCompletionOnPrefixChange(StandardPatterns.string().shorterThan(keyword.length))

        val configGroup = config.configGroup
        val project = configGroup.project
        val contextElement = context.contextElement
        val tailText = getExpressionTailText(context, config)
        val selector = selector(project, contextElement).localisation()
            .contextSensitive()
            .preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
        //.distinctByName() //这里selector不需要指定去重
        val processor = LimitedCompletionProcessor<ParadoxLocalisationProperty> p@{ syncedLocalisation ->
            //apply extraFilter since it's necessary
            if (context.extraFilter?.invoke(syncedLocalisation) == false) return@p true

            val name = syncedLocalisation.name //=localisation.paradoxLocalisationInfo?.name
            val typeFile = syncedLocalisation.containingFile
            val lookupElement = LookupElementBuilder.create(syncedLocalisation, name)
                .withTypeText(typeFile.name, typeFile.icon, true)
                .withPatchableIcon(PlsIcons.Nodes.Localisation)
                .withPatchableTailText(tailText)
                .forScriptExpression(context)
            result.addElement(lookupElement, context)
            true
        }
        //保证索引在此readAction中可用
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
        ParadoxDefinitionSearch.search(typeExpression, selector).processQueryAsync p@{ definition ->
            ProgressManager.checkCanceled()
            val definitionInfo = definition.definitionInfo ?: return@p true
            if (definitionInfo.name.isEmpty()) return@p true //ignore anonymous definitions

            //apply extraFilter since it's necessary
            if (context.extraFilter?.invoke(definition) == false) return@p true

            //排除不匹配可能存在的supported_scopes的情况
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

        completeExtendedDefinition(context, result)
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
                is CwtMemberConfig<*> -> ParadoxFileManager.getFileExtensionOptionValues(config)
                else -> emptySet()
            }
            //仅提示匹配file_extensions选项指定的扩展名的，如果存在
            val selector = selector(project, contextElement).file().contextSensitive()
                .withFileExtensions(fileExtensions)
                .distinctByFilePath()
            ParadoxFilePathSearch.search(configExpression, selector).processQueryAsync p@{ virtualFile ->
                ProgressManager.checkCanceled()
                val file = virtualFile.toPsiFile(project) ?: return@p true
                val filePath = virtualFile.fileInfo?.path?.path ?: return@p true
                val name = pathReferenceExpressionSupport.extract(configExpression, contextFile, filePath) ?: return@p true
                val icon = when {
                    ParadoxInlineScriptManager.isInlineScriptExpressionConfig(config) -> PlsIcons.Nodes.InlineScript
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

            if (ParadoxInlineScriptManager.isInlineScriptExpressionConfig(config)) {
                completeExtendedInlineScript(context, result)
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
        //提示简单枚举
        val enumConfig = configGroup.enums[enumName]
        if (enumConfig != null) {
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
        //提示复杂枚举
        val complexEnumConfig = configGroup.complexEnums[enumName]
        if (complexEnumConfig != null) {
            ProgressManager.checkCanceled()
            val typeFile = complexEnumConfig.pointer.containingFile
            val searchScope = complexEnumConfig.searchScopeType
            val selector = selector(project, contextElement).complexEnumValue()
                .withSearchScopeType(searchScope)
                .contextSensitive()
                .distinctByName()
            ParadoxComplexEnumValueSearch.search(enumName, selector).processQueryAsync { info ->
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

            completeExtendedComplexEnumValue(context, result)
        }
    }

    fun completeModifier(context: ProcessingContext, result: CompletionResultSet) {
        ParadoxModifierManager.completeModifier(context, result)
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
            //常量的值也可能是yes/no
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

    fun completeParameter(context: ProcessingContext, result: CompletionResultSet) {
        val config = context.config ?: return
        //提示参数名（仅限key）
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
        val rowConfig = ParadoxCsvManager.getRowConfig(file) ?: return
        val header = column.parent?.castOrNull<ParadoxCsvHeader>() ?: return
        val existingHeaderNames = header.children()
            .mapNotNull { it as? ParadoxCsvColumn }
            .filterIsInstance<ParadoxCsvColumn> { it != column }
            .map { it.value }
            .toSet()
        val columnConfigs = rowConfig.columns.filterNot { it.key in existingHeaderNames }.values
        if(columnConfigs.isEmpty()) return
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

    //endregion

    //region Complex Expression Completion Methods

    inline fun <T> markIncomplete(action: () -> T): T {
        return withState(PlsCoreManager.incompleteComplexExpression, action)
    }

    fun completeTemplateExpression(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()

        val offset = context.offsetInParent - context.expressionOffset
        if (offset < 0) return //unexpected

        val keyword = context.keyword
        val keywordOffset = context.keywordOffset
        val configGroup = context.configGroup ?: return
        val config = context.config
        val configs = context.configs

        val finalConfig = configs.firstOrNull() ?: config
        if (finalConfig == null) return

        val textRange = TextRange.create(keywordOffset, keywordOffset + keyword.length)
        val expression = markIncomplete { ParadoxTemplateExpression.resolve(keyword, textRange, configGroup, finalConfig) } ?: return

        val scopeContext = context.scopeContext ?: ParadoxScopeManager.getAnyScopeContext()
        val isKey = context.isKey
        context.scopeContext = null //skip check scope context here
        context.isKey = null
        for (node in expression.nodes) {
            ProgressManager.checkCanceled()
            val inRange = offset >= node.rangeInExpression.startOffset && offset <= node.rangeInExpression.endOffset
            if (node is ParadoxTemplateSnippetNode) {
                if (inRange) {
                    ProgressManager.checkCanceled()
                    val keywordToUse = node.text.substring(0, offset - node.rangeInExpression.startOffset)
                    val resultToUse = result.withPrefixMatcher(keywordToUse)
                    context.keyword = keywordToUse
                    context.keywordOffset = node.rangeInExpression.startOffset
                    context.config = node.config
                    context.configs = emptyList()
                    completeScriptExpression(context, resultToUse)
                    break
                }
            } else if (node is ParadoxTemplateSnippetConstantNode) {
                if (inRange) {
                    ProgressManager.checkCanceled()
                    //一般来说，仅适用于是第一个节点的情况（否则，仍然会匹配范围内的通配符）
                    val keywordToUse = node.text.substring(0, offset - node.rangeInExpression.startOffset)
                    val resultToUse = result.withPrefixMatcher(keywordToUse)
                    context.keyword = keywordToUse
                    context.keywordOffset = node.rangeInExpression.startOffset
                    context.config = CwtValueConfig.resolve(emptyPointer(), configGroup, node.text)
                    context.configs = emptyList()
                    completeConstant(context, resultToUse)
                    break
                }
            }
        }

        context.keyword = keyword
        context.keywordOffset = keywordOffset
        context.config = config
        context.configs = configs
        context.scopeContext = scopeContext
        context.isKey = isKey
    }

    fun completeDynamicValueExpression(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()

        val offset = context.offsetInParent - context.expressionOffset
        if (offset < 0) return //unexpected

        val keyword = context.keyword
        val keywordOffset = context.keywordOffset
        val configGroup = context.configGroup ?: return
        val config = context.config
        val configs = context.configs

        val finalConfigs = if (configs.isNotEmpty()) configs.toListOrThis() else config.singleton.listOrEmpty()
        if (finalConfigs.isEmpty()) return

        val textRange = TextRange.create(keywordOffset, keywordOffset + keyword.length)
        val expression = markIncomplete { ParadoxDynamicValueExpression.resolve(keyword, textRange, configGroup, finalConfigs) } ?: return

        val scopeContext = context.scopeContext ?: ParadoxScopeManager.getAnyScopeContext()
        val isKey = context.isKey
        context.config = expression.configs.first()
        context.configs = expression.configs
        context.scopeContext = null //skip check scope context here
        context.isKey = null
        for (node in expression.nodes) {
            val inRange = offset >= node.rangeInExpression.startOffset && offset <= node.rangeInExpression.endOffset
            if (node is ParadoxDynamicValueNode) {
                if (inRange) {
                    ProgressManager.checkCanceled()
                    val keywordToUse = node.text.substring(0, offset - node.rangeInExpression.startOffset)
                    val resultToUse = result.withPrefixMatcher(keywordToUse)
                    context.keyword = keywordToUse
                    context.keywordOffset = node.rangeInExpression.startOffset
                    completeDynamicValue(context, resultToUse)
                    break
                }
            } else if (node is ParadoxScopeFieldExpression) {
                if (inRange) {
                    ProgressManager.checkCanceled()
                    val keywordToUse = node.text.substring(0, offset - node.rangeInExpression.startOffset)
                    val resultToUse = result.withPrefixMatcher(keywordToUse)
                    context.keyword = keywordToUse
                    context.keywordOffset = node.rangeInExpression.startOffset
                    completeScopeFieldExpression(context, resultToUse)
                    break
                }
            }
        }

        context.keyword = keyword
        context.keywordOffset = keywordOffset
        context.config = config
        context.configs = configs
        context.scopeContext = scopeContext
        context.isKey = isKey
    }

    fun completeScopeFieldExpression(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()

        val offset = context.offsetInParent - context.expressionOffset
        if (offset < 0) return //unexpected

        val keyword = context.keyword
        val keywordOffset = context.keywordOffset
        val configGroup = context.configGroup ?: return

        val textRange = TextRange.create(keywordOffset, keywordOffset + keyword.length)
        val expression = markIncomplete { ParadoxScopeFieldExpression.resolve(keyword, textRange, configGroup) } ?: return

        val element = context.contextElement?.castOrNull<ParadoxExpressionElement>() ?: return
        val scopeContext = context.scopeContext ?: ParadoxScopeManager.getAnyScopeContext()
        val isKey = context.isKey
        context.isKey = null
        var scopeContextInExpression = scopeContext
        for (node in expression.nodes) {
            val inRange = offset >= node.rangeInExpression.startOffset && offset <= node.rangeInExpression.endOffset
            if (!inRange) {
                if (node is ParadoxErrorNode || node.text.isEmpty()) break //skip error or empty nodes
            }
            if (node is ParadoxScopeLinkNode) {
                if (inRange) {
                    ProgressManager.checkCanceled()
                    context.scopeContext = scopeContextInExpression
                    completeForScopeLinkNode(node, context, result)
                    break
                } else {
                    scopeContextInExpression = ParadoxScopeManager.getSwitchedScopeContextOfNode(element, node, scopeContextInExpression)
                }
            }
        }

        context.keyword = keyword
        context.keywordOffset = keywordOffset
        context.scopeContext = scopeContext
        context.isKey = isKey
    }

    fun completeScriptValueExpression(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()

        val offset = context.offsetInParent - context.expressionOffset
        if (offset < 0) return //unexpected

        val keyword = context.keyword
        val keywordOffset = context.keywordOffset
        val configGroup = context.configGroup ?: return
        val config = context.config ?: return
        val configs = context.configs

        val textRange = TextRange.create(keywordOffset, keywordOffset + keyword.length)
        val expression = markIncomplete { ParadoxScriptValueExpression.resolve(keyword, textRange, configGroup, config) } ?: return

        val element = context.contextElement?.castOrNull<ParadoxExpressionElement>() ?: return
        val scopeContext = context.scopeContext ?: ParadoxScopeManager.getAnyScopeContext()
        val isKey = context.isKey
        context.scopeContext = null //skip check scope context here
        context.isKey = null
        for (node in expression.nodes) {
            val inRange = offset >= node.rangeInExpression.startOffset && offset <= node.rangeInExpression.endOffset
            if (node is ParadoxScriptValueNode) {
                if (inRange) {
                    ProgressManager.checkCanceled()
                    val keywordToUse = node.text.substring(0, offset - node.rangeInExpression.startOffset)
                    val resultToUse = result.withPrefixMatcher(keywordToUse)
                    context.keyword = keywordToUse
                    context.keywordOffset = node.rangeInExpression.startOffset
                    context.config = expression.config
                    context.configs = emptyList()
                    completeScriptExpression(context, resultToUse)
                    context.config = config
                    context.configs = configs
                }
            } else if (node is ParadoxScriptValueArgumentNode) {
                if (inRange && expression.scriptValueNode.text.isNotEmpty()) {
                    val keywordToUse = node.text.substring(0, offset - node.rangeInExpression.startOffset)
                    val resultToUse = result.withPrefixMatcher(keywordToUse)
                    context.keyword = keywordToUse
                    context.keywordOffset = node.rangeInExpression.startOffset
                    ParadoxParameterManager.completeArguments(element, context, resultToUse)
                }
            } else if (node is ParadoxScriptValueArgumentValueNode && PlsFacade.getSettings().inference.configContextForParameters) {
                if (inRange && expression.scriptValueNode.text.isNotEmpty()) {
                    //尝试提示传入参数的值
                    run {
                        val keywordToUse = node.text.substring(0, offset - node.rangeInExpression.startOffset)
                        val resultToUse = result.withPrefixMatcher(keywordToUse)
                        val parameterElement = node.argumentNode?.getReference(element)?.resolve() ?: return@run
                        val inferredContextConfigs = ParadoxParameterManager.getInferredContextConfigs(parameterElement)
                        val inferredConfig = inferredContextConfigs.singleOrNull()?.castOrNull<CwtValueConfig>() ?: return@run
                        context.keyword = keywordToUse
                        context.keywordOffset = node.rangeInExpression.startOffset
                        context.config = inferredConfig
                        context.configs = emptyList()
                        completeScriptExpression(context, resultToUse)
                        context.config = config
                        context.configs = configs
                    }
                }
            }
        }
        context.keyword = keyword
        context.keywordOffset = keywordOffset
        context.scopeContext = scopeContext
        context.isKey = isKey
    }

    fun completeValueFieldExpression(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()

        val offset = context.offsetInParent - context.expressionOffset
        if (offset < 0) return //unexpected

        val keyword = context.keyword
        val keywordOffset = context.keywordOffset
        val configGroup = context.configGroup ?: return

        val textRange = TextRange.create(keywordOffset, keywordOffset + keyword.length)
        val expression = markIncomplete { ParadoxValueFieldExpression.resolve(keyword, textRange, configGroup) } ?: return

        val element = context.contextElement?.castOrNull<ParadoxExpressionElement>() ?: return
        val scopeContext = context.scopeContext ?: ParadoxScopeManager.getAnyScopeContext()
        val isKey = context.isKey
        context.isKey = null
        var scopeContextInExpression = scopeContext
        for (node in expression.nodes) {
            val inRange = offset >= node.rangeInExpression.startOffset && offset <= node.rangeInExpression.endOffset
            if (!inRange) {
                if (node is ParadoxErrorNode || node.text.isEmpty()) break //skip error or empty nodes
            }
            if (node is ParadoxScopeLinkNode) {
                if (inRange) {
                    ProgressManager.checkCanceled()
                    context.scopeContext = scopeContextInExpression
                    completeForScopeLinkNode(node, context, result)
                    break
                } else {
                    scopeContextInExpression = ParadoxScopeManager.getSwitchedScopeContextOfNode(element, node, scopeContextInExpression)
                }
            } else if (node is ParadoxValueFieldNode) {
                if (inRange) {
                    ProgressManager.checkCanceled()
                    context.scopeContext = scopeContextInExpression
                    val scopeNode = ParadoxScopeLinkNode.resolve(node.text, node.rangeInExpression, configGroup)
                    val afterPrefix = completeForScopeLinkNode(scopeNode, context, result)
                    if (afterPrefix) break
                    completeForValueFieldNode(node, context, result)
                    break
                }
            }
        }

        context.keyword = keyword
        context.keywordOffset = keywordOffset
        context.scopeContext = scopeContext
        context.isKey = isKey
    }

    fun completeVariableFieldExpression(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()

        val offset = context.offsetInParent - context.expressionOffset
        if (offset < 0) return //unexpected

        val keyword = context.keyword
        val keywordOffset = context.keywordOffset
        val configGroup = context.configGroup ?: return

        val textRange = TextRange.create(keywordOffset, keywordOffset + keyword.length)
        val expression = markIncomplete { ParadoxVariableFieldExpression.resolve(keyword, textRange, configGroup) } ?: return

        val element = context.contextElement?.castOrNull<ParadoxExpressionElement>() ?: return
        val scopeContext = context.scopeContext ?: ParadoxScopeManager.getAnyScopeContext()
        val isKey = context.isKey
        context.isKey = null
        var scopeContextInExpression = scopeContext
        for (node in expression.nodes) {
            val inRange = offset >= node.rangeInExpression.startOffset && offset <= node.rangeInExpression.endOffset
            if (!inRange) {
                if (node is ParadoxErrorNode || node.text.isEmpty()) break //skip error or empty nodes
            }
            if (node is ParadoxScopeLinkNode) {
                if (inRange) {
                    ProgressManager.checkCanceled()
                    context.scopeContext = scopeContextInExpression
                    completeForScopeLinkNode(node, context, result)
                    break
                } else {
                    scopeContextInExpression = ParadoxScopeManager.getSwitchedScopeContextOfNode(element, node, scopeContextInExpression)
                }
            } else if (node is ParadoxDataSourceNode) {
                if (inRange) {
                    ProgressManager.checkCanceled()
                    context.scopeContext = scopeContextInExpression
                    val scopeNode = ParadoxScopeLinkNode.resolve(node.text, node.rangeInExpression, configGroup)
                    val afterPrefix = completeForScopeLinkNode(scopeNode, context, result)
                    if (afterPrefix) break
                    completeForVariableFieldValueNode(node, context, result)
                    break
                }
            }
        }

        context.keyword = keyword
        context.keywordOffset = keywordOffset
        context.scopeContext = scopeContext
        context.isKey = isKey
    }

    fun completeCommandExpression(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()

        val offset = context.offsetInParent - context.expressionOffset
        if (offset < 0) return //unexpected

        val keyword = context.keyword
        val keywordOffset = context.keywordOffset
        val configGroup = context.configGroup ?: return

        val textRange = TextRange.create(keywordOffset, keywordOffset + keyword.length)
        val expression = markIncomplete { ParadoxCommandExpression.resolve(keyword, textRange, configGroup) } ?: return

        val element = context.contextElement?.castOrNull<ParadoxExpressionElement>() ?: return
        val scopeContext = context.scopeContext ?: ParadoxScopeManager.getAnyScopeContext()
        val isKey = context.isKey
        context.isKey = null
        var scopeContextInExpression = scopeContext
        for (node in expression.nodes) {
            val inRange = offset >= node.rangeInExpression.startOffset && offset <= node.rangeInExpression.endOffset
            if (!inRange) {
                if (node is ParadoxErrorNode || node.text.isEmpty()) break //skip error or empty nodes
            }
            if (node is ParadoxCommandScopeLinkNode) {
                if (inRange) {
                    ProgressManager.checkCanceled()
                    context.scopeContext = scopeContextInExpression
                    completeForCommandScopeLinkNode(node, context, result)
                    break
                } else {
                    scopeContextInExpression = ParadoxScopeManager.getSwitchedScopeContextOfNode(element, node, scopeContextInExpression)
                }
            } else if (node is ParadoxCommandFieldNode) {
                if (inRange) {
                    ProgressManager.checkCanceled()
                    context.scopeContext = scopeContextInExpression
                    val scopeNode = ParadoxCommandScopeLinkNode.resolve(node.text, node.rangeInExpression, configGroup)
                    val afterPrefix = completeForCommandScopeLinkNode(scopeNode, context, result)
                    if (afterPrefix) break
                    completeForCommandFieldNode(node, context, result)
                    break
                }
            }
        }

        context.keyword = keyword
        context.keywordOffset = keywordOffset
        context.scopeContext = scopeContext
        context.isKey = isKey
    }

    fun completeDatabaseObjectExpression(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()

        val offset = context.offsetInParent - context.expressionOffset
        if (offset < 0) return //unexpected

        val keyword = context.keyword
        val keywordOffset = context.keywordOffset
        val configGroup = context.configGroup ?: return

        val textRange = TextRange.create(keywordOffset, keywordOffset + keyword.length)
        val expression = markIncomplete { ParadoxDatabaseObjectExpression.resolve(keyword, textRange, configGroup) } ?: return

        val oldNode = context.node
        val isKey = context.isKey
        context.isKey = null

        for (node in expression.nodes) {
            val inRange = offset >= node.rangeInExpression.startOffset && offset <= node.rangeInExpression.endOffset
            if (node is ParadoxDatabaseObjectTypeNode) {
                if (inRange) {
                    ProgressManager.checkCanceled()
                    val keywordToUse = node.text.substring(0, offset - node.rangeInExpression.startOffset)
                    val resultToUse = result.withPrefixMatcher(keywordToUse)
                    context.keyword = keywordToUse
                    context.keywordOffset = node.rangeInExpression.startOffset
                    context.node = node
                    completeDatabaseObjectType(context, resultToUse)
                    break
                }
            } else if (node is ParadoxDatabaseObjectNode) {
                if (inRange) {
                    ProgressManager.checkCanceled()
                    val keywordToUse = node.text.substring(0, offset - node.rangeInExpression.startOffset)
                    val resultToUse = result.withPrefixMatcher(keywordToUse)
                    context.keyword = keywordToUse
                    context.keywordOffset = node.rangeInExpression.startOffset
                    context.node = node
                    completeDatabaseObject(context, resultToUse)
                    break
                }
            }
        }

        context.keyword = keyword
        context.keywordOffset = keywordOffset
        context.node = oldNode
        context.isKey = isKey
    }

    fun completeDefineReferenceExpression(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()

        val offset = context.offsetInParent - context.expressionOffset
        if (offset < 0) return //unexpected

        val keyword = context.keyword
        val keywordOffset = context.keywordOffset
        val configGroup = context.configGroup ?: return

        val textRange = TextRange.create(keywordOffset, keywordOffset + keyword.length)
        val expression = markIncomplete { ParadoxDefineReferenceExpression.resolve(keyword, textRange, configGroup) } ?: return

        val oldNode = context.node
        val isKey = context.isKey
        context.isKey = null
        for (node in expression.nodes) {
            if (node is ParadoxErrorNode && expression.nodes.size == 1) {
                ProgressManager.checkCanceled()
                completeDefinePrefix(context, result)
                break
            }
            val inRange = offset >= node.rangeInExpression.startOffset && offset <= node.rangeInExpression.endOffset
            if (node is ParadoxDefineNamespaceNode) {
                if (inRange) {
                    ProgressManager.checkCanceled()
                    val keywordToUse = node.text.substring(0, offset - node.rangeInExpression.startOffset)
                    val resultToUse = result.withPrefixMatcher(keywordToUse)
                    context.keyword = keywordToUse
                    context.keywordOffset = node.rangeInExpression.startOffset
                    context.node = node
                    completeDefineNamespace(context, resultToUse)
                    break
                }
            } else if (node is ParadoxDefineVariableNode) {
                if (inRange) {
                    ProgressManager.checkCanceled()
                    val keywordToUse = node.text.substring(0, offset - node.rangeInExpression.startOffset)
                    val resultToUse = result.withPrefixMatcher(keywordToUse)
                    context.keyword = keywordToUse
                    context.keywordOffset = node.rangeInExpression.startOffset
                    context.node = node
                    completeDefineVariable(context, resultToUse)
                    break
                }
            }
        }

        context.keyword = keyword
        context.keywordOffset = keywordOffset
        context.node = oldNode
        context.isKey = isKey
    }

    /**
     * @return 是否已经输入了前缀。
     */
    private fun completeForScopeLinkNode(node: ParadoxScopeLinkNode, context: ProcessingContext, result: CompletionResultSet): Boolean {
        val offset = context.offsetInParent - context.expressionOffset
        if (offset < 0) return false //unexpected

        val element = context.contextElement?.castOrNull<ParadoxExpressionElement>() ?: return false
        val scopeContext = context.scopeContext ?: ParadoxScopeManager.getAnyScopeContext()
        val scopeLinkNode = node.castOrNull<ParadoxDynamicScopeLinkNode>()
        val prefixNode = scopeLinkNode?.prefixNode
        val dsNode = scopeLinkNode?.valueNode
        val inDsNode = dsNode?.nodes?.first()
        val endOffset = dsNode?.rangeInExpression?.startOffset ?: -1
        if (prefixNode != null && dsNode != null && offset >= dsNode.rangeInExpression.startOffset) {
            context.scopeContext = ParadoxScopeManager.getSwitchedScopeContextOfNode(element, node, scopeContext)

            val keywordToUse = dsNode.text.substring(0, offset - endOffset)
            val resultToUse = result.withPrefixMatcher(keywordToUse)
            val keyword = context.keyword
            val keywordOffset = context.keywordOffset
            context.keyword = keywordToUse
            context.keywordOffset = dsNode.rangeInExpression.startOffset
            completeScopeLinkValue(context, resultToUse, prefixNode.text, inDsNode)
            context.keyword = keyword
            context.keywordOffset = keywordOffset
            context.scopeContext = scopeContext
            return true
        } else {
            val inFirstNode = dsNode == null || dsNode.nodes.isEmpty()
                || offset <= dsNode.nodes.first().rangeInExpression.endOffset
            val keywordToUse = node.text.substring(0, offset - node.rangeInExpression.startOffset)
            val resultToUse = result.withPrefixMatcher(keywordToUse)
            val keyword = context.keyword
            val keywordOffset = context.keywordOffset
            context.keyword = keywordToUse
            context.keywordOffset = node.rangeInExpression.startOffset
            if (inFirstNode) {
                completeSystemScope(context, resultToUse)
                completeScope(context, resultToUse)
                completeScopeLinkPrefix(context, resultToUse)
            }
            completeScopeLinkValue(context, resultToUse, null, inDsNode)
            context.keyword = keyword
            context.keywordOffset = keywordOffset
            return false
        }
    }

    /**
     * @return 是否已经输入了前缀。
     */
    private fun completeForValueFieldNode(node: ParadoxValueFieldNode, context: ProcessingContext, result: CompletionResultSet): Boolean {
        val offset = context.offsetInParent - context.expressionOffset
        if (offset < 0) return false //unexpected

        val keyword = context.keyword
        val keywordOffset = context.keywordOffset
        val fieldNode = node.castOrNull<ParadoxDynamicValueFieldNode>()
        val prefixNode = fieldNode?.prefixNode
        val dsNode = fieldNode?.valueNode
        val inDsNode = dsNode?.nodes?.first()
        val endOffset = dsNode?.rangeInExpression?.startOffset ?: -1
        if (prefixNode != null && dsNode != null && offset >= dsNode.rangeInExpression.startOffset) {
            //unlike link node, there is unnecessary to switch scope context

            val keywordToUse = dsNode.text.substring(0, offset - endOffset)
            val resultToUse = result.withPrefixMatcher(keywordToUse)
            context.keyword = keywordToUse
            context.keywordOffset = dsNode.rangeInExpression.startOffset
            completeValueFieldValue(context, resultToUse, prefixNode.text, inDsNode)
            context.keyword = keyword
            context.keywordOffset = keywordOffset
            return true
        } else {
            val inFirstNode = dsNode == null || dsNode.nodes.isEmpty()
                || offset <= dsNode.nodes.first().rangeInExpression.endOffset
            val keywordToUse = node.text.substring(0, offset - node.rangeInExpression.startOffset)
            val resultToUse = result.withPrefixMatcher(keywordToUse)
            context.keyword = keywordToUse
            context.keywordOffset = node.rangeInExpression.startOffset
            if (inFirstNode) {
                completeValueField(context, resultToUse)
                completeValueFieldPrefix(context, resultToUse)
            }
            completeValueFieldValue(context, resultToUse, null, inDsNode)
            context.keyword = keyword
            context.keywordOffset = keywordOffset
            return false
        }
    }

    private fun completeForVariableFieldValueNode(node: ParadoxDataSourceNode, context: ProcessingContext, result: CompletionResultSet) {
        val offset = context.offsetInParent - context.expressionOffset
        if (offset < 0) return //unexpected

        val keywordToUse = node.text.substring(0, offset - node.rangeInExpression.startOffset)
        val resultToUse = result.withPrefixMatcher(keywordToUse)
        context.keyword = keywordToUse
        completeValueFieldValue(context, resultToUse, null, node, variableOnly = true)
    }

    /**
     * @return 是否已经输入了前缀。
     */
    private fun completeForCommandScopeLinkNode(node: ParadoxCommandScopeLinkNode, context: ProcessingContext, result: CompletionResultSet): Boolean {
        val offset = context.offsetInParent - context.expressionOffset
        if (offset < 0) return false //unexpected

        val element = context.contextElement?.castOrNull<ParadoxExpressionElement>() ?: return false
        val scopeContext = context.scopeContext ?: ParadoxScopeManager.getAnyScopeContext()
        val scopeLinkNode = node.castOrNull<ParadoxDynamicCommandScopeLinkNode>()
        val prefixNode = scopeLinkNode?.prefixNode
        val dsNode = scopeLinkNode?.valueNode
        val endOffset = dsNode?.rangeInExpression?.startOffset ?: -1
        if (prefixNode != null && dsNode != null && offset >= dsNode.rangeInExpression.startOffset) {
            context.scopeContext = ParadoxScopeManager.getSwitchedScopeContextOfNode(element, node, scopeContext)

            val keywordToUse = dsNode.text.substring(0, offset - endOffset)
            val resultToUse = result.withPrefixMatcher(keywordToUse)
            val keyword = context.keyword
            val keywordOffset = context.keywordOffset
            context.keyword = keywordToUse
            context.keywordOffset = dsNode.rangeInExpression.startOffset
            completeCommandScopeLinkValue(context, resultToUse, prefixNode.text)
            context.keyword = keyword
            context.keywordOffset = keywordOffset
            context.scopeContext = scopeContext
            return true
        } else {
            val inFirstNode = dsNode == null || dsNode.nodes.isEmpty()
                || offset <= dsNode.nodes.first().rangeInExpression.endOffset
            val keywordToUse = node.text.substring(0, offset - node.rangeInExpression.startOffset)
            val resultToUse = result.withPrefixMatcher(keywordToUse)
            val keyword = context.keyword
            val keywordOffset = context.keywordOffset
            context.keyword = keywordToUse
            context.keywordOffset = node.rangeInExpression.startOffset
            if (inFirstNode) {
                completeSystemScope(context, resultToUse)
                completeCommandScope(context, resultToUse)
                completeCommandScopeLinkPrefix(context, resultToUse)
            }
            completeCommandScopeLinkValue(context, resultToUse, null)
            context.keyword = keyword
            context.keywordOffset = keywordOffset
            return false
        }
    }

    /**
     * @return 是否已经输入了前缀。
     */
    private fun completeForCommandFieldNode(node: ParadoxCommandFieldNode, context: ProcessingContext, result: CompletionResultSet): Boolean {
        val offset = context.offsetInParent - context.expressionOffset
        if (offset < 0) return false //unexpected

        val keyword = context.keyword
        val keywordOffset = context.keywordOffset
        val fieldNode = node.castOrNull<ParadoxDynamicCommandFieldNode>()
        val prefixNode = fieldNode?.prefixNode
        val dsNode = fieldNode?.valueNode
        val endOffset = dsNode?.rangeInExpression?.startOffset ?: -1
        if (prefixNode != null && dsNode != null && offset >= dsNode.rangeInExpression.startOffset) {
            //unlike link node, there is unnecessary to switch scope context

            val keywordToUse = dsNode.text.substring(0, offset - endOffset)
            val resultToUse = result.withPrefixMatcher(keywordToUse)
            context.keyword = keywordToUse
            context.keywordOffset = dsNode.rangeInExpression.startOffset
            completeCommandFieldValue(context, resultToUse, prefixNode.text)
            context.keyword = keyword
            context.keywordOffset = keywordOffset
            return true
        } else {
            val inFirstNode = dsNode == null || dsNode.nodes.isEmpty()
                || offset <= dsNode.nodes.first().rangeInExpression.endOffset
            val keywordToUse = node.text.substring(0, offset - node.rangeInExpression.startOffset)
            val resultToUse = result.withPrefixMatcher(keywordToUse)
            context.keyword = keywordToUse
            context.keywordOffset = node.rangeInExpression.startOffset
            if (inFirstNode) {
                completePredefinedCommandField(context, resultToUse)
                completeCommandField(context, resultToUse)
                completeCommandFieldPrefix(context, resultToUse)
            }
            completeCommandFieldValue(context, resultToUse, null)
            context.keyword = keyword
            context.keywordOffset = keywordOffset
            return false
        }
    }

    fun completeSystemScope(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val configGroup = context.configGroup!!

        //总是提示，无论作用域是否匹配
        val systemScopeConfigs = configGroup.systemScopes
        for (systemScopeConfig in systemScopeConfigs.values) {
            val name = systemScopeConfig.id
            val element = systemScopeConfig.pointer.element ?: continue
            val tailText = " from system scopes"
            val typeFile = systemScopeConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withIcon(PlsIcons.Nodes.SystemScope)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withCaseSensitivity(false) //忽略大小写
                .withPriority(ParadoxCompletionPriorities.systemScope)
                .withCompletionId()
            result.addElement(lookupElement, context)
        }
    }

    fun completeScope(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val configGroup = context.configGroup!!
        val scopeContext = context.scopeContext

        val linksConfigs = configGroup.links.values.filter { it.forScope() && !it.fromData && !it.fromArgument }
        for (linkConfig in linksConfigs) {
            val scopeMatched = ParadoxScopeManager.matchesScope(scopeContext, linkConfig.inputScopes, configGroup)
            if (!scopeMatched && PlsFacade.getSettings().completion.completeOnlyScopeIsMatched) continue

            val name = linkConfig.name
            val element = linkConfig.pointer.element ?: continue
            val tailText = " from links"
            val typeFile = linkConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withIcon(PlsIcons.Nodes.Scope)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withCaseSensitivity(false) //忽略大小写
                .withScopeMatched(scopeMatched)
                .withPriority(ParadoxCompletionPriorities.scope)
                .withCompletionId()
            result.addElement(lookupElement, context)
        }
    }

    fun completeScopeLinkPrefix(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val configGroup = context.configGroup!!
        val scopeContext = context.scopeContext

        val linkConfigsFromArgument = configGroup.links.values.filter { it.forScope() && it.prefix != null && it.fromArgument }
            .sortedByPriority({ it.dataSourceExpression }, { configGroup })
        for (linkConfig in linkConfigsFromArgument) {
            val scopeMatched = ParadoxScopeManager.matchesScope(scopeContext, linkConfig.inputScopes, configGroup)
            if (!scopeMatched && PlsFacade.getSettings().completion.completeOnlyScopeIsMatched) continue

            val name = linkConfig.prefix?.dropLast(1) ?: continue
            val element = linkConfig.pointer.element ?: continue
            val tailText = "(...) from link ${linkConfig.name}"
            val typeFile = linkConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withBoldness(true)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withPriority(ParadoxCompletionPriorities.prefix)
                .withCompletionId()
                .withInsertHandler { c, _ ->
                    val editor = c.editor
                    EditorModificationUtil.insertStringAtCaret(editor, "()", false, true, 1)
                }
            result.addElement(lookupElement, context)
        }

        val linkConfigsFromData = configGroup.links.values.filter { it.forScope() && it.prefix != null && it.fromData }
            .sortedByPriority({ it.dataSourceExpression }, { configGroup })
        for (linkConfig in linkConfigsFromData) {
            val scopeMatched = ParadoxScopeManager.matchesScope(scopeContext, linkConfig.inputScopes, configGroup)
            if (!scopeMatched && PlsFacade.getSettings().completion.completeOnlyScopeIsMatched) continue

            val name = linkConfig.prefix ?: continue
            val element = linkConfig.pointer.element ?: continue
            val tailText = " from link ${linkConfig.name}"
            val typeFile = linkConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withBoldness(true)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withScopeMatched(scopeMatched)
                .withPriority(ParadoxCompletionPriorities.prefix)
                .withCompletionId()
            result.addElement(lookupElement, context)
        }
    }

    fun completeScopeLinkValue(context: ProcessingContext, result: CompletionResultSet, prefix: String?, inDsNode: ParadoxComplexExpressionNode?) {
        ProgressManager.checkCanceled()
        val configGroup = context.configGroup!!
        val config = context.config
        val configs = context.configs
        val scopeContext = context.scopeContext

        val linkConfigs = configGroup.links.values.filter { it.forScope() && it.prefix == prefix }
            .sortedByPriority({ it.dataSourceExpression }, { configGroup })
        context.configs = linkConfigs

        if (inDsNode is ParadoxScopeLinkNode) {
            completeForScopeLinkNode(inDsNode, context, result)
            context.scopeContext = scopeContext
            return
        }
        if (inDsNode is ParadoxDynamicValueExpression) {
            completeDynamicValueExpression(context, result)
            return
        }
        if (inDsNode is ParadoxScopeFieldExpression) {
            completeScopeFieldExpression(context, result)
            return
        }
        for (linkConfig in linkConfigs) {
            ProgressManager.checkCanceled()
            context.config = linkConfig
            completeScriptExpression(context, result)
        }

        context.config = config
        context.configs = configs
    }

    fun completeValueField(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val configGroup = context.configGroup!!
        val scopeContext = context.scopeContext

        val linkConfigs = configGroup.links.values.filter { it.forValue() && !it.fromData && !it.fromArgument }
        for (linkConfig in linkConfigs) {
            ProgressManager.checkCanceled()
            //排除input_scopes不匹配前一个scope的output_scope的情况
            val scopeMatched = ParadoxScopeManager.matchesScope(scopeContext, linkConfig.inputScopes, configGroup)
            if (!scopeMatched && PlsFacade.getSettings().completion.completeOnlyScopeIsMatched) continue

            val name = linkConfig.name
            val element = linkConfig.pointer.element ?: continue
            val tailText = " from links"
            val typeFile = linkConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withIcon(PlsIcons.Nodes.ValueField)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withCaseSensitivity(false) //忽略大小写
                .withScopeMatched(scopeMatched)
                .withCompletionId()
            result.addElement(lookupElement, context)
        }
    }

    fun completeValueFieldPrefix(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val configGroup = context.configGroup!!
        val scopeContext = context.scopeContext

        val linkConfigsFromArgument = configGroup.links.values.filter { it.forValue() && it.prefix != null && it.fromArgument }
            .sortedByPriority({ it.dataSourceExpression }, { configGroup })
        for (linkConfig in linkConfigsFromArgument) {
            val scopeMatched = ParadoxScopeManager.matchesScope(scopeContext, linkConfig.inputScopes, configGroup)
            if (!scopeMatched && PlsFacade.getSettings().completion.completeOnlyScopeIsMatched) continue

            val name = linkConfig.prefix?.dropLast(1) ?: continue
            val element = linkConfig.pointer.element ?: continue
            val tailText = "(...) from link ${linkConfig.name}"
            val typeFile = linkConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withBoldness(true)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withPriority(ParadoxCompletionPriorities.prefix)
                .withCompletionId()
                .withInsertHandler { c, _ ->
                    val editor = c.editor
                    EditorModificationUtil.insertStringAtCaret(editor, "()", false, true, 1)
                }
            result.addElement(lookupElement, context)
        }

        val linkConfigsFromData = configGroup.links.values.filter { it.forValue() && it.prefix != null && it.fromData }
            .sortedByPriority({ it.dataSourceExpression }, { configGroup })
        for (linkConfig in linkConfigsFromData) {
            val scopeMatched = ParadoxScopeManager.matchesScope(scopeContext, linkConfig.inputScopes, configGroup)
            if (!scopeMatched && PlsFacade.getSettings().completion.completeOnlyScopeIsMatched) continue

            val name = linkConfig.prefix ?: continue
            val element = linkConfig.pointer.element ?: continue
            val tailText = " from link ${linkConfig.name}"
            val typeFile = linkConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withBoldness(true)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withPriority(ParadoxCompletionPriorities.prefix)
                .withCompletionId()
            result.addElement(lookupElement, context)
        }
    }

    fun completeValueFieldValue(context: ProcessingContext, result: CompletionResultSet, prefix: String?, inDsNode: ParadoxComplexExpressionNode?, variableOnly: Boolean = false) {
        ProgressManager.checkCanceled()
        val configGroup = context.configGroup!!
        val config = context.config
        val configs = context.configs

        val linkConfigs = when {
            variableOnly -> configGroup.linksOfVariable
            else -> configGroup.links.values.filter { it.forValue() && it.prefix == prefix }
                .sortedByPriority({ it.dataSourceExpression }, { configGroup })
        }
        context.configs = linkConfigs

        if (inDsNode is ParadoxDynamicValueExpression) {
            completeDynamicValueExpression(context, result)
            return
        }
        if (inDsNode is ParadoxScopeFieldExpression) {
            completeScopeFieldExpression(context, result)
            return
        }
        if (inDsNode is ParadoxScriptValueExpression) {
            completeScriptValueExpression(context, result)
            return
        }
        for (linkConfig in linkConfigs) {
            ProgressManager.checkCanceled()
            context.config = linkConfig
            completeScriptExpression(context, result)
        }

        context.config = config
        context.configs = configs
    }

    fun completeDatabaseObjectType(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val configGroup = context.configGroup!!
        val tailText = " from database object types"
        val configs = configGroup.databaseObjectTypes.values
        for (config in configs) {
            val name = config.name
            val element = config.pointer.element ?: continue
            val typeFile = config.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withPriority(ParadoxCompletionPriorities.prefix)
                .withPatchableIcon(PlsIcons.Nodes.DynamicValue)
                .withPatchableTailText(tailText)
                .forScriptExpression(context)
            result.addElement(lookupElement, context)
        }
    }

    fun completeDatabaseObject(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val node = context.node?.castOrNull<ParadoxDatabaseObjectNode>()
            ?.nodes?.findIsInstance<ParadoxDatabaseObjectDataSourceNode>()
            ?: return
        val config = node.config ?: return

        val typeToSearch = node.getTypeToSearch()
        if (typeToSearch == null) return

        val tailText = " from database object type ${config.name}"
        context.expressionTailText = tailText

        //complete forced base database object
        completeForcedBaseDatabaseObject(context, result, node)

        val extraFilter = f@{ e: PsiElement ->
            node.isValidDatabaseObject(e, typeToSearch)
        }
        val mockConfig = config.getConfigForType(node.isBase)
        val oldExtraFilter = context.extraFilter
        val oldConfig = context.config
        val oldTailText = context.expressionTailText
        context.extraFilter = extraFilter
        context.config = mockConfig
        if (config.localisation != null) {
            completeLocalisation(context, result)
        } else {
            completeDefinition(context, result)
        }
        context.extraFilter = oldExtraFilter
        context.config = oldConfig
        context.expressionTailText = oldTailText
    }

    private fun completeForcedBaseDatabaseObject(context: ProcessingContext, result: CompletionResultSet, dsNode: ParadoxDatabaseObjectDataSourceNode) {
        val configGroup = context.configGroup!!
        val config = dsNode.config ?: return
        if (!dsNode.isPossibleForcedBase()) return
        val valueNode = dsNode.expression.valueNode ?: return
        val project = configGroup.project
        val contextElement = context.contextElement
        val selector = selector(project, contextElement).definition().contextSensitive().distinctByName()
        ParadoxDefinitionSearch.search(valueNode.text, config.type, selector).processQueryAsync p@{ definition ->
            ProgressManager.checkCanceled()
            val definitionInfo = definition.definitionInfo ?: return@p true
            if (definitionInfo.name.isEmpty()) return@p true //ignore anonymous definitions

            val name = definitionInfo.name
            val typeFile = definition.containingFile
            val lookupElement = LookupElementBuilder.create(definition, name)
                .withTypeText(typeFile.name, typeFile.icon, true)
                .withPatchableIcon(PlsIcons.Nodes.Definition(definitionInfo.type))
                .withPatchableTailText(context.expressionTailText)
                .withDefinitionLocalizedNamesIfNecessary(definition)
                .forScriptExpression(context)
            result.addElement(lookupElement, context)
            true
        }
    }

    fun completeDefinePrefix(context: ProcessingContext, result: CompletionResultSet) {
        val name = "define:"
        val lookupElement = LookupElementBuilder.create(name)
            .withBoldness(true)
            .withPriority(ParadoxCompletionPriorities.prefix)
            .withCompletionId()
        result.addElement(lookupElement, context)
    }

    fun completeDefineNamespace(context: ProcessingContext, result: CompletionResultSet) {
        val project = context.parameters!!.originalFile.project
        val contextElement = context.contextElement
        val tailText = " from define namespaces"
        val selector = selector(project, contextElement).define().distinctByExpression()
        ParadoxDefineSearch.search(null, "", selector).processQueryAsync p@{ info ->
            ProgressManager.checkCanceled()
            val namespace = info.namespace
            val element = ParadoxDefineManager.getDefineElement(info, project) ?: return@p true
            val lookupElement = LookupElementBuilder.create(element, namespace)
                .withPatchableIcon(PlsIcons.Nodes.DefineNamespace)
                .withPatchableTailText(tailText)
                .forScriptExpression(context)
            result.addElement(lookupElement, context)
            true
        }
    }

    fun completeDefineVariable(context: ProcessingContext, result: CompletionResultSet) {
        val project = context.parameters!!.originalFile.project
        val contextElement = context.contextElement
        val node = context.node?.castOrNull<ParadoxDefineVariableNode>() ?: return
        val namespaceNode = node.expression.namespaceNode ?: return
        val namespace = namespaceNode.text
        val tailText = " from define namespace ${namespace}"
        val selector = selector(project, contextElement).define().distinctByExpression()
        ParadoxDefineSearch.search(namespace, null, selector).processQueryAsync p@{ info ->
            ProgressManager.checkCanceled()
            val variable = info.variable ?: return@p true
            val element = ParadoxDefineManager.getDefineElement(info, project) ?: return@p true
            val lookupElement = LookupElementBuilder.create(element, variable)
                .withPatchableIcon(PlsIcons.Nodes.DefineVariable)
                .withPatchableTailText(tailText)
                .forScriptExpression(context)
            result.addElement(lookupElement, context)
            true
        }
    }

    fun completeDynamicValue(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val config = context.config
        val configs = context.configs

        fun doComplete(config: CwtConfig<*>) {
            val keyword = context.keyword
            val contextElement = context.contextElement!!
            val configGroup = context.configGroup!!
            val project = configGroup.project

            val configExpression = config.configExpression ?: return
            val dynamicValueType = configExpression.value ?: return
            //提示预定义的value
            run {
                ProgressManager.checkCanceled()
                if (configExpression.type == CwtDataTypes.Value || configExpression.type == CwtDataTypes.DynamicValue) {
                    completePredefinedDynamicValue(context, result, dynamicValueType)
                }
            }
            //提示来自脚本文件的value
            run {
                ProgressManager.checkCanceled()
                val tailText = " by $configExpression"
                val selector = selector(project, contextElement).dynamicValue().distinctByName()
                ParadoxDynamicValueSearch.search(dynamicValueType, selector).processQueryAsync p@{ info ->
                    ProgressManager.checkCanceled()
                    if (info.name == keyword) return@p true //排除和当前输入的同名的
                    val (name, _, readWriteAccess, _, gameType) = info
                    val element = ParadoxDynamicValueElement(contextElement, name, dynamicValueType, readWriteAccess, gameType, project)
                    //去除后面的作用域信息
                    val icon = PlsIcons.Nodes.DynamicValue(dynamicValueType)
                    //不显示typeText
                    val lookupElement = LookupElementBuilder.create(element, info.name)
                        .withPatchableIcon(icon)
                        .withPatchableTailText(tailText)
                        .forScriptExpression(context)
                    result.addElement(lookupElement, context)
                    true
                }
            }

            completeExtendedDynamicValue(context, result)
        }

        if (configs.isNotNullOrEmpty()) {
            for (c in configs) {
                doComplete(c)
            }
        } else if (config != null) {
            doComplete(config)
        }
    }

    fun completePredefinedDynamicValue(context: ProcessingContext, result: CompletionResultSet, dynamicValueType: String) {
        ProgressManager.checkCanceled()
        val configGroup = context.configGroup!!
        val config = context.config

        val tailText = getExpressionTailText(context, config)
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

    fun completeCommandScope(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val configGroup = context.configGroup!!
        val scopeContext = context.scopeContext

        val localisationLinks = configGroup.localisationLinks.values.filter { it.forScope() && !it.fromData && !it.fromArgument }
        for (localisationScope in localisationLinks) {
            val scopeMatched = ParadoxScopeManager.matchesScope(scopeContext, localisationScope.inputScopes, configGroup)
            if (!scopeMatched && PlsFacade.getSettings().completion.completeOnlyScopeIsMatched) continue

            //optimize: make first char uppercase (e.g., owner -> Owner)
            val name = localisationScope.name.replaceFirstChar { it.uppercaseChar() }
            val element = localisationScope.pointer.element ?: continue
            val tailText = " from localisation links"
            val typeFile = localisationScope.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withIcon(PlsIcons.Nodes.LocalisationCommandScope)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withCaseSensitivity(false) //忽略大小写
                .withScopeMatched(scopeMatched)
                .withPriority(ParadoxCompletionPriorities.scope)
                .withCompletionId()
            result.addElement(lookupElement, context)
        }
    }

    fun completeCommandScopeLinkPrefix(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val configGroup = context.configGroup!!
        val scopeContext = context.scopeContext

        val linkConfigsFromArgument = configGroup.localisationLinks.values.filter { it.forScope() && it.prefix != null && it.fromArgument }
            .sortedByPriority({ it.dataSourceExpression }, { configGroup })
        for (linkConfig in linkConfigsFromArgument) {
            val scopeMatched = ParadoxScopeManager.matchesScope(scopeContext, linkConfig.inputScopes, configGroup)
            if (!scopeMatched && PlsFacade.getSettings().completion.completeOnlyScopeIsMatched) continue

            val name = linkConfig.prefix?.dropLast(1) ?: continue
            val element = linkConfig.pointer.element ?: continue
            val tailText = "(...) from localisation link ${linkConfig.name}"
            val typeFile = linkConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withBoldness(true)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withPriority(ParadoxCompletionPriorities.prefix)
                .withCompletionId()
                .withInsertHandler { c, _ ->
                    val editor = c.editor
                    EditorModificationUtil.insertStringAtCaret(editor, "()", false, true, 1)
                }
            result.addElement(lookupElement, context)
        }

        val linkConfigsFromData = configGroup.localisationLinks.values.filter { it.forScope() && it.prefix != null && it.fromData }
            .sortedByPriority({ it.dataSourceExpression }, { configGroup })
        for (linkConfig in linkConfigsFromData) {
            val scopeMatched = ParadoxScopeManager.matchesScope(scopeContext, linkConfig.inputScopes, configGroup)
            if (!scopeMatched && PlsFacade.getSettings().completion.completeOnlyScopeIsMatched) continue

            val name = linkConfig.prefix ?: continue
            val element = linkConfig.pointer.element ?: continue
            val tailText = " from localisation link ${linkConfig.name}"
            val typeFile = linkConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withBoldness(true)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withScopeMatched(scopeMatched)
                .withPriority(ParadoxCompletionPriorities.prefix)
                .withCompletionId()
            result.addElement(lookupElement, context)
        }
    }

    fun completeCommandScopeLinkValue(context: ProcessingContext, result: CompletionResultSet, prefix: String?) {
        ProgressManager.checkCanceled()
        val configGroup = context.configGroup!!
        val config = context.config
        val configs = context.configs

        val linkConfigs = configGroup.localisationLinks.values.filter { it.forScope() && it.prefix == prefix }
            .sortedByPriority({ it.dataSourceExpression }, { configGroup })
        context.configs = linkConfigs

        for (linkConfig in linkConfigs) {
            ProgressManager.checkCanceled()
            context.config = linkConfig
            completeScriptExpression(context, result)
        }

        context.config = config
        context.configs = configs
    }

    fun completePredefinedCommandField(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val configGroup = context.configGroup!!
        val scopeContext = context.scopeContext

        val localisationCommands = configGroup.localisationCommands
        for (localisationCommand in localisationCommands.values) {
            val scopeMatched = ParadoxScopeManager.matchesScope(scopeContext, localisationCommand.supportedScopes, configGroup)
            if (!scopeMatched && PlsFacade.getSettings().completion.completeOnlyScopeIsMatched) continue

            val name = localisationCommand.name
            val element = localisationCommand.pointer.element ?: continue
            val tailText = " from localisation commands"
            val typeFile = localisationCommand.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withIcon(PlsIcons.Nodes.LocalisationCommandField)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withCaseSensitivity(false) //忽略大小写
                .withScopeMatched(scopeMatched)
                .withCompletionId()
            result.addElement(lookupElement, context)
        }
    }

    fun completeCommandField(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val configGroup = context.configGroup!!
        val scopeContext = context.scopeContext

        val linkConfigs = configGroup.localisationLinks.values.filter { it.forValue() && !it.fromData && !it.fromArgument }
        for (linkConfig in linkConfigs) {
            ProgressManager.checkCanceled()
            //排除input_scopes不匹配前一个scope的output_scope的情况
            val scopeMatched = ParadoxScopeManager.matchesScope(scopeContext, linkConfig.inputScopes, configGroup)
            if (!scopeMatched && PlsFacade.getSettings().completion.completeOnlyScopeIsMatched) continue

            val name = linkConfig.name
            val element = linkConfig.pointer.element ?: continue
            val tailText = " from localisation links"
            val typeFile = linkConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withIcon(PlsIcons.Nodes.ValueField)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withCaseSensitivity(false) //忽略大小写
                .withScopeMatched(scopeMatched)
                .withCompletionId()
            result.addElement(lookupElement, context)
        }
    }

    fun completeCommandFieldPrefix(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val configGroup = context.configGroup!!
        val scopeContext = context.scopeContext

        val linkConfigsFromArgument = configGroup.localisationLinks.values.filter { it.forValue() && it.prefix != null && it.fromArgument }
            .sortedByPriority({ it.dataSourceExpression }, { configGroup })
        for (linkConfig in linkConfigsFromArgument) {
            val scopeMatched = ParadoxScopeManager.matchesScope(scopeContext, linkConfig.inputScopes, configGroup)
            if (!scopeMatched && PlsFacade.getSettings().completion.completeOnlyScopeIsMatched) continue

            val name = linkConfig.prefix?.dropLast(1) ?: continue
            val element = linkConfig.pointer.element ?: continue
            val tailText = "(...) from localisation link ${linkConfig.name}"
            val typeFile = linkConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withBoldness(true)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withPriority(ParadoxCompletionPriorities.prefix)
                .withCompletionId()
                .withInsertHandler { c, _ ->
                    val editor = c.editor
                    EditorModificationUtil.insertStringAtCaret(editor, "()", false, true, 1)
                }
            result.addElement(lookupElement, context)
        }

        val linkConfigsFromData = configGroup.localisationLinks.values.filter { it.forValue() && it.prefix != null && it.fromData }
            .sortedByPriority({ it.dataSourceExpression }, { configGroup })
        for (linkConfig in linkConfigsFromData) {
            val scopeMatched = ParadoxScopeManager.matchesScope(scopeContext, linkConfig.inputScopes, configGroup)
            if (!scopeMatched && PlsFacade.getSettings().completion.completeOnlyScopeIsMatched) continue

            val name = linkConfig.prefix ?: continue
            val element = linkConfig.pointer.element ?: continue
            val tailText = " from localisation link ${linkConfig.name}"
            val typeFile = linkConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withBoldness(true)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withPriority(ParadoxCompletionPriorities.prefix)
                .withCompletionId()
            result.addElement(lookupElement, context)
        }
    }

    fun completeCommandFieldValue(context: ProcessingContext, result: CompletionResultSet, prefix: String?) {
        ProgressManager.checkCanceled()
        val configGroup = context.configGroup!!
        val config = context.config
        val configs = context.configs

        val linkConfigs = configGroup.localisationLinks.values.filter { it.forValue() && it.prefix == prefix }
            .sortedByPriority({ it.dataSourceExpression }, { configGroup })
        context.configs = linkConfigs

        for (linkConfig in linkConfigs) {
            ProgressManager.checkCanceled()
            context.config = linkConfig
            completeScriptExpression(context, result)
        }

        context.config = config
        context.configs = configs
    }

    //endregion

    //region Extended Completion Methods

    fun completeExtendedScriptedVariable(context: ProcessingContext, result: CompletionResultSet) {
        if (!PlsFacade.getSettings().completion.completeByExtendedConfigs) return
        ProgressManager.checkCanceled()

        val configGroup = context.configGroup ?: return
        configGroup.extendedScriptedVariables.values.forEach f@{ config0 ->
            ProgressManager.checkCanceled()
            val name = config0.name
            if (checkExtendedConfigName(name)) return@f
            val element = config0.pointer.element ?: return@f
            val typeFile = config0.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withIcon(PlsIcons.Nodes.ScriptedVariable)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withItemTextUnderlined(true) //used for completions from extended configs
                .withCompletionId()
            result.addElement(lookupElement, context)
        }
    }

    fun completeExtendedDefinition(context: ProcessingContext, result: CompletionResultSet) {
        if (!PlsFacade.getSettings().completion.completeByExtendedConfigs) return
        ProgressManager.checkCanceled()

        val config = context.config ?: return
        val typeExpression = config.configExpression?.value ?: return
        val configGroup = config.configGroup
        val tailText = getExpressionTailText(context, config)

        run r1@{
            configGroup.extendedDefinitions.values.forEach { configs0 ->
                configs0.forEach f@{ config0 ->
                    ProgressManager.checkCanceled()
                    val name = config0.name
                    if (name.isEmpty()) return@f
                    if (checkExtendedConfigName(name)) return@f
                    val type = config0.type
                    if (!ParadoxDefinitionTypeExpression.resolve(type).matches(typeExpression)) return@f
                    val element = config0.pointer.element
                    val typeFile = config0.pointer.containingFile
                    val lookupElement = LookupElementBuilder.create(name).withPsiElement(element)
                        .withTypeText(typeFile?.name, typeFile?.icon, true)
                        .withItemTextUnderlined(true) //used for completions from extended configs
                        .withPatchableIcon(PlsIcons.Nodes.Definition(type))
                        .withPatchableTailText(tailText)
                        .forScriptExpression(context)
                    result.addElement(lookupElement, context)
                }
            }
        }
        run r1@{
            val tGameRule = ParadoxDefinitionTypes.GameRule
            if (typeExpression != tGameRule) return@r1
            configGroup.extendedGameRules.values.forEach f@{ config0 ->
                ProgressManager.checkCanceled()
                val name = config0.name
                if (checkExtendedConfigName(name)) return@f
                val element = config0.pointer.element
                val typeFile = config0.pointer.containingFile
                val lookupElement = LookupElementBuilder.create(name).withPsiElement(element)
                    .withTypeText(typeFile?.name, typeFile?.icon, true)
                    .withItemTextUnderlined(true) //used for completions from extended configs
                    .withPatchableIcon(PlsIcons.Nodes.Definition(tGameRule))
                    .withPatchableTailText(tailText)
                    .forScriptExpression(context)
                result.addElement(lookupElement, context)
            }
        }
        run r1@{
            val tOnAction = ParadoxDefinitionTypes.OnAction
            if (typeExpression != tOnAction) return@r1
            configGroup.extendedOnActions.values.forEach f@{ config0 ->
                ProgressManager.checkCanceled()
                val name = config0.name
                if (checkExtendedConfigName(name)) return@f
                val element = config0.pointer.element
                val typeFile = config0.pointer.containingFile
                val lookupElement = LookupElementBuilder.create(name).withPsiElement(element)
                    .withTypeText(typeFile?.name, typeFile?.icon, true)
                    .withItemTextUnderlined(true) //used for completions from extended configs
                    .withPatchableIcon(PlsIcons.Nodes.Definition(tOnAction))
                    .withPatchableTailText(tailText)
                    .forScriptExpression(context)
                result.addElement(lookupElement, context)
            }
        }
    }

    fun completeExtendedInlineScript(context: ProcessingContext, result: CompletionResultSet) {
        if (!PlsFacade.getSettings().completion.completeByExtendedConfigs) return
        ProgressManager.checkCanceled()

        val config = context.config ?: return
        val configGroup = config.configGroup
        val tailText = getExpressionTailText(context, config)

        configGroup.extendedInlineScripts.values.forEach f@{ config0 ->
            ProgressManager.checkCanceled()
            val name = config0.name
            if (checkExtendedConfigName(name)) return@f
            val element = config0.pointer.element
            val typeFile = config0.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(name).withPsiElement(element)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withItemTextUnderlined(true) //used for completions from extended configs
                .withPatchableIcon(PlsIcons.Nodes.InlineScript)
                .withPatchableTailText(tailText)
                .forScriptExpression(context)
            result.addElement(lookupElement, context)
        }
    }

    fun completeExtendedParameter(context: ProcessingContext, result: CompletionResultSet) {
        if (!PlsFacade.getSettings().completion.completeByExtendedConfigs) return
        ProgressManager.checkCanceled()

        val configGroup = context.configGroup ?: return
        val contextKey = context.contextKey ?: return
        val argumentNames = context.argumentNames
        val contextElement = context.contextElement ?: return

        configGroup.extendedParameters.values.forEach { configs0 ->
            configs0.forEach f@{ config0 ->
                if (!config0.contextKey.matchFromPattern(contextKey, contextElement, configGroup)) return@f
                val name = config0.name
                if (checkExtendedConfigName(name)) return@f
                if (argumentNames != null && !argumentNames.add(name)) return@f  //排除已输入的
                val element = config0.pointer.element
                val typeFile = config0.pointer.containingFile
                val lookupElement = LookupElementBuilder.create(name).withPsiElement(element)
                    .withTypeText(typeFile?.name, typeFile?.icon, true)
                    .withItemTextUnderlined(true) //used for completions from extended configs
                    .withPatchableIcon(PlsIcons.Nodes.Parameter)
                    .forScriptExpression(context)
                result.addElement(lookupElement, context)
            }
        }
    }

    fun completeExtendedComplexEnumValue(context: ProcessingContext, result: CompletionResultSet) {
        if (!PlsFacade.getSettings().completion.completeByExtendedConfigs) return
        ProgressManager.checkCanceled()

        val config = context.config ?: return
        val enumName = config.configExpression?.value ?: return
        val configGroup = config.configGroup
        val tailText = getExpressionTailText(context, config)

        configGroup.extendedComplexEnumValues[enumName]?.values?.forEach f@{ config0 ->
            ProgressManager.checkCanceled()
            val name = config0.name
            if (checkExtendedConfigName(name)) return@f
            val element = config0.pointer.element
            val typeFile = config0.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(name).withPsiElement(element)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withItemTextUnderlined(true) //used for completions from extended configs
                .withPatchableIcon(PlsIcons.Nodes.EnumValue)
                .withPatchableTailText(tailText)
                .forScriptExpression(context)
            result.addElement(lookupElement, context)
        }
    }

    fun completeExtendedDynamicValue(context: ProcessingContext, result: CompletionResultSet) {
        if (!PlsFacade.getSettings().completion.completeByExtendedConfigs) return
        ProgressManager.checkCanceled()

        val config = context.config ?: return
        val dynamicValueType = config.configExpression?.value ?: return
        val configGroup = config.configGroup
        val tailText = getExpressionTailText(context, config)

        configGroup.extendedDynamicValues[dynamicValueType]?.values?.forEach f@{ config0 ->
            ProgressManager.checkCanceled()
            val name = config0.name
            if (checkExtendedConfigName(name)) return@f
            val type = config0.type
            val element = config0.pointer.element
            val typeFile = config0.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(name).withPsiElement(element)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withItemTextUnderlined(true) //used for completions from extended configs
                .withPatchableIcon(PlsIcons.Nodes.DynamicValue(type))
                .withPatchableTailText(tailText)
                .forScriptExpression(context)
            result.addElement(lookupElement, context)
        }
    }

    private val ignoredCharsForExtendedConfigName = ".:<>[]".toCharArray()

    private fun checkExtendedConfigName(text: String): Boolean {
        //ignored if config name is empty
        if (text.isEmpty()) return true
        //ignored if config name is a template expression, ant expression or regex
        if (text.any { it in ignoredCharsForExtendedConfigName }) return true
        return false
    }

    //endregion
}
