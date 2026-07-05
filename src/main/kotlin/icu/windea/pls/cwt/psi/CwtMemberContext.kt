package icu.windea.pls.cwt.psi

import com.intellij.psi.NavigatablePsiElement

/**
 * 成员上下文。可能直接或间接包含成员。
 *
 * @see CwtFile
 * @see CwtMember
 * @see CwtMemberContainer
 */
interface CwtMemberContext : NavigatablePsiElement {
    val memberContainer: CwtMemberContainer? get() = null
    val members: List<CwtMember>? get() = null
}
