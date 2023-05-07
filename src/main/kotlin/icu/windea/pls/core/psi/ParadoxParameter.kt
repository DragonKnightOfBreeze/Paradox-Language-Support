package icu.windea.pls.core.psi

import com.intellij.psi.*
import icu.windea.pls.core.expression.*

interface ParadoxParameter : ParadoxTypedElement, NavigatablePsiElement {
	override fun getName(): String?
	
	fun setName(name: String): ParadoxParameter
	
	val defaultValue: String? get() = null
	
	override val type: ParadoxDataType get() = ParadoxDataType.ParameterType
}
