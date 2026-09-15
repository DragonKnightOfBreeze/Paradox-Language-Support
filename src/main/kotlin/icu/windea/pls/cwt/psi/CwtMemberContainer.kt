package icu.windea.pls.cwt.psi

/**
 * 成员容器。可以直接包含成员。
 *
 * @see CwtBlock
 * @see CwtRootBlock
 */
interface CwtMemberContainer : CwtMemberContext {
    override val memberContainer: CwtMemberContainer get() = this
    override val members: List<CwtMember> get() = emptyList()
}
