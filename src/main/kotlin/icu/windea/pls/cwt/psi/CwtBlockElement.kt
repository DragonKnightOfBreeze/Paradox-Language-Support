package icu.windea.pls.cwt.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiListLikeElement

interface CwtBlockElement : CwtMemberContainer, PsiListLikeElement {
    override fun getComponents(): List<PsiElement>
}
