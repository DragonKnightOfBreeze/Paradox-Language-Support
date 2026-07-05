package icu.windea.pls.script.psi

import icu.windea.pls.core.psi.PsiBoundElement

/**
 * 带边界的成员容器。可以直接包含 [ParadoxScriptMember]。
 *
 * @see ParadoxScriptBlock
 * @see ParadoxScriptConditionalBlock
 */
interface ParadoxScriptBoundMemberContainer : ParadoxScriptMemberContainer, PsiBoundElement {
    override val memberContainer: ParadoxScriptBoundMemberContainer get() = this
    override val members: List<ParadoxScriptMember> get() = emptyList()
}
