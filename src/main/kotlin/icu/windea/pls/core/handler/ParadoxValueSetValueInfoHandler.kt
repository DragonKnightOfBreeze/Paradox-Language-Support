@file:Suppress("UNUSED_PARAMETER")

package icu.windea.pls.core.handler

import com.intellij.lang.*
import com.intellij.psi.stubs.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.model.*
import icu.windea.pls.script.psi.*

object ParadoxValueSetValueInfoHandler {
	@JvmStatic
	fun resolve(element: ParadoxScriptValue, parentStub: StubElement<*>? = null): ParadoxValueSetValueInfo? {
		//TODO 避免SOF
		if(element !is ParadoxScriptString) return null
		val config = ParadoxCwtConfigHandler.resolveValueConfig(element) ?: return null
		if(config.expression.type != CwtDataTypes.ValueSet) return null
		val name = element.value
		val valueSetName = config.expression.value?.takeIfNotEmpty() ?: return null
		return ParadoxValueSetValueInfo(name, valueSetName)
	}
	
	@JvmStatic
	fun resolve(node: ASTNode): ParadoxValueSetValueInfo? {
		val psi = node.psi
		if(psi !is ParadoxScriptString) return null
		return resolve(psi)
	}
}