package icu.windea.pls.core.psi

import com.intellij.psi.*
import icu.windea.pls.lang.model.*

interface ParadoxParameter : ParadoxTypedElement, NavigatablePsiElement {
	override fun getName(): String?
	
	fun setName(name: String): ParadoxParameter
	
	val defaultValue: String? get() = null
	
	override val type: ParadoxType get() = ParadoxType.Parameter
}
