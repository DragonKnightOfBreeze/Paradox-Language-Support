package icu.windea.pls.core.handler

import com.intellij.openapi.progress.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.model.*
import icu.windea.pls.script.psi.*

object ParadoxValueSetValueHandler {
	@JvmStatic
	fun resolveInfo(element: ParadoxScriptStringExpressionElement): ParadoxValueSetValueInfo? {
		if(!element.isExpressionElement()) return null
		//排除带参数的情况
		if(element.isParameterAwareExpression()) return null
		
		ProgressManager.checkCanceled()
		//cannot use index here
		val matchType = 0
		//only accept "value[x]" or "value_set[x]", rather than "scope_field" or "value_field"
		//so, e.g., if there is only an expression "event_target:target", "target" will not be shown during code completion
		val config = ParadoxCwtConfigHandler.resolveValueConfigs(element, true, true, matchType)
			.firstOrNull { 
				val type = it.expression.type
				type == CwtDataTypes.Value || type == CwtDataTypes.ValueSet
			}
			?: return null
		if(config.expression.type != CwtDataTypes.Value && config.expression.type != CwtDataTypes.ValueSet) return null
		val name = getName(element.value) ?: return null
		val valueSetName = config.expression.value?.takeIfNotEmpty() ?: return null
		val configGroup = config.info.configGroup
		val gameType = configGroup.gameType
		val read = config.expression.type == CwtDataTypes.Value
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
		return config.expression.type == CwtDataTypes.Value
	}
}
