package icu.windea.pls.lang

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.expression.nodes.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.ParadoxConfigMatcher.Options
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.references.*
import kotlin.collections.mapNotNullTo

object ParadoxValueSetValueHandler {
    const val EVENT_TARGET_PREFIX = "event_target:"
    val EVENT_TARGETS = setOf("event_target", "global_event_target")
    
    fun getInfo(element: ParadoxValueSetValueElement): ParadoxValueSetValueInfo {
        return ParadoxValueSetValueInfo(element.name, element.valueSetNames, element.readWriteAccess, element.parent.startOffset, element.gameType)
    }
    
    fun getInfos(element: ParadoxScriptStringExpressionElement): List<ParadoxValueSetValueInfo>? {
        if(!element.isExpression()) return null
        return doGetInfoFromCache(element)
    }
    
    private fun doGetInfoFromCache(element: ParadoxScriptStringExpressionElement): List<ParadoxValueSetValueInfo>? {
        return CachedValuesManager.getCachedValue(element, PlsKeys.cachedValueSetValueInfosKey) {
            ProgressManager.checkCanceled()
            val file = element.containingFile
            val value = doGetInfo(element)
            //invalidated on file modification
            CachedValueProvider.Result.create(value, file)
        }
    }
    
    //icu.windea.pls.script.references.ParadoxScriptExpressionElementReferenceProvider.getReferencesByElement
    
    private fun doGetInfo(element: ParadoxScriptStringExpressionElement): List<ParadoxValueSetValueInfo>? {
        //可以识别scopeFieldExpression、valueFieldExpression、variableFieldExpression中的valueSetValue
        //一个脚本表达式可能对应多个valueSetValueInfo（例如，"event_target:a.var@event_target:b"）
        
        val matchOptions = Options.SkipIndex or Options.SkipScope
        val configs = ParadoxConfigHandler.getConfigs(element, orDefault = true, matchOptions = matchOptions)
        if(configs.isEmpty()) return null
        
        for(config in configs) {
            val configExpression = config.expression
            when {
                configExpression.type.isValueSetValueType() -> {
                    val references = element.references
                    return references.mapNotNullTo(SmartList()) { resolveReferenceToInfo(it) }
                }
                configExpression.type.isScopeFieldType() -> {
                    val references = element.references
                    return references.mapNotNullTo(SmartList()) { resolveReferenceToInfo(it) }
                }
                configExpression.type.isValueFieldType() -> {
                    val references = element.references
                    return references.mapNotNullTo(SmartList()) { resolveReferenceToInfo(it) }
                }
                configExpression.type.isVariableFieldType() -> {
                    val references = element.references
                    return references.mapNotNullTo(SmartList()) { resolveReferenceToInfo(it) }
                }
                else -> pass() //continue to check next config
            }
        }
        return null
    }
    
    private fun resolveReferenceToInfo(reference: PsiReference): ParadoxValueSetValueInfo? {
        when {
            reference is ParadoxScriptExpressionPsiReference -> {
                val config = reference.config.takeIf { it.expression?.type?.isValueSetValueType() == true } ?: return null
                val valueSetName = config.expression?.value?.takeIfNotEmpty() ?: return null
                val name = reference.rangeInElement.substring(reference.element.text)
                val readWriteAccess = getReadWriteAccess(config.expression ?: return null)
                val elementOffset = reference.element.startOffset + reference.rangeInElement.startOffset
                val gameType = config.info.configGroup.gameType ?: return null
                return ParadoxValueSetValueInfo(name, setOf(valueSetName), readWriteAccess, elementOffset, gameType)
            }
            reference is ParadoxDataExpressionNode.Reference -> {
                val configs = reference.linkConfigs.filter { it.expression?.type?.isValueSetValueType() == true }.takeIfNotEmpty() ?: return null
                val config = configs.first()
                val element = reference.element
                val valueSetNames = configs.mapNotNullTo(mutableSetOf()) { it.expression?.value }.takeIfNotEmpty() ?: return null
                val name = reference.rangeInElement.substring(element.text)
                val readWriteAccess = getReadWriteAccess(config.expression ?: return null)
                val elementOffset = element.startOffset + reference.rangeInElement.startOffset
                val gameType = config.info.configGroup.gameType ?: return null
                return ParadoxValueSetValueInfo(name, valueSetNames, readWriteAccess, elementOffset, gameType)
            }
            reference is ParadoxValueSetValueExpressionNode.Reference -> {
                val configs = reference.configs.filter { it.expression?.type?.isValueSetValueType() == true }.takeIfNotEmpty() ?: return null
                val config = configs.first()
                val element = reference.element
                val valueSetNames = configs.mapNotNullTo(mutableSetOf()) { it.expression?.value }.takeIfNotEmpty() ?: return null
                val name = reference.rangeInElement.substring(element.text)
                val readWriteAccess = getReadWriteAccess(config.expression ?: return null)
                val elementOffset = element.startOffset + reference.rangeInElement.startOffset
                val gameType = config.info.configGroup.gameType ?: return null
                return ParadoxValueSetValueInfo(name, valueSetNames, readWriteAccess, elementOffset, gameType)
            }
            else -> return null
        }
    }
    
    fun getName(expression: String): String? {
        //exclude if name contains invalid chars
        return expression.substringBefore('@').takeIf { it.isExactIdentifier('.') }?.takeIfNotEmpty()
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
    
    fun getReadWriteAccess(configExpression: CwtDataExpression): Access {
        return when(configExpression.type) {
            CwtDataType.Value -> Access.Read
            CwtDataType.ValueSet -> Access.Write
            else -> Access.ReadWrite
        }
    }
}
