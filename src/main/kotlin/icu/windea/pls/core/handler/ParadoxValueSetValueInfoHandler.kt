package icu.windea.pls.core.handler

import com.intellij.openapi.progress.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.handler.ParadoxCwtConfigHandler.resolveConfigs
import icu.windea.pls.core.model.*
import icu.windea.pls.script.psi.*

object ParadoxValueSetValueInfoHandler {
	@JvmStatic
	fun resolve(element: ParadoxScriptString): ParadoxValueSetValueInfo? {
		//排除带参数的情况和用引号括起的情况
		if(element.isParameterAwareExpression() || element.isQuoted()) return null
		
		ProgressManager.checkCanceled()
		val config = resolveConfigs(element, CwtValueConfig::class.java) {
			!shouldBeSkipped(it)
		}.firstOrNull() ?: return null
		if(config.expression.type != CwtDataTypes.Value && config.expression.type != CwtDataTypes.ValueSet) return null
		val name = element.value.substringBefore('@')
		val valueSetName = config.expression.value?.takeIfNotEmpty() ?: return null
		val configGroup = config.info.configGroup
		val gameType = configGroup.gameType
		return ParadoxValueSetValueInfo(name, valueSetName, gameType)
	}
	
	private fun shouldBeSkipped(it: CwtValueExpression): Boolean {
		val type = it.type
		return when(type){
			CwtDataTypes.Localisation -> true
			CwtDataTypes.SyncedLocalisation -> true
			CwtDataTypes.InlineLocalisation -> true
			CwtDataTypes.Icon -> true
			CwtDataTypes.FilePath -> true
			else -> false
		}
	}
}