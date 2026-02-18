package icu.windea.pls.cwt.psi

import com.intellij.psi.PsiListLikeElement

/**
 * @see CwtBlock
 */
interface CwtBoundMemberContainer : CwtMemberContainer, PsiListLikeElement {
    override val membersRoot: CwtBoundMemberContainer get() = this
    override val members: List<CwtMember> get() = emptyList()
}
