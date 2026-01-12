package icu.windea.pls.cwt.psi

import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement

/**
 * @see CwtFile
 * @see CwtMember
 * @see CwtBlockElement
 */
interface CwtMemberContainer : PsiElement, NavigatablePsiElement {
    val members: List<CwtMember>? get() = null
    val properties: List<CwtProperty>? get() = null
    val values: List<CwtValue>? get() = null
}
