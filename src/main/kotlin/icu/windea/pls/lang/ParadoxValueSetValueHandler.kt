package icu.windea.pls.lang

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.expression.nodes.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.references.*
import kotlin.collections.mapNotNullTo

object ParadoxValueSetValueHandler {
    fun getInfos(element: ParadoxScriptStringExpressionElement): List<ParadoxValueSetValueInfo>? {
        if(!element.isExpression()) return null
        //if(element.isParameterized()) return null //排除可能带参数的情况 - 不能在这里排除
        return getInfoFromCache(element)
    }
    
    private fun getInfoFromCache(element: ParadoxScriptStringExpressionElement): List<ParadoxValueSetValueInfo>? {
        return CachedValuesManager.getCachedValue(element, PlsKeys.cachedValueSetValueInfosKey) {
            ProgressManager.checkCanceled()
            val file = element.containingFile
            val value = resolveInfo(element)
            //invalidated on file modification
            CachedValueProvider.Result.create(value, file)
        }
    }
    
    //icu.windea.pls.script.references.ParadoxScriptExpressionElementReferenceProvider.getReferencesByElement
    
    private fun resolveInfo(element: ParadoxScriptStringExpressionElement): List<ParadoxValueSetValueInfo>? {
        //可以识别scopeFieldExpression、valueFieldExpression、variableFieldExpression中的valueSetValue
        //一个脚本表达式可能对应多个valueSetValueInfo（例如，"event_target:a.var@event_target:b"）
        
        val isKey = element is ParadoxScriptPropertyKey
        
        val matchType = CwtConfigMatchType.STATIC //这里需要静态匹配
        val configs = ParadoxConfigHandler.getConfigs(element, orDefault = true, matchType = matchType)
        if(configs.isEmpty()) return null
        
        for(config in configs) {
            val configGroup = config.info.configGroup
            val configExpression = config.expression
            when {
                configExpression.type.isValueSetValueType() -> {
                    val text = element.text
                    val textRange = getTextRange(element, text)
                    //quoted -> only value set value name, no scope info
                    if(text.isLeftQuoted()) {
                        val reference = ParadoxScriptExpressionPsiReference(element, textRange, config, isKey)
                        return resolveReferenceToInfo(reference).toSingletonListOrEmpty()
                    }
                    val valueFieldExpression = ParadoxValueSetValueExpression.resolve(text, textRange, config, configGroup, isKey)
                    if(valueFieldExpression == null) return null
                    val references = valueFieldExpression.getReferences(element)
                    return references.mapNotNullTo(SmartList()) { resolveReferenceToInfo(it) }
                }
                configExpression.type.isScopeFieldType() -> {
                    val text = element.text
                    val textRange = getTextRange(element, text)
                    if(text.isLeftQuoted()) return null
                    val scopeFieldExpression = ParadoxScopeFieldExpression.resolve(text, textRange, configGroup, isKey)
                    if(scopeFieldExpression == null) return null
                    val references = scopeFieldExpression.getReferences(element)
                    return references.mapNotNullTo(SmartList()) { reference -> resolveReferenceToInfo(reference) }
                }
                configExpression.type.isValueFieldType() -> {
                    val text = element.text
                    val textRange = getTextRange(element, text)
                    if(text.isLeftQuoted()) return null
                    val valueFieldExpression = ParadoxValueFieldExpression.resolve(text, textRange, configGroup, isKey)
                    if(valueFieldExpression == null) return null
                    val references = valueFieldExpression.getReferences(element)
                    return references.mapNotNullTo(SmartList()) { reference -> resolveReferenceToInfo(reference) }
                }
                configExpression.type.isVariableFieldType() -> {
                    val text = element.text
                    val textRange = getTextRange(element, text)
                    if(text.isLeftQuoted()) return null
                    val variableFieldExpression = ParadoxVariableFieldExpression.resolve(text, textRange, configGroup, isKey)
                    if(variableFieldExpression == null) return null
                    val references = variableFieldExpression.getReferences(element)
                    return references.mapNotNullTo(SmartList()) { reference -> resolveReferenceToInfo(reference) }
                }
                else -> pass() //continue to check next config
            }
        }
        return null
    }
    
    private fun getTextRange(element: PsiElement, text: String): TextRange {
        return when {
            element is ParadoxScriptBlock -> TextRange.create(0, 1) //left curly brace
            else -> TextRange.create(0, text.length).unquote(text) //unquoted text
        }
    }
    
    private fun resolveReferenceToInfo(reference: PsiReference): ParadoxValueSetValueInfo? {
        when{
            reference is ParadoxScriptExpressionPsiReference -> {
                val config = reference.config.takeIf { it.expression.type.isValueSetValueType() } ?: return null
                return resolveReferenceToInfoWithConfig(reference, config)
            }
            reference is ParadoxDataExpressionNode.Reference -> {
                val config = reference.linkConfigs.find { it.expression?.type?.isValueSetValueType() == true } ?: return null
                val configExpression = config.expression!!
                val element = reference.element
                val name = reference.rangeInElement.substring(element.text)
                val valueSetName = configExpression.value?.takeIfNotEmpty() ?: return null
                val readWriteAccess = getReadWriteAccess(configExpression)
                val elementOffset = element.startOffset + reference.rangeInElement.startOffset
                val gameType = config.info.configGroup.gameType ?: return null
                return ParadoxValueSetValueInfo(name, valueSetName, readWriteAccess, elementOffset, gameType)
            }
            reference is ParadoxValueSetValueExpressionNode.Reference -> {
                val config = reference.configs.find { it.expression?.type?.isValueSetValueType() == true } ?: return null
                val configExpression = config.expression!!
                val element = reference.element
                val name = reference.rangeInElement.substring(element.text)
                val valueSetName = configExpression.value?.takeIfNotEmpty() ?: return null
                val readWriteAccess = getReadWriteAccess(configExpression)
                val elementOffset = element.startOffset + reference.rangeInElement.startOffset
                val gameType = config.info.configGroup.gameType ?: return null
                return ParadoxValueSetValueInfo(name, valueSetName, readWriteAccess, elementOffset, gameType)
            }
            else -> return null
        }
    }
    
    private fun resolveReferenceToInfoWithConfig(reference: ParadoxScriptExpressionPsiReference, config: CwtDataConfig<*>): ParadoxValueSetValueInfo? {
        val configExpression = config.expression
        val element = reference.element
        val name = reference.rangeInElement.substring(element.text)
        val valueSetName = configExpression.value?.takeIfNotEmpty() ?: return null
        val readWriteAccess = getReadWriteAccess(configExpression)
        val elementOffset = element.startOffset + reference.rangeInElement.startOffset
        val gameType = config.info.configGroup.gameType ?: return null
        return ParadoxValueSetValueInfo(name, valueSetName, readWriteAccess, elementOffset, gameType)
    }
    
    fun getName(expression: String): String? {
        //exclude if name contains invalid chars
        return expression.substringBefore('@').takeIf { it.isExactIdentifier('.') }?.takeIfNotEmpty()
    }
    
    fun isDeclaration(info: ParadoxValueSetValueInfo): Boolean {
        return info.readWriteAccess == Access.Write
    }
    
    fun resolveValueSetValue(element: ParadoxScriptExpressionElement, name: String, configExpression: CwtDataExpression, configGroup: CwtConfigGroup): ParadoxValueSetValueElement? {
        val gameType = configGroup.gameType ?: return null
        if(element !is ParadoxScriptStringExpressionElement) return null
        val readWriteAccess = getReadWriteAccess(configExpression)
        val valueSetName = configExpression.value ?: return null
        return ParadoxValueSetValueElement(element, name, valueSetName, readWriteAccess, gameType, configGroup.project)
    }
    
    fun resolveValueSetValue(element: ParadoxScriptExpressionElement, name: String, configExpressions: Iterable<CwtDataExpression>, configGroup: CwtConfigGroup): ParadoxValueSetValueElement? {
        val gameType = configGroup.gameType ?: return null
        if(element !is ParadoxScriptStringExpressionElement) return null
        val configExpression = configExpressions.firstOrNull() ?: return null
        val readWriteAccess = getReadWriteAccess(configExpression)
        val valueSetNames = configExpressions.mapNotNullTo(mutableSetOf()) { it.value }
        return ParadoxValueSetValueElement(element, name, valueSetNames, readWriteAccess, gameType, configGroup.project)
    }
    
    private fun getReadWriteAccess(configExpression: CwtDataExpression): Access {
        return when(configExpression.type) {
            CwtDataType.Value -> Access.Read
            CwtDataType.ValueSet -> Access.Write
            else -> Access.ReadWrite
        }
    }
}
