package icu.windea.pls.script.psi

import com.intellij.psi.*

interface ParadoxScriptBlockElement : PsiListLikeElement {
	val propertyList: List<ParadoxScriptProperty>
	val valueList: List<ParadoxScriptValue>
	val scriptedVariableList: List<ParadoxScriptScriptedVariable>
	val isEmpty: Boolean
	val isNotEmpty: Boolean
	
	override fun getComponents(): List<PsiElement>
}
