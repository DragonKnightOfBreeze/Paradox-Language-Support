package icu.windea.pls.lang.expression.impl

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.highlighting.*
import com.intellij.lang.annotation.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.patterns.*
import com.intellij.psi.*
import com.intellij.util.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.chained.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.expression.*
import icu.windea.pls.lang.scope.*
import icu.windea.pls.script.highlighter.*
import icu.windea.pls.script.psi.*

class ParadoxScriptLocalisationExpressionSupport : ParadoxScriptExpressionSupport() {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type == CwtDataType.Localisation
    }
    
    override fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        val attributesKey = ParadoxScriptAttributesKeys.LOCALISATION_REFERENCE_KEY
        val textRange = element.textRange
        val range = rangeInElement?.shiftRight(textRange.startOffset) ?: textRange.unquote(element.text)
        ParadoxConfigHandler.annotateScriptExpression(element, range, attributesKey, holder)
    }
    
    override fun resolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, isKey: Boolean?, exact: Boolean): PsiElement? {
        val configGroup = config.info.configGroup
        val project = configGroup.project
        val selector = localisationSelector(project, element).contextSensitive(exact).preferLocale(preferredParadoxLocale(), exact)
        return ParadoxLocalisationSearch.search(expression, selector).find()
    }
    
    override fun multiResolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, isKey: Boolean?): Collection<PsiElement> {
        val configGroup = config.info.configGroup
        val project = configGroup.project
        val selector = localisationSelector(project, element).contextSensitive().preferLocale(preferredParadoxLocale())
        return ParadoxLocalisationSearch.search(expression, selector).findAll()
    }
    
    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        if(!context.quoted && context.keyword.isParameterized()) return //排除可能带参数的情况
        
        val config = context.config ?: return
        val keyword = context.keyword
        
        //因为这里的提示结果可能有上千条，按照输入的关键字过滤结果，关键字变更时重新提示
        result.restartCompletionOnPrefixChange(StandardPatterns.string().shorterThan(keyword.length))
        
        val configGroup = config.info.configGroup
        val project = configGroup.project
        val contextElement = context.contextElement
        val tailText = ParadoxConfigHandler.getScriptExpressionTailText(config)
        val selector = localisationSelector(project, contextElement).contextSensitive()
            .preferLocale(preferredParadoxLocale())
            //.distinctByName() //这里selector不需要指定去重
        ParadoxLocalisationSearch.processVariants(keyword, selector) { localisation ->
            val name = localisation.name //=localisation.paradoxLocalisationInfo?.name
            val typeFile = localisation.containingFile
            val builder = ParadoxScriptExpressionLookupElementBuilder.create(localisation, name)
                .withIcon(PlsIcons.Localisation)
                .withTailText(tailText)
                .withTypeText(typeFile.name)
                .withTypeIcon(typeFile.icon)
            result.addScriptExpressionElement(context, builder)
            true
        }
    }
}

class ParadoxScriptSyncedLocalisationExpressionSupport : ParadoxScriptExpressionSupport() {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type == CwtDataType.Localisation
    }
    
    override fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        val attributesKey = ParadoxScriptAttributesKeys.LOCALISATION_REFERENCE_KEY
        val textRange = element.textRange
        val range = rangeInElement?.shiftRight(textRange.startOffset) ?: textRange.unquote(element.text)
        ParadoxConfigHandler.annotateScriptExpression(element, range, attributesKey, holder)
    }
    
    override fun resolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, isKey: Boolean?, exact: Boolean): PsiElement? {
        val configGroup = config.info.configGroup
        val project = configGroup.project
        val selector = localisationSelector(project, element).contextSensitive(exact).preferLocale(preferredParadoxLocale(), exact)
        return ParadoxSyncedLocalisationSearch.search(expression, selector).find()
    }
    
    override fun multiResolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, isKey: Boolean?): Collection<PsiElement> {
        val configGroup = config.info.configGroup
        val project = configGroup.project
        val selector = localisationSelector(project, element).contextSensitive().preferLocale(preferredParadoxLocale())
        return ParadoxSyncedLocalisationSearch.search(expression, selector).findAll()
    }
    
    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        if(!context.quoted && context.keyword.isParameterized()) return //排除可能带参数的情况
        
        val config = context.config ?: return
        val keyword = context.keyword
        
        //因为这里的提示结果可能有上千条，按照输入的关键字过滤结果，关键字变更时重新提示
        result.restartCompletionOnPrefixChange(StandardPatterns.string().shorterThan(keyword.length))
        
        val configGroup = config.info.configGroup
        val project = configGroup.project
        val contextElement = context.contextElement
        val tailText = ParadoxConfigHandler.getScriptExpressionTailText(config)
        //这里selector不需要指定去重
        val selector = localisationSelector(project, contextElement).contextSensitive().preferLocale(preferredParadoxLocale())
        ParadoxSyncedLocalisationSearch.processVariants(keyword, selector) { syncedLocalisation ->
            val name = syncedLocalisation.name //=localisation.paradoxLocalisationInfo?.name
            val typeFile = syncedLocalisation.containingFile
            val builder = ParadoxScriptExpressionLookupElementBuilder.create(syncedLocalisation, name)
                .withIcon(PlsIcons.Localisation)
                .withTailText(tailText)
                .withTypeText(typeFile.name)
                .withTypeIcon(typeFile.icon)
            result.addScriptExpressionElement(context, builder)
            true
        }
    }
}

class ParadoxScriptInlineLocalisationExpressionSupport : ParadoxScriptExpressionSupport() {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type == CwtDataType.InlineLocalisation
    }
    
    override fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        if(expression.isLeftQuoted()) return
        val attributesKey = ParadoxScriptAttributesKeys.LOCALISATION_REFERENCE_KEY
        val range = rangeInElement?.shiftRight(element.startOffset) ?: element.textRange.unquote(element.text)
        ParadoxConfigHandler.annotateScriptExpression(element, range, attributesKey, holder)
    }
    
    override fun resolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, isKey: Boolean?, exact: Boolean): PsiElement? {
        if(element.text.isLeftQuoted()) return null //inline string
        val configGroup = config.info.configGroup
        val project = configGroup.project
        val selector = localisationSelector(project, element).contextSensitive(exact).preferLocale(preferredParadoxLocale(), exact)
        return ParadoxSyncedLocalisationSearch.search(expression, selector).find()
    }
    
    override fun multiResolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, isKey: Boolean?): Collection<PsiElement> {
        if(element.text.isLeftQuoted()) return emptySet() //specific expression
        val configGroup = config.info.configGroup
        val project = configGroup.project
        val selector = localisationSelector(project, element).contextSensitive().preferLocale(preferredParadoxLocale())
        return ParadoxLocalisationSearch.search(expression, selector).findAll()
    }
    
    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        if(!context.quoted && context.keyword.isParameterized()) return //排除可能带参数的情况
        
        val config = context.config ?: return
        if(context.quoted) return
        val keyword = context.keyword
        
        //因为这里的提示结果可能有上千条，按照输入的关键字过滤结果，关键字变更时重新提示
        result.restartCompletionOnPrefixChange(StandardPatterns.string().shorterThan(keyword.length))
        
        val configGroup = config.info.configGroup
        val project = configGroup.project
        val contextElement = context.contextElement
        val tailText = ParadoxConfigHandler.getScriptExpressionTailText(config)
        val selector = localisationSelector(project, contextElement).contextSensitive()
            .preferLocale(preferredParadoxLocale())
            //.distinctByName() //这里selector不需要指定去重
        ParadoxLocalisationSearch.processVariants(keyword, selector) { localisation ->
            val name = localisation.name //=localisation.paradoxLocalisationInfo?.name
            val typeFile = localisation.containingFile
            val builder = ParadoxScriptExpressionLookupElementBuilder.create(localisation, name)
                .withIcon(PlsIcons.Localisation)
                .withTailText(tailText)
                .withTypeText(typeFile.name)
                .withTypeIcon(typeFile.icon)
            result.addScriptExpressionElement(context, builder)
            true
        }
    }
}

class ParadoxScriptDefinitionExpressionSupport : ParadoxScriptExpressionSupport() {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type == CwtDataType.Definition
    }
    
    override fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        val attributesKey = ParadoxScriptAttributesKeys.DEFINITION_REFERENCE_KEY
        val textRange = element.textRange
        val range = rangeInElement?.shiftRight(textRange.startOffset) ?: textRange.unquote(element.text)
        ParadoxConfigHandler.annotateScriptExpression(element, range, attributesKey, holder)
    }
    
    override fun resolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, isKey: Boolean?, exact: Boolean): PsiElement? {
        val configGroup = config.info.configGroup
        val project = configGroup.project
        val typeExpression = config.expression?.value ?: return null
        val selector = definitionSelector(project, element).contextSensitive(exact)
        return ParadoxDefinitionSearch.search(expression, typeExpression, selector).find()
    }
    
    override fun multiResolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, isKey: Boolean?): Collection<PsiElement> {
        val configGroup = config.info.configGroup
        val project = configGroup.project
        val typeExpression = config.expression?.value ?: return emptySet()
        val selector = definitionSelector(project, element).contextSensitive()
        return ParadoxDefinitionSearch.search(expression, typeExpression, selector).findAll()
    }
    
    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        if(!context.quoted && context.keyword.isParameterized()) return //排除可能带参数的情况
        
        val config = context.config ?: return
        val scopeContext = context.scopeContext
        val typeExpression = config.expression?.value ?: return
        val configGroup = config.info.configGroup
        val project = configGroup.project
        val contextElement = context.contextElement
        val tailText = ParadoxConfigHandler.getScriptExpressionTailText(config)
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
            val builder = ParadoxScriptExpressionLookupElementBuilder.create(definition, name)
                .withIcon(PlsIcons.Definition)
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
            result.addScriptExpressionElement(context, builder)
            true
        }
    }
}

class ParadoxScriptPathReferenceExpressionSupport : ParadoxScriptExpressionSupport() {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type?.isPathReferenceType() == true
    }
    
    override fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        val attributesKey = ParadoxScriptAttributesKeys.PATH_REFERENCE_KEY
        val textRange = element.textRange
        val range = rangeInElement?.shiftRight(textRange.startOffset) ?: textRange.unquote(element.text)
        ParadoxConfigHandler.annotateScriptExpression(element, range, attributesKey, holder)
    }
    
    override fun resolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, isKey: Boolean?, exact: Boolean): PsiElement? {
        val configExpression = config.expression ?: return null
        val configGroup = config.info.configGroup
        val project = configGroup.project
        if(configExpression.type == CwtDataType.AbsoluteFilePath) {
            return expression.toVirtualFile(false)?.toPsiFile(project)
        } else {
            //if(ParadoxPathReferenceExpressionSupport.get(configExpression) == null) return null
            val pathReference = expression.normalizePath()
            val selector = fileSelector(project, element).contextSensitive()
            return ParadoxFilePathSearch.search(pathReference, configExpression, selector).find()?.toPsiFile(project)
        }
    }
    
    override fun multiResolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, isKey: Boolean?): Collection<PsiElement> {
        val configExpression = config.expression ?: return emptySet()
        val configGroup = config.info.configGroup
        val project = configGroup.project
        if(configExpression.type == CwtDataType.AbsoluteFilePath) {
            return expression.toVirtualFile(false)?.toPsiFile(project).toSingletonSetOrEmpty()
        } else {
            //if(ParadoxPathReferenceExpressionSupport.get(configExpression) == null) return null
            val pathReference = expression.normalizePath()
            val selector = fileSelector(project, element).contextSensitive()
            return ParadoxFilePathSearch.search(pathReference, configExpression, selector).findAll().mapNotNull { it.toPsiFile(project) }
        }
    }
    
    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        if(!context.quoted && context.keyword.isParameterized()) return //排除可能带参数的情况
        
        val config = context.config ?: return
        val configExpression = config.expression ?: return
        val configGroup = config.info.configGroup
        val project = configGroup.project
        val contextElement = context.contextElement
        val contextFile = context.originalFile
        val pathReferenceExpressionSupport = ParadoxPathReferenceExpressionSupport.get(configExpression)
        if(pathReferenceExpressionSupport != null) {
            val tailText = ParadoxConfigHandler.getScriptExpressionTailText(config)
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
                val builder = ParadoxScriptExpressionLookupElementBuilder.create(file, name)
                    .withIcon(PlsIcons.PathReference)
                    .withTailText(tailText)
                    .withTypeText(file.name)
                    .withTypeIcon(file.icon)
                result.addScriptExpressionElement(context, builder)
                true
            }
        }
    }
}

class ParadoxScriptEnumValueExpressionSupport : ParadoxScriptExpressionSupport() {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type == CwtDataType.EnumValue
    }
    
    override fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        val configGroup = config.info.configGroup
        val enumName = config.expression?.value ?: return
        val attributesKey = when {
            configGroup.enums[enumName] != null -> ParadoxScriptAttributesKeys.ENUM_VALUE_KEY
            configGroup.complexEnums[enumName] != null -> ParadoxScriptAttributesKeys.COMPLEX_ENUM_VALUE_KEY
            else -> ParadoxScriptAttributesKeys.ENUM_VALUE_KEY
        }
        val textRange = element.textRange
        val range = rangeInElement?.shiftRight(textRange.startOffset) ?: textRange.unquote(element.text)
        ParadoxConfigHandler.annotateScriptExpression(element, range, attributesKey, holder)
    }
    
    override fun resolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, isKey: Boolean?, exact: Boolean): PsiElement? {
        val enumName = config.expression?.value ?: return null
        val configGroup = config.info.configGroup
        val project = configGroup.project
        //尝试解析为简单枚举
        val enumConfig = configGroup.enums[enumName]
        if(enumConfig != null) {
            return ParadoxConfigHandler.resolvePredefinedEnumValue(element, expression, enumName, configGroup)
        }
        //尝试解析为复杂枚举
        val complexEnumConfig = configGroup.complexEnums[enumName]
        if(complexEnumConfig != null) {
            val searchScope = complexEnumConfig.searchScopeType
            val selector = complexEnumValueSelector(project, element)
                .withSearchScopeType(searchScope)
                .contextSensitive(exact)
            val info = ParadoxComplexEnumValueSearch.search(expression, enumName, selector).findFirst()
            if(info != null) {
                val readWriteAccess = ReadWriteAccessDetector.Access.Read //usage
                return ParadoxComplexEnumValueElement(element, info.name, info.enumName, readWriteAccess, info.gameType, project)
            }
        }
        return null
    }
    
    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        if(!context.quoted && context.keyword.isParameterized()) return //排除可能带参数的情况
        
        val config = context.config ?: return
        val enumName = config.expression?.value ?: return
        val configGroup = config.info.configGroup
        val project = configGroup.project
        val contextElement = context.contextElement
        val tailText = ParadoxConfigHandler.getScriptExpressionTailText(config)
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
                val builder = ParadoxScriptExpressionLookupElementBuilder.create(element, name)
                    .withIcon(PlsIcons.EnumValue)
                    .withTailText(tailText)
                    .withTypeText(typeFile?.name)
                    .withTypeIcon(typeFile?.icon)
                    .caseInsensitive()
                    .withScopeMatched(context.scopeMatched)
                    .withPriority(PlsCompletionPriorities.enumPriority)
                result.addScriptExpressionElement(context, builder)
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
                val builder = ParadoxScriptExpressionLookupElementBuilder.create(element, name)
                    .withIcon(PlsIcons.ComplexEnumValue)
                    .withTailText(tailText)
                    .withTypeText(typeFile?.name)
                    .withTypeIcon(typeFile?.icon)
                result.addScriptExpressionElement(context, builder)
                true
            }
        }
    }
}

class ParadoxScriptModifierExpressionSupport : ParadoxScriptExpressionSupport() {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type == CwtDataType.Modifier
    }
    
    override fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        val attributesKey = ParadoxScriptAttributesKeys.MODIFIER_KEY
        val textRange = element.textRange
        val range = rangeInElement?.shiftRight(textRange.startOffset) ?: textRange.unquote(element.text)
        ParadoxConfigHandler.annotateScriptExpression(element, range, attributesKey, holder)
    }
    
    override fun resolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, isKey: Boolean?, exact: Boolean): PsiElement? {
        val configGroup = config.info.configGroup
        return ParadoxConfigHandler.resolveModifier(element, expression, configGroup)
    }
    
    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        if(!context.quoted && context.keyword.isParameterized()) return //排除可能带参数的情况
        
        //提示预定义的modifier
        ParadoxConfigHandler.completeModifier(context, result)
    }
}

class ParadoxScriptAliasNameExpressionSupport : ParadoxScriptExpressionSupport() {
    override fun supports(config: CwtConfig<*>): Boolean {
        val type = config.expression?.type ?: return false
        return type == CwtDataType.AliasName || type == CwtDataType.AliasKeysField
    }
    
    override fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        val configGroup = config.info.configGroup
        val configExpression = config.expression
        val aliasName = configExpression?.value ?: return
        val aliasMap = configGroup.aliasGroups.get(aliasName) ?: return
        val aliasSubName = ParadoxConfigHandler.getAliasSubName(element, expression, false, aliasName, configGroup) ?: return
        val aliasConfig = aliasMap[aliasSubName]?.first() ?: return
        INSTANCE.annotate(element, rangeInElement, expression, holder, aliasConfig)
    }
    
    override fun resolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, isKey: Boolean?, exact: Boolean): PsiElement? {
        val aliasName = config.expression?.value ?: return null
        val configGroup = config.info.configGroup
        val aliasGroup = configGroup.aliasGroups[aliasName] ?: return null
        val aliasSubName = ParadoxConfigHandler.getAliasSubName(element, expression, element.text.isLeftQuoted(), aliasName, configGroup)
        val alias = aliasGroup[aliasSubName]?.firstOrNull() ?: return null
        return ParadoxConfigHandler.resolveScriptExpression(element, rangeInElement, alias, alias.expression, configGroup, isKey, exact)
    }
    
    override fun multiResolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, isKey: Boolean?): Collection<PsiElement> {
        val aliasName = config.expression?.value ?: return emptySet()
        val configGroup = config.info.configGroup
        val aliasGroup = configGroup.aliasGroups[aliasName] ?: return emptySet()
        val aliasSubName = ParadoxConfigHandler.getAliasSubName(element, expression, element.text.isLeftQuoted(), aliasName, configGroup)
        val alias = aliasGroup[aliasSubName]?.firstOrNull() ?: return emptySet()
        return ParadoxConfigHandler.multiResolveScriptExpression(element, rangeInElement, alias, alias.expression, configGroup, isKey)
    }
    
    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        if(!context.quoted && context.keyword.isParameterized()) return //排除可能带参数的情况
        
        val config = context.config ?: return
        val aliasName = config.expression?.value ?: return
        ParadoxConfigHandler.completeAliasName(aliasName, context, result)
    }
}

abstract class ParadoxScriptConstantLikeExpressionSupport : ParadoxScriptExpressionSupport() {
    override fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        val annotated = annotateByAliasName(element, rangeInElement, holder, config)
        if(annotated) return
        val configExpression = config.expression ?: return
        if(rangeInElement == null) {
            if(element is ParadoxScriptPropertyKey && configExpression is CwtKeyExpression) return //unnecessary
            if(element is ParadoxScriptString && configExpression is CwtValueExpression) return //unnecessary
        }
        val attributesKey = when(configExpression) {
            is CwtKeyExpression -> ParadoxScriptAttributesKeys.PROPERTY_KEY_KEY
            is CwtValueExpression -> ParadoxScriptAttributesKeys.STRING_KEY
        }
        val textRange = element.textRange
        val range = rangeInElement?.shiftRight(textRange.startOffset) ?: textRange.unquote(element.text)
        if(range.isEmpty) return
        ParadoxConfigHandler.annotateScriptExpression(element, range, attributesKey, holder)
    }
    
    private fun annotateByAliasName(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, holder: AnnotationHolder, config: CwtConfig<*>): Boolean {
        val aliasConfig = config.findAliasConfig() ?: return false
        val type = aliasConfig.expression.type
        if(!type.isConstantLikeType()) return false
        val aliasName = aliasConfig.name
        val attributesKey = when {
            aliasName == "modifier" -> ParadoxScriptAttributesKeys.MODIFIER_KEY
            aliasName == "trigger" -> ParadoxScriptAttributesKeys.TRIGGER_KEY
            aliasName == "effect" -> ParadoxScriptAttributesKeys.EFFECT_KEY
            else -> return false
        }
        val textRange = element.textRange
        val range = rangeInElement?.shiftRight(textRange.startOffset) ?: textRange.unquote(element.text)
        ParadoxConfigHandler.annotateScriptExpression(element, range, attributesKey, holder)
        return true
    }
}

class ParadoxScriptConstantExpressionSupport : ParadoxScriptConstantLikeExpressionSupport() {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type == CwtDataType.Constant
    }
    
    override fun resolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, isKey: Boolean?, exact: Boolean): PsiElement? {
        return when {
            config is CwtPropertyConfig && config.inlineableConfig is CwtSingleAliasConfig -> config.pointer.element
            config is CwtMemberConfig<*> -> config.resolved().pointer.element
            else -> config.pointer.element
        }
    }
    
    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        if(!context.quoted && context.keyword.isParameterized()) return //排除可能带参数的情况
        
        val config = context.config ?: return
        val configExpression = config.expression ?: return
        val icon = when(configExpression) {
            is CwtKeyExpression -> PlsIcons.Property
            is CwtValueExpression -> PlsIcons.Value
        }
        val name = configExpression.value ?: return
        if(configExpression is CwtValueExpression) {
            //常量的值也可能是yes/no
            if(name == "yes") {
                if(context.quoted) return
                result.addExpressionElement(context, PlsLookupElements.yesLookupElement)
                return
            }
            if(name == "no") {
                if(context.quoted) return
                result.addExpressionElement(context, PlsLookupElements.noLookupElement)
                return
            }
        }
        val element = config.resolved().pointer.element ?: return
        val typeFile = config.resolved().pointer.containingFile
        val builder = ParadoxScriptExpressionLookupElementBuilder.create(element, name)
            .withIcon(icon)
            .withTypeText(typeFile?.name)
            .withTypeIcon(typeFile?.icon)
            .caseInsensitive()
            .withScopeMatched(context.scopeMatched)
            .withPriority(PlsCompletionPriorities.constantPriority)
        result.addScriptExpressionElement(context, builder)
    }
}

class ParadoxScriptTemplateExpressionSupport : ParadoxScriptConstantLikeExpressionSupport() {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type == CwtDataType.Template
    }
    
    override fun resolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, isKey: Boolean?, exact: Boolean): PsiElement? {
        val configExpression = config.expression ?: return null
        val configGroup = config.info.configGroup
        return ParadoxConfigHandler.resolveTemplateExpression(element, expression, configExpression, configGroup)
    }
    
    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        if(!context.quoted && context.keyword.isParameterized()) return //排除可能带参数的情况
        
        ParadoxConfigHandler.completeTemplateExpression(context, result)
    }
}