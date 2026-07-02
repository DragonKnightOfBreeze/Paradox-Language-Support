package icu.windea.pls.cwt.psi

import com.intellij.psi.NavigatablePsiElement

/**
 * @see CwtFile
 * @see CwtMember
 * @see CwtBlockElement
 */
interface CwtMemberContainer : NavigatablePsiElement {
    val membersRoot: CwtMemberContainer? get() = null
    val members: List<CwtMember>? get() = null
}
