package icu.windea.pls.core.handler

import com.intellij.openapi.progress.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.model.*
import icu.windea.pls.script.psi.*

object ParadoxValueSetValueInfoHandler {
	private val expressionTypes = arrayOf(CwtDataTypes.Value, CwtDataTypes.ValueSet)
	
	@JvmStatic
	fun resolve(element: ParadoxScriptString): ParadoxValueSetValueInfo? {
		if(!element.isExpressionElement()) return null
		//排除带参数的情况和用引号括起的情况
		if(element.isParameterAwareExpression() || element.isQuoted()) return null
		
		ProgressManager.checkCanceled()
		//cannot use stub index here
		val matchType = CwtConfigMatchType.NO_STUB_INDEX
		val config = ParadoxCwtConfigHandler.resolveValueConfigs(element, true, true, matchType)
			.firstOrNull { it.expression.type in expressionTypes }
			?: return null
		if(config.expression.type != CwtDataTypes.Value && config.expression.type != CwtDataTypes.ValueSet) return null
		val name = element.value.substringBefore('@')
		val valueSetName = config.expression.value?.takeIfNotEmpty() ?: return null
		val configGroup = config.info.configGroup
		val gameType = configGroup.gameType
		return ParadoxValueSetValueInfo(name, valueSetName, gameType)
	}
	
	@JvmStatic
	fun getName(element: ParadoxScriptString): String? {
		val stub = runCatching { element.stub }.getOrNull()
		val name = stub?.valueSetValueInfo?.name ?: element.value.substringBefore('@')
		return name.takeIfNotEmpty()
	}
}
