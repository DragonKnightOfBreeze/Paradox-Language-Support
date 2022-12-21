package icu.windea.pls.script.psi

import com.intellij.psi.*

interface ParadoxScriptBlockElement : PsiListLikeElement {
	val valueList: List<ParadoxScriptValue>
	val propertyList: List<ParadoxScriptProperty>
	val scriptedVariableList: List<ParadoxScriptScriptedVariable>
	val isEmpty: Boolean
	val isNotEmpty: Boolean
	
	override fun getComponents(): List<PsiElement>
}
