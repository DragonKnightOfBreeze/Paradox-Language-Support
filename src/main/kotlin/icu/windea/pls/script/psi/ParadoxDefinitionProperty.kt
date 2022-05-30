package icu.windea.pls.script.psi

import com.intellij.psi.*

/**
 * 定义的属性 - 兼容scriptFile和scriptProperty，本身可能就是定义。
 */
interface ParadoxDefinitionProperty : PsiNamedElement {
	fun getStub(): ParadoxDefinitionPropertyStub<out ParadoxDefinitionProperty>?
	
	override fun getName(): String
	
	val pathName: String
	val originalPathName: String
	
	val block: IParadoxScriptBlock?
	val variableList: List<ParadoxScriptVariable> get() = block?.variableList.orEmpty()
	val propertyList: List<ParadoxScriptProperty> get() = block?.propertyList.orEmpty()
	val valueList: List<ParadoxScriptValue> get() = block?.valueList.orEmpty()
}

