package icu.windea.pls.core.psi

import com.intellij.psi.*
import icu.windea.pls.script.psi.*

/**
 * 定义的属性 - 兼容scriptFile和scriptProperty，本身可能就是定义。
 */
interface ParadoxDefinitionProperty : ParadoxScriptNamedElement, ParadoxPathAwareElement {
	fun getStub(): ParadoxDefinitionPropertyStub<out ParadoxDefinitionProperty>?
	
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
			return buildList { block?.processValue(includeConditional = true) { add(it) } }
		}
	val propertyList: List<ParadoxScriptProperty>
		get() {
			return buildList { block?.processProperty(includeConditional = true) { add(it) } }
		}
	
	val parameterMap: Map<String, Set<SmartPsiElementPointer<ParadoxParameter>>> get() = emptyMap()
}

interface ParadoxPathAwareElement: PsiElement, NavigatablePsiElement