package icu.windea.pls.cwt.psi

import com.intellij.psi.*

interface CwtBlockElement : PsiListLikeElement {
	val propertyList: List<CwtProperty>
	val valueList: List<CwtValue>
	val isEmpty: Boolean
	val isNotEmpty: Boolean
	
	override fun getComponents(): List<PsiElement>
}