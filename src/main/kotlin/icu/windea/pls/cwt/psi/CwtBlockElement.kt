package icu.windea.pls.cwt.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiListLikeElement

interface CwtBlockElement : CwtMemberContainer, PsiListLikeElement {
    override val members: List<CwtMember>
    override val properties: List<CwtProperty>
    override val values: List<CwtValue>

    override fun getComponents(): List<PsiElement>
}
