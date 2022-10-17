@file:Suppress("UNUSED_PARAMETER")

package icu.windea.pls.core.handler

import com.intellij.psi.stubs.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.handler.ParadoxCwtConfigHandler.resolveConfigs
import icu.windea.pls.core.model.*
import icu.windea.pls.script.psi.*

object ParadoxValueSetValueInfoHandler {
	@JvmStatic
	fun resolve(element: ParadoxScriptString, parentStub: StubElement<*>? = null): ParadoxValueSetValueInfo? {
		if(element.isParameterAwareExpression() || element.isQuoted()) return null //快速判断
		val config = resolveConfigs(element, CwtValueConfig::class.java) {
			!shouldBeSkipped(it)
		}.firstOrNull() ?: return null
		if(config.expression.type != CwtDataTypes.Value && config.expression.type != CwtDataTypes.ValueSet) return null
		val name = element.value.substringBefore('@')
		val valueSetName = config.expression.value?.takeIfNotEmpty() ?: return null
		return ParadoxValueSetValueInfo(name, valueSetName)
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