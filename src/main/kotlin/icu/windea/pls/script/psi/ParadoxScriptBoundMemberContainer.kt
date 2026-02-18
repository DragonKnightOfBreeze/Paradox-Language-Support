package icu.windea.pls.script.psi

import com.intellij.psi.PsiListLikeElement

/**
 * @see ParadoxScriptBlock
 * @see ParadoxScriptParameterCondition
 */
interface ParadoxScriptBoundMemberContainer : ParadoxScriptMemberContainer, PsiListLikeElement {
    override val membersRoot: ParadoxScriptBoundMemberContainer get() = this
    override val members: List<ParadoxScriptMember> get() = emptyList()
}
