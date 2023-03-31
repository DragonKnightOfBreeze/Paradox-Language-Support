package icu.windea.pls.lang

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector.*
import com.intellij.openapi.progress.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

object ParadoxValueSetValueHandler {
    @JvmStatic
    fun getInfo(element: ParadoxScriptStringExpressionElement): ParadoxValueSetValueInfo? {
        ProgressManager.checkCanceled()
        if(!element.isExpression()) return null
        return getInfoFromCache(element)
    }
    
    private fun getInfoFromCache(element: ParadoxScriptStringExpressionElement): ParadoxValueSetValueInfo? {
        return CachedValuesManager.getCachedValue(element, PlsKeys.cachedValueSetValueInfoKey) {
            val file = element.containingFile
            val value = resolveInfo(element)
            //invalidated on file modification
            CachedValueProvider.Result.create(value, file)
        }
    }
    
    private fun resolveInfo(element: ParadoxScriptStringExpressionElement): ParadoxValueSetValueInfo? {
        if(!element.isExpression()) return null
        //排除带参数的情况
        if(element.isParameterAwareExpression()) return null
        
        ProgressManager.checkCanceled()
        val matchType = CwtConfigMatchType.STATIC
        //only accept "value[x]" or "value_set[x]"
        //rather than "scope_field" or "value_field" or in localisation commands
        //so, e.g., if there is only an expression "event_target:target", "target" will not be shown during code completion
        val config = ParadoxConfigHandler.getConfigs(element, orDefault = true, matchType = matchType)
            .firstOrNull {
                val type = it.expression.type
                type == CwtDataType.Value || type == CwtDataType.ValueSet
            }
            ?: return null
        if(config.expression.type != CwtDataType.Value && config.expression.type != CwtDataType.ValueSet) return null
        val name = getName(element.value) ?: return null
        val valueSetName = config.expression.value?.takeIfNotEmpty() ?: return null
        val configGroup = config.info.configGroup
        val gameType = configGroup.gameType ?: return null
        val readWriteAccess = getReadWriteAccess(config.expression)
        return ParadoxValueSetValueInfo(name, valueSetName, gameType, readWriteAccess)
    }
    
    @JvmStatic
    fun getName(expression: String): String? {
        //exclude if name contains invalid chars
        return expression.substringBefore('@').takeIf { it.isExactIdentifier('.') }?.takeIfNotEmpty()
    }
    
    @JvmStatic
    fun isDeclaration(info: ParadoxValueSetValueInfo): Boolean {
        return info.readWriteAccess == Access.Write
    }
    
    @JvmStatic
    fun resolveValueSetValue(element: ParadoxScriptExpressionElement, name: String, configExpression: CwtDataExpression, configGroup: CwtConfigGroup): ParadoxValueSetValueElement? {
        val gameType = configGroup.gameType ?: return null
        if(element !is ParadoxScriptStringExpressionElement) return null
        val readWriteAccess = getReadWriteAccess(configExpression)
        val valueSetName = configExpression.value ?: return null
        return ParadoxValueSetValueElement(element, name, valueSetName, gameType, readWriteAccess, configGroup.project)
    }
    
    @JvmStatic
    fun resolveValueSetValue(element: ParadoxScriptExpressionElement, name: String, configExpressions: Iterable<CwtDataExpression>, configGroup: CwtConfigGroup): ParadoxValueSetValueElement? {
        val gameType = configGroup.gameType ?: return null
        if(element !is ParadoxScriptStringExpressionElement) return null
        val configExpression = configExpressions.firstOrNull() ?: return null
        val readWriteAccess = getReadWriteAccess(configExpression)
        val valueSetNames = configExpressions.mapNotNullTo(mutableSetOf()) { it.value }
        return ParadoxValueSetValueElement(element, name, valueSetNames, gameType, readWriteAccess, configGroup.project)
    }
    
    private fun getReadWriteAccess(configExpression: CwtDataExpression): Access {
        return when(configExpression.type) {
            CwtDataType.Value -> Access.Read
            CwtDataType.ValueSet -> Access.Write
            else -> Access.ReadWrite
        }
    }
}
