package icu.windea.pls.script.psi

import com.intellij.psi.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.psi.*

/**
 * @see ParadoxScriptPropertyKey
 * @see ParadoxScriptValue
 */
interface ParadoxScriptExpressionElement: ParadoxTypedElement, NavigatablePsiElement {
	val value: String
	
	fun setValue(value: String): ParadoxScriptExpressionElement
	
	override val type: ParadoxDataType
}