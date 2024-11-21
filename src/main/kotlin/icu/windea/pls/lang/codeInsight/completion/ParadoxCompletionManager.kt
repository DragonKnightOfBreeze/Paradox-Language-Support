package icu.windea.pls.lang.codeInsight.completion

import com.intellij.application.options.*
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.patterns.*
import com.intellij.psi.*
import com.intellij.util.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.config.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.codeInsight.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.util.*
import icu.windea.pls.ep.config.*
import icu.windea.pls.ep.expression.*
import icu.windea.pls.ep.scope.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.expression.*
import icu.windea.pls.lang.expression.complex.*
import icu.windea.pls.lang.expression.complex.nodes.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.script.codeStyle.*
import icu.windea.pls.script.psi.*

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
        val configGroup = getConfigGroup(project, gameType)
        context.configGroup = configGroup
    }

    fun addRootKeyCompletions(memberElement: ParadoxScriptMemberElement, context: ProcessingContext, result: CompletionResultSet) {
        val elementPath = ParadoxExpressionPathManager.get(memberElement, PlsConstants.Settings.maxDefinitionDepth) ?: return
        if (elementPath.path.isParameterized()) return //忽略表达式路径带参数的情况

        context.isKey = true

        completeRootKey(context, result, elementPath)
    }

    fun addKeyCompletions(memberElement: ParadoxScriptMemberElement, context: ProcessingContext, result: CompletionResultSet) {
        val configContext = ParadoxExpressionManager.getConfigContext(memberElement)
        if (configContext == null) return
        if (!configContext.isRootOrMember()) {
            //仅提示不在定义声明中的rootKey    
            addRootKeyCompletions(memberElement, context, result)
            return
        }

        val configGroup = configContext.configGroup
        //这里不要使用合并后的子规则，需要先尝试精确匹配或者合并所有非精确匹配的规则，最后得到子规则列表
        val matchOptions = ParadoxExpressionMatcher.Options.Default or ParadoxExpressionMatcher.Options.Relax or ParadoxExpressionMatcher.Options.AcceptDefinition
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
        context.configGroup = configGroup
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

        context.config = null
        context.configs = emptyList()
        return
    }

    fun addValueCompletions(memberElement: ParadoxScriptMemberElement, context: ProcessingContext, result: CompletionResultSet) {
        val configContext = ParadoxExpressionManager.getConfigContext(memberElement)
        if (configContext == null) return
        if (!configContext.isRootOrMember()) return

        val configGroup = configContext.configGroup
        //这里不要使用合并后的子规则，需要先尝试精确匹配或者合并所有非精确匹配的规则，最后得到子规则列表
        val matchOptions = ParadoxExpressionMatcher.Options.Default or ParadoxExpressionMatcher.Options.Relax or ParadoxExpressionMatcher.Options.AcceptDefinition
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
        context.configGroup = configGroup
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

        context.config = null
        return
    }

    fun addPropertyValueCompletions(element: ParadoxScriptStringExpressionElement, propertyElement: ParadoxScriptProperty, context: ProcessingContext, result: CompletionResultSet) {
        val configContext = ParadoxExpressionManager.getConfigContext(element)
        if (configContext == null) return
        if (!configContext.isRootOrMember()) return

        val configGroup = configContext.configGroup
        val configs = configContext.getConfigs()
        if (configs.isEmpty()) return

        context.isKey = false
        context.configGroup = configGroup
        context.scopeContext = ParadoxScopeManager.getSwitchedScopeContext(propertyElement)

        for (config in configs) {
            if (config is CwtValueConfig) {
                context.config = config
                completeScriptExpression(context, result)
            }
        }

        context.config = null
        return
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
                val configExpression = config?.expression
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
    fun completeRootKey(context: ProcessingContext, result: CompletionResultSet, elementPath: ParadoxExpressionPath) {
        val originalFile = context.parameters?.originalFile ?: return
        val fileInfo = originalFile.fileInfo ?: return
        val gameType = context.gameType ?: return
        val configGroup = context.configGroup ?: return
        val path = fileInfo.path //这里使用pathToEntry
        val infoMap = mutableMapOf<String, MutableList<Tuple2<CwtTypeConfig, CwtSubtypeConfig?>>>()
        for (typeConfig in configGroup.types.values) {
            if (ParadoxDefinitionManager.matchesTypeByUnknownDeclaration(path, null, null, typeConfig)) {
                val skipRootKeyConfig = typeConfig.skipRootKey
                if (skipRootKeyConfig.isNullOrEmpty()) {
                    if (elementPath.isEmpty()) {
                        typeConfig.typeKeyFilter?.takeIfTrue()?.forEach {
                            infoMap.getOrPut(it) { mutableListOf() }.add(typeConfig to null)
                        }
                        typeConfig.subtypes.values.forEach { subtypeConfig ->
                            subtypeConfig.typeKeyFilter?.takeIfTrue()?.forEach {
                                infoMap.getOrPut(it) { mutableListOf() }.add(typeConfig to subtypeConfig)
                            }
                        }
                    }
                } else {
                    for (skipConfig in skipRootKeyConfig) {
                        val relative = elementPath.relativeTo(skipConfig) ?: continue
                        if (relative.isEmpty()) {
                            typeConfig.typeKeyFilter?.takeIfTrue()?.forEach {
                                infoMap.getOrPut(it) { mutableListOf() }.add(typeConfig to null)
                            }
                            typeConfig.subtypes.values.forEach { subtypeConfig ->
                                subtypeConfig.typeKeyFilter?.takeIfTrue()?.forEach {
                                    infoMap.getOrPut(it) { mutableListOf() }.add(typeConfig to subtypeConfig)
                                }
                            }
                        } else {
                            infoMap.getOrPut(relative) { mutableListOf() }
                        }
                        break
                    }
                }
            }
        }
        for ((key, tuples) in infoMap) {
            if (key == "any") return //skip any wildcard
            val typeConfigToUse = tuples.map { it.first }.distinctBy { it.name }.singleOrNull()
            val typeToUse = typeConfigToUse?.name
            //需要考虑不指定子类型的情况
            val subtypesToUse = when {
                typeConfigToUse == null || tuples.isEmpty() -> null
                else -> tuples.mapNotNull { it.second }.ifEmpty { null }?.distinctBy { it.name }?.map { it.name }
            }
            val config = if (typeToUse == null) null else {
                val declarationConfig = configGroup.declarations.get(typeToUse)
                if (declarationConfig == null) null else {
                    val configContext = CwtDeclarationConfigContextProvider.getContext(context.contextElement!!, null, typeToUse, subtypesToUse, gameType, configGroup)
                    configContext?.getConfig(declarationConfig)
                }
            }
            val element = config?.pointer?.element
            val icon = if (config != null) PlsIcons.Nodes.Definition(typeToUse) else PlsIcons.Nodes.Property
            val tailText = if (tuples.isEmpty()) null else tuples.joinToString(", ", " for ") { (typeConfig, subTypeConfig) ->
                if (subTypeConfig != null) "${typeConfig.name}.${subTypeConfig.name}" else typeConfig.name
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
        val configExpression = context.config!!.expression ?: return
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
            if (!scopeMatched1 && getSettings().completion.completeOnlyScopeIsMatched) return
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

    fun completeLocalisation(context: ProcessingContext, result: CompletionResultSet) {
        val config = context.config ?: return
        val keyword = context.keyword

        //优化：如果已经输入的关键词不是合法的本地化的名字，不要尝试进行本地化的代码补全
        if (keyword.isNotEmpty() && !PlsConstants.Patterns.localisationPropertyNameRegex.matches(keyword)) return

        //本地化的提示结果可能有上千条，因此这里改为先按照输入的关键字过滤结果，关键字变更时重新提示
        result.restartCompletionOnPrefixChange(StandardPatterns.string().shorterThan(keyword.length))

        val configGroup = config.configGroup
        val project = configGroup.project
        val contextElement = context.contextElement
        val tailText = getExpressionTailText(context, config)
        val selector = localisationSelector(project, contextElement)
            .contextSensitive()
            .preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
        //.distinctByName() //这里selector不需要指定去重
        val processor = LimitedCompletionProcessor<ParadoxLocalisationProperty> { localisation ->
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
        DumbService.getInstance(project).runReadActionInSmartMode {
            ParadoxLocalisationSearch.processVariants(result.prefixMatcher, selector, processor)
        }
    }

    fun completeSyncedLocalisation(context: ProcessingContext, result: CompletionResultSet) {
        val config = context.config ?: return
        val keyword = context.keyword

        //优化：如果已经输入的关键词不是合法的本地化的名字，不要尝试进行本地化的代码补全
        if (keyword.isNotEmpty() && !PlsConstants.Patterns.localisationPropertyNameRegex.matches(keyword)) return

        //本地化的提示结果可能有上千条，因此这里改为先按照输入的关键字过滤结果，关键字变更时重新提示
        result.restartCompletionOnPrefixChange(StandardPatterns.string().shorterThan(keyword.length))

        val configGroup = config.configGroup
        val project = configGroup.project
        val contextElement = context.contextElement
        val tailText = getExpressionTailText(context, config)
        val selector = localisationSelector(project, contextElement)
            .contextSensitive()
            .preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
        //.distinctByName() //这里selector不需要指定去重
        val processor = LimitedCompletionProcessor<ParadoxLocalisationProperty> { syncedLocalisation ->
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
        DumbService.getInstance(project).runReadActionInSmartMode {
            ParadoxSyncedLocalisationSearch.processVariants(result.prefixMatcher, selector, processor)
        }
    }

    fun completeDefinition(context: ProcessingContext, result: CompletionResultSet) {
        val config = context.config ?: return
        val scopeContext = context.scopeContext
        val typeExpression = config.expression?.value ?: return
        val configGroup = config.configGroup
        val project = configGroup.project
        val contextElement = context.contextElement
        val tailText = getExpressionTailText(context, config)
        val selector = definitionSelector(project, contextElement).contextSensitive().distinctByName()
        ParadoxDefinitionSearch.search(typeExpression, selector).processQueryAsync p@{ definition ->
            ProgressManager.checkCanceled()
            val definitionInfo = definition.definitionInfo ?: return@p true
            if (definitionInfo.name.isEmpty()) return@p true //ignore anonymous definitions

            //apply extraFilter since it's necessary
            if (context.extraFilter?.invoke(definition) == false) return@p true

            //排除不匹配可能存在的supported_scopes的情况
            val supportedScopes = ParadoxDefinitionSupportedScopesProvider.getSupportedScopes(definition, definitionInfo)
            val scopeMatched = ParadoxScopeManager.matchesScope(scopeContext, supportedScopes, configGroup)
            if (!scopeMatched && getSettings().completion.completeOnlyScopeIsMatched) return@p true

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
        val configExpression = config.expression ?: return
        val contextElement = context.contextElement
        val pathReferenceExpressionSupport = ParadoxPathReferenceExpressionSupport.get(configExpression)
        if (pathReferenceExpressionSupport != null) {
            val tailText = getExpressionTailText(context, config)
            val fileExtensions = when (config) {
                is CwtMemberConfig<*> -> ParadoxFilePathManager.getFileExtensionOptionValues(config)
                else -> emptySet()
            }
            //仅提示匹配file_extensions选项指定的扩展名的，如果存在
            val selector = fileSelector(project, contextElement).contextSensitive()
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
        val enumName = config.expression?.value ?: return
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
            val selector = complexEnumValueSelector(project, contextElement)
                .withSearchScopeType(searchScope)
                .contextSensitive()
                .distinctByName()
            ParadoxComplexEnumValueSearch.search(enumName, selector).processQueryAsync { info ->
                ProgressManager.checkCanceled()
                val name = info.name
                val element = ParadoxComplexEnumValueElement(contextElement, info, project)
                val lookupElement = LookupElementBuilder.create(element, name)
                    .withTypeText(typeFile?.name, typeFile?.icon, true)
                    .withPriority(ParadoxCompletionPriorities.complexEnumValue)
                    .withPatchableIcon(PlsIcons.Nodes.ComplexEnumValue)
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
        val configExpression = config.expression ?: return
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

    fun completeTemplateExpression(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val contextElement = context.contextElement!!
        val configGroup = context.configGroup!!
        val config = context.config!!
        val scopeMatched = context.scopeMatched

        if (contextElement !is ParadoxScriptStringExpressionElement) return
        val configExpression = config.expression ?: return
        val template = CwtTemplateExpression.resolve(configExpression.expressionString)
        val tailText = getExpressionTailText(context, config)
        CwtTemplateExpressionManager.processResolveResult(contextElement, template, configGroup) { expression ->
            val templateExpressionElement = ParadoxExpressionManager.resolveTemplateExpression(contextElement, expression, configExpression, configGroup)
            val lookupElement = LookupElementBuilder.create(expression).withPsiElement(templateExpressionElement)
                .withCaseSensitivity(false)
                .withPatchableIcon(PlsIcons.Nodes.TemplateExpression)
                .withPatchableTailText(tailText)
                .withScopeMatched(scopeMatched)
                .forScriptExpression(context)
            result.addElement(lookupElement, context)
            true
        }
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
        configGroup.inlineConfigGroup["inline_script"]?.forEach f@{ config0 ->
            ProgressManager.checkCanceled()
            context.config = config0
            context.isKey = true
            val name = config0.name
            val element = config0.pointer.element ?: return@f
            val typeFile = config0.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withCaseSensitivity(false)
                .withPriority(ParadoxCompletionPriorities.constant)
                .withPatchableIcon(PlsIcons.Nodes.Inline)
                .forScriptExpression(context)
            result.addElement(lookupElement, context)
        }
    }
    //endregion

    //region Complex Expression Completion Methods
    inline fun <T> markIncomplete(action: () -> T): T {
        try {
            PlsStates.incompleteComplexExpression.set(true)
            return action()
        } finally {
            PlsStates.incompleteComplexExpression.remove()
        }
    }

    fun completeDynamicValueExpression(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val keyword = context.keyword
        val keywordOffset = context.keywordOffset
        val configGroup = context.configGroup ?: return
        val config = context.config
        val configs = context.configs

        val finalConfigs = if (configs.isNotEmpty()) configs.toListOrThis() else config.toSingletonListOrEmpty()
        if (finalConfigs.isEmpty()) return

        val textRange = TextRange.create(keywordOffset, keywordOffset + keyword.length)
        val expression = markIncomplete { ParadoxDynamicValueExpression.resolve(keyword, textRange, configGroup, finalConfigs) } ?: return

        val scopeContext = context.scopeContext ?: ParadoxScopeManager.getAnyScopeContext()
        val isKey = context.isKey
        context.config = expression.configs.first()
        context.configs = expression.configs
        context.scopeContext = null //skip check scope context here
        context.isKey = null

        val offset = context.offsetInParent!! - context.expressionOffset
        if (offset < 0) return //unexpected
        for (node in expression.nodes) {
            val inRange = offset >= node.rangeInExpression.startOffset && offset <= node.rangeInExpression.endOffset
            if (node is ParadoxDynamicValueNode) {
                if (inRange) {
                    val keywordToUse = node.text.substring(0, offset - node.rangeInExpression.startOffset)
                    val resultToUse = result.withPrefixMatcher(keywordToUse)
                    context.keyword = keywordToUse
                    context.keywordOffset = node.rangeInExpression.startOffset
                    completeDynamicValue(context, resultToUse)
                    break
                }
            } else if (node is ParadoxScopeFieldExpression) {
                if (inRange) {
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
        val keyword = context.keyword
        val keywordOffset = context.keywordOffset
        val configGroup = context.configGroup ?: return

        val textRange = TextRange.create(keywordOffset, keywordOffset + keyword.length)
        val expression = markIncomplete { ParadoxScopeFieldExpression.resolve(keyword, textRange, configGroup) } ?: return

        val element = context.contextElement?.castOrNull<ParadoxExpressionElement>() ?: return
        val scopeContext = context.scopeContext ?: ParadoxScopeManager.getAnyScopeContext()
        val isKey = context.isKey
        context.isKey = null

        val offset = context.offsetInParent!! - context.expressionOffset
        if (offset < 0) return //unexpected
        var scopeContextInExpression = scopeContext
        for (node in expression.nodes) {
            val inRange = offset >= node.rangeInExpression.startOffset && offset <= node.rangeInExpression.endOffset
            if (!inRange) {
                if (node is ParadoxErrorNode || node.text.isEmpty()) break //skip error or empty nodes
            }
            if (node is ParadoxScopeLinkNode) {
                if (inRange) {
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

        val offset = context.offsetInParent!! - context.expressionOffset
        if (offset < 0) return //unexpected
        for (node in expression.nodes) {
            val inRange = offset >= node.rangeInExpression.startOffset && offset <= node.rangeInExpression.endOffset
            if (node is ParadoxScriptValueNode) {
                if (inRange) {
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
            } else if (node is ParadoxScriptValueArgumentValueNode && getSettings().inference.configContextForParameters) {
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
        val keyword = context.keyword
        val keywordOffset = context.keywordOffset
        val configGroup = context.configGroup ?: return

        val textRange = TextRange.create(keywordOffset, keywordOffset + keyword.length)
        val expression = markIncomplete { ParadoxValueFieldExpression.resolve(keyword, textRange, configGroup) } ?: return

        val element = context.contextElement?.castOrNull<ParadoxExpressionElement>() ?: return
        val scopeContext = context.scopeContext ?: ParadoxScopeManager.getAnyScopeContext()
        val isKey = context.isKey
        context.isKey = null

        val offset = context.offsetInParent!! - context.expressionOffset
        if (offset < 0) return //unexpected
        var scopeContextInExpression = scopeContext
        for (node in expression.nodes) {
            val inRange = offset >= node.rangeInExpression.startOffset && offset <= node.rangeInExpression.endOffset
            if (!inRange) {
                if (node is ParadoxErrorNode || node.text.isEmpty()) break //skip error or empty nodes
            }
            if (node is ParadoxScopeLinkNode) {
                if (inRange) {
                    context.scopeContext = scopeContextInExpression
                    completeForScopeLinkNode(node, context, result)
                    break
                } else {
                    scopeContextInExpression = ParadoxScopeManager.getSwitchedScopeContextOfNode(element, node, scopeContextInExpression)
                }
            } else if (node is ParadoxValueFieldNode) {
                if (inRange) {
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
        val keyword = context.keyword
        val keywordOffset = context.keywordOffset
        val configGroup = context.configGroup ?: return

        val textRange = TextRange.create(keywordOffset, keywordOffset + keyword.length)
        val expression = markIncomplete { ParadoxVariableFieldExpression.resolve(keyword, textRange, configGroup) } ?: return

        val element = context.contextElement?.castOrNull<ParadoxExpressionElement>() ?: return
        val scopeContext = context.scopeContext ?: ParadoxScopeManager.getAnyScopeContext()
        val isKey = context.isKey
        context.isKey = null

        val offset = context.offsetInParent!! - context.expressionOffset
        if (offset < 0) return //unexpected
        var scopeContextInExpression = scopeContext
        for (node in expression.nodes) {
            val inRange = offset >= node.rangeInExpression.startOffset && offset <= node.rangeInExpression.endOffset
            if (!inRange) {
                if (node is ParadoxErrorNode || node.text.isEmpty()) break //skip error or empty nodes
            }
            if (node is ParadoxScopeLinkNode) {
                if (inRange) {
                    context.scopeContext = scopeContextInExpression
                    completeForScopeLinkNode(node, context, result)
                    break
                } else {
                    scopeContextInExpression = ParadoxScopeManager.getSwitchedScopeContextOfNode(element, node, scopeContextInExpression)
                }
            } else if (node is ParadoxDataSourceNode) {
                if (inRange) {
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
        val keyword = context.keyword
        val keywordOffset = context.keywordOffset
        val configGroup = context.configGroup ?: return

        val textRange = TextRange.create(keywordOffset, keywordOffset + keyword.length)
        val expression = markIncomplete { ParadoxCommandExpression.resolve(keyword, textRange, configGroup) } ?: return

        val element = context.contextElement?.castOrNull<ParadoxExpressionElement>() ?: return
        val scopeContext = context.scopeContext ?: ParadoxScopeManager.getAnyScopeContext()
        val isKey = context.isKey
        context.isKey = null

        val offset = context.offsetInParent!! - context.expressionOffset
        if (offset < 0) return //unexpected
        var scopeContextInExpression = scopeContext
        for (node in expression.nodes) {
            val inRange = offset >= node.rangeInExpression.startOffset && offset <= node.rangeInExpression.endOffset
            if (!inRange) {
                if (node is ParadoxErrorNode || node.text.isEmpty()) break //skip error or empty nodes
            }
            if (node is ParadoxCommandScopeLinkNode) {
                if (inRange) {
                    context.scopeContext = scopeContextInExpression
                    completeForCommandScopeLinkNode(node, context, result)
                    break
                } else {
                    scopeContextInExpression = ParadoxScopeManager.getSwitchedScopeContextOfNode(element, node, scopeContextInExpression)
                }
            } else if (node is ParadoxCommandFieldNode) {
                if (inRange) {
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
        val keyword = context.keyword
        val keywordOffset = context.keywordOffset
        val configGroup = context.configGroup ?: return

        val textRange = TextRange.create(keywordOffset, keywordOffset + keyword.length)
        val expression = markIncomplete { ParadoxDatabaseObjectExpression.resolve(keyword, textRange, configGroup) } ?: return

        val oldNode = context.node
        val isKey = context.isKey
        context.isKey = null

        val offset = context.offsetInParent!! - context.expressionOffset
        if (offset < 0) return //unexpected
        for (node in expression.nodes) {
            val inRange = offset >= node.rangeInExpression.startOffset && offset <= node.rangeInExpression.endOffset
            if (node is ParadoxDatabaseObjectTypeNode) {
                if (inRange) {
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
        val keyword = context.keyword
        val keywordOffset = context.keywordOffset
        val configGroup = context.configGroup ?: return

        val textRange = TextRange.create(keywordOffset, keywordOffset + keyword.length)
        val expression = markIncomplete { ParadoxDefineReferenceExpression.resolve(keyword, textRange, configGroup) } ?: return

        val oldNode = context.node
        val isKey = context.isKey
        context.isKey = null

        val offset = context.offsetInParent!! - context.expressionOffset
        if (offset < 0) return //unexpected
        for (node in expression.nodes) {
            val inRange = offset >= node.rangeInExpression.startOffset && offset <= node.rangeInExpression.endOffset
            //TODO 1.3.25
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
        val element = context.contextElement?.castOrNull<ParadoxExpressionElement>() ?: return false
        val offsetInParent = context.offsetInParent!!
        val scopeContext = context.scopeContext ?: ParadoxScopeManager.getAnyScopeContext()
        val nodeRange = node.rangeInExpression
        val scopeLinkNode = node.castOrNull<ParadoxDynamicScopeLinkNode>()
        val prefixNode = scopeLinkNode?.prefixNode
        val dsNode = scopeLinkNode?.dataSourceNode
        val inDsNode = dsNode?.nodes?.first()
        val endOffset = dsNode?.rangeInExpression?.startOffset ?: -1
        if (prefixNode != null && dsNode != null && offsetInParent >= dsNode.rangeInExpression.startOffset) {
            context.scopeContext = ParadoxScopeManager.getSwitchedScopeContextOfNode(element, node, scopeContext)

            val keywordToUse = dsNode.text.substring(0, offsetInParent - endOffset)
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
                || offsetInParent <= dsNode.nodes.first().rangeInExpression.endOffset
            val keywordToUse = node.text.substring(0, offsetInParent - nodeRange.startOffset)
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
        val element = context.contextElement?.castOrNull<ParadoxExpressionElement>() ?: return false
        val keyword = context.keyword
        val keywordOffset = context.keywordOffset
        val offsetInParent = context.offsetInParent!!
        val scopeContext = context.scopeContext ?: ParadoxScopeManager.getAnyScopeContext()
        val nodeRange = node.rangeInExpression
        val valueFieldNode = node.castOrNull<ParadoxDynamicValueFieldNode>()
        val prefixNode = valueFieldNode?.prefixNode
        val dsNode = valueFieldNode?.dataSourceNode
        val inDsNode = dsNode?.nodes?.first()
        val endOffset = dsNode?.rangeInExpression?.startOffset ?: -1
        if (prefixNode != null && dsNode != null && offsetInParent >= dsNode.rangeInExpression.startOffset) {
            context.scopeContext = ParadoxScopeManager.getSwitchedScopeContextOfNode(element, node, scopeContext)

            val keywordToUse = dsNode.text.substring(0, offsetInParent - endOffset)
            val resultToUse = result.withPrefixMatcher(keywordToUse)
            context.keyword = keywordToUse
            context.keywordOffset = dsNode.rangeInExpression.startOffset
            completeValueFieldValue(context, resultToUse, prefixNode.text, inDsNode)
            context.keyword = keyword
            context.keywordOffset = keywordOffset
            context.scopeContext = scopeContext
            return true
        } else {
            val inFirstNode = dsNode == null || dsNode.nodes.isEmpty()
                || offsetInParent <= dsNode.nodes.first().rangeInExpression.endOffset
            val keywordToUse = node.text.substring(0, offsetInParent - nodeRange.startOffset)
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
        val offsetInParent = context.offsetInParent!!
        val nodeRange = node.rangeInExpression
        val keywordToUse = node.text.substring(0, offsetInParent - nodeRange.startOffset)
        val resultToUse = result.withPrefixMatcher(keywordToUse)
        context.keyword = keywordToUse
        completeValueFieldValue(context, resultToUse, null, node, variableOnly = true)
    }

    /**
     * @return 是否已经输入了前缀。
     */
    private fun completeForCommandScopeLinkNode(node: ParadoxCommandScopeLinkNode, context: ProcessingContext, result: CompletionResultSet): Boolean {
        val element = context.contextElement?.castOrNull<ParadoxExpressionElement>() ?: return false
        val offsetInParent = context.offsetInParent!!
        val scopeContext = context.scopeContext ?: ParadoxScopeManager.getAnyScopeContext()
        val nodeRange = node.rangeInExpression
        val scopeLinkNode = node.castOrNull<ParadoxDynamicCommandScopeLinkNode>()
        val prefixNode = scopeLinkNode?.prefixNode
        val dsNode = scopeLinkNode?.dataSourceNode
        val endOffset = dsNode?.rangeInExpression?.startOffset ?: -1
        if (prefixNode != null && dsNode != null && offsetInParent >= dsNode.rangeInExpression.startOffset) {
            context.scopeContext = ParadoxScopeManager.getSwitchedScopeContextOfNode(element, node, scopeContext)

            val keywordToUse = dsNode.text.substring(0, offsetInParent - endOffset)
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
                || offsetInParent <= dsNode.nodes.first().rangeInExpression.endOffset
            val keywordToUse = node.text.substring(0, offsetInParent - nodeRange.startOffset)
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
        val keyword = context.keyword
        val keywordOffset = context.keywordOffset
        val offsetInParent = context.offsetInParent!!
        val nodeRange = node.rangeInExpression

        val keywordToUse = node.text.substring(0, offsetInParent - nodeRange.startOffset)
        val resultToUse = result.withPrefixMatcher(keywordToUse)
        context.keyword = keywordToUse
        context.keywordOffset = node.rangeInExpression.startOffset
        completePredefinedCommandField(context, resultToUse)
        completeScriptedLoc(context, resultToUse)
        completeVariable(context, resultToUse)
        context.keyword = keyword
        context.keywordOffset = keywordOffset

        return false
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

        val linksConfigs = configGroup.links.values.filter { it.forScope() && !it.fromData }
        for (linkConfig in linksConfigs) {
            val scopeMatched = ParadoxScopeManager.matchesScope(scopeContext, linkConfig.inputScopes, configGroup)
            if (!scopeMatched && getSettings().completion.completeOnlyScopeIsMatched) continue

            val name = linkConfig.name
            val element = linkConfig.pointer.element ?: continue
            val tailText = " from scopes"
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

        val linkConfigs = configGroup.links.values.filter { it.forScope() && it.fromData && it.prefix != null }
        for (linkConfig in linkConfigs) {
            val scopeMatched = ParadoxScopeManager.matchesScope(scopeContext, linkConfig.inputScopes, configGroup)
            if (!scopeMatched && getSettings().completion.completeOnlyScopeIsMatched) continue

            val name = linkConfig.prefix ?: continue
            val element = linkConfig.pointer.element ?: continue
            val tailText = " from scope link ${linkConfig.name}"
            val typeFile = linkConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withBoldness(true)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withScopeMatched(scopeMatched)
                .withPriority(ParadoxCompletionPriorities.linkPrefix)
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

        if (inDsNode is ParadoxScopeLinkNode) {
            completeForScopeLinkNode(inDsNode, context, result)
            context.scopeContext = scopeContext
            return
        }
        if (inDsNode is ParadoxDynamicValueExpression) {
            completeDynamicValueExpression(context, result)
            return
        }

        context.configs = linkConfigs
        for (linkConfig in linkConfigs) {
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

        val linkConfigs = configGroup.links.values.filter { it.forValue() && !it.fromData }
        for (linkConfig in linkConfigs) {
            //排除input_scopes不匹配前一个scope的output_scope的情况
            val scopeMatched = ParadoxScopeManager.matchesScope(scopeContext, linkConfig.inputScopes, configGroup)
            if (!scopeMatched && getSettings().completion.completeOnlyScopeIsMatched) continue

            val name = linkConfig.name
            val element = linkConfig.pointer.element ?: continue
            val tailText = " from values"
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

        val linkConfigs = configGroup.links.values.filter { it.forValue() && it.fromData && it.prefix != null }
        for (linkConfig in linkConfigs) {
            val scopeMatched = ParadoxScopeManager.matchesScope(scopeContext, linkConfig.inputScopes, configGroup)
            if (!scopeMatched && getSettings().completion.completeOnlyScopeIsMatched) continue

            val name = linkConfig.prefix ?: continue
            val element = linkConfig.pointer.element ?: continue
            val tailText = " from value link ${linkConfig.name}"
            val typeFile = linkConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withBoldness(true)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withPriority(ParadoxCompletionPriorities.linkPrefix)
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
        }

        if (inDsNode is ParadoxDynamicValueExpression) {
            completeDynamicValueExpression(context, result)
            return
        }
        if (inDsNode is ParadoxScriptValueExpression) {
            completeScriptValueExpression(context, result)
            return
        }

        context.configs = linkConfigs
        for (linkConfig in linkConfigs) {
            context.config = linkConfig
            completeScriptExpression(context, result)
        }
        context.config = config
        context.configs = configs
    }

    fun completeCommandScope(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val configGroup = context.configGroup!!
        val scopeContext = context.scopeContext

        val localisationLinks = configGroup.localisationLinks
        for (localisationScope in localisationLinks.values) {
            val scopeMatched = ParadoxScopeManager.matchesScope(scopeContext, localisationScope.inputScopes, configGroup)
            if (!scopeMatched && getSettings().completion.completeOnlyScopeIsMatched) continue

            //optimize: make first char uppercase (e.g., owner -> Owner)
            val name = localisationScope.name.replaceFirstChar { it.uppercaseChar() }
            val element = localisationScope.pointer.element ?: continue
            val tailText = " from command scopes"
            val typeFile = localisationScope.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withIcon(PlsIcons.LocalisationNodes.CommandScope)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withCaseSensitivity(false) //忽略大小写
                .withScopeMatched(scopeMatched)
                .withPriority(ParadoxCompletionPriorities.scope)
                .withCompletionId()
            result.addElement(lookupElement, context)
        }
    }

    fun completePredefinedCommandField(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val configGroup = context.configGroup!!
        val scopeContext = context.scopeContext

        val localisationCommands = configGroup.localisationCommands
        for (localisationCommand in localisationCommands.values) {
            val scopeMatched = ParadoxScopeManager.matchesScope(scopeContext, localisationCommand.supportedScopes, configGroup)
            if (!scopeMatched && getSettings().completion.completeOnlyScopeIsMatched) continue

            val name = localisationCommand.name
            val element = localisationCommand.pointer.element ?: continue
            val tailText = " from command fields"
            val typeFile = localisationCommand.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withIcon(PlsIcons.LocalisationNodes.CommandField)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withCaseSensitivity(false) //忽略大小写
                .withScopeMatched(scopeMatched)
                .withCompletionId()
            result.addElement(lookupElement, context)
        }
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
                .withPriority(ParadoxCompletionPriorities.databaseObjectType)
                .withPatchableIcon(PlsIcons.Nodes.DynamicValue)
                .withPatchableTailText(tailText)
                .forScriptExpression(context)
            result.addElement(lookupElement, context)
        }
    }

    fun completeDatabaseObject(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val configGroup = context.configGroup!!
        val node = context.node?.castOrNull<ParadoxDatabaseObjectNode>() ?: return
        val config = node.config ?: return
        val typeToSearch = if (node.isBase) config.type else config.swapType
        if (typeToSearch == null) return

        val tailText = " from database object type ${config.name}"

        run {
            //complete forced base database object
            if (!node.isPossibleForcedBase()) return@run
            val valueNode = node.expression.valueNode ?: return@run
            val project = configGroup.project
            val contextElement = context.contextElement
            val selector = definitionSelector(project, contextElement).contextSensitive().distinctByName()
            ParadoxDefinitionSearch.search(valueNode.text, config.type, selector).processQueryAsync p@{ definition ->
                ProgressManager.checkCanceled()
                val definitionInfo = definition.definitionInfo ?: return@p true
                if (definitionInfo.name.isEmpty()) return@p true //ignore anonymous definitions

                val name = definitionInfo.name
                val typeFile = definition.containingFile
                val lookupElement = LookupElementBuilder.create(definition, name)
                    .withTypeText(typeFile.name, typeFile.icon, true)
                    .withPatchableIcon(PlsIcons.Nodes.Definition(definitionInfo.type))
                    .withPatchableTailText(tailText)
                    .withDefinitionLocalizedNamesIfNecessary(definition)
                    .forScriptExpression(context)
                result.addElement(lookupElement, context)
                true
            }
        }

        val extraFilter = f@{ e: PsiElement ->
            val definition = e as? ParadoxScriptDefinitionElement ?: return@f true
            node.isValidDatabaseObject(definition, typeToSearch)
        }
        val mockConfig = CwtValueConfig.resolve(emptyPointer(), configGroup, "<$typeToSearch>")
        val oldExtraFilter = context.extraFilter
        val oldConfig = context.config
        val oldTailText = context.expressionTailText
        context.extraFilter = extraFilter
        context.config = mockConfig
        context.expressionTailText = tailText
        completeDefinition(context, result)
        context.extraFilter = oldExtraFilter
        context.config = oldConfig
        context.expressionTailText = oldTailText
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

            val configExpression = config.expression ?: return
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
                val selector = dynamicValueSelector(project, contextElement).distinctByName()
                ParadoxDynamicValueSearch.search(dynamicValueType, selector).processQueryAsync p@{ info ->
                    ProgressManager.checkCanceled()
                    if (info.name == keyword) return@p true //排除和当前输入的同名的
                    val element = ParadoxDynamicValueElement(contextElement, info, project)
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

    fun completeCommandScopeLinkPrefix(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val configGroup = context.configGroup!!
        val scopeContext = context.scopeContext

        val linkConfigs = configGroup.localisationLinks.values.filter { it.forScope() && it.fromData && it.prefix != null }
        for (linkConfig in linkConfigs) {
            val scopeMatched = ParadoxScopeManager.matchesScope(scopeContext, linkConfig.inputScopes, configGroup)
            if (!scopeMatched && getSettings().completion.completeOnlyScopeIsMatched) continue

            val name = linkConfig.prefix ?: continue
            val element = linkConfig.pointer.element ?: continue
            val tailText = " from command scope link ${linkConfig.name}"
            val typeFile = linkConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withBoldness(true)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withScopeMatched(scopeMatched)
                .withPriority(ParadoxCompletionPriorities.linkPrefix)
                .withCompletionId()
            result.addElement(lookupElement, context)
        }
    }

    fun completeCommandScopeLinkValue(context: ProcessingContext, result: CompletionResultSet, prefix: String?) {
        ProgressManager.checkCanceled()
        val configGroup = context.configGroup!!
        val configs = context.configs

        val linkConfigs = when {
            prefix == null -> configGroup.localisationLinksOfEventTarget
            else -> configGroup.localisationLinks.values.filter { it.forScope() && it.prefix == prefix }
        }

        context.configs = linkConfigs
        completeDynamicValue(context, result)
        context.configs = configs
    }

    fun completeScriptedLoc(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val file = context.parameters?.originalFile ?: return
        val project = file.project

        val scriptedLocSelector = definitionSelector(project, file).contextSensitive().distinctByName()
        ParadoxDefinitionSearch.search("scripted_loc", scriptedLocSelector).processQueryAsync p@{ scriptedLoc ->
            ProgressManager.checkCanceled()
            val name = scriptedLoc.definitionInfo?.name ?: return@p true //不应该为空
            val icon = PlsIcons.Nodes.Definition("scripted_loc")
            val tailText = " from <scripted_loc>"
            val typeFile = scriptedLoc.containingFile
            val lookupElement = LookupElementBuilder.create(scriptedLoc, name).withIcon(icon)
                .withTailText(tailText, true)
                .withTypeText(typeFile.name, typeFile.icon, true)
                .withCaseSensitivity(false) //忽略大小写
                .withCompletionId()
            result.addElement(lookupElement, context)
            true
        }
    }

    fun completeVariable(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val file = context.parameters?.originalFile ?: return
        val project = file.project
        val contextElement = context.contextElement!!
        val keyword = context.keyword

        val variableSelector = dynamicValueSelector(project, file).contextSensitive().distinctByName()
        ParadoxDynamicValueSearch.search("variable", variableSelector).processQueryAsync p@{ info ->
            ProgressManager.checkCanceled()
            if (info.name == keyword) return@p true //排除和当前输入的同名的
            val element = ParadoxDynamicValueElement(contextElement, info, project)
            val icon = PlsIcons.Nodes.Variable
            val tailText = " from variables"
            val lookupElement = LookupElementBuilder.create(element, info.name)
                .withIcon(icon)
                .withTailText(tailText, true)
                .withCaseSensitivity(false) //忽略大小写
                .withCompletionId()
            result.addElement(lookupElement, context)
            true
        }
    }
    //endregion

    //region Extended Completion Methods
    fun completeExtendedScriptedVariable(context: ProcessingContext, result: CompletionResultSet) {
        if (!getSettings().completion.completeByExtendedConfigs) return
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
        if (!getSettings().completion.completeByExtendedConfigs) return
        ProgressManager.checkCanceled()

        val config = context.config ?: return
        val typeExpression = config.expression?.value ?: return
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
            if (typeExpression != "game_rule") return@r1
            configGroup.extendedGameRules.values.forEach f@{ config0 ->
                ProgressManager.checkCanceled()
                val name = config0.name
                if (checkExtendedConfigName(name)) return@f
                val element = config0.pointer.element
                val typeFile = config0.pointer.containingFile
                val lookupElement = LookupElementBuilder.create(name).withPsiElement(element)
                    .withTypeText(typeFile?.name, typeFile?.icon, true)
                    .withItemTextUnderlined(true) //used for completions from extended configs
                    .withPatchableIcon(PlsIcons.Nodes.Definition("game_rule"))
                    .withPatchableTailText(tailText)
                    .forScriptExpression(context)
                result.addElement(lookupElement, context)
            }
        }
        run r1@{
            if (typeExpression != "on_action") return@r1
            configGroup.extendedOnActions.values.forEach f@{ config0 ->
                ProgressManager.checkCanceled()
                val name = config0.name
                if (checkExtendedConfigName(name)) return@f
                val element = config0.pointer.element
                val typeFile = config0.pointer.containingFile
                val lookupElement = LookupElementBuilder.create(name).withPsiElement(element)
                    .withTypeText(typeFile?.name, typeFile?.icon, true)
                    .withItemTextUnderlined(true) //used for completions from extended configs
                    .withPatchableIcon(PlsIcons.Nodes.Definition("on_action"))
                    .withPatchableTailText(tailText)
                    .forScriptExpression(context)
                result.addElement(lookupElement, context)
            }
        }
    }

    fun completeExtendedInlineScript(context: ProcessingContext, result: CompletionResultSet) {
        if (!getSettings().completion.completeByExtendedConfigs) return
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
        if (!getSettings().completion.completeByExtendedConfigs) return
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
        if (!getSettings().completion.completeByExtendedConfigs) return
        ProgressManager.checkCanceled()

        val config = context.config ?: return
        val enumName = config.expression?.value ?: return
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
                .withPatchableIcon(PlsIcons.Nodes.ComplexEnumValue)
                .withPatchableTailText(tailText)
                .forScriptExpression(context)
            result.addElement(lookupElement, context)
        }
    }

    fun completeExtendedDynamicValue(context: ProcessingContext, result: CompletionResultSet) {
        if (!getSettings().completion.completeByExtendedConfigs) return
        ProgressManager.checkCanceled()

        val config = context.config ?: return
        val dynamicValueType = config.expression?.value ?: return
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
