package icu.windea.pls.script.psi

import com.intellij.psi.PsiListLikeElement
import icu.windea.pls.core.psi.PsiBoundElement

/**
 * @see ParadoxScriptBlock
 * @see ParadoxScriptConditionalBlock
 */
interface ParadoxScriptBoundMemberContainer : ParadoxScriptMemberContainer, PsiBoundElement, PsiListLikeElement {
    override val membersRoot: ParadoxScriptBoundMemberContainer get() = this
    override val members: List<ParadoxScriptMember> get() = emptyList()
}
