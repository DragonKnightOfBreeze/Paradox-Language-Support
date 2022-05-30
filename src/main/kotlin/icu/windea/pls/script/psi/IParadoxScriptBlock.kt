package icu.windea.pls.script.psi

import com.intellij.psi.*

interface IParadoxScriptBlock : PsiListLikeElement {
	val propertyList: List<ParadoxScriptProperty>
	val valueList: List<ParadoxScriptValue>
	val variableList: List<ParadoxScriptVariable>
	val isEmpty: Boolean
	val isNotEmpty: Boolean
	override fun getComponents(): List<PsiElement>
}