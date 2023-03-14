package icu.windea.pls.lang

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector.*
import com.intellij.openapi.progress.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

object ParadoxValueSetValueHandler {
    @JvmStatic
    fun resolveInfo(element: ParadoxScriptStringExpressionElement): ParadoxValueSetValueInfo? {
        if(!element.isExpression()) return null
        //排除带参数的情况
        if(element.isParameterAwareExpression()) return null
        
        ProgressManager.checkCanceled()
        val matchType = CwtConfigMatchType.STATIC
        //only accept "value[x]" or "value_set[x]", rather than "scope_field" or "value_field"
        //so, e.g., if there is only an expression "event_target:target", "target" will not be shown during code completion
        val config = ParadoxConfigHandler.getValueConfigs(element, true, true, matchType)
            .firstOrNull {
                val type = it.expression.type
                type == CwtDataType.Value || type == CwtDataType.ValueSet
            }
            ?: return null
        if(config.expression.type != CwtDataType.Value && config.expression.type != CwtDataType.ValueSet) return null
        val name = getName(element.value) ?: return null
        val valueSetName = config.expression.value?.takeIfNotEmpty() ?: return null
        val configGroup = config.info.configGroup
        val gameType = configGroup.gameType
        val read = config.expression.type == CwtDataType.Value
        return ParadoxValueSetValueInfo(name, valueSetName, gameType, read)
    }
    
    @JvmStatic
    fun getName(element: ParadoxScriptString): String? {
        val stub = runCatching { element.stub }.getOrNull()
        return stub?.valueSetValueInfo?.name?.takeIfNotEmpty() 
            ?: getName(element.value)
    }
    
    @JvmStatic
    fun getName(expression: String): String? {
        //exclude if name contains invalid chars
        return expression.substringBefore('@').takeIf { it.isExactIdentifier() }?.takeIfNotEmpty()
    }
    
    @JvmStatic
    fun isDeclaration(element: ParadoxScriptString): Boolean {
        val stub = runCatching { element.stub }.getOrNull()
        stub?.valueSetValueInfo?.read
            ?.let { return it }
        val config = ParadoxConfigHandler.getConfigs(element).firstOrNull() ?: return true
        return config.expression.type == CwtDataType.Value
    }
    
    @JvmStatic
    fun resolveValueSetValue(element: ParadoxScriptExpressionElement, name: String, configExpression: CwtDataExpression, configGroup: CwtConfigGroup): ParadoxValueSetValueElement? {
        val gameType = configGroup.gameType ?: return null
        if(element !is ParadoxScriptStringExpressionElement) return null
        val readWriteAccess = getReadWriteAccess(configExpression)
        val valueSetName = configExpression.value ?: return null
        return ParadoxValueSetValueElement(element, name, valueSetName, configGroup.project, gameType, readWriteAccess)
    }
    
    @JvmStatic
    fun resolveValueSetValue(element: ParadoxScriptExpressionElement, name: String, configExpressions: Iterable<CwtDataExpression>, configGroup: CwtConfigGroup): ParadoxValueSetValueElement? {
        val gameType = configGroup.gameType ?: return null
        if(element !is ParadoxScriptStringExpressionElement) return null
        val configExpression = configExpressions.firstOrNull() ?: return null
        val readWriteAccess = getReadWriteAccess(configExpression)
        val valueSetNames = configExpressions.mapNotNullTo(mutableSetOf()) { it.value }
        return ParadoxValueSetValueElement(element, name, valueSetNames, gameType, configGroup.project, readWriteAccess)
    }
    
    private fun getReadWriteAccess(configExpression: CwtDataExpression): Access {
        return when(configExpression.type) {
            CwtDataType.Value -> Access.Read
            CwtDataType.ValueSet -> Access.Write
            else -> Access.ReadWrite
        }
    }
}
