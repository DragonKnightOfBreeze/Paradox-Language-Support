package icu.windea.pls.lang

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector.*
import icu.windea.pls.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

object ParadoxValueSetValueHandler {
    const val EVENT_TARGET_PREFIX = "event_target:"
    val EVENT_TARGETS = setOf("event_target", "global_event_target")
    
    fun getInfo(element: ParadoxValueSetValueElement): ParadoxValueSetValueInfo {
        return ParadoxValueSetValueInfo(element.name, element.valueSetNames, element.readWriteAccess, element.parent.startOffset, element.gameType)
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
