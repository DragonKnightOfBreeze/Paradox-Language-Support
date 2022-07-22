package icu.windea.pls.script.psi

import com.intellij.psi.*

/**
 * 定义的属性 - 兼容scriptFile和scriptProperty，本身可能就是定义。
 */
interface ParadoxDefinitionProperty : PsiNamedElement, NavigatablePsiElement {
	fun getStub(): ParadoxDefinitionPropertyStub<out ParadoxDefinitionProperty>?
	
	override fun getName(): String
	
	val pathName: String
	val originalPathName: String
	
	val block: IParadoxScriptBlock?
	val variableList: List<ParadoxScriptVariable>
		get() {
			return block?.variableList.orEmpty()
		}
	val propertyList: List<ParadoxScriptProperty>
		get() {
			return buildList { block?.processProperty(includeConditional = true) { add(it) } }
		}
	val valueList: List<ParadoxScriptValue>
		get() {
			return buildList { block?.processValue(includeConditional = true) { add(it) } }
		}
	
	/**
	 * 得到特定定义声明（scripted_effect/scripted_trigger等）中所有被引用的参数的名字。
	 */
	val parameterNames: Set<String>? get() = null
}

