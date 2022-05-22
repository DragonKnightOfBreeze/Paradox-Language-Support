package icu.windea.pls.script.reference

import com.intellij.openapi.util.*
import icu.windea.pls.script.psi.*

class ParadoxScriptVariableReferenceReference(
	element: ParadoxScriptVariableReference,
	rangeInElement: TextRange
) : AbstractParadoxScriptVariableReferenceReference(element, rangeInElement) {
	override fun String.toReferenceName(): String {
		return if(this.startsWith('@')) this else "@$this"
	}
}