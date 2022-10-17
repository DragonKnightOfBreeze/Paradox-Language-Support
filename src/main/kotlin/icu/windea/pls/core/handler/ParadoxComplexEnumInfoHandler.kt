@file:Suppress("UNUSED_PARAMETER")

package icu.windea.pls.core.handler

import com.intellij.psi.stubs.*
import icu.windea.pls.core.model.*
import icu.windea.pls.script.psi.*

/**
 * 用于处理复杂枚举信息。
 */
object ParadoxComplexEnumInfoHandler {
	fun resolve(element: ParadoxScriptExpressionElement, parentStub: StubElement<*>? = null): ParadoxComplexEnumInfo? {
		if(element.isParameterAwareExpression()) return null //快速判断
		//TODO 0.7.4
		val name = element.value
		val enumName = ""
		return ParadoxComplexEnumInfo(name, enumName)
	}
}