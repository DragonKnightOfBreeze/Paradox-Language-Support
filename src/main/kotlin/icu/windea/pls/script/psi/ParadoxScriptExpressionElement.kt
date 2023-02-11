package icu.windea.pls.script.psi

import com.intellij.psi.*
import icu.windea.pls.core.psi.*

/**
 * @see ParadoxScriptPropertyKey
 * @see ParadoxScriptValue
 */
interface ParadoxScriptExpressionElement: ParadoxTypedElement, NavigatablePsiElement {
	var value: String
		@get:JvmName("getValue")
		get
	
	fun setValue(value: String): ParadoxScriptExpressionElement
}