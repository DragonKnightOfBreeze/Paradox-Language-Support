package icu.windea.pls.cwt.psi

import com.intellij.psi.PsiListLikeElement
import icu.windea.pls.core.psi.PsiBoundElement

/**
 * @see CwtBlock
 */
interface CwtBoundMemberContainer : CwtMemberContainer, PsiBoundElement, PsiListLikeElement {
    override val membersRoot: CwtBoundMemberContainer get() = this
    override val members: List<CwtMember> get() = emptyList()
}
