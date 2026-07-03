package icu.windea.pls.cwt.psi

import icu.windea.pls.core.psi.PsiBoundElement

/**
 * 带边界的成员容器。可以直接包含成员。
 *
 * @see CwtBlock
 */
interface CwtBoundMemberContainer : CwtMemberContainer, PsiBoundElement {
    override val memberContainer: CwtBoundMemberContainer get() = this
    override val members: List<CwtMember> get() = emptyList()
}
