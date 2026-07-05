package icu.windea.pls.script.psi

import com.intellij.psi.PsiListLikeElement

/**
 * 成员容器。可以直接包含成员（也可能直接包含特定的语句）。
 *
 * @see ParadoxScriptBlock
 * @see ParadoxScriptRootBlock
 * @see ParadoxScriptConditionalBlock
 */
interface ParadoxScriptMemberContainer : ParadoxScriptMemberContext, PsiListLikeElement {
    override val memberContainer: ParadoxScriptMemberContainer get() = this
    override val members: List<ParadoxScriptMember> get() = emptyList()
}
