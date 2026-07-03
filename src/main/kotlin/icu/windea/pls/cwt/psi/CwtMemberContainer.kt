package icu.windea.pls.cwt.psi

import com.intellij.psi.PsiListLikeElement

/**
 * 成员容器。可以直接包含成员。
 *
 * @see CwtBlock
 * @see CwtRootBlock
 */
interface CwtMemberContainer : CwtMemberContext, PsiListLikeElement {
    override val memberContainer: CwtMemberContainer get() = this
    override val members: List<CwtMember> get() = emptyList()
}
