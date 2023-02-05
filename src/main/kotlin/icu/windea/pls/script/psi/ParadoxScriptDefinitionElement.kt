package icu.windea.pls.script.psi

import com.intellij.psi.*
import icu.windea.pls.lang.model.*

/**
 * @see ParadoxScriptFile
 * @see ParadoxScriptProperty
 */
interface ParadoxScriptDefinitionElement : ParadoxScriptNamedElement, ParadoxScriptMemberElement {
	fun getStub(): ParadoxScriptDefinitionElementStub<out ParadoxScriptDefinitionElement>?
	
	/**
	 * 注意：如果这个对象是定义，这里得到的是定义的顶级键名（rootKey），而不一定是定义的名字（definitionName）。
	 */
	override fun getName(): String
	
	override fun getNameIdentifier(): PsiElement? = null
	
	val pathName: String
	val originalPathName: String
	
	val block: ParadoxScriptBlockElement?
	val variableList: List<ParadoxScriptScriptedVariable>
		get() {
			return block?.scriptedVariableList.orEmpty()
		}
	val valueList: List<ParadoxScriptValue>
		get() {
			return buildList { block?.processValue(inline = true) { add(it) } }
		}
	val propertyList: List<ParadoxScriptProperty>
		get() {
			return buildList { block?.processProperty(inline = true) { add(it) } }
		}
	
	val parameters: Map<String, ParadoxParameterInfo>
}