package icu.windea.pls.script.psi

import icu.windea.pls.lang.psi.*
import icu.windea.pls.model.*

/**
 * @see ParadoxScriptPropertyKey
 * @see ParadoxScriptValue
 */
interface ParadoxScriptExpressionElement: ParadoxExpressionElement, ParadoxTypedElement {
	override fun setValue(value: String): ParadoxScriptExpressionElement
	
	override val type: ParadoxType
}
