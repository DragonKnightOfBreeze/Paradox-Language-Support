package icu.windea.pls.script.psi

import com.intellij.psi.*
import icu.windea.pls.core.psi.*

/**
 * @see ParadoxScriptFile
 * @see ParadoxScriptProperty
 */
interface ParadoxScriptDefinitionElement : ParadoxScriptNamedElement, ParadoxScriptMemberElement {
	fun getStub(): ParadoxScriptDefinitionElementStub<out ParadoxScriptDefinitionElement>?
	
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
	
	val parameterMap: Map<String, Set<SmartPsiElementPointer<ParadoxParameter>>> get() = emptyMap()
}