package icu.windea.pls.lang

import com.intellij.openapi.progress.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
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
		val config = ParadoxCwtConfigHandler.resolveValueConfigs(element, true, true, matchType)
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
		val name = stub?.valueSetValueInfo?.name ?: getName(element.value)
		return name?.takeIfNotEmpty()
	}
	
	@JvmStatic
	fun getName(expression: String) : String? {
		//exclude if name contains invalid chars
		return expression.substringBefore('@').takeIf { it.all { c -> c.isExactIdentifierChar() } }
	}
	
	@JvmStatic
	fun getRead(element: ParadoxScriptString) : Boolean {
		val stub = runCatching { element.stub }.getOrNull()
		stub?.valueSetValueInfo?.read
			?.let { return it }
		val config = ParadoxCwtConfigHandler.resolveConfigs(element).firstOrNull() ?: return true
		return config.expression.type == CwtDataType.Value
	}
	
	@JvmStatic
	fun resolveValueSetValue(element: ParadoxScriptExpressionElement, name: String, configExpression: CwtDataExpression, configGroup: CwtConfigGroup): ParadoxValueSetValueElement? {
		val gameType = configGroup.gameType ?: return null
		if(element !is ParadoxScriptStringExpressionElement) return null
		val read = configExpression.type == CwtDataType.Value
		val valueSetName = configExpression.value ?: return null
		return ParadoxValueSetValueElement(element, name, valueSetName, configGroup.project, gameType, read)
	}
	
	@JvmStatic
	fun resolveValueSetValue(element: ParadoxScriptExpressionElement, configExpressions: List<CwtDataExpression>, name: String, configGroup: CwtConfigGroup): ParadoxValueSetValueElement? {
		val gameType = configGroup.gameType ?: return null
		if(element !is ParadoxScriptStringExpressionElement) return null
		val configExpression = configExpressions.firstOrNull() ?: return null
		val read = configExpression.type == CwtDataType.Value
		val valueSetNames = configExpressions.mapNotNull { it.value }
		return ParadoxValueSetValueElement(element, name, valueSetNames, configGroup.project, gameType, read)
	}
}
