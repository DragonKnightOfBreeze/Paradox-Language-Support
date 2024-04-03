package icu.windea.pls.ep.expression

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.highlighting.*
import com.intellij.lang.annotation.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.ep.expression.ParadoxScriptExpressionSupport.*
import icu.windea.pls.lang.codeInsight.completion.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.highlighter.*
import icu.windea.pls.script.psi.*

class ParadoxLocalisationScriptExpressionSupport : ParadoxScriptExpressionSupport {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type == CwtDataTypes.Localisation
    }
    
    override fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        val attributesKey = ParadoxScriptAttributesKeys.LOCALISATION_REFERENCE_KEY
        val textRange = element.textRange
        val range = rangeInElement?.shiftRight(textRange.startOffset) ?: textRange.unquote(element.text)
        CwtConfigHandler.annotateScriptExpression(element, range, attributesKey, holder)
    }
    
    override fun resolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, isKey: Boolean?, exact: Boolean): PsiElement? {
        val configGroup = config.info.configGroup
        val project = configGroup.project
        val selector = localisationSelector(project, element).contextSensitive(exact).preferLocale(ParadoxLocaleHandler.getPreferredLocale(), exact)
        return ParadoxLocalisationSearch.search(expression, selector).find()
    }
    
    override fun multiResolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, isKey: Boolean?): Collection<PsiElement> {
        val configGroup = config.info.configGroup
        val project = configGroup.project
        val selector = localisationSelector(project, element).contextSensitive().preferLocale(ParadoxLocaleHandler.getPreferredLocale())
        return ParadoxLocalisationSearch.search(expression, selector).findAll()
    }
    
    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        if(context.keyword.isParameterized()) return //排除可能带参数的情况
        
        ParadoxCompletionManager.completeLocalisation(context, result)
    }
}

class ParadoxSyncedLocalisationScriptExpressionSupport : ParadoxScriptExpressionSupport {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type == CwtDataTypes.Localisation
    }
    
    override fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        val attributesKey = ParadoxScriptAttributesKeys.LOCALISATION_REFERENCE_KEY
        val textRange = element.textRange
        val range = rangeInElement?.shiftRight(textRange.startOffset) ?: textRange.unquote(element.text)
        CwtConfigHandler.annotateScriptExpression(element, range, attributesKey, holder)
    }
    
    override fun resolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, isKey: Boolean?, exact: Boolean): PsiElement? {
        val configGroup = config.info.configGroup
        val project = configGroup.project
        val selector = localisationSelector(project, element).contextSensitive(exact).preferLocale(ParadoxLocaleHandler.getPreferredLocale(), exact)
        return ParadoxSyncedLocalisationSearch.search(expression, selector).find()
    }
    
    override fun multiResolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, isKey: Boolean?): Collection<PsiElement> {
        val configGroup = config.info.configGroup
        val project = configGroup.project
        val selector = localisationSelector(project, element).contextSensitive().preferLocale(ParadoxLocaleHandler.getPreferredLocale())
        return ParadoxSyncedLocalisationSearch.search(expression, selector).findAll()
    }
    
    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        if(context.keyword.isParameterized()) return //排除可能带参数的情况
        
        ParadoxCompletionManager.completeSyncedLocalisation(context, result)
    }
}

class ParadoxInlineLocalisationScriptExpressionSupport : ParadoxScriptExpressionSupport {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type == CwtDataTypes.InlineLocalisation
    }
    
    override fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        if(expression.isLeftQuoted()) return
        val attributesKey = ParadoxScriptAttributesKeys.LOCALISATION_REFERENCE_KEY
        val range = rangeInElement?.shiftRight(element.startOffset) ?: element.textRange.unquote(element.text)
        CwtConfigHandler.annotateScriptExpression(element, range, attributesKey, holder)
    }
    
    override fun resolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, isKey: Boolean?, exact: Boolean): PsiElement? {
        if(element.text.isLeftQuoted()) return null //inline string
        val configGroup = config.info.configGroup
        val project = configGroup.project
        val selector = localisationSelector(project, element).contextSensitive(exact).preferLocale(ParadoxLocaleHandler.getPreferredLocale(), exact)
        return ParadoxSyncedLocalisationSearch.search(expression, selector).find()
    }
    
    override fun multiResolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, isKey: Boolean?): Collection<PsiElement> {
        if(element.text.isLeftQuoted()) return emptySet() //specific expression
        val configGroup = config.info.configGroup
        val project = configGroup.project
        val selector = localisationSelector(project, element).contextSensitive().preferLocale(ParadoxLocaleHandler.getPreferredLocale())
        return ParadoxLocalisationSearch.search(expression, selector).findAll()
    }
    
    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        if(context.keyword.isParameterized()) return //排除可能带参数的情况
        if(context.quoted) return
        
        ParadoxCompletionManager.completeLocalisation(context, result)
    }
}

class ParadoxDefinitionScriptExpressionSupport : ParadoxScriptExpressionSupport {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type == CwtDataTypes.Definition
    }
    
    override fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        val attributesKey = ParadoxScriptAttributesKeys.DEFINITION_REFERENCE_KEY
        val textRange = element.textRange
        val range = rangeInElement?.shiftRight(textRange.startOffset) ?: textRange.unquote(element.text)
        CwtConfigHandler.annotateScriptExpression(element, range, attributesKey, holder)
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
        if(context.keyword.isParameterized()) return //排除可能带参数的情况
        
        ParadoxCompletionManager.completeDefinition(context, result)
    }
}

class ParadoxPathReferenceScriptExpressionSupport : ParadoxScriptExpressionSupport {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type in CwtDataTypeGroups.PathReference
    }
    
    override fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        val attributesKey = ParadoxScriptAttributesKeys.PATH_REFERENCE_KEY
        val textRange = element.textRange
        val range = rangeInElement?.shiftRight(textRange.startOffset) ?: textRange.unquote(element.text)
        CwtConfigHandler.annotateScriptExpression(element, range, attributesKey, holder)
    }
    
    override fun resolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, isKey: Boolean?, exact: Boolean): PsiElement? {
        val configExpression = config.expression ?: return null
        val configGroup = config.info.configGroup
        val project = configGroup.project
        if(configExpression.type == CwtDataTypes.AbsoluteFilePath) {
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
        if(configExpression.type == CwtDataTypes.AbsoluteFilePath) {
            return expression.toVirtualFile(false)?.toPsiFile(project).toSingletonSetOrEmpty()
        } else {
            //if(ParadoxPathReferenceExpressionSupport.get(configExpression) == null) return null
            val pathReference = expression.normalizePath()
            val selector = fileSelector(project, element).contextSensitive()
            return ParadoxFilePathSearch.search(pathReference, configExpression, selector).findAll().mapNotNull { it.toPsiFile(project) }
        }
    }
    
    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        if(context.keyword.isParameterized()) return //排除可能带参数的情况
        
        ParadoxCompletionManager.completePathReference(context, result)
    }
}

class ParadoxEnumValueScriptExpressionSupport : ParadoxScriptExpressionSupport {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type == CwtDataTypes.EnumValue
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
        CwtConfigHandler.annotateScriptExpression(element, range, attributesKey, holder)
    }
    
    override fun resolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, isKey: Boolean?, exact: Boolean): PsiElement? {
        val enumName = config.expression?.value ?: return null
        val configGroup = config.info.configGroup
        val project = configGroup.project
        //尝试解析为简单枚举
        val enumConfig = configGroup.enums[enumName]
        if(enumConfig != null) {
            return CwtConfigHandler.resolvePredefinedEnumValue(element, expression, enumName, configGroup)
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
        if(context.keyword.isParameterized()) return //排除可能带参数的情况
        
        ParadoxCompletionManager.completeEnumValue(context, result)
    }
}

class ParadoxModifierScriptExpressionSupport : ParadoxScriptExpressionSupport {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type == CwtDataTypes.Modifier
    }
    
    override fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        val attributesKey = ParadoxScriptAttributesKeys.MODIFIER_KEY
        val textRange = element.textRange
        val range = rangeInElement?.shiftRight(textRange.startOffset) ?: textRange.unquote(element.text)
        CwtConfigHandler.annotateScriptExpression(element, range, attributesKey, holder)
    }
    
    override fun resolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, isKey: Boolean?, exact: Boolean): PsiElement? {
        val configGroup = config.info.configGroup
        return CwtConfigHandler.resolveModifier(element, expression, configGroup)
    }
    
    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        if(context.keyword.isParameterized()) return //排除可能带参数的情况
        
        ParadoxModifierHandler.completeModifier(context, result)
    }
}

class ParadoxAliasNameScriptExpressionSupport : ParadoxScriptExpressionSupport {
    override fun supports(config: CwtConfig<*>): Boolean {
        val type = config.expression?.type ?: return false
        return type == CwtDataTypes.AliasName || type == CwtDataTypes.AliasKeysField
    }
    
    override fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        val configGroup = config.info.configGroup
        val configExpression = config.expression
        val aliasName = configExpression?.value ?: return
        val aliasMap = configGroup.aliasGroups.get(aliasName) ?: return
        val aliasSubName = CwtConfigHandler.getAliasSubName(element, expression, false, aliasName, configGroup) ?: return
        val aliasConfig = aliasMap[aliasSubName]?.first() ?: return
        INSTANCE.annotate(element, rangeInElement, expression, holder, aliasConfig)
    }
    
    override fun resolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, isKey: Boolean?, exact: Boolean): PsiElement? {
        val aliasName = config.expression?.value ?: return null
        val configGroup = config.info.configGroup
        val aliasGroup = configGroup.aliasGroups[aliasName] ?: return null
        val aliasSubName = CwtConfigHandler.getAliasSubName(element, expression, element.text.isLeftQuoted(), aliasName, configGroup)
        val alias = aliasGroup[aliasSubName]?.firstOrNull() ?: return null
        return CwtConfigHandler.resolveScriptExpression(element, rangeInElement, alias, alias.expression, configGroup, isKey, exact)
    }
    
    override fun multiResolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, isKey: Boolean?): Collection<PsiElement> {
        val aliasName = config.expression?.value ?: return emptySet()
        val configGroup = config.info.configGroup
        val aliasGroup = configGroup.aliasGroups[aliasName] ?: return emptySet()
        val aliasSubName = CwtConfigHandler.getAliasSubName(element, expression, element.text.isLeftQuoted(), aliasName, configGroup)
        val alias = aliasGroup[aliasSubName]?.firstOrNull() ?: return emptySet()
        return CwtConfigHandler.multiResolveScriptExpression(element, rangeInElement, alias, alias.expression, configGroup, isKey)
    }
    
    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        if(context.keyword.isParameterized()) return //排除可能带参数的情况
        
        val config = context.config ?: return
        val aliasName = config.expression?.value ?: return
        ParadoxCompletionManager.completeAliasName(context, result, aliasName)
    }
}

abstract class ParadoxScriptConstantLikeExpressionSupport : ParadoxScriptExpressionSupport {
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
        CwtConfigHandler.annotateScriptExpression(element, range, attributesKey, holder)
    }
    
    private fun annotateByAliasName(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, holder: AnnotationHolder, config: CwtConfig<*>): Boolean {
        val aliasConfig = when {
            config is CwtPropertyConfig -> config.inlineableConfig?.castOrNull<CwtAliasConfig?>()
            config is CwtAliasConfig -> config
            else -> null
        } ?: return false
        val type = aliasConfig.expression.type
        if(type !in CwtDataTypeGroups.ConstantLike) return false
        val aliasName = aliasConfig.name
        val attributesKey = when {
            aliasName == "modifier" -> ParadoxScriptAttributesKeys.MODIFIER_KEY
            aliasName == "trigger" -> ParadoxScriptAttributesKeys.TRIGGER_KEY
            aliasName == "effect" -> ParadoxScriptAttributesKeys.EFFECT_KEY
            else -> return false
        }
        val textRange = element.textRange
        val range = rangeInElement?.shiftRight(textRange.startOffset) ?: textRange.unquote(element.text)
        CwtConfigHandler.annotateScriptExpression(element, range, attributesKey, holder)
        return true
    }
}

class ParadoxConstantScriptExpressionSupport : ParadoxScriptConstantLikeExpressionSupport() {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type == CwtDataTypes.Constant
    }
    
    override fun resolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, isKey: Boolean?, exact: Boolean): PsiElement? {
        return when {
            config is CwtPropertyConfig && config.inlineableConfig is CwtSingleAliasConfig -> config.pointer.element
            config is CwtMemberConfig<*> -> config.resolved().pointer.element
            else -> config.pointer.element
        }
    }
    
    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        if(context.keyword.isParameterized()) return //排除可能带参数的情况
        
        ParadoxCompletionManager.completeConstant(context, result)
    }
}

class ParadoxTemplateScriptExpressionSupport : ParadoxScriptConstantLikeExpressionSupport() {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type == CwtDataTypes.Template
    }
    
    override fun resolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, isKey: Boolean?, exact: Boolean): PsiElement? {
        val configExpression = config.expression ?: return null
        val configGroup = config.info.configGroup
        return CwtConfigHandler.resolveTemplateExpression(element, expression, configExpression, configGroup)
    }
    
    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        if(context.keyword.isParameterized()) return //排除可能带参数的情况
        
        ParadoxCompletionManager.completeTemplateExpression(context, result)
    }
}
