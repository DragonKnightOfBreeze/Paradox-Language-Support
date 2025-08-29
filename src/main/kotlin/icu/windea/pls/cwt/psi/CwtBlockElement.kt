package icu.windea.pls.cwt.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiListLikeElement

interface CwtBlockElement : PsiListLikeElement {
    val valueList: List<CwtValue>
    val propertyList: List<CwtProperty>
    val isEmpty: Boolean
    val isNotEmpty: Boolean

    override fun getComponents(): List<PsiElement>
}
