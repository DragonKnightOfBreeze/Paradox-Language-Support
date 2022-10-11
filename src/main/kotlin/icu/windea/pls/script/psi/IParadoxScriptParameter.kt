package icu.windea.pls.script.psi

import com.intellij.navigation.*
import com.intellij.psi.*
import icu.windea.pls.core.model.*
import icu.windea.pls.script.navigation.*

/**
 * 脚本参数。
 */
interface IParadoxScriptParameter : ParadoxScriptNamedElement, PsiNameIdentifierOwner, ParadoxScriptTypedElement {
	override fun getName(): String
	
	val defaultValue: String? get() = null
	
	override val valueType: ParadoxValueType get() = ParadoxValueType.ParameterType
	
	override fun getPresentation(): ItemPresentation {
		return ParadoxScriptParameterPresentation(this)
	}
}