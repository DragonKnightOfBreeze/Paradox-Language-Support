package icu.windea.pls.script.psi

import com.intellij.psi.*
import com.intellij.psi.stubs.StubElement

/**
 * 定义的属性 - 兼容scriptFile和scriptProperty，本身可能就是定义。
 */
interface ParadoxDefinitionProperty : PsiNamedElement {
	override fun getName(): String
	
	fun getStub(): ParadoxDefinitionPropertyStub<out ParadoxDefinitionProperty>?
	
	val block: ParadoxScriptBlock?
	//val variables get() = block?.variableList.orEmpty()
	val properties get() = block?.propertyList.orEmpty()
	val values get() = block?.valueList.orEmpty()
}