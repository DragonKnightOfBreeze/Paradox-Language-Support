package icu.windea.pls.script.psi

import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement

/**
 * @see ParadoxScriptFile
 * @see ParadoxScriptMember
 * @see ParadoxScriptBlockElement
 * @see ParadoxScriptParameterCondition
 */
interface ParadoxScriptMemberContainer : PsiElement, NavigatablePsiElement {
    val membersRoot: ParadoxScriptMemberContainer? get() = null
    val members: List<ParadoxScriptMember>? get() = null
}
