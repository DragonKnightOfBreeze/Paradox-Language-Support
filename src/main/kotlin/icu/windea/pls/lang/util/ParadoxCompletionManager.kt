package icu.windea.pls.lang.util

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.patterns.*
import com.intellij.util.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.ep.config.*
import icu.windea.pls.ep.data.*
import icu.windea.pls.ep.expression.*
import icu.windea.pls.ep.scope.*
import icu.windea.pls.lang.codeInsight.completion.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.CwtConfigMatcher.Options
import icu.windea.pls.model.*
import icu.windea.pls.model.expression.*
import icu.windea.pls.model.expression.complex.*
import icu.windea.pls.model.expression.complex.nodes.*
import icu.windea.pls.model.path.*
import icu.windea.pls.script.psi.*
import kotlin.collections.component1
import kotlin.collections.component2

object ParadoxCompletionManager {
    //region Core Methods
    fun addRootKeyCompletions(memberElement: ParadoxScriptMemberElement, context: ProcessingContext, result: CompletionResultSet) {
        val elementPath = ParadoxElementPathHandler.get(memberElement, PlsConstants.maxDefinitionDepth) ?: return
        
        context.isKey = true
        
        completeRootKey(context, result, elementPath)
    }
    
    fun addKeyCompletions(memberElement: ParadoxScriptMemberElement, context: ProcessingContext, result: CompletionResultSet) {
        val configContext = CwtConfigHandler.getConfigContext(memberElement)
        if(configContext == null) return
        if(!configContext.isRootOrMember()) {
            //仅提示不在定义声明中的rootKey    
            addRootKeyCompletions(memberElement, context, result)
            return
        }
        
        val configGroup = configContext.configGroup
        //这里不要使用合并后的子规则，需要先尝试精确匹配或者合并所有非精确匹配的规则，最后得到子规则列表
        val matchOptions = Options.Default or Options.Relax or Options.AcceptDefinition
        val parentConfigs = CwtConfigHandler.getConfigs(memberElement, matchOptions = matchOptions)
        val configs = mutableListOf<CwtPropertyConfig>()
        parentConfigs.forEach { c1 ->
            c1.configs?.forEach { c2 ->
                if(c2 is CwtPropertyConfig) {
                    //这里需要进行必要的内联
                    val c3 = CwtConfigManipulator.inlineSingleAlias(c2) ?: c2
                    configs.add(c3)
                }
            }
        }
        if(configs.isEmpty()) return
        val occurrenceMap = CwtConfigHandler.getChildOccurrenceMap(memberElement, parentConfigs)
        
        context.isKey = true
        context.configGroup = configGroup
        context.scopeContext = ParadoxScopeHandler.getScopeContext(memberElement)
        
        configs.groupBy { it.key }.forEach { (_, configsWithSameKey) ->
            for(config in configsWithSameKey) {
                if(shouldComplete(config, occurrenceMap)) {
                    val overriddenConfigs = CwtOverriddenConfigProvider.getOverriddenConfigs(context.contextElement!!, config)
                    if(overriddenConfigs.isNotNullOrEmpty()) {
                        for(overriddenConfig in overriddenConfigs) {
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
        val configContext = CwtConfigHandler.getConfigContext(memberElement)
        if(configContext == null) return
        if(!configContext.isRootOrMember()) return
        
        val configGroup = configContext.configGroup
        //这里不要使用合并后的子规则，需要先尝试精确匹配或者合并所有非精确匹配的规则，最后得到子规则列表
        val matchOptions = Options.Default or Options.Relax or Options.AcceptDefinition
        val parentConfigs = CwtConfigHandler.getConfigs(memberElement, matchOptions = matchOptions)
        val configs = mutableListOf<CwtValueConfig>()
        parentConfigs.forEach { c1 ->
            c1.configs?.forEach { c2 ->
                if(c2 is CwtValueConfig) {
                    configs.add(c2)
                }
            }
        }
        if(configs.isEmpty()) return
        val occurrenceMap = CwtConfigHandler.getChildOccurrenceMap(memberElement, parentConfigs)
        
        context.isKey = false
        context.configGroup = configGroup
        context.scopeContext = ParadoxScopeHandler.getScopeContext(memberElement)
        
        for(config in configs) {
            if(shouldComplete(config, occurrenceMap)) {
                val overriddenConfigs = CwtOverriddenConfigProvider.getOverriddenConfigs(context.contextElement!!, config)
                if(overriddenConfigs.isNotNullOrEmpty()) {
                    for(overriddenConfig in overriddenConfigs) {
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
        context.configs = emptyList()
        return
    }
    
    fun addPropertyValueCompletions(element: ParadoxScriptStringExpressionElement, propertyElement: ParadoxScriptProperty, context: ProcessingContext, result: CompletionResultSet) {
        val configContext = CwtConfigHandler.getConfigContext(element)
        if(configContext == null) return
        if(!configContext.isRootOrMember()) return
        
        val configGroup = configContext.configGroup
        val configs = configContext.getConfigs()
        if(configs.isEmpty()) return
        
        context.isKey = false
        context.configGroup = configGroup
        context.scopeContext = ParadoxScopeHandler.getScopeContext(propertyElement)
        
        for(config in configs) {
            if(config is CwtValueConfig) {
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
        if(expression.type == CwtDataTypes.AliasName) return true
        val actualCount = occurrenceMap[expression]?.actual ?: 0
        //如果写明了cardinality，则为cardinality.max，否则如果类型为常量，则为1，否则为null，null表示没有限制
        //如果上限是动态的值（如，基于define的值），也不作限制
        val cardinality = config.cardinality
        val maxCount = when {
            cardinality == null -> if(expression.type == CwtDataTypes.Constant) 1 else null
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
            cardinality == null -> if(expression.type == CwtDataTypes.Constant) 1 else null
            config.cardinalityMaxDefine != null -> null
            else -> cardinality.max
        }
        return maxCount == null || actualCount < maxCount
    }
    
    fun getScriptExpressionTailText(
        context: ProcessingContext,
        config: CwtConfig<*>?,
        withConfigExpression: Boolean = true,
        withFileName: Boolean = true,
    ): String? {
        if(!context.showScriptExpressionTailText) return null
        
        return buildString {
            if(withConfigExpression) {
                val configExpression = config?.expression
                if(configExpression != null) {
                    append(" by ").append(configExpression)
                }
            }
            if(withFileName) {
                val fileName = config?.resolved()?.pointer?.containingFile?.name
                if(fileName != null) {
                    append(" in ").append(fileName)
                }
            }
        }
    }
    //endregion
    
    //region Base Completion Methods
    fun completeRootKey(context: ProcessingContext, result: CompletionResultSet, elementPath: ParadoxElementPath) {
        val originalFile = context.parameters?.originalFile ?: return
        val fileInfo = originalFile.fileInfo ?: return
        val gameType = context.gameType ?: return
        val configGroup = context.configGroup ?: return
        val path = fileInfo.pathToEntry //这里使用pathToEntry
        val infoMap = mutableMapOf<String, MutableList<Tuple2<CwtTypeConfig, CwtSubtypeConfig?>>>()
        for(typeConfig in configGroup.types.values) {
            if(ParadoxDefinitionHandler.matchesTypeByUnknownDeclaration(path, null, null, typeConfig)) {
                val skipRootKeyConfig = typeConfig.skipRootKey
                if(skipRootKeyConfig.isNullOrEmpty()) {
                    if(elementPath.isEmpty()) {
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
                    for(skipConfig in skipRootKeyConfig) {
                        val relative = elementPath.relativeTo(skipConfig) ?: continue
                        if(relative.isEmpty()) {
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
        for((key, tuples) in infoMap) {
            if(key == "any") return //skip any wildcard
            val typeConfigToUse = tuples.map { it.first }.distinctBy { it.name }.singleOrNull()
            val typeToUse = typeConfigToUse?.name
            //需要考虑不指定子类型的情况
            val subtypesToUse = when {
                typeConfigToUse == null || tuples.isEmpty() -> null
                else -> tuples.mapNotNull { it.second }.ifEmpty { null }?.distinctBy { it.name }?.map { it.name }
            }
            val config = if(typeToUse == null) null else {
                val declarationConfig = configGroup.declarations.get(typeToUse)
                if(declarationConfig == null) null else {
                    val configContext = CwtDeclarationConfigContextProvider.getContext(context.contextElement!!, null, typeToUse, subtypesToUse, gameType, configGroup)
                    configContext?.getConfig(declarationConfig)
                }
            }
            val element = config?.pointer?.element
            val icon = if(config != null) PlsIcons.Nodes.Definition(typeToUse) else PlsIcons.Nodes.Property
            val tailText = if(tuples.isEmpty()) null
            else tuples.joinToString(", ", " for ") { (typeConfig, subTypeConfig) ->
                if(subTypeConfig != null) "${typeConfig.name}.${subTypeConfig.name}" else typeConfig.name
            }
            val typeFile = config?.pointer?.containingFile
            context.config = config
            val lookupElement = PlsLookupElementBuilder.create(element, key)
                .withIcon(icon)
                .withTailText(tailText)
                .withTypeText(typeFile?.name)
                .withTypeIcon(typeFile?.icon)
                .withForceInsertCurlyBraces(tuples.isEmpty())
                .caseInsensitive()
                .withPriority(PlsCompletionPriorities.rootKeyPriority)
                .build(context)
            result.addPlsElement(lookupElement)
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
        
        if(configExpression.expressionString.isEmpty()) return
        
        //匹配作用域
        if(scopeMatched) {
            val supportedScopes = when {
                config is CwtPropertyConfig -> config.supportedScopes
                config is CwtAliasConfig -> config.supportedScopes
                config is CwtLinkConfig -> config.inputScopes
                else -> null
            }
            val scopeMatched1 = when {
                scopeContext == null -> true
                else -> ParadoxScopeHandler.matchesScope(scopeContext, supportedScopes, configGroup)
            }
            if(!scopeMatched1 && getSettings().completion.completeOnlyScopeIsMatched) return
            context.scopeMatched = scopeMatched1
        }
        
        ParadoxScriptExpressionSupport.complete(context, result)
        
        context.scopeMatched = true
        context.scopeContext = scopeContext
    }
    
    fun completeLocalisation(context: ProcessingContext, result: CompletionResultSet) {
        val config = context.config ?: return
        val keyword = context.keyword
        
        //本地化的提示结果可能有上千条，因此这里改为先按照输入的关键字过滤结果，关键字变更时重新提示
        result.restartCompletionOnPrefixChange(StandardPatterns.string().shorterThan(keyword.length))
        
        val configGroup = config.configGroup
        val project = configGroup.project
        val contextElement = context.contextElement
        val tailText = getScriptExpressionTailText(context, config)
        val selector = localisationSelector(project, contextElement).contextSensitive()
            .preferLocale(ParadoxLocaleHandler.getPreferredLocale())
        //.distinctByName() //这里selector不需要指定去重
        ParadoxLocalisationSearch.processVariants(result.prefixMatcher, selector, LimitedCompletionProcessor { localisation ->
            val name = localisation.name //=localisation.paradoxLocalisationInfo?.name
            val typeFile = localisation.containingFile
            val lookupElement = PlsLookupElementBuilder.create(localisation, name)
                .withIcon(PlsIcons.Nodes.Localisation)
                .withTailText(tailText)
                .withTypeText(typeFile.name)
                .withTypeIcon(typeFile.icon)
                .build(context)
            result.addPlsElement(lookupElement)
            true
        })
    }
    
    fun completeSyncedLocalisation(context: ProcessingContext, result: CompletionResultSet) {
        val config = context.config ?: return
        val keyword = context.keyword
        
        //本地化的提示结果可能有上千条，因此这里改为先按照输入的关键字过滤结果，关键字变更时重新提示
        result.restartCompletionOnPrefixChange(StandardPatterns.string().shorterThan(keyword.length))
        
        val configGroup = config.configGroup
        val project = configGroup.project
        val contextElement = context.contextElement
        val tailText = getScriptExpressionTailText(context, config)
        //这里selector不需要指定去重
        val selector = localisationSelector(project, contextElement).contextSensitive().preferLocale(ParadoxLocaleHandler.getPreferredLocale())
        ParadoxSyncedLocalisationSearch.processVariants(result.prefixMatcher, selector) { syncedLocalisation ->
            val name = syncedLocalisation.name //=localisation.paradoxLocalisationInfo?.name
            val typeFile = syncedLocalisation.containingFile
            val lookupElement = PlsLookupElementBuilder.create(syncedLocalisation, name)
                .withIcon(PlsIcons.Nodes.Localisation)
                .withTailText(tailText)
                .withTypeText(typeFile.name)
                .withTypeIcon(typeFile.icon)
                .build(context)
            result.addPlsElement(lookupElement)
            true
        }
    }
    
    fun completeDefinition(context: ProcessingContext, result: CompletionResultSet) {
        val config = context.config ?: return
        val scopeContext = context.scopeContext
        val typeExpression = config.expression?.value ?: return
        val configGroup = config.configGroup
        val project = configGroup.project
        val contextElement = context.contextElement
        val tailText = getScriptExpressionTailText(context, config)
        val selector = definitionSelector(project, contextElement).contextSensitive().distinctByName()
        ParadoxDefinitionSearch.search(typeExpression, selector).processQueryAsync p@{ definition ->
            ProgressManager.checkCanceled()
            val definitionInfo = definition.definitionInfo ?: return@p true
            if(definitionInfo.name.isEmpty()) return@p true //ignore anonymous definitions
            
            //排除不匹配可能存在的supported_scopes的情况
            val supportedScopes = ParadoxDefinitionSupportedScopesProvider.getSupportedScopes(definition, definitionInfo)
            val scopeMatched = ParadoxScopeHandler.matchesScope(scopeContext, supportedScopes, configGroup)
            if(!scopeMatched && getSettings().completion.completeOnlyScopeIsMatched) return@p true
            
            val name = definitionInfo.name
            val typeFile = definition.containingFile
            val lookupElement = PlsLookupElementBuilder.create(definition, name)
                .withIcon(PlsIcons.Nodes.Definition(definitionInfo.type))
                .withTailText(tailText)
                .withTypeText(typeFile.name)
                .withTypeIcon(typeFile.icon)
                .withScopeMatched(scopeMatched)
                .letIf(getSettings().completion.completeByLocalizedName) {
                    //如果启用，也基于定义的本地化名字进行代码补全
                    ProgressManager.checkCanceled()
                    val localizedNames = ParadoxDefinitionHandler.getLocalizedNames(definition)
                    it.withLocalizedNames(localizedNames)
                }
                .build(context)
            result.addPlsElement(lookupElement)
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
        if(pathReferenceExpressionSupport != null) {
            val tailText = getScriptExpressionTailText(context, config)
            val fileExtensions = when(config) {
                is CwtMemberConfig<*> -> ParadoxFilePathHandler.getFileExtensionOptionValues(config)
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
                    ParadoxInlineScriptHandler.isInlineScriptExpressionConfig(config) -> PlsIcons.Nodes.InlineScript
                    else -> PlsIcons.Nodes.PathReference
                }
                val lookupElement = PlsLookupElementBuilder.create(file, name)
                    .withIcon(icon)
                    .withTailText(tailText)
                    .withTypeText(file.name)
                    .withTypeIcon(file.icon)
                    .build(context)
                result.addPlsElement(lookupElement)
                true
            }
            
            if(ParadoxInlineScriptHandler.isInlineScriptExpressionConfig(config)) {
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
        val tailText = getScriptExpressionTailText(context, config)
        //提示简单枚举
        val enumConfig = configGroup.enums[enumName]
        if(enumConfig != null) {
            ProgressManager.checkCanceled()
            val enumValueConfigs = enumConfig.valueConfigMap.values
            if(enumValueConfigs.isEmpty()) return
            val typeFile = enumConfig.pointer.containingFile
            for(enumValueConfig in enumValueConfigs) {
                val name = enumValueConfig.value
                val element = enumValueConfig.pointer.element ?: continue
                val lookupElement = PlsLookupElementBuilder.create(element, name)
                    .withIcon(PlsIcons.Nodes.EnumValue)
                    .withTailText(tailText)
                    .withTypeText(typeFile?.name)
                    .withTypeIcon(typeFile?.icon)
                    .caseInsensitive()
                    .withPriority(PlsCompletionPriorities.enumPriority)
                    .build(context)
                result.addPlsElement(lookupElement)
            }
        }
        //提示复杂枚举
        val complexEnumConfig = configGroup.complexEnums[enumName]
        if(complexEnumConfig != null) {
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
                val lookupElement = PlsLookupElementBuilder.create(element, name)
                    .withIcon(PlsIcons.Nodes.ComplexEnumValue)
                    .withTailText(tailText)
                    .withTypeText(typeFile?.name)
                    .withTypeIcon(typeFile?.icon)
                    .withPriority(PlsCompletionPriorities.complexEnumPriority)
                    .build(context)
                result.addPlsElement(lookupElement)
                true
            }
            
            completeExtendedComplexEnumValue(context, result)
        }
    }
    
    fun completeModifier(context: ProcessingContext, result: CompletionResultSet) {
        ParadoxModifierHandler.completeModifier(context, result)
    }
    
    fun completeAliasName(context: ProcessingContext, result: CompletionResultSet, aliasName: String) {
        ProgressManager.checkCanceled()
        val configGroup = context.configGroup!!
        val config = context.config!!
        val configs = context.configs
        
        val aliasGroup = configGroup.aliasGroups[aliasName] ?: return
        for(aliasConfigs in aliasGroup.values) {
            if(context.isKey == true) {
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
        val icon = when(configExpression) {
            is CwtKeyExpression -> PlsIcons.Nodes.Property
            is CwtValueExpression -> PlsIcons.Nodes.Value
        }
        val name = configExpression.value ?: return
        if(configExpression is CwtValueExpression) {
            //常量的值也可能是yes/no
            if(name == "yes") {
                if(context.quoted) return
                result.addSimpleScriptExpressionElement(PlsLookupElements.yesLookupElement, context)
                return
            }
            if(name == "no") {
                if(context.quoted) return
                result.addSimpleScriptExpressionElement(PlsLookupElements.noLookupElement, context)
                return
            }
        }
        val element = config.resolved().pointer.element ?: return
        val typeFile = config.resolved().pointer.containingFile
        val lookupElement = PlsLookupElementBuilder.create(element, name)
            .withIcon(icon)
            .withTypeText(typeFile?.name)
            .withTypeIcon(typeFile?.icon)
            .caseInsensitive()
            .withScopeMatched(context.scopeMatched)
            .withPriority(PlsCompletionPriorities.constantPriority)
            .build(context)
        result.addPlsElement(lookupElement)
    }
    
    fun completeTemplateExpression(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val contextElement = context.contextElement!!
        val configGroup = context.configGroup!!
        val config = context.config!!
        val scopeMatched = context.scopeMatched
        
        if(contextElement !is ParadoxScriptStringExpressionElement) return
        val configExpression = config.expression ?: return
        val template = CwtTemplateExpression.resolve(configExpression.expressionString)
        val tailText = getScriptExpressionTailText(context, config)
        template.processResolveResult(contextElement, configGroup) { expression ->
            val templateExpressionElement = CwtConfigHandler.resolveTemplateExpression(contextElement, expression, configExpression, configGroup)
            val lookupElement = PlsLookupElementBuilder.create(templateExpressionElement, expression)
                .withIcon(PlsIcons.Nodes.TemplateExpression)
                .withTailText(tailText)
                .caseInsensitive()
                .withScopeMatched(scopeMatched)
                .build(context)
            result.addPlsElement(lookupElement)
            true
        }
    }
    
    fun completeScopeFieldExpression(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val quoted = context.quoted
        val keyword = context.keyword
        val configGroup = context.configGroup!!
        if(quoted) return
        
        //基于当前位置的代码补全
        val keywordOffset = context.keywordOffset
        val textRange = TextRange.create(keywordOffset, keywordOffset + keyword.length)
        try {
            PlsStatus.incompleteComplexExpression.set(true)
            val scopeFieldExpression = ParadoxScopeFieldExpression.resolve(keyword, textRange, configGroup) ?: return
            return scopeFieldExpression.complete(context, result)
        } finally {
            PlsStatus.incompleteComplexExpression.remove()
        }
    }
    
    fun completeValueFieldExpression(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val quoted = context.quoted
        val keyword = context.keyword
        val configGroup = context.configGroup!!
        
        if(quoted) return
        
        //基于当前位置的代码补全
        val keywordOffset = context.keywordOffset
        val textRange = TextRange.create(keywordOffset, keywordOffset + keyword.length)
        try {
            PlsStatus.incompleteComplexExpression.set(true)
            val valueFieldExpression = ParadoxValueFieldExpression.resolve(keyword, textRange, configGroup) ?: return
            return valueFieldExpression.complete(context, result)
        } finally {
            PlsStatus.incompleteComplexExpression.remove()
        }
    }
    
    fun completeVariableFieldExpression(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val quoted = context.quoted
        val keyword = context.keyword
        val configGroup = context.configGroup!!
        
        if(quoted) return
        
        //基于当前位置的代码补全
        val keywordOffset = context.keywordOffset
        val textRange = TextRange.create(keywordOffset, keywordOffset + keyword.length)
        try {
            PlsStatus.incompleteComplexExpression.set(true)
            val variableFieldExpression = ParadoxVariableFieldExpression.resolve(keyword, textRange, configGroup) ?: return
            return variableFieldExpression.complete(context, result)
        } finally {
            PlsStatus.incompleteComplexExpression.remove()
        }
    }
    
    fun completeDynamicValueExpression(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val quoted = context.quoted
        val keyword = context.keyword
        val configGroup = context.configGroup!!
        val config = context.config!!
        
        if(quoted) return
        
        //基于当前位置的代码补全
        val keywordOffset = context.keywordOffset
        val textRange = TextRange.create(keywordOffset, keywordOffset + keyword.length)
        try {
            PlsStatus.incompleteComplexExpression.set(true)
            val dynamicValueExpression = ParadoxDynamicValueExpression.resolve(keyword, textRange, configGroup, config) ?: return
            return dynamicValueExpression.complete(context, result)
        } finally {
            PlsStatus.incompleteComplexExpression.remove()
        }
    }
    
    fun completeSystemScope(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val configGroup = context.configGroup!!
        
        //总是提示，无论作用域是否匹配
        val systemLinkConfigs = configGroup.systemLinks
        for(systemLinkConfig in systemLinkConfigs.values) {
            val name = systemLinkConfig.id
            val element = systemLinkConfig.pointer.element ?: continue
            val tailText = " from system scopes"
            val typeFile = systemLinkConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withIcon(PlsIcons.Nodes.SystemScope)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withCaseSensitivity(false) //忽略大小写
                .withPriority(PlsCompletionPriorities.systemScopePriority)
            result.addElement(lookupElement)
        }
    }
    
    fun completeScope(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val configGroup = context.configGroup!!
        val scopeContext = context.scopeContext
        
        val linkConfigs = configGroup.linksAsScopeNotData
        for(scope in linkConfigs.values) {
            val scopeMatched = ParadoxScopeHandler.matchesScope(scopeContext, scope.inputScopes, configGroup)
            if(!scopeMatched && getSettings().completion.completeOnlyScopeIsMatched) continue
            
            val name = scope.name
            val element = scope.pointer.element ?: continue
            val tailText = " from scopes"
            val typeFile = scope.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withIcon(PlsIcons.Nodes.Scope)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withCaseSensitivity(false) //忽略大小写
                .withScopeMatched(scopeMatched)
                .withPriority(PlsCompletionPriorities.scopePriority)
            result.addElement(lookupElement)
        }
    }
    
    fun completeScopeLinkPrefix(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val configGroup = context.configGroup!!
        val scopeContext = context.scopeContext
        
        val linkConfigs = configGroup.linksAsScopeWithPrefix
        for(linkConfig in linkConfigs.values) {
            val scopeMatched = ParadoxScopeHandler.matchesScope(scopeContext, linkConfig.inputScopes, configGroup)
            if(!scopeMatched && getSettings().completion.completeOnlyScopeIsMatched) continue
            
            val name = linkConfig.prefix ?: continue
            val element = linkConfig.pointer.element ?: continue
            val tailText = " from scope link ${linkConfig.name}"
            val typeFile = linkConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withBoldness(true)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withScopeMatched(scopeMatched)
                .withPriority(PlsCompletionPriorities.scopeLinkPrefixPriority)
            result.addElement(lookupElement)
        }
    }
    
    fun completeScopeLinkDataSource(context: ProcessingContext, result: CompletionResultSet, prefix: String?, dataSourceNodeToCheck: ParadoxExpressionNode?) {
        ProgressManager.checkCanceled()
        val configGroup = context.configGroup!!
        val config = context.config
        val configs = context.configs
        val scopeContext = context.scopeContext
        
        val linkConfigs = when {
            prefix == null -> configGroup.linksAsScopeWithoutPrefix.values
            else -> configGroup.linksAsScopeWithPrefix.values.filter { prefix == it.prefix }
        }
        
        if(dataSourceNodeToCheck is ParadoxScopeFieldExpressionNode) {
            completeForScopeExpressionNode(dataSourceNodeToCheck, context, result)
            context.scopeContext = scopeContext
            return
        }
        if(dataSourceNodeToCheck is ParadoxDynamicValueExpression) {
            dataSourceNodeToCheck.complete(context, result)
            return
        }
        
        context.configs = linkConfigs
        for(linkConfig in linkConfigs) {
            context.config = linkConfig
            completeScriptExpression(context, result)
        }
        context.config = config
        context.configs = configs
        context.scopeMatched = true
    }
    
    fun completeValueLinkValue(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val configGroup = context.configGroup!!
        val scopeContext = context.scopeContext
        
        val linkConfigs = configGroup.linksAsValueNotData
        for(linkConfig in linkConfigs.values) {
            //排除input_scopes不匹配前一个scope的output_scope的情况
            val scopeMatched = ParadoxScopeHandler.matchesScope(scopeContext, linkConfig.inputScopes, configGroup)
            if(!scopeMatched && getSettings().completion.completeOnlyScopeIsMatched) continue
            
            val name = linkConfig.name
            val element = linkConfig.pointer.element ?: continue
            val tailText = " from values"
            val typeFile = linkConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withIcon(PlsIcons.Nodes.ValueLinkValue)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withCaseSensitivity(false) //忽略大小写
                .withScopeMatched(scopeMatched)
            result.addElement(lookupElement)
        }
    }
    
    fun completeValueLinkPrefix(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val configGroup = context.configGroup!!
        val scopeContext = context.scopeContext
        
        val linkConfigs = configGroup.linksAsValueWithPrefix
        for(linkConfig in linkConfigs.values) {
            val scopeMatched = ParadoxScopeHandler.matchesScope(scopeContext, linkConfig.inputScopes, configGroup)
            if(!scopeMatched && getSettings().completion.completeOnlyScopeIsMatched) continue
            
            val name = linkConfig.prefix ?: continue
            val element = linkConfig.pointer.element ?: continue
            val tailText = " from value link ${linkConfig.name}"
            val typeFile = linkConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withBoldness(true)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withPriority(PlsCompletionPriorities.valueLinkPrefixPriority)
            result.addElement(lookupElement)
        }
    }
    
    fun completeValueLinkDataSource(context: ProcessingContext, result: CompletionResultSet, prefix: String?, dataSourceNodeToCheck: ParadoxExpressionNode?, variableOnly: Boolean = false) {
        ProgressManager.checkCanceled()
        val configGroup = context.configGroup!!
        val config = context.config
        val configs = context.configs
        
        val linkConfigs = when {
            variableOnly -> configGroup.linksAsVariable
            prefix == null -> configGroup.linksAsValueWithoutPrefix.values
            else -> configGroup.linksAsValueWithPrefix.values.filter { prefix == it.prefix }
        }
        
        if(dataSourceNodeToCheck is ParadoxDynamicValueExpression) {
            dataSourceNodeToCheck.complete(context, result)
            return
        }
        if(dataSourceNodeToCheck is ParadoxScriptValueExpression) {
            dataSourceNodeToCheck.complete(context, result)
            return
        }
        
        context.configs = linkConfigs
        for(linkConfig in linkConfigs) {
            context.config = linkConfig
            completeScriptExpression(context, result)
        }
        context.config = config
        context.configs = configs
        context.scopeMatched = true
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
                if(configExpression.type == CwtDataTypes.Value || configExpression.type == CwtDataTypes.DynamicValue) {
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
                    if(info.name == keyword) return@p true //排除和当前输入的同名的
                    val element = ParadoxDynamicValueElement(contextElement, info, project)
                    //去除后面的作用域信息
                    val icon = PlsIcons.Nodes.DynamicValue(dynamicValueType)
                    //不显示typeText
                    val lookupElement = PlsLookupElementBuilder.create(element, info.name)
                        .withIcon(icon)
                        .withTailText(tailText)
                        .build(context)
                    result.addPlsElement(lookupElement)
                    true
                }
            }
            
            completeExtendedDynamicValue(context, result)
        }
        
        if(configs.isNotNullOrEmpty()) {
            for(c in configs) {
                doComplete( c)
            }
        } else if(config != null) {
            doComplete( config)
        }
    }
    
    fun completePredefinedDynamicValue(context: ProcessingContext, result: CompletionResultSet, dynamicValueType: String) {
        ProgressManager.checkCanceled()
        val configGroup = context.configGroup!!
        val config = context.config
        
        val tailText = getScriptExpressionTailText(context, config)
        val valueConfig = configGroup.dynamicValueTypes[dynamicValueType] ?: return
        val dynamicValueTypeConfigs = valueConfig.valueConfigMap.values
        if(dynamicValueTypeConfigs.isEmpty()) return
        for(dynamicValueTypeConfig in dynamicValueTypeConfigs) {
            val name = dynamicValueTypeConfig.value
            val element = dynamicValueTypeConfig.pointer.element ?: continue
            val typeFile = valueConfig.pointer.containingFile
            val lookupElement = PlsLookupElementBuilder.create(element, name)
                .withIcon(PlsIcons.Nodes.DynamicValue)
                .withTailText(tailText)
                .withTypeText(typeFile?.name)
                .withTypeIcon(typeFile?.icon)
                .build(context)
            result.addPlsElement(lookupElement)
        }
    }
    
    fun completeParameter(context: ProcessingContext, result: CompletionResultSet) {
        val config = context.config ?: return
        //提示参数名（仅限key）
        val contextElement = context.contextElement!!
        val isKey = context.isKey
        if(isKey != true || config !is CwtPropertyConfig) return
        ParadoxParameterHandler.completeArguments(contextElement, context, result)
    }
    
    fun completePredefinedLocalisationScope(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val configGroup = context.configGroup!!
        val scopeContext = context.scopeContext
        
        val localisationLinks = configGroup.localisationLinks
        for(localisationScope in localisationLinks.values) {
            val scopeMatched = ParadoxScopeHandler.matchesScope(scopeContext, localisationScope.inputScopes, configGroup)
            if(!scopeMatched && getSettings().completion.completeOnlyScopeIsMatched) continue
            
            val name = localisationScope.name
            val element = localisationScope.pointer.element ?: continue
            val tailText = " from localisation scopes"
            val typeFile = localisationScope.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withIcon(PlsIcons.LocalisationNodes.CommandScope)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withCaseSensitivity(false) //忽略大小写
                .withScopeMatched(scopeMatched)
                .withPriority(PlsCompletionPriorities.scopePriority)
            result.addElement(lookupElement)
        }
    }
    
    fun completePredefinedLocalisationCommand(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val configGroup = context.configGroup!!
        val scopeContext = context.scopeContext
        
        val localisationCommands = configGroup.localisationCommands
        for(localisationCommand in localisationCommands.values) {
            val scopeMatched = ParadoxScopeHandler.matchesScope(scopeContext, localisationCommand.supportedScopes, configGroup)
            if(!scopeMatched && getSettings().completion.completeOnlyScopeIsMatched) continue
            
            val name = localisationCommand.name
            val element = localisationCommand.pointer.element ?: continue
            val tailText = " from localisation commands"
            val typeFile = localisationCommand.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withIcon(PlsIcons.LocalisationNodes.CommandField)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withCaseSensitivity(false) //忽略大小写
                .withScopeMatched(scopeMatched)
            result.addElement(lookupElement)
        }
    }
    
    fun completeEventTarget(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val file = context.parameters?.originalFile ?: return
        val project = file.project
        val contextElement = context.contextElement!!
        val keyword = context.keyword
        
        val eventTargetSelector = dynamicValueSelector(project, file).contextSensitive().distinctByName()
        ParadoxDynamicValueSearch.search(ParadoxDynamicValueHandler.EVENT_TARGETS, eventTargetSelector).processQueryAsync p@{ info ->
            ProgressManager.checkCanceled()
            if(info.name == keyword) return@p true //排除和当前输入的同名的
            val element = ParadoxDynamicValueElement(contextElement, info, project)
            val icon = PlsIcons.Nodes.DynamicValue
            val tailText = " from value[event_target]"
            val lookupElement = LookupElementBuilder.create(element, info.name)
                .withIcon(icon)
                .withTailText(tailText, true)
                .withCaseSensitivity(false) //忽略大小写
            result.addElement(lookupElement)
            true
        }
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
            result.addElement(lookupElement)
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
            if(info.name == keyword) return@p true //排除和当前输入的同名的
            val element = ParadoxDynamicValueElement(contextElement, info, project)
            val icon = PlsIcons.Nodes.Variable
            val tailText = " from variables"
            val lookupElement = LookupElementBuilder.create(element, info.name)
                .withIcon(icon)
                .withTailText(tailText, true)
                .withCaseSensitivity(false) //忽略大小写
            result.addElement(lookupElement)
            true
        }
    }
    
    fun completeConcept(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val file = context.parameters?.originalFile ?: return
        val project = file.project
        
        val conceptSelector = definitionSelector(project, file).contextSensitive().distinctByName()
        val keysToDistinct = mutableSetOf<String>()
        ParadoxDefinitionSearch.search("game_concept", conceptSelector).processQueryAsync p@{ element ->
            val tailText = " from concepts"
            val icon = PlsIcons.LocalisationNodes.Concept
            run action@{
                val key = element.name
                if(!keysToDistinct.add(key)) return@action
                val lookupElement = LookupElementBuilder.create(element, key)
                    .withIcon(icon)
                    .withTailText(tailText, true)
                result.addElement(lookupElement)
            }
            element.getData<StellarisGameConceptDataProvider.Data>()?.alias?.forEach action@{ alias ->
                val key = alias
                if(!keysToDistinct.add(key)) return@action
                val lookupElement = LookupElementBuilder.create(element, key)
                    .withIcon(icon)
                    .withTailText(tailText, true)
                result.addElement(lookupElement)
            }
            true
        }
    }
    
    fun completeInlineScriptInvocation(context: ProcessingContext, result: CompletionResultSet) {
        val configGroup = context.configGroup ?: return
        val tailText = " from inline script invocations"
        configGroup.inlineConfigGroup["inline_script"]?.forEach f@{ config0 ->
            ProgressManager.checkCanceled()
            context.config = config0
            context.isKey = true
            val name = config0.name
            val element = config0.pointer.element ?: return@f
            val typeFile = config0.pointer.containingFile
            val lookupElement = PlsLookupElementBuilder.create(element, name)
                .withIcon(PlsIcons.Nodes.Property)
                .withTailText(tailText)
                .withTypeText(typeFile?.name)
                .withTypeIcon(typeFile?.icon)
                .caseInsensitive()
                .withPriority(PlsCompletionPriorities.constantPriority)
                .build(context)
            result.addPlsElement(lookupElement)
        }
    }
    //endregion
    
    //region Extended Completion Methods
    fun completeExtendedScriptedVariable(context: ProcessingContext, result: CompletionResultSet) {
        if(!getSettings().completion.completeByExtendedCwtConfigs) return
        ProgressManager.checkCanceled()
        
        val configGroup = context.configGroup ?: return
        configGroup.extendedScriptedVariables.values.forEach f@{ config0 ->
            ProgressManager.checkCanceled()
            val name = config0.name
            if(checkExtendedConfigName(name)) return@f
            if(context.completionIds?.add(name) == false) return@f //排除重复项
            val element = config0.pointer.element ?: return@f
            val typeFile = config0.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withIcon(PlsIcons.Nodes.ScriptedVariable)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withItemTextUnderlined(true) //used for completions from extended configs
            result.addElement(lookupElement)
        }
    }
    
    fun completeExtendedDefinition(context: ProcessingContext, result: CompletionResultSet) {
        if(!getSettings().completion.completeByExtendedCwtConfigs) return
        ProgressManager.checkCanceled()
        
        val config = context.config ?: return
        val typeExpression = config.expression?.value ?: return
        val configGroup = config.configGroup
        val tailText = getScriptExpressionTailText(context, config)
        
        run r1@{
            configGroup.extendedDefinitions.values.forEach { configs0 ->
                configs0.forEach f@{ config0 ->
                    ProgressManager.checkCanceled()
                    val name = config0.name
                    if(name.isEmpty()) return@f
                    if(checkExtendedConfigName(name)) return@f
                    val type = config0.type
                    if(!ParadoxDefinitionTypeExpression.resolve(type).matches(typeExpression)) return@f
                    val element = config0.pointer.element
                    val typeFile = config0.pointer.containingFile
                    val lookupElement = PlsLookupElementBuilder.create(element, name)
                        .withIcon(PlsIcons.Nodes.Definition(type))
                        .withTailText(tailText)
                        .withTypeText(typeFile?.name)
                        .withTypeIcon(typeFile?.icon)
                        .underlined() //used for completions from extended configs
                        .build(context)
                    result.addPlsElement(lookupElement)
                }
            }
        }
        run r1@{
            if(typeExpression != "game_rule") return@r1
            configGroup.extendedGameRules.values.forEach f@{ config0 ->
                ProgressManager.checkCanceled()
                val name = config0.name
                if(checkExtendedConfigName(name)) return@f
                val element = config0.pointer.element
                val typeFile = config0.pointer.containingFile
                val lookupElement = PlsLookupElementBuilder.create(element, name)
                    .withIcon(PlsIcons.Nodes.Definition("game_rule"))
                    .withTailText(tailText)
                    .withTypeText(typeFile?.name)
                    .withTypeIcon(typeFile?.icon)
                    .underlined() //used for completions from extended configs
                    .build(context)
                result.addPlsElement(lookupElement)
            }
        }
        run r1@{
            if(typeExpression != "on_action") return@r1
            configGroup.extendedOnActions.values.forEach f@{ config0 ->
                ProgressManager.checkCanceled()
                val name = config0.name
                if(checkExtendedConfigName(name)) return@f
                val element = config0.pointer.element
                val typeFile = config0.pointer.containingFile
                val lookupElement = PlsLookupElementBuilder.create(element, name)
                    .withIcon(PlsIcons.Nodes.Definition("on_action"))
                    .withTailText(tailText)
                    .withTypeText(typeFile?.name)
                    .withTypeIcon(typeFile?.icon)
                    .underlined() //used for completions from extended configs
                    .build(context)
                result.addPlsElement(lookupElement)
            }
        }
    }
    
    fun completeExtendedInlineScript(context: ProcessingContext, result: CompletionResultSet) {
        if(!getSettings().completion.completeByExtendedCwtConfigs) return
        ProgressManager.checkCanceled()
        
        val config = context.config ?: return
        val configGroup = config.configGroup
        val tailText = getScriptExpressionTailText(context, config)
        
        configGroup.extendedInlineScripts.values.forEach f@{ config0 ->
            ProgressManager.checkCanceled()
            val name = config0.name
            if(checkExtendedConfigName(name)) return@f
            val element = config0.pointer.element
            val typeFile = config0.pointer.containingFile
            val lookupElement = PlsLookupElementBuilder.create(element, name)
                .withIcon(PlsIcons.Nodes.InlineScript)
                .withTailText(tailText)
                .withTypeText(typeFile?.name)
                .withTypeIcon(typeFile?.icon)
                .underlined() //used for completions from extended configs
                .build(context)
            result.addPlsElement(lookupElement)
        }
    }
    
    fun completeExtendedParameter(context: ProcessingContext, result: CompletionResultSet) {
        if(!getSettings().completion.completeByExtendedCwtConfigs) return
        ProgressManager.checkCanceled()
        
        val configGroup = context.configGroup ?: return
        val contextKey = context.contextKey ?: return
        val argumentNames = context.argumentNames
        val contextElement = context.contextElement ?: return
        
        configGroup.extendedParameters.values.forEach { configs0 ->
            configs0.forEach f@{ config0 ->
                if(!config0.contextKey.matchByPattern(contextKey, contextElement, configGroup)) return@f
                val name = config0.name
                if(checkExtendedConfigName(name)) return@f
                if(argumentNames != null && !argumentNames.add(name)) return@f  //排除已输入的
                val element = config0.pointer.element
                val typeFile = config0.pointer.containingFile
                val lookupElement = PlsLookupElementBuilder.create(element, name)
                    .withIcon(PlsIcons.Nodes.Parameter)
                    .withTypeText(typeFile?.name)
                    .withTypeIcon(typeFile?.icon)
                    .underlined() //used for completions from extended configs
                    .build(context)
                result.addPlsElement(lookupElement)
            }
        }
    }
    
    fun completeExtendedComplexEnumValue(context: ProcessingContext, result: CompletionResultSet) {
        if(!getSettings().completion.completeByExtendedCwtConfigs) return
        ProgressManager.checkCanceled()
        
        val config = context.config ?: return
        val enumName = config.expression?.value ?: return
        val configGroup = config.configGroup
        val tailText = getScriptExpressionTailText(context, config)
        
        configGroup.extendedComplexEnumValues[enumName]?.values?.forEach f@{ config0 ->
            ProgressManager.checkCanceled()
            val name = config0.name
            if(checkExtendedConfigName(name)) return@f
            val element = config0.pointer.element
            val typeFile = config0.pointer.containingFile
            val lookupElement = PlsLookupElementBuilder.create(element, name)
                .withIcon(PlsIcons.Nodes.ComplexEnumValue)
                .withTailText(tailText)
                .withTypeText(typeFile?.name)
                .withTypeIcon(typeFile?.icon)
                .underlined() //used for completions from extended configs
                .build(context)
            result.addPlsElement(lookupElement)
        }
    }
    
    fun completeExtendedDynamicValue(context: ProcessingContext, result: CompletionResultSet) {
        if(!getSettings().completion.completeByExtendedCwtConfigs) return
        ProgressManager.checkCanceled()
        
        val config = context.config ?: return
        val dynamicValueType = config.expression?.value ?: return
        val configGroup = config.configGroup
        val tailText = getScriptExpressionTailText(context, config)
        
        configGroup.extendedDynamicValues[dynamicValueType]?.values?.forEach f@{ config0 ->
            ProgressManager.checkCanceled()
            val name = config0.name
            if(checkExtendedConfigName(name)) return@f
            val type = config0.type
            val element = config0.pointer.element
            val typeFile = config0.pointer.containingFile
            val lookupElement = PlsLookupElementBuilder.create(element, name)
                .withIcon(PlsIcons.Nodes.DynamicValue(type))
                .withTailText(tailText)
                .withTypeText(typeFile?.name)
                .withTypeIcon(typeFile?.icon)
                .underlined() //used for completions from extended configs
                .build(context)
            result.addPlsElement(lookupElement)
        }
    }
    
    private fun checkExtendedConfigName(text: String) : Boolean {
        //ignored if config name is empty
        if(text.isEmpty()) return true
        //ignored if config name is a template expression,
        //although we can use CwtTemplateExpression.processResolveResult to list matched literals
        if(CwtTemplateExpression.resolve(text).expressionString.isNotEmpty()) return true
        return false
    }
    //endregion
}