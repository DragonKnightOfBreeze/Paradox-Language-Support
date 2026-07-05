package icu.windea.pls.script.psi

import com.intellij.psi.NavigatablePsiElement

/**
 * 成员上下文。可能直接或间接包含成员（也可能直接或间接包含特定的语句）。
 *
 * @see ParadoxScriptFile
 * @see ParadoxScriptMember
 * @see ParadoxScriptMemberContainer
 */
interface ParadoxScriptMemberContext : NavigatablePsiElement {
    val memberContainer: ParadoxScriptMemberContainer? get() = null
    val members: List<ParadoxScriptMember>? get() = null
}
