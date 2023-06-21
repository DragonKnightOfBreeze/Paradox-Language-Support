package icu.windea.pls.lang

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.expression.nodes.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.ParadoxConfigMatcher.Options
import icu.windea.pls.lang.cwt.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.lang.cwt.expression.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*

object ParadoxValueSetValueHandler {
    const val EVENT_TARGET_PREFIX = "event_target:"
    val EVENT_TARGETS = setOf("event_target", "global_event_target")
    
    fun getInfos(element: ParadoxValueSetValueElement): List<ParadoxValueSetValueInfo> {
        return element.valueSetNames.map { valueSetName ->
            ParadoxValueSetValueInfo(element.name, valueSetName, element.readWriteAccess, element.parent.startOffset, element.gameType)
        }
    }
    
    fun getInfos(element: ParadoxScriptStringExpressionElement): List<ParadoxValueSetValueInfo> {
        if(!element.isExpression()) return emptyList()
        return doGetInfo(element)
    }
    
    private fun doGetInfo(element: ParadoxScriptStringExpressionElement): List<ParadoxValueSetValueInfo> {
        val isKey = element is ParadoxScriptPropertyKey
        
        val matchOptions = Options.SkipIndex or Options.SkipScope
        val configs = ParadoxConfigResolver.getConfigs(element, matchOptions = matchOptions)
        if(configs.isEmpty()) return emptyList()
        
        configs.forEachFast { config ->
            val configGroup = config.info.configGroup
            val configExpression = config.expression
            when {
                configExpression.type.isValueSetValueType() -> {
                    val text = element.text
                    val textRange = getTextRange(element, text)
                    //quoted -> only value set value name, no scope info
                    if(text.isLeftQuoted()) {
                        return doGetInfoFromExpression(element, config)
                    }
                    val valueSetValueExpression = ParadoxValueSetValueExpression.resolve(text, textRange, config, configGroup, isKey)
                    if(valueSetValueExpression == null) return emptyList()
                    return doGetInfoFromComplexExpression(element, valueSetValueExpression)
                }
                configExpression.type.isScopeFieldType() -> {
                    val text = element.text
                    val textRange = getTextRange(element, text)
                    if(text.isLeftQuoted()) return emptyList()
                    val scopeFieldExpression = ParadoxScopeFieldExpression.resolve(text, textRange, configGroup, isKey)
                    if(scopeFieldExpression == null) return emptyList()
                    return doGetInfoFromComplexExpression(element,scopeFieldExpression)
                }
                configExpression.type.isValueFieldType() -> {
                    val text = element.text
                    val textRange = getTextRange(element, text)
                    if(text.isLeftQuoted()) return emptyList()
                    val valueFieldExpression = ParadoxValueFieldExpression.resolve(text, textRange, configGroup, isKey)
                    if(valueFieldExpression == null) return emptyList()
                    return doGetInfoFromComplexExpression(element,valueFieldExpression)
                }
                configExpression.type.isVariableFieldType() -> {
                    val text = element.text
                    val textRange = getTextRange(element, text)
                    if(text.isLeftQuoted()) return emptyList()
                    val variableFieldExpression = ParadoxVariableFieldExpression.resolve(text, textRange, configGroup, isKey)
                    if(variableFieldExpression == null) return emptyList()
                    return doGetInfoFromComplexExpression(element,variableFieldExpression)
                }
                else -> pass() //continue to check next config
            }
        }
        return emptyList()
    }
    
    private fun getTextRange(element: PsiElement, text: String): TextRange {
        return when {
            element is ParadoxScriptBlock -> TextRange.create(0, 1) //left curly brace
            else -> TextRange.create(0, text.length).unquote(text) //unquoted text
        }
    }
    
    private fun doGetInfoFromExpression(element: ParadoxScriptStringExpressionElement, config: CwtMemberConfig<*>): List<ParadoxValueSetValueInfo> {
        val name = element.value
        val configExpression = config.expression
        val valueSetName = configExpression.value?.takeIfNotEmpty() ?: return emptyList()
        val readWriteAccess = getReadWriteAccess(configExpression)
        //elementOffset has not been used yet
        val elementOffset = -1 /*element.startOffset*/
        val gameType = config.info.configGroup.gameType ?: return emptyList()
        return ParadoxValueSetValueInfo(name, valueSetName, readWriteAccess, elementOffset, gameType).toSingletonList()
    }
    
    private fun doGetInfoFromComplexExpression(element: ParadoxScriptStringExpressionElement, expression: ParadoxComplexExpression): List<ParadoxValueSetValueInfo> {
        return buildList {
            expression.processAllNodes p@{ node ->
                when {
                    node is ParadoxDataExpressionNode -> {
                        val reference = node.getReference(element) ?: return@p true
                        reference.linkConfigs.forEachFast { config ->
                            if(config.expression?.type?.isValueSetValueType() != true) return@p true
                            val configExpression = config.expression!!
                            val name = reference.rangeInElement.substring(element.text)
                            val valueSetName = configExpression.value?.takeIfNotEmpty() ?: return@p true
                            val readWriteAccess = getReadWriteAccess(configExpression)
                            //elementOffset has not been used yet
                            val elementOffset = -1 /*element.startOffset + reference.rangeInElement.startOffset*/
                            val gameType = config.info.configGroup.gameType ?: return@p true
                            val info = ParadoxValueSetValueInfo(name, valueSetName, readWriteAccess, elementOffset, gameType)
                            add(info)
                        }
                    }
                    node is ParadoxValueSetValueExpressionNode -> {
                        val reference = node.getReference(element) ?: return@p true
                        reference.configs.forEachFast {config ->
                            if(config.expression?.type?.isValueSetValueType() != true) return@p true
                            val configExpression = config.expression!!
                            val name = reference.rangeInElement.substring(element.text)
                            val valueSetName = configExpression.value?.takeIfNotEmpty() ?: return@p true
                            val readWriteAccess = getReadWriteAccess(configExpression)
                            //elementOffset has not been used yet
                            val elementOffset = -1 /*element.startOffset + reference.rangeInElement.startOffset*/
                            val gameType = config.info.configGroup.gameType ?: return@p true
                            val info = ParadoxValueSetValueInfo(name, valueSetName, readWriteAccess, elementOffset, gameType)
                            add(info)
                        }
                    }
                }
                true
            }
        }
    }
    
    fun getInfos(element: ParadoxLocalisationCommandIdentifier): List<ParadoxValueSetValueInfo> {
        return doGetInfos(element)
    }
    
    private fun doGetInfos(element: ParadoxLocalisationCommandIdentifier): List<ParadoxValueSetValueInfo> {
        val reference = element.reference ?: return emptyList()
        if(reference.canResolveValueSetValue()) {
            val resolved = reference.resolve()
            if(resolved is ParadoxValueSetValueElement) return getInfos(resolved)
        }
        return emptyList()
    }
    
    fun getName(expression: String): String? {
        //exclude if name contains invalid chars
        return expression.substringBefore('@').takeIf { it.isExactIdentifier('.') }?.takeIfNotEmpty()
    }
    
    fun getReadWriteAccess(configExpression: CwtDataExpression): Access {
        return when(configExpression.type) {
            CwtDataType.Value -> Access.Read
            CwtDataType.ValueSet -> Access.Write
            else -> Access.ReadWrite
        }
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
}
