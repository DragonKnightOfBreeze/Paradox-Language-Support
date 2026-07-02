package icu.windea.pls.cwt.psi

import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement

/**
 * @see CwtFile
 * @see CwtMember
 * @see CwtBlockElement
 */
interface CwtMemberContainer : PsiElement, NavigatablePsiElement {
    val membersRoot: CwtMemberContainer? get() = null
    val members: List<CwtMember>? get() = null
}
