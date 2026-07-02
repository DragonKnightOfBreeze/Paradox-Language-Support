package icu.windea.pls.script.psi

import com.intellij.psi.NavigatablePsiElement

/**
 * @see ParadoxScriptFile
 * @see ParadoxScriptMember
 * @see ParadoxScriptBlockElement
 * @see ParadoxScriptConditionalBlock
 */
interface ParadoxScriptMemberContainer : NavigatablePsiElement {
    val membersRoot: ParadoxScriptMemberContainer? get() = null
    val members: List<ParadoxScriptMember>? get() = null
}
