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
    val members: List<ParadoxScriptMember>? get() = null
    val properties: List<ParadoxScriptProperty>? get() = null
    val values: List<ParadoxScriptValue>? get() = null
}
